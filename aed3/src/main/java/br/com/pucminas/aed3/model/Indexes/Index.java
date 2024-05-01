package br.com.pucminas.aed3.model.Indexes;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import br.com.pucminas.aed3.model.AnimeIMDB;

public class Index extends IndexBase {

    public static Short caminhos = 2;
    public static Short SIZE_BLOCK = 10;
    public static String indexFileName = "index_padrao";

    public Index(Path p) {
        super(p, indexFileName);
        tipoRegistro = RegistroIndexBase.class;
    }

    public void clearIdx() {
        try {
            RandomAccessFile raf = new RandomAccessFile(indexFile.toFile(), "rw");
            raf.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sort() {
        Long lastId;
        try {
            RandomAccessFile raf = openIdxFile();
            raf.seek(0);
            lastId = (Long) readLastIdHeader(raf, tipoRegistro);
            raf.readBoolean();
            ordenacaoExterna(raf);
            raf.seek(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ordenacaoExterna(RandomAccessFile idxFile) {
        RandomAccessFile arq1 = null, arq2 = null, arq3 = null, arq4 = null;
        List<File> files = new ArrayList<File>();

        try {
            arq1 = new RandomAccessFile(indexFile.getParent().resolve("idx_temp_1.db").toFile(), "rw");
            arq2 = new RandomAccessFile(indexFile.getParent().resolve("idx_temp_2.db").toFile(), "rw");
            arq3 = new RandomAccessFile(indexFile.getParent().resolve("idx_temp_3.db").toFile(), "rw");
            arq4 = new RandomAccessFile(indexFile.getParent().resolve("idx_temp_4.db").toFile(), "rw");
            skipHeader(idxFile);

            arq1.setLength(0);
            arq2.setLength(0);

            distribuicao(idxFile, new RandomAccessFile[] { arq1, arq2 });
            idxFile.close();
            RandomAccessFile[] orig = new RandomAccessFile[] { arq1, arq2 },
                    dest = new RandomAccessFile[] { arq3, arq4 };
            intercalacao(orig, dest);

            arq1 = dest[0];
            arq1.seek(0);

            idxFile = new RandomAccessFile(indexFile.getParent().resolve(indexFileName + "_temp.db").toFile(), "rw");
            idxFile.setLength(0);
            Long maxId = 0L;
            // skipHeader(idxFile);
            idxFile.writeLong(0);
            idxFile.writeBoolean(false);

            while (arq1.getFilePointer() < arq1.length()) {
                RegistroIndexBase r = new RegistroIndexBase();
                r.setIdxAddr(arq1.getFilePointer());
                r.setId(readValue(r.getId(), arq1));
                r.setAddress(readValue(r.getAddress(), arq1));
                if (r.getAddress() > 0) {
                    idxFile.write(gerarRegistro(r));
                }
                maxId = r.getId();
            }

            idxFile.seek(0);
            idxFile.writeLong(maxId);
            idxFile.writeBoolean(true);
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

    private void distribuicao(RandomAccessFile origem, RandomAccessFile[] dest) {
        try {

            List<RegistroIndexBase> registros = new ArrayList<RegistroIndexBase>();
            RegistroIndexBase registro;
            Integer indicador = 0;

            while (origem.getFilePointer() < origem.length()) {
                registro = new RegistroIndexBase();
                registro.setIdxAddr(origem.getFilePointer());
                registro.setId(readValue(registro.getId(), origem));
                registro.setAddress(readValue(registro.getAddress(), origem));
                registros.add(registro);
                if (registros.size() == 10) {
                    RandomAccessFile a;
                    // Realiza a ordenação dos registros com base no Id
                    registros = registros.stream().sorted(Comparator.comparing(RegistroIndexBase::getId))
                            .collect(Collectors.toList());
                    for (RegistroIndexBase sortedReg : registros) {
                        a = dest[indicador % dest.length];
                        a.write(gerarRegistro(sortedReg));
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

    private void intercalacao(RandomAccessFile[] orig, RandomAccessFile[] dest) {
        try {
            // Garante que os ponteiros dos arquivos de origem estão no início do seu
            // respectivo arquivo
            for (int i = 0; i < orig.length; i++) {
                orig[i].seek(0);
            }

            RegistroIndexBase[] registros = new RegistroIndexBase[orig.length];
            Integer[] regCounters = new Integer[orig.length];
            Arrays.fill(regCounters, 0);

            Boolean ordenacaoCompleta = false, intercalacaoCompleta, intercalacaoParcial;
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
                        Long tamSegmento = Math.round(Math.pow(caminhos, contador) * SIZE_BLOCK);
                        if (Arrays.stream(registros).allMatch(r -> Objects.isNull(r))) {
                            // Para cada arquivo de origem faz a leitura do primeiro registro;
                            // Em prepaparação para intercalação;
                            // Essa etapa ocorre só na primeira iteracao do laço interno;
                            Arrays.fill(regCounters, 0);
                            for (int i = 0; i < orig.length; i++) {
                                if (orig[i].getFilePointer() < orig[i].length()) {
                                    RegistroIndexBase aux = new RegistroIndexBase();
                                    aux.setIdxAddr(orig[i].getFilePointer());
                                    aux.setId(readValue(aux.getId(), orig[i]));
                                    aux.setAddress(readValue(aux.getAddress(), orig[i]));
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

                        List<RegistroIndexBase> lista = Arrays.stream(registros).filter(e -> Objects.nonNull(e))
                                .collect(Collectors.toList());

                        lista = lista.stream().sorted(Comparator.comparing(RegistroIndexBase::getId))
                                .collect(Collectors.toList());

                        RegistroIndexBase r = lista.get(0);
                        dest[seletorDestino % orig.length].write(gerarRegistro(r));
                        Integer pos = Arrays.asList(registros).indexOf(r);
                        if (regCounters[pos] < tamSegmento) {
                            if (orig[pos].getFilePointer() < orig[pos].length()) {
                                RegistroIndexBase regAux = new RegistroIndexBase();
                                regAux.setId(readValue(regAux.getId(), orig[pos]));
                                regAux.setAddress(readValue(regAux.getAddress(), orig[pos]));
                                registros[pos] = regAux;
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
                        List<RegistroIndexBase> lista = Arrays.stream(registros).filter(r -> Objects.nonNull(r))
                                .sorted(Comparator.comparing(RegistroIndexBase::getId)).collect(Collectors.toList());
                        for (RegistroIndexBase r : lista) {
                            dest[seletorDestino % orig.length].write(gerarRegistro(r));
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

    public RegistroIndexBase findById(Long id) {
        try {
            RandomAccessFile raf = openIdxFile();
            Long lastId = (Long) readLastIdHeader(raf, Long.class);
            return (RegistroIndexBase) super.findById(id, lastId, raf);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected IndexRegisterInterface<?> buscaOrdenada(Object id, Object lastId, RandomAccessFile file) {
        RegistroIndexBase retorno = null;
        try {
            retorno = new RegistroIndexBase();
            Integer tamanho_registro = 0;
            Long inicio, fim;
            skipHeader(file);
            RegistroIndexBase menor = new RegistroIndexBase(), maior = new RegistroIndexBase();

            tamanho_registro = lengthOf(menor.getId()) + lengthOf(menor.getAddress());

            menor.setIdxAddr(file.getFilePointer());
            menor.setId(readValue(menor.getId(), file));
            menor.setAddress(readValue(menor.getAddress(), file));
            file.seek(file.getFilePointer() - tamanho_registro);
            maior.setIdxAddr(file.getFilePointer());
            maior.setId(readValue(maior.getId(), file));
            maior.setAddress(readValue(maior.getAddress(), file));

            if ((comparaDoisNumeros(menor.getId(), id) < 0) || (comparaDoisNumeros(id, maior.getId()) < 0)) {
                // Se id está fora do alcance se ids válidos
                // encerra a busca e retorna nulo;
                return null;
            } else {
                // caso não esteja fora, realiza uma busca binária;
                inicio = menor.getIdxAddr();
                fim = maior.getIdxAddr();
                while (inicio <= fim) {
                    Long media = Double.valueOf(Math.floor(maior.getId() - menor.getId() / 2)).longValue();
                    Long posicaoMedia = (media * tamanho_registro) + inicio;
                    file.seek(posicaoMedia);
                    retorno.setIdxAddr(posicaoMedia);
                    retorno.setId(readValue(retorno.getId(), file));
                    retorno.setAddress(readValue(retorno.getAddress(), file));

                    switch (comparaDoisNumeros(retorno.getId(), id)) {
                        case -1:
                            // O id encontrado é maior que o buscado;
                            fim = file.getFilePointer();
                            break;
                        case 0:
                            // O id encontrado é igual ao buscado
                            return retorno;
                        case 1:
                            // O id encontrado é menor que o buscado
                            inicio = file.getFilePointer();
                            break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
    }

}
