class A implements Serializable {
    private static final long serialVersionUID = 0L;
    static {
        if ("foo".equals("foo")) {      
            throw new Error();
        }
    }
}
public class PartialClassDesc {
    public static void main(String[] args) throws Exception {
        Class cl = Class.forName(
            "A", false, PartialClassDesc.class.getClassLoader());
        ObjectStreamClass desc = null;
        try {
            desc = ObjectStreamClass.lookup(cl);
        } catch (Throwable th) {
        }
        try {
            desc = ObjectStreamClass.lookup(cl);
        } catch (Throwable th) {
        }
        if (desc != null) {
            throw new Error("should not be able to obtain class descriptor");
        }
    }
}
