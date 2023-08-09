public class Bug6645263 {
    public static void main(String[] args) {
        Calendar cal = new GregorianCalendar(Locale.US);
        cal.setLenient(false);
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.WEEK_OF_MONTH, 4);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.getTime();
   }
}
