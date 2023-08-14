public class Bug4965260  {
  static Locale[] locales2Test = new Locale[] {
    new Locale("de"),
    new Locale("es"),
    new Locale("fr"),
    new Locale("it"),
    new Locale("sv")
  };
  static String[] expectedNames = new String[] {
    "Niederl\u00e4ndisch",
    "neerland\u00e9s",
    "n\u00e9erlandais",
    "neerlandese",
    "nederl\u00e4ndska"
  };
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    if (locales2Test.length != expectedNames.length) {
      throw new Exception("\nData sizes does not match!\n");
    }
    StringBuffer message = new StringBuffer("");
    Locale dutch = new Locale("nl", "BE");
    String current;
    for (int i = 0; i < locales2Test.length; i++) {
      Locale locale = locales2Test[i];
      current = dutch.getDisplayLanguage(locale);
      if (!current.equals(expectedNames[i])) {
        message.append("[");
        message.append(locale.getDisplayLanguage());
        message.append("] ");
        message.append("Language name is ");
        message.append(current);
        message.append(" should be ");
        message.append(expectedNames[i]);
        message.append("\n");
      }
    }
    if (message.length() >0) {
      throw new Exception("\n" + message.toString());
    }
  }
}
