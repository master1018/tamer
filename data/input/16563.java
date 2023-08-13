class GoodOOS1 extends ObjectOutputStream {
    GoodOOS1(OutputStream out) throws IOException { super(out); }
}
class GoodOOS2 extends GoodOOS1 {
    GoodOOS2(OutputStream out) throws IOException { super(out); }
}
class BadOOS1 extends ObjectOutputStream {
    BadOOS1(OutputStream out) throws IOException { super(out); }
    public PutField putFields() throws IOException { return null; }
}
class BadOOS2 extends ObjectOutputStream {
    BadOOS2(OutputStream out) throws IOException { super(out); }
    public void writeUnshared(Object obj) throws IOException {}
}
class BadOOS3 extends GoodOOS1 {
    BadOOS3(OutputStream out) throws IOException { super(out); }
    public void writeUnshared(Object obj) throws IOException {}
}
class GoodOIS1 extends ObjectInputStream {
    GoodOIS1(InputStream in) throws IOException { super(in); }
}
class GoodOIS2 extends GoodOIS1 {
    GoodOIS2(InputStream in) throws IOException { super(in); }
}
class BadOIS1 extends ObjectInputStream {
    BadOIS1(InputStream in) throws IOException { super(in); }
    public GetField readFields() throws IOException, ClassNotFoundException {
        return null;
    }
}
class BadOIS2 extends ObjectInputStream {
    BadOIS2(InputStream in) throws IOException { super(in); }
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return null;
    }
}
class BadOIS3 extends GoodOIS1 {
    BadOIS3(InputStream in) throws IOException { super(in); }
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return null;
    }
}
public class AuditStreamSubclass {
    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.flush();
        byte[] buf = bout.toByteArray();
        new GoodOOS1(bout);
        new GoodOOS2(bout);
        new GoodOIS1(new ByteArrayInputStream(buf));
        new GoodOIS2(new ByteArrayInputStream(buf));
        try {
            new BadOOS1(bout);
            throw new Error();
        } catch (SecurityException ex) {
        }
        try {
            new BadOOS2(bout);
            throw new Error();
        } catch (SecurityException ex) {
        }
        try {
            new BadOOS3(bout);
            throw new Error();
        } catch (SecurityException ex) {
        }
        try {
            new BadOIS1(new ByteArrayInputStream(buf));
            throw new Error();
        } catch (SecurityException ex) {
        }
        try {
            new BadOIS2(new ByteArrayInputStream(buf));
            throw new Error();
        } catch (SecurityException ex) {
        }
        try {
            new BadOIS3(new ByteArrayInputStream(buf));
            throw new Error();
        } catch (SecurityException ex) {
        }
    }
}
