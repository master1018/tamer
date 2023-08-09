public class DerValueEqualsHashCode {
    public static void main(String[] args) throws Exception {
        String name = "CN=user";
        X500Name dn = new X500Name(name);
        DerOutputStream deros;
        byte[] ba;
        deros = new DerOutputStream();
        dn.encode(deros);
        ba = deros.toByteArray();
        DerValue dv1 = new DerValue(ba);
        DerValue dv2 = new DerValue(ba);
        if ( (dv1.equals(dv2)) == (dv1.hashCode()==dv2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception("FAILED equals()/hashCode() contract");
    }
}
