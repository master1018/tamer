public class IDGenerator implements Serializable {
    protected String prefix;
    protected BigInteger nextid;
    public IDGenerator(String prefix) {
        this.prefix = prefix;
        nextid = BigInteger.valueOf(0);
    }
    public ID getNextID() {
        ID id = new ID(prefix, nextid);
        nextid = nextid.add(BigInteger.valueOf(1));
        return id;
    }
}
