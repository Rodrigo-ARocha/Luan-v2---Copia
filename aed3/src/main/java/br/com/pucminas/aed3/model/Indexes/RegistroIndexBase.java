package br.com.pucminas.aed3.model.Indexes;

public class RegistroIndexBase implements IndexRegisterInterface<Long> {

    protected Long id;
    protected Long address;
    protected Long idxAddr;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long value) {
        this.id = value;
    }

    @Override
    public Long getAddress() {
        return this.address;
    }

    @Override
    public void setAddress(Long value) {
        this.address = value;
    }

    @Override
    public Long getIdxAddr() {
        return this.idxAddr;
    }

    @Override
    public void setIdxAddr(Long value) {
        this.idxAddr = value;
    }

}
