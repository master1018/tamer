package com.icteam.fiji.model;

/**
 * TipAudt generated by hbm2java
 */
public class TipAudt extends Auditable implements java.io.Serializable {

    private Long CTipAudt;

    private String NTipAudt;

    private TipOperAudt tipOperAudt;

    /**
    * default constructor
    */
    public TipAudt() {
    }

    public Long getCTipAudt() {
        return this.CTipAudt;
    }

    public void setCTipAudt(Long CTipAudt) {
        if (CTipAudt != null && CTipAudt <= 0) this.CTipAudt = null; else this.CTipAudt = CTipAudt;
    }

    public String getNTipAudt() {
        return this.NTipAudt;
    }

    public void setNTipAudt(String NTipAudt) {
        this.NTipAudt = NTipAudt;
    }

    public TipOperAudt getTipOperAudt() {
        return tipOperAudt;
    }

    public void setTipOperAudt(TipOperAudt tipOperAudt) {
        this.tipOperAudt = tipOperAudt;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipAudt)) return false;
        TipAudt that = (TipAudt) o;
        Long tip = that.getCTipAudt();
        if (CTipAudt != null ? !CTipAudt.equals(tip) : tip != null) return false;
        return true;
    }

    public int hashCode() {
        return (CTipAudt != null ? CTipAudt.hashCode() : 0);
    }
}