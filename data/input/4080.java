public class StartDateTest {
    public static void main(String[] args) throws Exception {
        Calendar cal = new GregorianCalendar();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        new File("jks").delete();
        run("-keystore jks -storetype jks -storepass changeit -keypass changeit -alias me " +
                "-genkeypair -dname CN=Haha -startdate +1y");
        cal.setTime(getIssueDate());
        System.out.println(cal);
        if (cal.get(Calendar.YEAR) != year + 1) {
            throw new Exception("Function check #1 fails");
        }
        run("-keystore jks -storetype jks -storepass changeit -keypass changeit -alias me " +
                "-selfcert -startdate +1m");
        cal.setTime(getIssueDate());
        System.out.println(cal);
        if (cal.get(Calendar.MONTH) != (month + 1) % 12) {
            throw new Exception("Function check #2 fails");
        }
        new File("jks").delete();
        Method m = KeyTool.class.getDeclaredMethod("getStartDate", String.class);
        m.setAccessible(true);
        for (String s: new String[] {
                null,       
                "+1m+1d",
                "+1y-1m+1d",
                "+3H",
                "+1M",
                "-5M",
                "+011d",
                "+22S",
                "+500S",
                "2001/01/01",
                "15:15:15",
                "2001/01/01 11:11:11",
                }) {
            try {
                System.out.println(s + " " + m.invoke(null, s));
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Failed at " + s);
            }
        }
        for (String s: new String[] {
                "",         
                "+3",
                "+3m+",
                "+3m+3",
                "1m",       
                "+0x011d",  
                "+1m1d",    
                "m",
                "+1h",      
                "-1m1d",
                "-m",
                "x",
                "+1m +1d",
                "2007/07",
                "01:01",
                "+01:01:01",                
                "1:01:01",
                "12pm",
                "2007/07/07  12:12:12",     
                "2001/01/01-11:11:11",
                "2007-07-07",               
                "2007/7/7",                 
                "07/07/07",                 
                "1:1:1",
                }) {
            boolean failed = false;
            try {
                System.out.println(m.invoke(null, s));
            } catch (Exception e) {
                System.out.println(s + " " + e.getCause());
                failed = true;
            }
            if (!failed) throw new Exception("Failed at " + s);
        }
    }
    static void run(String s) throws Exception {
        KeyTool.main((s+" -debug").split(" "));
    }
    static Date getIssueDate() throws Exception {
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(new FileInputStream("jks"), "changeit".toCharArray());
        X509Certificate cert = (X509Certificate)ks.getCertificate("me");
        return cert.getNotBefore();
    }
}
