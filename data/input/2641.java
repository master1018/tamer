public class AVAEqualsHashCode {
   public static void main(String[] args) throws Exception {
        int data[] = { 1, 2, 840, 113549, 2, 5 };
        String name = "CN=eve s. dropper";
        X500Name dn = new X500Name(name);
        DerOutputStream deros = new DerOutputStream();
        ObjectIdentifier oid = new ObjectIdentifier(data);
        dn.encode(deros);
        byte[] ba = deros.toByteArray();
        DerValue dv = new DerValue(ba);
        GetAVAConstructor a = new GetAVAConstructor();
        java.security.AccessController.doPrivileged(a);
        Constructor c = a.getCons();
        Object[] objs = new Object[2];
        objs[0] = oid;
        objs[1] = dv;
        Object ava1 = null, ava2 = null;
        try {
            ava1 = c.newInstance(objs);
            ava2 = c.newInstance(objs);
        } catch (Exception e) {
            System.out.println("Caught unexpected exception " + e);
            throw e;
        }
        if ( (ava1.equals(ava2)) == (ava1.hashCode()==ava2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception("FAILED equals()/hashCode() contract");
    }
}
class GetAVAConstructor implements java.security.PrivilegedExceptionAction {
    private Class avaClass = null;
    private Constructor avaCons = null;
    public Object run() throws Exception {
        try {
            avaClass = Class.forName("sun.security.x509.AVA");
            Constructor[] cons = avaClass.getDeclaredConstructors();
            int i;
            for (i = 0; i < cons.length; i++) {
                Class [] parms = cons[i].getParameterTypes();
                if (parms.length == 2) {
                    if (parms[0].getName().equalsIgnoreCase("sun.security.util.ObjectIdentifier") &&
                            parms[1].getName().equalsIgnoreCase("sun.security.util.DerValue")) {
                        avaCons = cons[i];
                        avaCons.setAccessible(true);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Caught unexpected exception " + e);
            throw e;
        }
        return avaCons;
    }
    public Constructor getCons(){
        return avaCons;
    }
}
