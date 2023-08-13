public class DefaultLocaleTest {
    public static void main(String[] args) {
        String setting =
            "language:"   + System.getProperty("user.language") + "_" +
            "country:"    + System.getProperty("user.country")  + "_" +
            "encoding:"   + System.getProperty("file.encoding") + "_" +
            "jnuEncoding:"+ System.getProperty("sun.jnu.encoding");
        if (args.length != 0) {
            if (!setting.equals(args[0])) {
                System.exit(1);
            }
        } else {
            System.out.println(setting);
        }
        System.exit(0);
   }
}
