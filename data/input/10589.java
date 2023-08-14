class A implements Serializable {
    static {
        throwMe(new RuntimeException("blargh"));
    }
    static void throwMe(RuntimeException ex) throws RuntimeException {
        throw ex;
    }
}
class B implements Serializable {
}
class C implements Serializable {
    static { System.out.println("C.<clinit>"); }
}
class B1 extends B {
}
class B2 extends B {
    static { System.out.println("B2.<clinit>"); }
}
class C1 extends C {
}
class C2 extends C {
    static { System.out.println("C2.<clinit>"); }
}
public class GetSuidClinitError {
    public static void main(String[] args) throws Exception {
        Class cl = Class.forName(
            "A", false, GetSuidClinitError.class.getClassLoader());
        for (int i = 0; i < 2; i++) {
            try {
                ObjectStreamClass.lookup(cl).getSerialVersionUID();
                throw new Error();
            } catch (ExceptionInInitializerError er) {
            } catch (NoClassDefFoundError er) {
                System.out.println("warning: caught " + er +
                    " instead of ExceptionInInitializerError");
            }
        }
        Class[] cls = new Class[] {
            B.class, B1.class, B2.class,
            C.class, C1.class, C2.class
        };
        long[] suids = new long[] {     
            369445310364440919L, 7585771686008346939L, -8952923334200087495L,
            3145821251853463625L, 327577314910517070L, -92102021266426451L
        };
        for (int i = 0; i < cls.length; i++) {
            if (ObjectStreamClass.lookup(cls[i]).getSerialVersionUID() !=
                suids[i])
            {
                throw new Error();
            }
        }
    }
}
