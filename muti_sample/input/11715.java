public class Bug4685470
{
   public static void main(String args[])
   {
        int result = 0;
        Bug4685470 testsuite = new Bug4685470();
        if(!testsuite.TestSCH()) result ++;
        if(!testsuite.TestTCH()) result ++;
        if(result > 0) throw new RuntimeException();
   }
   private boolean TestSCH()
   {
      Date now = new Date();
      DateFormat s = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL,Locale.SIMPLIFIED_CHINESE);
      return Test(s.format(now), getDayofWeek(now, Locale.SIMPLIFIED_CHINESE), "\"EEEE\" in " + Locale.SIMPLIFIED_CHINESE.toString());
   }
   private boolean TestTCH()
   {
      Date now = new Date();
      DateFormat s = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL,Locale.TRADITIONAL_CHINESE);
      return Test(s.format(now), getDayofWeek(now, Locale.TRADITIONAL_CHINESE), "\"EEEE\" in " + Locale.TRADITIONAL_CHINESE.toString());
   }
   private boolean Test(String parent, String child, String patterninfo)
   {
      boolean result = true;
      if( ! contains(parent, child)){
        System.out.println("Full date: " + parent);
        System.out.println("Which should contain the day of the week: " + child);
        System.out.println("DateFormat.FULL don't contain pattern for the day of the week : " + patterninfo);
        result = false;
      }
      return result;
   }
   private boolean contains(String parent, String child)
   {
        boolean result = false;
        if(parent.length() < child.length()) result = false;
        else {
                for ( int i = 0; i < parent.length() - child.length(); i++){
                        result = parent.regionMatches(i, child, 0, child.length());
                        if ( result == true) break;
                }
        }
        return result;
   }
   private String getDayofWeek(Date date, Locale loc){
        return (new SimpleDateFormat("EEEE", loc)).format(date);
   }
}
