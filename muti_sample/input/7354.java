public class UserAndPasswordTest {
    static String[] specs = {
        "ftp:
        "ftp:
        "ftp:
        "ftp:
        "ftp:
    };
    public static void main(String[] args) throws Exception {
        for (int i=0; i<specs.length; i++) {
            URL ftp = new URL(specs[i]);
            if (!"springbank.eng".equals(ftp.getHost())) {
                throw new Exception("Parsing of URLs with : broken");
            }
        }
    }
}
