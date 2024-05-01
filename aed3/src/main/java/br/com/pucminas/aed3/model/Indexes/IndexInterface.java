package br.com.pucminas.aed3.model.Indexes;

import java.nio.file.Path;

public interface IndexInterface {

    public void setDataFile(Path p);

    public Path getDataFile();

    public String getIndexFilePath();

    public <T> T insert(T t) throws Exception;

    public <T> void delete(T t) throws Exception;

    public <T> T update(T t) throws Exception;

    public <T> IndexRegisterInterface<?> read(T t) throws Exception;

    public void sort();

}
