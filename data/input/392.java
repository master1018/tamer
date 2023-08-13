public class Bug5096553
{
  public static void main(String[] args) {
      String expectedMed = "30-04-2008";
      String expectedShort="30-04-08";
      Locale dk = new Locale("da", "DK");
      DateFormat df1 = DateFormat.getDateInstance(DateFormat.MEDIUM, dk);
      DateFormat df2 = DateFormat.getDateInstance(DateFormat.SHORT, dk);
      String medString = new String (df1.format(new Date(108, Calendar.APRIL, 30)));
      String shortString = new String (df2.format(new Date(108, Calendar.APRIL, 30)));
      System.out.println(df1.format(new Date()));
      System.out.println(df2.format(new Date()));
      if (expectedMed.compareTo(medString) != 0) {
            throw new RuntimeException("Error: " + medString  + " should be " + expectedMed);
        }
      if (expectedShort.compareTo(shortString) != 0) {
            throw new RuntimeException("Error: " + shortString  + " should be " + expectedShort);
        }
  }
}
