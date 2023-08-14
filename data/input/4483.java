public class RDNwithMultipleAVAs {
    public static void main(String[] args) throws Exception {
        X500Name name = null;
        try {
            name = new X500Name("cn=john doe +   , c=us");
            throw new Exception("Expected IOException not thrown");
        } catch (IOException ioe) {
        }
        try {
            name = new X500Name("cn=john doe + l=ca\\++liformia, c=us");
            throw new Exception("Expected IOException not thrown");
        } catch (IOException ioe) {
        }
        name = new X500Name("cn=john \\+doe   , c=us");
        System.out.println(name);
        name = new X500Name("cn=john doe + l=ca\\+lifornia + l =sf, c=us");
        System.out.println(name);
        name = new X500Name("cn=john \\+doe + l=mpk, c=us");
        String loc = name.getLocality();
        if (loc == null || !loc.endsWith("mpk"))
            throw new Exception("AVA has been ignored");
    }
}
