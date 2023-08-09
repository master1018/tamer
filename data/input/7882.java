class Z implements Serializable {
    private static final boolean serialVersionUID = false;
}
class B implements Serializable {
    private static final byte serialVersionUID = 5;
}
class C implements Serializable {
    private static final char serialVersionUID = 5;
}
class S implements Serializable {
    private static final short serialVersionUID = 5;
}
class I implements Serializable {
    private static final int serialVersionUID = 5;
}
class F implements Serializable {
    private static final float serialVersionUID = 5.0F;
}
class D implements Serializable {
    private static final double serialVersionUID = 5.0;
}
class L implements Serializable {
    private static final Object serialVersionUID = "5";
}
public class BadSerialVersionUID {
    public static void main(String[] args) throws Exception {
        Class[] ignore = { Z.class, F.class, D.class, L.class };
        Class[] convert = { B.class, C.class, S.class, I.class };
        for (int i = 0; i < ignore.length; i++) {
            ObjectStreamClass.lookup(ignore[i]).getSerialVersionUID();
        }
        for (int i = 0; i < convert.length; i++) {
            ObjectStreamClass desc = ObjectStreamClass.lookup(convert[i]);
            if (desc.getSerialVersionUID() != 5L) {
                throw new Error();
            }
        }
    }
}
