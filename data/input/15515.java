public class Bug6645292 {
    public static void main(String[] args) {
        Locale loc = Locale.getDefault();
        TimeZone zone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(1986, JUNE, 1);
            Date d1 = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzz");
            String s = df.format(d1);
            Date d2 = null;
            try {
                d2 = df.parse(s);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!d1.equals(d2)) {
                throw new RuntimeException("d1 (" + d1 + ") != d2 (" + d2 + ")");
            }
        } finally {
            Locale.setDefault(loc);
            TimeZone.setDefault(zone);
        }
    }
}
