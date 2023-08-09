public final class ObjID implements Serializable {
    public static final int REGISTRY_ID = 0;
    public static final int ACTIVATOR_ID = 1;
    public static final int DGC_ID = 2;
    private static final long serialVersionUID = -6386392263968365220L;
    private static final AtomicLong nextObjNum = new AtomicLong(0);
    private static final UID mySpace = new UID();
    private static final SecureRandom secureRandom = new SecureRandom();
    private final long objNum;
    private final UID space;
    public ObjID() {
        if (useRandomIDs()) {
            space = new UID();
            objNum = secureRandom.nextLong();
        } else {
            space = mySpace;
            objNum = nextObjNum.getAndIncrement();
        }
    }
    public ObjID(int objNum) {
        space = new UID((short) 0);
        this.objNum = objNum;
    }
    private ObjID(long objNum, UID space) {
        this.objNum = objNum;
        this.space = space;
    }
    public void write(ObjectOutput out) throws IOException {
        out.writeLong(objNum);
        space.write(out);
    }
    public static ObjID read(ObjectInput in) throws IOException {
        long num = in.readLong();
        UID space = UID.read(in);
        return new ObjID(num, space);
    }
    public int hashCode() {
        return (int) objNum;
    }
    public boolean equals(Object obj) {
        if (obj instanceof ObjID) {
            ObjID id = (ObjID) obj;
            return objNum == id.objNum && space.equals(id.space);
        } else {
            return false;
        }
    }
    public String toString() {
        return "[" + (space.equals(mySpace) ? "" : space + ", ") +
            objNum + "]";
    }
    private static boolean useRandomIDs() {
        String value = AccessController.doPrivileged(
            new GetPropertyAction("java.rmi.server.randomIDs"));
        return value == null ? true : Boolean.parseBoolean(value);
    }
}
