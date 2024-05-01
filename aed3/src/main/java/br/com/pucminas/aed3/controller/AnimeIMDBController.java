package br.com.pucminas.aed3.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;

import br.com.pucminas.aed3.model.AnimeIMDB;
import br.com.pucminas.aed3.model.Indexes.Index;
import br.com.pucminas.aed3.model.Indexes.RegistroIndexBase;

@Controller
public class AnimeIMDBController {

    private static final String arquivoDadosNome = ".\\dados_anime_imdb.db";

    private Path arquivoDadosPath = Paths.get(arquivoDadosNome);

    private Index arquivoIndex;

    private RandomAccessFile arquivoDados;

    private ArrayList<Integer> listaIds = new ArrayList<Integer>();

    private Integer maxID = 0;

    private static final Integer caminhos = 2;

    public AnimeIMDBController() throws Exception, FileNotFoundException {
        arquivoDados = new RandomAccessFile(arquivoDadosPath.toFile(), "rw");
        arquivoIndex = new Index(arquivoDadosPath);
    }

    public AnimeIMDB getById(Integer id) throws Exception {
        // Tenta realizar busca pelo arquivo de índice
        AnimeIMDB retorno = new AnimeIMDB();
        Boolean lapide;
        Short tamReg = 0;
        Integer idReg;
        if (Objects.nonNull(arquivoIndex)) {
            try {
                RegistroIndexBase reg = arquivoIndex.findById(id.longValue());
                if (Objects.nonNull(reg)) {
                    if (reg.getAddress() > 0) {
                        arquivoDados.seek(reg.getAddress());
                        try {
                            lapide = arquivoDados.readBoolean();
                            tamReg = arquivoDados.readShort();
                            idReg = arquivoDados.readInt();
                            if (idReg.equals(id)) {
                                arquivoDados.seek(arquivoDados.getFilePointer() - 7);
                                retorno = desserializar();
                                return retorno;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // caso a busca pelo índice falhe (seja por não achar no índice seja por
        // inconsistência entre os arquivos busca sequencialmente e inserre/atualiza o
        // índice)
        if (Objects.nonNull(arquivoDados)) {
            arquivoDados.seek(0);
            Integer maxID = arquivoDados.readInt();

            if (id > maxID) {
                throw new Exception("Id solicitado maior que o último id cadastrado");
            }
            do {
                try {
                    lapide = arquivoDados.readBoolean();
                    tamReg = arquivoDados.readShort();
                    idReg = arquivoDados.readInt();
                    if (idReg.equals(id)) {
                        // Retorna 7 posições para retornar ao primeiro byte referente ao registro (byte
                        // de lápide)
                        // Sendo 7 bytes pela leitura do lápide (1 byte), leitura do tamanho (2 bytes) e
                        // leitura do ID para fazer a comparação (4 bytes)
                        arquivoDados.seek(arquivoDados.getFilePointer() - 7);
                        retorno = desserializar();
                        arquivoIndex.atualizaIndice(retorno);

                        // Só retorna se o registro estiver com a lápide como false (registro valido);
                        // Se não for válido segue procurando por um registro válido pro ID e com lápide
                        // falsa
                        if (!lapide)
                            return retorno;
                    }

                    arquivoDados.seek(arquivoDados.getFilePointer() + (tamReg - 4));
                } catch (Exception e) {
                    System.out.println("Erro");
                    e.printStackTrace();
                }

            } while (arquivoDados.getFilePointer() < arquivoDados.length());

            return retorno;
        }
        return null;
    }

    public AnimeIMDB salvar(AnimeIMDB r) throws Exception {
        RegistroIndexBase idx = new RegistroIndexBase();
        if (Objects.nonNull(arquivoDados)) {
            // A operação salvar realiza tanto o insert quanto o update;
            // Ela diferencia somente pela presenta ou não de um ID válido na entidade
            // enviada como parametro.
            // Uma entidade nova gera Id negativo e é tratada na primeira condição; Caso a a
            // entidade possua um id
            // válido; o registro antigo será marcado como "excluido" (lapide = true) e o
            // registro será escrito no fim do arquivo;
            // Registros inválidos são tratados após a ordenacao do arquivo;

            if (r.getId() <= 0) {
                // Criação
                arquivoDados.seek(0);
                r.setId(arquivoDados.readInt() + 1);
                idx.setId(r.getId().longValue());

                arquivoDados.seek(arquivoDados.length());
                idx.setAddress(arquivoDados.getFilePointer());

                arquivoDados.write(r.gerarRegistro());
                idx = arquivoIndex.insertIdx(idx);

                arquivoDados.seek(0);
                arquivoDados.write(r.getId());

            } else {
                // Atualização
                idx = arquivoIndex.findById(r.getId().longValue());
                if (Objects.nonNull(idx) && idx.getAddress() > 0) {
                    byte[] dados = r.gerarRegistro();
                    Short tam;
                    Boolean lapide;
                    arquivoDados.seek(idx.getAddress());
                    lapide = arquivoDados.readBoolean();
                    tam = arquivoDados.readShort();
                    if (r.getExcluido() || tam < dados.length) {
                        arquivoDados.seek(idx.getAddress());
                        arquivoDados.writeBoolean(true);
                        if (!r.getExcluido()) {
                            arquivoDados.seek(arquivoDados.length());
                            idx.setAddress(arquivoDados.getFilePointer());
                            arquivoDados.writeBoolean(r.getExcluido());
                            arquivoDados.writeShort(dados.length);
                            arquivoDados.write(dados);
                        } else {
                            idx.setAddress(-1L);
                        }
                    } else {
                        arquivoDados.write(dados);
                    }
                    arquivoIndex.atualizaIndice(idx);

                } else {
                    arquivoDados.seek(4);
                    Boolean lapide;
                    Integer id;
                    Short tam;
                    byte[] dados = r.gerarRegistro();
                    idx.setId(r.getId().longValue());
                    while (arquivoDados.getFilePointer() < arquivoDados.length()) {
                        lapide = arquivoDados.readBoolean();
                        tam = arquivoDados.readShort();
                        id = arquivoDados.readInt();
                        if (r.getId().equals(id)) {
                            if (!lapide) {
                                arquivoDados.seek(arquivoDados.getFilePointer() - 7);
                                idx.setAddress(arquivoDados.getFilePointer());
                                if (r.getExcluido() || tam < dados.length) {
                                    arquivoDados.writeBoolean(true);
                                    if (!r.getExcluido()) {
                                        arquivoDados.seek(arquivoDados.length());
                                        idx.setAddress(arquivoDados.getFilePointer());
                                        arquivoDados.writeBoolean(r.getExcluido());
                                        arquivoDados.writeShort(dados.length);
                                        arquivoDados.write(dados);
                                    } else {
                                        idx.setAddress(-1L);
                                    }
                                } else {
                                    arquivoDados.writeBoolean(false);
                                    arquivoDados.writeShort(dados.length);
                                    arquivoDados.write(dados);
                                }
                                idx.setId(r.getId().longValue());
                                arquivoIndex.atualizaIndice(idx);
                                break;
                            }
                        }
                        arquivoDados.seek(arquivoDados.getFilePointer() + tam - 4);
                    }
                }
            }

        }
        return r;
    }

    public void deletar(AnimeIMDB r) throws Exception {
        Integer id;
        Short tam;
        Boolean lapide;
        RegistroIndexBase idx = arquivoIndex.findById(r.getId().longValue());
        if (Objects.nonNull(idx) && idx.getAddress() > 0) {
            arquivoDados.seek(idx.getAddress());
            lapide = arquivoDados.readBoolean();
            tam = arquivoDados.readShort();
            id = arquivoDados.readInt();
            if (id.equals(r.getId())) {
                arquivoDados.seek(idx.getAddress());
                idx.setAddress(-1L);
                arquivoDados.writeBoolean(true);
                arquivoIndex.atualizaIndice(idx);
                return;
            }
        }
        arquivoDados.seek(4);
        while (arquivoDados.getFilePointer() < arquivoDados.length()) {
            lapide = arquivoDados.readBoolean();
            tam = arquivoDados.readShort();
            id = arquivoDados.readInt();
            if (id.equals(r.getId()) && !lapide) {
                arquivoDados.seek(arquivoDados.getFilePointer() - 7);
                arquivoDados.writeBoolean(true);
                break;
            }
            arquivoDados.seek(arquivoDados.getFilePointer() + tam - 4);
        }

    }

    private AnimeIMDB desserializar() throws Exception {
        AnimeIMDB reg = new AnimeIMDB();
        reg.setExcluido(arquivoDados.readBoolean());
        Short tamReg = arquivoDados.readShort(), tamAux, count;
        reg.setId(arquivoDados.readInt());
        tamAux = arquivoDados.readShort();
        byte[] b = new byte[tamAux];
        arquivoDados.read(b);
        reg.setTitulo(new String(b, StandardCharsets.UTF_8));
        reg.setAvaliacao(arquivoDados.readFloat());
        reg.setVotos(arquivoDados.readInt());

        count = arquivoDados.readShort();
        for (int i = 0; i < count; i++) {
            tamAux = arquivoDados.readShort();
            b = new byte[tamAux];
            arquivoDados.read(b);
            reg.getGeneros().add(new String(b, StandardCharsets.UTF_8));
        }

        tamAux = arquivoDados.readShort();
        b = new byte[tamAux];
        arquivoDados.read(b);
        reg.setResumo(new String(b, StandardCharsets.UTF_8));

        reg.setAnoLancamento(arquivoDados.readInt());
        reg.setAnoEncerramento(arquivoDados.readInt());

        count = arquivoDados.readShort();
        for (int i = 0; i < count; i++) {
            tamAux = arquivoDados.readShort();
            b = new byte[tamAux];
            arquivoDados.read(b);
            reg.getElenco().add(new String(b, StandardCharsets.UTF_8));
        }
        Long date = arquivoDados.readLong();
        reg.setCadastro((date > 0 ? new Date(date) : null));
        return reg;
    }

    public void carregarCSV(String arquivo) throws Exception {
        listaIds.clear();
        maxID = 0;

        BufferedReader bf;
        Path p = Paths.get(arquivo);
        if (p.toFile().exists() && p.toFile().isFile()) {
            bf = new BufferedReader(new FileReader(p.toFile()));
            arquivoDados.seek(0);
            arquivoDados.setLength(0);
            arquivoDados.writeInt(maxID);

            String line = bf.readLine();
            while ((line = bf.readLine()) != null) {
                AnimeIMDB registro = parseRegistroCSV(line);
                if (registro.getTitulo().equals(""))
                    continue;
                arquivoDados.write(registro.gerarRegistro());
            }
            arquivoDados.seek(0);
            arquivoDados.writeInt(maxID);

        } else {
            throw new Exception("O arquivo informado não foi encontrado");
        }
    }

    private AnimeIMDB parseRegistroCSV(String linha) {
        AnimeIMDB retorno = new AnimeIMDB();

        try {
            String[] splited = linha.replace(";", ",").split("\",\"");
            for (int i = 0; i < splited.length; i++) {

                String s = splited[i].replace("\"", "");
                s = new String(s.getBytes(), StandardCharsets.UTF_8);
                switch (i) {
                    case 0:
                        retorno.setTitulo(s);
                        break;
                    case 1:
                        retorno.setGeneros(Stream.of(s.split(",")).collect(Collectors.toList()));
                        break;
                    case 2:
                        retorno.setAvaliacao(Float.parseFloat(s));
                        break;
                    case 3:
                        retorno.setVotos(Integer.parseInt(s.replace(",", "")));
                        break;
                    case 4:
                        break;
                    case 5:
                        retorno.setAnoLancamento(
                                Integer.parseInt(s.replace("(", "").replace(")", "").substring(0, 4)));

                        if (s.contains("–"))
                            retorno.setAnoEncerramento(s.indexOf(")") - s.indexOf("–") >= 4
                                    ? Integer.parseInt(s.substring(s.indexOf("–") + 1, s.indexOf(")")))
                                    : 0);
                        else
                            retorno.setAnoEncerramento(retorno.getAnoLancamento());
                        break;
                    case 6:
                        retorno.setResumo(s);
                        break;
                    case 7:
                        retorno.setElenco(Stream.of(s.split(",")).collect(Collectors.toList()));
                        break;
                }
                if (i > 7)
                    break;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        retorno.gerarDataCadastro();
        retorno.setId(gerarId());
        return retorno;
    }

    private Integer gerarId() {
        while (true) {
            Integer id = randomId();
            Integer check = listaIds.stream().filter(i -> i == id).findAny().orElse(null);
            if (Objects.isNull(check)) {
                listaIds.add(id);
                if (id > maxID)
                    maxID = id;
                return id;
            }
        }
    }

    private Integer randomId() {
        int min = 1, max = 50000;
        return Integer.valueOf((int) ((Math.random() * (max - min)) + min));
    }

    public void gerarIndex() {
        if (Objects.isNull(arquivoIndex)) {
            arquivoIndex = new Index(arquivoDadosPath);
        }
        arquivoIndex.clearIdx();        
        Long addr;
        try {
            arquivoDados.seek(4);
            while (arquivoDados.getFilePointer() < arquivoDados.length()) {
                addr = arquivoDados.getFilePointer();
                Boolean lapide = arquivoDados.readBoolean();
                Short tam = arquivoDados.readShort();
                if (!lapide) {
                    byte[] dados = new byte[tam];
                    arquivoDados.read(dados);
                    AnimeIMDB r = AnimeIMDB.desserializar(dados);
                    arquivoIndex.insert(r, addr);
                }
            }
            arquivoIndex.sort();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void atualizarIndex() {
        if (Objects.isNull(arquivoIndex)) {
            arquivoIndex = new Index(arquivoDadosPath);
        }
        arquivoIndex.sort();
    }

    @Deprecated
    public void ordenacaoExterna() {
        RandomAccessFile arq1 = null, arq2 = null, arq3 = null, arq4 = null;
        List<File> files = new ArrayList<File>();

        try {
            Path p1 = Paths.get(".\\temp_1.db"), p2 = Paths.get(".\\temp_2.db");

            arquivoDados.close();
            String novoArquivo = Files.move(arquivoDadosPath, arquivoDadosPath.resolveSibling("old_anime_imbd.db"))
                    .toString();
            // arquivoDados = new RandomAccessFile(arquivoDadosPath.toFile(), "rw");
            arquivoDados = new RandomAccessFile(Paths.get(novoArquivo).toFile(), "rw");
            arquivoDados.seek(4);

            files.add(p1.toFile());
            files.add(p2.toFile());
            files.add(p1.getParent().resolve(".\\temp_3.db").toFile());
            files.add(p2.getParent().resolve(".\\temp_4.db").toFile());
            arq1 = new RandomAccessFile(files.get(0), "rw");
            arq2 = new RandomAccessFile(files.get(1), "rw");
            arq3 = new RandomAccessFile(files.get(2), "rw");
            arq4 = new RandomAccessFile(files.get(3), "rw");

            arq1.setLength(0);
            arq2.setLength(0);

            distribuicao(arquivoDados, new RandomAccessFile[] { arq1, arq2 });
            arquivoDados.close();
            RandomAccessFile[] orig = new RandomAccessFile[] { arq1, arq2 },
                    dest = new RandomAccessFile[] { arq3, arq4 };
            intercalacao(orig, dest);

            arq1 = dest[0];
            arq1.seek(0);

            arquivoDados = new RandomAccessFile(arquivoDadosPath.toFile(), "rw");
            arquivoDados.setLength(0);
            Integer maxId = 0;
            arquivoDados.writeInt(maxId);

            while (arq1.getFilePointer() < arq1.length()) {
                Boolean lapide = arq1.readBoolean();
                Short tam = arq1.readShort();
                byte[] b = new byte[tam];

                arq1.read(b);
                AnimeIMDB anime = AnimeIMDB.desserializar(b);
                if (lapide) {
                    arq1.seek(arq1.getFilePointer() + 3);
                    Integer nextId = arq1.readInt();
                    if (anime.getId().equals(nextId)) {
                        anime = null;
                    }
                }
                if (Objects.nonNull(anime)) {
                    arquivoDados.write(anime.gerarRegistro());
                }
                maxId = anime.getId();
            }

            arquivoDados.seek(0);
            arquivoDados.writeInt(maxId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {

                if (Objects.nonNull(arq1)) {
                    arq1.close();
                }
                if (Objects.nonNull(arq2)) {
                    arq2.close();
                }
                if (Objects.nonNull(arq3)) {
                    arq3.close();
                }
                if (Objects.nonNull(arq4)) {
                    arq4.close();
                }
                for (File f : files) {
                    f.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Deprecated
    private void distribuicao(RandomAccessFile origem, RandomAccessFile[] dest) {
        try {
            List<AnimeIMDB> registros = new ArrayList<AnimeIMDB>();

            Integer indicador = 0;
            Boolean lapide;
            Short tamReg;

            while (origem.getFilePointer() < origem.length()) {
                lapide = origem.readBoolean();
                tamReg = origem.readShort();
                byte[] bytesReg = new byte[tamReg];
                origem.read(bytesReg);
                AnimeIMDB r = AnimeIMDB.desserializar(bytesReg);
                r.setExcluido(lapide);
                registros.add(r);
                if (registros.size() == 10) {
                    RandomAccessFile a;
                    // Realiza a ordenação dos registros com base no Id
                    registros = registros.stream().sorted(Comparator.comparing(AnimeIMDB::getId))
                            .collect(Collectors.toList());
                    for (AnimeIMDB sortedReg : registros) {
                        a = dest[indicador % dest.length];
                        a.write(sortedReg.gerarRegistro());

                    }
                    indicador++;
                    registros.clear();
                }
            }

            origem.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private void intercalacao(RandomAccessFile[] orig, RandomAccessFile[] dest) {
        try {
            // Garante que os ponteiros dos arquivos de origem estão no início do seu
            // respectivo arquivo
            for (int i = 0; i < orig.length; i++) {
                orig[i].seek(0);
            }

            AnimeIMDB[] registros = new AnimeIMDB[orig.length];
            Integer[] regCounters = new Integer[orig.length];
            Arrays.fill(regCounters, 0);

            Boolean lapide, ordenacaoCompleta = false, intercalacaoCompleta, intercalacaoParcial;
            Short tam;

            Integer contador = 0;
            while (!ordenacaoCompleta) { // Executa até que reste apenas 1 arquivo de destino com toda a massa de dados;

                Integer seletorDestino = 0;
                intercalacaoCompleta = false;
                while (!intercalacaoCompleta) {
                    // Executa até que todos arquivos de origem sejam completamente percorridos;
                    intercalacaoParcial = false;
                    while (!intercalacaoParcial) {
                        // Executa até que o segumento atual em cada arquivo seja completamente
                        // percorrido;
                        Long tamSegmento = Math.round(Math.pow(caminhos, contador) * 10);
                        if (Arrays.stream(registros).allMatch(r -> Objects.isNull(r))) {
                            // Para cada arquivo de origem faz a leitura do primeiro registro;
                            // Em prepaparação para intercalação;
                            // Essa etapa ocorre só na primeira iteracao do laço interno;
                            Arrays.fill(regCounters, 0);
                            for (int i = 0; i < orig.length; i++) {
                                if (orig[i].getFilePointer() < orig[i].length()) {
                                    AnimeIMDB aux;
                                    lapide = orig[i].readBoolean();
                                    tam = orig[i].readShort();
                                    byte[] b = new byte[tam];
                                    orig[i].read(b);
                                    aux = AnimeIMDB.desserializar(b);
                                    aux.setExcluido(lapide);
                                    registros[i] = aux;
                                    regCounters[i]++;
                                } else {
                                    regCounters[i] = tamSegmento.intValue();
                                }
                            }
                        }
                        // Cria uma lista com os registros lidos dos arquivos de origem. Se algum
                        // arquivo
                        // não produziu registro, seja por fim do arquivo ou fim do segmento de
                        // registros,
                        // ele irá colocar null em sua posição no array de registros e esse elemento
                        // será,
                        // filtrado. Após a inserção no arquivo de destino o registro inserido é
                        // substituído pelo seu, sucessor no arquivo de origem;

                        List<AnimeIMDB> lista = Arrays.stream(registros).filter(e -> Objects.nonNull(e))
                                .collect(Collectors.toList());

                        lista = lista.stream().sorted(Comparator.comparing(AnimeIMDB::getId))
                                .collect(Collectors.toList());

                        AnimeIMDB r = lista.get(0);
                        dest[seletorDestino % orig.length].write(r.gerarRegistro());
                        Integer pos = Arrays.asList(registros).indexOf(r);
                        if (regCounters[pos] < tamSegmento) {
                            if (orig[pos].getFilePointer() < orig[pos].length()) {
                                lapide = orig[pos].readBoolean();
                                tam = orig[pos].readShort();
                                byte[] b = new byte[tam];
                                orig[pos].read(b);
                                registros[pos] = AnimeIMDB.desserializar(b);
                                registros[pos].setExcluido(lapide);
                                regCounters[pos]++;
                            } else {
                                regCounters[pos] = tamSegmento.intValue();
                                registros[pos] = null;
                            }
                        } else {
                            registros[pos] = null;
                        }

                        // Valida se todos os dados do segmento dessa fase já foram inseridos nos
                        // respectivos arquivos de destino;
                        intercalacaoParcial = (Arrays.stream(regCounters)
                                .filter(el -> el.equals(tamSegmento.intValue())).collect(Collectors.toList())
                                .size() == regCounters.length);

                    }
                    if (!Arrays.stream(registros).allMatch(r -> Objects.isNull(r))) {
                        List<AnimeIMDB> lista = Arrays.stream(registros).filter(r -> Objects.nonNull(r))
                                .sorted(Comparator.comparing(AnimeIMDB::getId)).collect(Collectors.toList());
                        for (AnimeIMDB r : lista) {
                            dest[seletorDestino % orig.length].write(r.gerarRegistro());
                        }
                        Arrays.fill(registros, null);
                    }
                    // Se todos arquivos de origem já foram percorridos completamente
                    if ((orig[0].getFilePointer() == orig[0].length())
                            || (orig[1].getFilePointer() == orig[1].length())) {
                        System.out.print("");
                    }
                    intercalacaoCompleta = Arrays.stream(orig).filter(
                            el -> {
                                try {
                                    return el.getFilePointer() < el.length();
                                } catch (Exception e) {
                                    return false;
                                }
                            }).collect(Collectors.toList()).size() == 0;
                    if (intercalacaoCompleta) {
                        break;
                    }
                    // Altera o arquivo de destino;
                    seletorDestino++;
                }
                Arrays.fill(regCounters, 0);
                contador++;

                ordenacaoCompleta = Arrays.stream(dest).filter(el -> {
                    try {
                        return (el.length() > 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }).collect(Collectors.toList()).size() == 1;
                if (ordenacaoCompleta) {
                    // Se ao termino da intercação somente restar um arquivo de destino então não há
                    // mais o que intercalar
                    break;
                } else {
                    // Para fazer a próxima fase de intercalação inverte os papéis dos arquivos de
                    // origem e destino;
                    // O arquivo de destino passa ser origem (pois recebeu os dados intercalados da
                    // iteração anterior) e voltam para o byte inicial do arquivo;
                    // E os arquivos que foram origem são zerados para receber os dados na nova fase
                    // de intercalação
                    RandomAccessFile[] aux = Arrays.copyOf(orig, orig.length);
                    orig = dest;
                    dest = aux;
                    for (int i = 0; i < orig.length; i++) {
                        orig[i].seek(0);
                        dest[i].setLength(0);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}