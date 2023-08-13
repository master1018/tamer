public class Bug4807540 {
    public static void main(String[] args) {
        Locale si = new Locale("sl", "si");
        String expected = "30.4.2008";
        DateFormat dfSi = DateFormat.getDateInstance (DateFormat.MEDIUM, si);
        String siString = new String (dfSi.format(new Date(108, Calendar.APRIL, 30)));
        if (expected.compareTo(siString) != 0) {
            throw new RuntimeException("Error: " + siString  + " should be " + expected);
        }
    }
}
