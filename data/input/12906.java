class SerialTest {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("out"))
            out(args[1]);
        else if (args[0].equals("in"))
            in(args[1]);
        else
            badin();
    }
    static void in(String oid) throws Exception {
        ObjectIdentifier o = (ObjectIdentifier) (new ObjectInputStream(System.in).readObject());
        if (!o.toString().equals(oid))
            throw new Exception("Read Fail " + o + ", not " + oid);
    }
    static void badin() throws Exception {
        boolean pass = true;
        try {
            new ObjectInputStream(System.in).readObject();
        } catch (Exception e) {
            pass = false;
        }
        if (pass) throw new Exception("Should fail but not");
    }
    static void out(String oid) throws Exception {
        new ObjectOutputStream(System.out).writeObject(new ObjectIdentifier(oid));
    }
}
