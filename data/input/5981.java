public class Bug4651568 {
    public static void main (String argv[] )  {
        String expectedCurrencyPattern = "\u00A4 #.##0,00";
        Locale locale = new Locale ("pt", "BR");
        Locale.setDefault(locale);
        DecimalFormat formatter =
            (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
        if (formatter.toLocalizedPattern().equals(expectedCurrencyPattern))
{
            System.out.println ("Passed.");
        } else {
             System.out.println ("Failed Currency pattern." +
                    "  Expected:  " + expectedCurrencyPattern +
                    "  Received:  " + formatter.toLocalizedPattern() );
             throw new RuntimeException();
 }
    }
}
