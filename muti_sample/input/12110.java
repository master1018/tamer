public class Bug4810032
{
        public static void main(String[] arg)
        {
                String s = "2003\u5e749\u670826\u65e5"; 
                DateFormat df =
                   DateFormat.getDateInstance(DateFormat.FULL,Locale.JAPANESE);
                try {
                        if ( !s.equals(df.format(df.parse(s))) )
                           throw new RuntimeException();
                } catch (ParseException e) {
                        throw new RuntimeException();
                }
        }
}
