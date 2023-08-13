public class Bug4848242 {
    public static void main(String[] args) {
        getTzInfo("de", "DE");
        getTzInfo("es", "ES");
        getTzInfo("fr", "FR");
        getTzInfo("it", "IT");
        getTzInfo("sv", "SV");
    }
    static void getTzInfo(String langName, String locName)
    {
        Locale tzLocale = new Locale(langName, locName);
        TimeZone euroTz = TimeZone.getTimeZone("MET");
        System.out.println("Locale is " + langName + "_" + locName);
        if ( euroTz.getID().equalsIgnoreCase("GMT") ) {
            throw new RuntimeException("Error: no time zone found");
        }
        System.out.println(euroTz.getDisplayName(false, TimeZone.SHORT, tzLocale));
        if(!euroTz.getDisplayName(false, TimeZone.SHORT, tzLocale).equals("MET"))
          throw new RuntimeException("Timezone name is incorrect (should be MET)\n");
        System.out.println(euroTz.getDisplayName(false, TimeZone.LONG, tzLocale));
        System.out.println(euroTz.getDisplayName(true, TimeZone.SHORT, tzLocale));
        if(!euroTz.getDisplayName(true, TimeZone.SHORT, tzLocale).equals("MEST"))
            throw new RuntimeException("Summer timezone name is incorrect (should be MEST)\n");
        System.out.println(euroTz.getDisplayName(true, TimeZone.LONG, tzLocale) + "\n");
    }
}
