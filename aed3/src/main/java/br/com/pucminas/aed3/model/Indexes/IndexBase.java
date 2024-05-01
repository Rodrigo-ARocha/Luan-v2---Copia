package br.com.pucminas.aed3.model.Indexes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import br.com.pucminas.aed3.model.Notations.KeyP;

public class IndexBase implements IndexInterface {
    protected Path dataFile;
    protected Path indexFile;

    protected IndexRegisterInterface<?> registroBase;

    protected Class<?> tipoRegistro = null;

    public IndexBase(Path data, String indexFileName) {
        dataFile = data;
        indexFile = data.getParent().resolve(indexFileName + ".db");
        registroBase = new RegistroIndexBase();
    }

    public IndexBase() {
        indexFile = Paths.get("index.db");
        registroBase = new RegistroIndexBase();
    }

    @Override
    public void setDataFile(Path p) {
        dataFile = p;
    }

    @Override
    public Path getDataFile() {
        return dataFile;
    }

    @Override
    public String getIndexFilePath() {
        return indexFile.toString();
    }

    @Override
    public <T> T insert(T t) throws Exception {

        IndexRegisterInterface<?> idx = read(t);
        RandomAccessFile raf = null;
        Object lastId = null;
        Boolean sortedBoolean = true;
        raf = openIdxFile();
        lastId = readLastIdHeader(raf, idx.getId().getClass());
        sortedBoolean = raf.readBoolean();
        if (Objects.isNull(lastId))
            throw new Exception(
                    "Não foi possível tratar o tipo de campo da chave da entidade. A opração de inserção no índice foi interrompida");
        else {
            int comparacao = comparaDoisNumeros(idx.getId(), lastId);
            if (comparacao == -1) {
                // Se id da entidade é maior que lastId armazenado;
                // armazena no final do arquivo e atualiza o lastId;
                lastId = idx.getId();
                raf.seek(raf.length());
                raf.write(gerarRegistro(idx));
                raf.seek(0);
                writeValue(lastId, raf);
                raf.seek(0);
            } else {
                // Caso o id da entidade é menor ou igual que o maior id;
                // Verifica se registro do índice retornado possui enderço válido no arquivo
                // principal;
                // Caso não aconteça o registro novo é inserido no índice;
                // Caso aconteça é gerado uma exceção de erro de inserção
                if (Objects.isNull(idx) || (Objects.isNull(idx.getAddress()) || idx.getAddress() < 0)) {
                    raf.seek(raf.length());
                    raf.write(gerarRegistro(t));
                    raf.seek(lengthOf(idx.getId()));
                    sortedBoolean = false;
                    raf.writeBoolean(sortedBoolean);
                } else {
                    throw new Exception("Erro de inserção: Já existe um registro com id " + idx.getId().toString());
                }
            }
        }
        return t;
    }

    @Override
    public <T> void delete(T t) throws Exception {
        if (Objects.nonNull(t)) {
            IndexRegisterInterface<?> aux = read(t);
            if (Objects.nonNull(aux) && Objects.nonNull(aux.getAddress()) && aux.getAddress() > 0) {
                RandomAccessFile raf = openIdxFile();
                raf.seek(aux.getIdxAddr());
                aux.setAddress(-1L);
                raf.write(gerarRegistro(aux));
            } else {
                throw new Exception("Não foi encontrado um índice para o registro de id " + aux.getId().toString());
            }
        }
    }

    @Override
    public <T> T update(T t) throws Exception {
        if (Objects.nonNull(t)) {
            IndexRegisterInterface<?> aux = read(t);
            if (Objects.nonNull(aux) && Objects.nonNull(aux.getAddress())) {
                RandomAccessFile raf = openIdxFile();
                raf.seek(aux.getIdxAddr());
                raf.write(gerarRegistro(aux));
            } else {
                throw new Exception("Não foi encontrado um índice para o registro informado");
            }
        }
        return t;
    }

    @Override
    public void sort() {

    }

    @Override
    public <T> IndexRegisterInterface<?> read(T t) throws Exception {

        IndexRegisterInterface<?> retorno = null;
        if (Objects.nonNull(tipoRegistro) && IndexRegisterInterface.class.isAssignableFrom(tipoRegistro)) {

            Constructor<?> constructor = null;
            try {
                constructor = tipoRegistro.getDeclaredConstructor();
                if (Objects.nonNull(constructor)) {
                    retorno = (IndexRegisterInterface<?>) constructor.newInstance();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                retorno = registroBase.getClass().getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Objects.nonNull(retorno)) {
            Object value = readPkValue(t);
            ((IndexRegisterInterface<Object>) retorno).setId(value);

            RandomAccessFile raf = openIdxFile();
            Object lastId = null;
            if (Objects.nonNull(raf)) {
                retorno = findById(value, lastId, raf);
            }
        }
        return retorno;
    }

    protected <T> byte[] gerarRegistro(T t) throws IOException, NoSuchFieldException, IllegalAccessException {
        byte[] retorno = null, aux = null;
        Class<?> clazz = t.getClass();
        try {
            Field f = Arrays.stream(clazz.getFields()).filter(el -> el.getName().equals("id")).findFirst().orElse(null);
            if (Objects.isNull(f)) {
                return null;
            }

            f.setAccessible(true);
            Object valor = f.get(t);
            if (Serializable.class.isAssignableFrom(f.getType())) {
                try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        ObjectOutputStream objectStream = new ObjectOutputStream(outStream)) {

                    objectStream.writeObject(valor);
                    objectStream.flush();
                    retorno = outStream.toByteArray();
                }

            } else {
                retorno = valor.toString().getBytes();
            }
            f = Arrays.stream(clazz.getFields()).filter(el -> el.getName().equals("filePosition")).findFirst()
                    .orElse(null);
            if (Objects.nonNull(f)) {
                if (Serializable.class.isAssignableFrom(f.getType())) {
                    try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                            ObjectOutputStream objectStream = new ObjectOutputStream(outStream)) {

                        objectStream.writeObject(valor);
                        objectStream.flush();
                        aux = outStream.toByteArray();
                        retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                        System.arraycopy(aux, 0, retorno, retorno.length - aux.length - 1, aux.length);

                    }

                } else {
                    aux = valor.toString().getBytes();
                    retorno = Arrays.copyOf(retorno, retorno.length + aux.length);
                    System.arraycopy(aux, 0, retorno, retorno.length - aux.length - 1, aux.length);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retorno;
    }

    protected byte[] gerarRegistro(IndexRegisterInterface<?> obj) throws Exception {
        byte[] retorno = null, idBytes, addrBytes;
        if (Objects.nonNull(obj) && Objects.nonNull(obj.getIdxAddr()))
            idBytes = toByteArray(obj.getId());
        else
            throw new Exception("O id do registro não pode ser nulo");

        if (Objects.nonNull(obj) && Objects.nonNull(obj.getAddress()))
            addrBytes = toByteArray(obj.getAddress());
        else
            throw new Exception("O endereço referenciado não pode ser nulo");

        retorno = new byte[idBytes.length + addrBytes.length];
        System.arraycopy(idBytes, 0, retorno, 0, idBytes.length);
        System.arraycopy(addrBytes, 0, retorno, idBytes.length, addrBytes.length);

        return retorno;
    }

    protected Integer comparaDoisNumeros(Object obj1, Object obj2) {
        Integer retorno = 0;
        Number num1 = (Number) obj1, num2 = (Number) obj2;
        retorno = num1.doubleValue() < num2.doubleValue() ? 1 : num1.doubleValue() > num2.doubleValue() ? -1 : 0;

        return retorno;
    }

    protected void writeValue(Object value, RandomAccessFile raf) throws Exception {
        if (Objects.isNull(raf)) {
            throw new Exception("Arquivo de índice não definido (null)");
        }
        if (Objects.isNull(value)) {
            throw new Exception("Valor null não pode ser escrito");
        }
        switch (typeOf(value)) {
            case 0:
                raf.writeByte((Byte) value);
                break;
            case 1:
                raf.writeShort((Short) value);
                break;
            case 2:
                raf.writeInt((Integer) value);
                break;
            case 3:
                raf.writeFloat((Float) value);
                break;
            case 4:
                raf.writeLong((Long) value);
                break;
            case 5:
                raf.writeDouble((Double) value);
                break;
        }
    }

    protected <T> T readValue(T id, RandomAccessFile raf) throws Exception {
        if (Objects.isNull(raf)) {
            throw new Exception("Arquivo de índice não definido (null)");
        }

        Object retorno = null;
        switch (typeOf(id)) {
            case 0:
                retorno = raf.readByte();
                break;
            case 1:
                retorno = raf.readShort();
                break;
            case 2:
                retorno = raf.readInt();
                break;
            case 3:
                retorno = raf.readFloat();
                break;
            case 4:
                retorno = raf.readLong();
                break;
            case 5:
                retorno = raf.readDouble();
                break;
            default:
                throw new Exception(
                        String.format("O tipo de dado %s não é um tipo tratável para numericos",
                                id.getClass().getName()));
        }
        return (T) retorno;
    }

    protected Object readLastIdHeader(RandomAccessFile raf, Class<?> idType) throws IOException, Exception {
        if (Objects.nonNull(raf)) {
            raf.seek(0);
            if (sameTypeClass(idType, Byte.class))
                return raf.readByte();
            else if (sameTypeClass(idType, Short.class))
                return raf.readShort();
            else if (sameTypeClass(idType, Integer.class))
                return raf.readInt();
            else if (sameTypeClass(idType, Float.class))
                return raf.readFloat();
            else if (sameTypeClass(idType, Long.class))
                return raf.readLong();
            else if (sameTypeClass(idType, Double.class))
                return raf.readDouble();
            else
                throw new Exception("Tipo informado de Id não válido");
        }
        return null;
    }

    private int typeOf(Object o) {
        int retorno = -1;
        retorno = (o instanceof Byte) ? 0
                : (o instanceof Short) ? 1
                        : (o instanceof Integer) ? 2
                                : (o instanceof Float) ? 3
                                        : (o instanceof Long) ? 4
                                                : (o instanceof Double) ? 5 : (o instanceof String) ? 100 : 999;
        return retorno;
    }

    protected Integer lengthOf(Object o) {
        switch (typeOf(o)) {
            case 0:
                return Byte.BYTES;
            case 1:
                return Short.BYTES;
            case 2:
                return Integer.BYTES;
            case 3:
                return Float.BYTES;
            case 4:
                return Long.BYTES;
            case 5:
                return Double.BYTES;
            case 100:
                return (new String(o.toString().getBytes(), StandardCharsets.UTF_8)).length();
            default:
                return null;
        }
    }

    protected <T> Long getRegisterAddr(T t) {
        try {
            IndexRegisterInterface<?> idx = read(t);
            if (Objects.nonNull(idx)) {
                return idx.getAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] toByteArray(Object o) throws IOException, NoSuchFieldException, IllegalAccessException {
        byte[] retorno = null;
        if (Serializable.class.isAssignableFrom(o.getClass())) {
            try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectStream = new ObjectOutputStream(outStream)) {

                objectStream.writeObject(o);
                objectStream.flush();
                retorno = outStream.toByteArray();
            }

        } else {
            retorno = o.toString().getBytes();
        }
        return retorno;
    }

    private Boolean sameTypeClass(Class<?> testeClass, Class<?> targetClass) {
        return targetClass.isAssignableFrom(testeClass);
    }

    protected <T> Object readPkValue(T obj) throws Exception {
        Class<?> clazz = obj.getClass();
        Field f = null;

        f = Arrays.stream(clazz.getDeclaredFields()).filter(el -> el.isAnnotationPresent(KeyP.class))
                .findAny()
                .orElse(null);
        if (Objects.isNull(f)) {
            throw new Exception(String.format("Não foi definido para classe \"%s\" um campo com a notação \"%s\"",
                    clazz.getName(), KeyP.class.getName()));
        }
        Object retorno;
        f.setAccessible(true);
        // retorno = f.getType().getDeclaredConstructor().newInstance();
        retorno = f.get(obj);

        return retorno;
    }

    protected RandomAccessFile openIdxFile() throws Exception {
        return new RandomAccessFile(indexFile.toFile(), "rw");
    }

    protected IndexRegisterInterface<?> findById(Object id, Object lastId, RandomAccessFile file) {
        Boolean sorted;
        IndexRegisterInterface<?> idx = null;
        try {
            file.seek(0);
            lastId = readLastIdHeader(file, id.getClass());
            sorted = file.readBoolean();
            if (sorted) {
                idx = buscaOrdenada(id, lastId, file);
            } else {
                idx = buscaSequencial(id, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idx;
    }

    protected IndexRegisterInterface<?> buscaOrdenada(Object id, Object lastId, RandomAccessFile file) {
        return null;
    }

    protected IndexRegisterInterface<?> buscaSequencial(Object id, RandomAccessFile file) {
        IndexRegisterInterface<?> retorno = gerarRegistro();
        Long addr;
        try {
            // reposiciona o arquivo e lê os dados do cabeçalho
            file.seek(0);
            readLastIdHeader(file, id.getClass());
            file.readBoolean();

            // Realiza a busca sequencioal no arquivo de indice;
            while (file.getFilePointer() < file.length()) {
                retorno.setIdxAddr(file.getFilePointer());
                Object test = readValue(id, file);
                addr = file.readLong();
                if (comparaDoisNumeros(id, test) == 0) {
                    // Se encontrar o valor do id posiciona no início do registro e retorna
                    file.seek(retorno.getIdxAddr());
                    invokeSetId(retorno, test);
                    retorno.setAddress(addr);
                    return retorno;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void skipHeader(RandomAccessFile file) {
        try {
            IndexRegisterInterface<?> idx = gerarRegistro();
            if (Objects.nonNull(file)) {
                file.seek(0);
                readLastIdHeader(file, idx.getId().getClass());
                file.readBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected IndexRegisterInterface<?> gerarRegistro() {
        Object o = null;
        if (Objects.nonNull(tipoRegistro) && sameTypeClass(tipoRegistro, IndexRegisterInterface.class)) {
            try {
                o = tipoRegistro.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                o = registroBase.getClass().getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (IndexRegisterInterface<?>) o;
    }

    protected void invokeSetId(IndexRegisterInterface<?> obj, Object v) {
        try {
            Method m = IndexRegisterInterface.class.getMethod("setId", Object.class);
            m.invoke(obj, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void atualizaIndice(T entity) {

        try {
            IndexRegisterInterface<?> idx = read(entity);
            if (Objects.nonNull(idx)) {
                Class<?> clazz = entity.getClass();
                Field f = clazz.getField("lapide");
                if (Objects.nonNull(f)) {
                    f.setAccessible(true);
                    if ((Boolean) f.get(entity)) {
                        idx.setAddress(-1L);
                    }
                }
            } else {
                insert(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> T insertIdx(T value) {
        IndexRegisterInterface<?> idx, idxValue;
        if (!IndexRegisterInterface.class.isAssignableFrom(value.getClass())) {
            return null;
        }
        try {
            idxValue = ((IndexRegisterInterface<?>) value);
            RandomAccessFile raf = openIdxFile();
            Object lastId = readLastIdHeader(raf, idxValue.getId().getClass());
            idx = findById(idxValue.getId(), lastId, raf);
            if (Objects.nonNull(idx)) {
                idxValue.setAddress(idx.getAddress());
                idxValue.setIdxAddr(idx.getIdxAddr());
            } else {
                switch (comparaDoisNumeros(idxValue.getId(), lastId)) {
                    case -1:
                        // Caso o id seja maior que o último id armazenado
                        // insere al final do arquivo e atualiza o último id;

                        lastId = idxValue.getId();
                        raf.seek(raf.length());
                        idxValue.setAddress(raf.length());
                        raf.write(gerarRegistro(idxValue));
                        raf.seek(0);
                        writeValue(lastId, raf);
                        break;
                    case 1:
                        // Caso o id seja menor que o último id do arquivo;
                        // Salva o registro ao final do arquivo e
                        // Marca o arquivo como não ordenado
                        raf.seek(raf.length());
                        idxValue.setAddress(raf.length());
                        raf.write(gerarRegistro(idxValue));
                        raf.seek(0);
                        readValue(idxValue.getId(), raf);
                        raf.writeBoolean(false);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public <T> void insert(T entity, Long addr) {
        try {

            Object idValue = readPkValue(entity);
            Object lastId;
            Boolean sortedBoolean;

            IndexRegisterInterface<?> idx = gerarRegistro();

            if (Objects.isNull(idValue)) {
                throw new Exception("não encontrado campo id da entidade");
            }
            RandomAccessFile raf = openIdxFile();
            if (raf.length() > 0) {
                lastId = readLastIdHeader(raf, idx.getId().getClass());
                sortedBoolean = raf.readBoolean();
            } else {
                // idx.setId((Number) 0);
                writeValue(idx.getId(), raf);
                raf.seek(0);
                lastId = readValue(idx.getId(), raf);
                if (Number.class.isAssignableFrom(lastId.getClass())) {
                    lastId = 0;
                } else {
                    lastId = "";
                }
                raf.seek(0);
                writeValue(lastId, raf);
                raf.writeBoolean(true);
            }
            if (Objects.nonNull(findById(idValue, lastId, raf))) {
                return;
            }
            switch (comparaDoisNumeros(idValue, lastId)) {
                case 1:
                    raf.seek(raf.length());
                    writeValue(idValue, raf);
                    writeValue(addr, raf);
                    skipHeader(raf);
                    raf.seek(raf.getFilePointer() - 1);
                    raf.writeBoolean(false);
                    break;
                case -1:
                    raf.seek(raf.length());
                    writeValue(idValue, raf);
                    writeValue(addr, raf);
                    raf.seek(0);
                    writeValue(idValue, raf);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
