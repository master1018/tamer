public class DerInputBufferEqualsHashCode {
    public static void main(String[] args) throws Exception {
        String name1 = "CN=eve s. dropper";
        DerOutputStream deros;
        byte[] ba;
        X500Name dn1 = new X500Name(name1);
        deros = new DerOutputStream();
        dn1.encode(deros);
        ba = deros.toByteArray();
        GetDIBConstructor a = new GetDIBConstructor();
        java.security.AccessController.doPrivileged(a);
        Constructor c = a.getCons();
        Object[] objs = new Object[1];
        objs[0] = ba;
        Object db1 = null, db2 = null;
        try {
            db1 = c.newInstance(objs);
            db2 = c.newInstance(objs);
        } catch (Exception e) {
            System.out.println("Caught unexpected exception " + e);
            throw e;
        }
        if ( (db1.equals(db2)) == (db1.hashCode()==db2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception("FAILED equals()/hashCode() contract");
    }
}
class GetDIBConstructor implements java.security.PrivilegedExceptionAction {
    private Class dibClass = null;
    private Constructor dibCons = null;
    public Object run() throws Exception {
        try {
            dibClass = Class.forName("sun.security.util.DerInputBuffer");
            Constructor[] cons = dibClass.getDeclaredConstructors();
            int i;
            for (i = 0; i < cons.length; i++) {
                Class [] parms = cons[i].getParameterTypes();
                if (parms.length == 1) {
                    if (parms[0].getName().equalsIgnoreCase("[B")) {
                        cons[i].setAccessible(true);
                        break;
                    }
                }
            }
            dibCons = cons[i];
        } catch (Exception e) {
            System.out.println("Caught unexpected exception " + e);
            throw e;
        }
        return dibCons;
    }
    public Constructor getCons(){
        return dibCons;
    }
}
