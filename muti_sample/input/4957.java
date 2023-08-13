public class Krb5NameEquals {
    private static String NAME_STR1 = "service@host";
    private static String NAME_STR2 = "service@host2";
    private static final Oid MECH;
    static {
        Oid temp = null;
        try {
            temp = new Oid("1.2.840.113554.1.2.2"); 
        } catch (Exception e) {
        }
        MECH = temp;
    }
    public static void main(String[] argv) throws Exception {
        GSSManager mgr = GSSManager.getInstance();
        boolean result = true;
        GSSName name1 = mgr.createName(NAME_STR1,
            GSSName.NT_HOSTBASED_SERVICE, MECH);
        GSSName name2 = mgr.createName(NAME_STR2,
            GSSName.NT_HOSTBASED_SERVICE, MECH);
        GSSName name3 = mgr.createName(NAME_STR1,
            GSSName.NT_HOSTBASED_SERVICE, MECH);
        if (!name1.equals(name3) || !name1.equals(name3) ||
            !name1.equals((Object) name1) ||
            !name1.equals((Object) name3)) {
            System.out.println("Error: should be the same name");
            result = false;
        } else if (name1.hashCode() != name3.hashCode()) {
            System.out.println("Error: should have same hash");
            result = false;
        }
        if (name1.equals(name2) || name1.equals((Object) name2)) {
            System.out.println("Error: should be different names");
            result = false;
        }
        if (result) {
            System.out.println("Done");
        } else System.exit(1);
    }
}
