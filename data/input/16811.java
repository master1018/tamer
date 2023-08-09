public class KerberosHashEqualsTest {
    public static void main(String[] args) throws Exception {
        new KerberosHashEqualsTest().check();
    }
    void checkSame(Object o1, Object o2) {
        if(!o1.equals(o2)) {
            throw new RuntimeException("equals() fails");
        }
        if(o1.hashCode() != o2.hashCode()) {
            throw new RuntimeException("hashCode() not same");
        }
    }
    void checkNotSame(Object o1, Object o2) {
        if(o1.equals(o2)) {
            throw new RuntimeException("equals() succeeds");
        }
    }
    void check() throws Exception {
        KerberosKey k1, k2;
        k1 = new KerberosKey(new KerberosPrincipal("A"), "pass".getBytes(), 1, 1);
        k2 = new KerberosKey(new KerberosPrincipal("A"), "pass".getBytes(), 1, 1);
        checkSame(k1, k1);  
        checkSame(k1, k2);  
        k2.destroy();
        checkNotSame(k1, k2);
        checkNotSame(k2, k1);
        checkSame(k2, k2);
        k2 = new KerberosKey(new KerberosPrincipal("B"), "pass".getBytes(), 1, 1);
        checkNotSame(k1, k2);
        k2 = new KerberosKey(new KerberosPrincipal("A"), "ssap".getBytes(), 1, 1);
        checkNotSame(k1, k2);
        k2 = new KerberosKey(new KerberosPrincipal("A"), "pass".getBytes(), 2, 1);
        checkNotSame(k1, k2);
        k2 = new KerberosKey(new KerberosPrincipal("A"), "pass".getBytes(), 1, 2);
        checkNotSame(k1, k2);
        k1 = new KerberosKey(null, "pass".getBytes(), 1, 2);
        checkNotSame(k1, k2); 
        k2 = new KerberosKey(null, "pass".getBytes(), 1, 2);
        checkSame(k1, k2);    
        checkNotSame(k1, "Another Object");
        KerberosTicket t1, t2;
        t1 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkSame(t1, t1);
        checkSame(t1, t2);
        t2 = new KerberosTicket("asn11".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client1"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server1"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass1".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 2, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {false, true}, new Date(0), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(1), new Date(0), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(1), new Date(0), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(1), new Date(0), null);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(0), new InetAddress[2]);
        checkNotSame(t1, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(1), null);
        t1 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true}, new Date(0), new Date(0), new Date(0), new Date(2), null);
        checkSame(t1, t2);  
        t2.destroy();
        checkNotSame(t1, t2);
        checkNotSame(t2, t1);
        checkSame(t2, t2);
        t2 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true, true, true, true, true, true, true, true, true}, new Date(0), new Date(0), new Date(0), new Date(1), null);
        t1 = new KerberosTicket("asn1".getBytes(), new KerberosPrincipal("client"), new KerberosPrincipal("server"), "pass".getBytes(), 1, new boolean[] {true, true, true, true, true, true, true, true, true, true}, new Date(0), new Date(0), new Date(0), new Date(2), null);
        checkNotSame(t1, t2);  
        checkNotSame(t1, "Another Object");
        System.out.println("Good!");
    }
}
