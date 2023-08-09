public class Bug4518811 {
  public static void main(String [] args) {
      int totalErrors=0;
      totalErrors += getDays("ca", "ES");
      totalErrors += getDays("cs", "CZ");
      totalErrors += getDays("da", "DK");
      totalErrors += getDays("de", "AT");
      totalErrors += getDays("el", "GR");
      totalErrors += getDays("en", "GB");
      totalErrors += getDays("en", "IE");
      totalErrors += getDays("es", "ES");
      totalErrors += getDays("et", "EE");
      totalErrors += getDays("fi", "FI");
      totalErrors += getDays("fr", "BE");
      totalErrors += getDays("fr", "FR");
      totalErrors += getDays("is", "IS");
      totalErrors += getDays("lt", "LT");
      totalErrors += getDays("nl", "BE");
      totalErrors += getDays("pl", "PL");
      totalErrors += getDays("pt", "PT");
      if (totalErrors > 0)
          throw new RuntimeException();
  }
    static int getDays(String lang, String loc){
        int errors=0;
        Locale newlocale = new Locale(lang, loc);
        Calendar newCal = Calendar.getInstance(newlocale);
        int minDays = newCal.getMinimalDaysInFirstWeek();
        System.out.println("The Min Days in First Week for "+ lang +"_" + loc + " is " + minDays);
        if (minDays != 4){
            System.out.println("Warning! Should be 4, not " + minDays +"!");
            errors++;
        }
        return errors;
    }
}
