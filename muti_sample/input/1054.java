public class CanonicalName {
    public static void main(String args[]) throws Exception {
        final Hashtable<String,String> env = new Hashtable<String,String>();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ctx = new InitialDirContext(env);
        String ids[] = { "CNAME" };
        Attributes attrs = ctx.getAttributes(args[0], ids);
        NamingEnumeration ne = attrs.getAll();
        if (!ne.hasMoreElements()) {
            throw new Exception("no CNAME record");
        }
        while (ne.hasMoreElements()) {
            Attribute attr = (Attribute)ne.next();
            for (NamingEnumeration e = attr.getAll(); e.hasMoreElements();) {
                System.out.println(args[0] + " -> " + e.next());
            }
        }
    }
}
