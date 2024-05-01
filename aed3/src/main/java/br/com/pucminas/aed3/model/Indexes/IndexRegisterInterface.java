package br.com.pucminas.aed3.model.Indexes;

public interface IndexRegisterInterface<T> {

    public T getId();

    public void setId(T value);

    public Long getAddress();

    public void setAddress(Long value);

    public Long getIdxAddr();

    public void setIdxAddr(Long value);

    public String toString();
}
