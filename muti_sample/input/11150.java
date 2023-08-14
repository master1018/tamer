public class Bug4762201
{
        public static void main(String[] arg)
        {
                int result = 0;
                Locale loc = new Locale("zh","CN");
                Date now = new Date();
                DateFormat df =
                   DateFormat.getTimeInstance(DateFormat.SHORT,loc);
                SimpleDateFormat sdf = new SimpleDateFormat("",loc);
                sdf.applyPattern("ah:mm");                              
                if( !sdf.format(now).equals(df.format(now))) result++;
                df =  DateFormat.getTimeInstance(DateFormat.MEDIUM,loc);
                sdf.applyPattern("H:mm:ss");                            
                if( !sdf.format(now).equals(df.format(now))) result++;
                df = DateFormat.getTimeInstance(DateFormat.LONG,loc);
                sdf.applyPattern("ahh'\u65f6'mm'\u5206'ss'\u79d2'");    
                if( !sdf.format(now).equals(df.format(now))) result++;
                df = DateFormat.getTimeInstance(DateFormat.FULL,loc);
                sdf.applyPattern("ahh'\u65f6'mm'\u5206'ss'\u79d2' z");  
                if( !sdf.format(now).equals(df.format(now))) result++;
           if(result > 0) throw new RuntimeException();
        }
}
