public class Bug6912560 {
    public static void main(String[] args) {
        String tzname = "Asia/Tokyo";
        System.setProperty("user.timezone", tzname);
        System.setSecurityManager(new SecurityManager());
        TimeZone tz = TimeZone.getDefault();
        if (!tzname.equals(tz.getID())) {
            throw new RuntimeException("got " + tz.getID()
                                       + ", expected " + tzname);
        }
    }
}
