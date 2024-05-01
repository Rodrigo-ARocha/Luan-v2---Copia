package br.com.pucminas.aed3.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import br.com.pucminas.aed3.model.Notations.KeyP;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimeIMDB {

    @KeyP
    private Integer id;

    private String titulo;

    private List<String> generos;

    private Float avaliacao;

    private Integer votos;

    private Integer anoLancamento;

    private Integer anoEncerramento = 0;

    private String resumo;

    private List<String> elenco;

    private Date cadastro;

    private Boolean excluido = false;

    public AnimeIMDB() {
        this.id = -1;
        this.titulo = "";
        this.generos = new ArrayList<String>();
        this.avaliacao = 0f;
        this.votos = 0;
        this.anoLancamento = 0;
        this.anoEncerramento = 0;
        this.resumo = "";
        this.elenco = new ArrayList<String>();
        this.cadastro = null;
        this.excluido = false;
    }

    public void gerarDataCadastro() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            long min = sf.parse(anoLancamento.toString() + "-01-01").getTime(), max = (new Date()).getTime();
            if (Objects.nonNull(anoLancamento) && !anoLancamento.equals(0))
                cadastro = new Date((long) ((Math.random() * (max - min)) + min));
            else
                cadastro = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] gerarRegistro() {
        byte[] retorno, aux;

        // retorno = BytesUtils.toBytes(excluido);
        retorno = new byte[0];

        aux = BytesUtils.toBytes(id);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(titulo.length()).shortValue()));
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(titulo);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(avaliacao);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(votos);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        if (Objects.nonNull(generos) && generos.size() > 0) {

            aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(generos.size()).shortValue()));
            retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
            System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

            for (String gen : generos) {
                aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(gen.length()).shortValue()));
                retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

                aux = BytesUtils.toBytes(gen);
                retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);
            }

        } else {
            aux = BytesUtils.toBytes(Short.valueOf("0"));
            retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
            System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);
        }

        aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(resumo.length()).shortValue()));
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(resumo);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(anoLancamento);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = BytesUtils.toBytes(anoEncerramento);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        if (Objects.nonNull(elenco) && elenco.size() > 0) {

            aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(elenco.size()).shortValue()));
            retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
            System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

            for (String e : elenco) {
                aux = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(e.length()).shortValue()));
                retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

                aux = BytesUtils.toBytes(e);
                retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);
            }

        } else {
            aux = BytesUtils.toBytes(Short.valueOf("0"));
            retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
            System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);
        }

        if (Objects.isNull(cadastro))
            gerarDataCadastro();

        aux = BytesUtils.toBytes(Objects.isNull(cadastro) ? 0L : cadastro.getTime());
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = retorno;
        retorno = BytesUtils.toBytes(Short.valueOf(Integer.valueOf(retorno.length).shortValue()));
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        aux = retorno;
        retorno = BytesUtils.toBytes(excluido);
        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
        System.arraycopy(aux, 0, retorno, retorno.length - aux.length, aux.length);

        return retorno;
    }

    public static AnimeIMDB desserializar(byte[] dados) {
        AnimeIMDB retorno = new AnimeIMDB();

        byte[] auxBool = new byte[1], auxInt = new byte[4], auxLong = new byte[8], auxShort = new byte[2], auxString;
        Short tam = 0, field = 1, count = 0;
        Integer pos = 0;

        while (pos < dados.length) {
            switch (field) {
                case 1:
                    System.arraycopy(dados, pos, auxInt, 0, auxInt.length);
                    retorno.setId(BytesUtils.fromBytes(auxInt, retorno.getId().getClass()));
                    pos += auxInt.length;
                    break;
                case 2:
                    System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                    tam = BytesUtils.fromBytes(auxShort, tam.getClass());
                    pos += auxShort.length;

                    auxString = new byte[tam];
                    System.arraycopy(dados, pos, auxString, 0, tam);
                    retorno.setTitulo(BytesUtils.fromBytes(auxString, retorno.getTitulo().getClass()));
                    pos += auxString.length;
                    break;
                case 3:
                    System.arraycopy(dados, pos, auxInt, 0, auxInt.length);
                    retorno.setAvaliacao(BytesUtils.fromBytes(auxInt, retorno.getAvaliacao().getClass()));
                    pos += auxInt.length;
                    break;
                case 4:
                    System.arraycopy(dados, pos, auxInt, 0, auxInt.length);
                    retorno.setVotos(BytesUtils.fromBytes(auxInt, retorno.getVotos().getClass()));
                    pos += auxInt.length;
                    break;
                case 5:
                    System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                    count = BytesUtils.fromBytes(auxShort, tam.getClass());
                    pos += auxShort.length;

                    if (Objects.isNull(retorno.getGeneros())) {
                        retorno.setGeneros(new ArrayList<String>());
                    }

                    for (int i = 0; i < count; i++) {
                        System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                        tam = BytesUtils.fromBytes(auxShort, tam.getClass());
                        pos += auxShort.length;

                        auxString = new byte[tam];
                        System.arraycopy(dados, pos, auxString, 0, tam);
                        retorno.getGeneros().add(BytesUtils.fromBytes(auxString, retorno.getTitulo().getClass()));
                        pos += auxString.length;
                    }
                    break;
                case 6:
                    System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                    tam = BytesUtils.fromBytes(auxShort, tam.getClass());
                    pos += auxShort.length;

                    auxString = new byte[tam];
                    System.arraycopy(dados, pos, auxString, 0, tam);
                    retorno.setResumo(BytesUtils.fromBytes(auxString, retorno.getTitulo().getClass()));
                    pos += auxString.length;
                    break;
                case 7:
                    System.arraycopy(dados, pos, auxInt, 0, auxInt.length);
                    retorno.setAnoLancamento(BytesUtils.fromBytes(auxInt, retorno.getAnoLancamento().getClass()));
                    pos += auxInt.length;
                    break;
                case 8:
                    System.arraycopy(dados, pos, auxInt, 0, auxInt.length);
                    retorno.setAnoEncerramento(BytesUtils.fromBytes(auxInt, retorno.getAnoEncerramento().getClass()));
                    pos += auxInt.length;
                    break;
                case 9:
                    System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                    count = BytesUtils.fromBytes(auxShort, tam.getClass());
                    pos += auxShort.length;

                    if (Objects.isNull(retorno.getElenco())) {
                        retorno.setElenco(new ArrayList<String>());
                    }

                    for (int i = 0; i < count; i++) {
                        System.arraycopy(dados, pos, auxShort, 0, auxShort.length);
                        tam = BytesUtils.fromBytes(auxShort, tam.getClass());
                        pos += auxShort.length;

                        auxString = new byte[tam];
                        System.arraycopy(dados, pos, auxString, 0, tam);
                        retorno.getElenco().add(BytesUtils.fromBytes(auxString, retorno.getTitulo().getClass()));
                        pos += auxString.length;
                    }
                    break;
                case 10:
                    System.arraycopy(dados, pos, auxLong, 0, auxLong.length);
                    retorno.setCadastro(
                            new Date(BytesUtils.fromBytes(auxLong, Long.valueOf(0l).getClass()).longValue()));
                    pos += auxInt.length;
                    break;
                default:
                    pos++;
                    break;
            }

            field++;
        }
        return retorno;
    }

}