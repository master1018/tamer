public abstract class RefAddr implements java.io.Serializable {
    protected String addrType;
    protected RefAddr(String addrType) {
        this.addrType = addrType;
    }
    public String getType() {
        return addrType;
    }
    public abstract Object getContent();
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof RefAddr)) {
            RefAddr target = (RefAddr)obj;
            if (addrType.compareTo(target.addrType) == 0) {
                Object thisobj = this.getContent();
                Object thatobj = target.getContent();
                if (thisobj == thatobj)
                    return true;
                if (thisobj != null)
                    return thisobj.equals(thatobj);
            }
        }
        return false;
    }
    public int hashCode() {
        return (getContent() == null)
                ? addrType.hashCode()
                : addrType.hashCode() + getContent().hashCode();
    }
    public String toString(){
        StringBuffer str = new StringBuffer("Type: " + addrType + "\n");
        str.append("Content: " + getContent() + "\n");
        return (str.toString());
    }
    private static final long serialVersionUID = -1468165120479154358L;
}
