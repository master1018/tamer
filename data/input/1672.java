public class SuperClassConsts implements Serializable {
    private static final long serialVersionUID = 6733861379283244755L;
    public static final int SUPER_INT_CONSTANT = 3;
    public final static float SUPER_FLOAT_CONSTANT = 99.3f;
    public final static double SUPER_DOUBLE_CONSTANT  = 33.2;
    public final static boolean SUPER_BOOLEAN_CONSTANT  = false;
    int instanceField;
    public SuperClassConsts(String p) {
    }
    public native int numValues();
    private void writeObject(ObjectOutputStream s)
        throws IOException
    {
        System.err.println("writing state");
    }
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        System.err.println("reading back state");
    }
}
