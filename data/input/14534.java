public class WebGet {
    static String user = System.getProperty("user");
    static String pass = System.getProperty("pass");
    static String kuser = System.getProperty("kuser");
    static String kpass = System.getProperty("kpass");
    static String showhint = System.getProperty("showhint");
    static class MyAuthenticator extends Authenticator {
        public MyAuthenticator () {
            super ();
        }
        public PasswordAuthentication getPasswordAuthentication ()
        {
            if(getRequestingScheme().equalsIgnoreCase("negotiate") ||
                    getRequestingScheme().equalsIgnoreCase("kerberos")) {
                if(showhint != null)
                    System.out.println("::::: PROVIDING Kerberos PASSWORD AND USERNAME " + kuser +":"+kpass+" :::::");
                return (new PasswordAuthentication (kuser, kpass.toCharArray()));
            } else {
                if(showhint != null)
                    System.out.println("::::: PROVIDING PASSWORD AND USERNAME " + user +":"+pass+" :::::");
                return (new PasswordAuthentication (user, pass.toCharArray()));
            }
        }
    }
    static void url(String urls) throws Exception {
        Authenticator.setDefault (new MyAuthenticator ());
        URL url = new URL(urls);
        InputStream ins = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        String str;
        while((str = reader.readLine()) != null)
            System.out.println(str);
    }
    public static void main(String[] args) throws Exception {
        url(args[0]);
    }
}
