public class Bug4938846 {
   public static void main(String[] args) {
       String zoneInfo = new String();
       Locale tzLocale = new Locale("en", "IE");
       TimeZone ieTz = TimeZone.getTimeZone("Europe/London");
        if (!ieTz.getDisplayName(true, TimeZone.LONG, tzLocale).equals ("Irish Summer Time"))
             throw new RuntimeException("\nString for IST, en_IE locale should be \"Irish Summer Time\"");
   }
}
