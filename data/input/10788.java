public class EmailKeyword {
    public static void main(String[] arg) throws Exception {
        X500Name dN;
        dN = new X500Name("EMAIL=johndoe@example.com");
        System.out.println(dN.getName());
        dN = new X500Name("EMAILADDRESS=johndoe@example.com");
        System.out.println(dN.getName());
    }
}
