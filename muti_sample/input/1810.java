public class Bug6190861 {
    static public void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        List localeList = new ArrayList();
        localeList.add(Locale.ENGLISH);
        localeList.add(Locale.KOREA);
        localeList.add(Locale.UK);
        localeList.add(new Locale("en", "CA"));
        localeList.add(Locale.ENGLISH);
        Iterator iter = localeList.iterator();
        while (iter.hasNext()){
            Locale currentLocale = (Locale) iter.next();
            System.out.println("\ncurrentLocale = "
                               + currentLocale.getDisplayName());
            ResourceBundle messages = ResourceBundle.getBundle("Bug6190861Data",currentLocale);
            Locale messagesLocale = messages.getLocale();
            System.out.println("messagesLocale = "
                               + messagesLocale.getDisplayName());
            checkMessages(messages);
        }
    }
    static void checkMessages(ResourceBundle messages) {
        String greetings = messages.getString("greetings");
        String inquiry = messages.getString("inquiry");
        String farewell = messages.getString("farewell");
        System.out.println(greetings);
        System.out.println(inquiry);
        System.out.println(farewell);
        if (!greetings.equals("Hiya.")) {
            throw new RuntimeException("got wrong resource bundle");
        }
    }
}
