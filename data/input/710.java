public class Bug4395196
{
        public static void main(String[] arg)
        {
                int result = 0;
                Locale loc = new Locale("ko","KR");
                Date now = new Date(108, Calendar.APRIL, 9);
                DateFormat df =
                   DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT,loc);
                SimpleDateFormat sdf = new SimpleDateFormat("",loc);
                sdf.applyPattern("yyyy. M. d a h:mm");
                if( !sdf.format(now).equals(df.format(now))){
                 result++;
                 System.out.println("error at " + sdf.format(now));
                 }
                df =  DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM,loc);
                sdf.applyPattern("yyyy'\ub144' M'\uc6d4' d'\uc77c' '('EE')' a h:mm:ss");
                if( !sdf.format(now).equals(df.format(now))){
                 result++;
                 System.out.println("error at " + sdf.format(now));
                 }
                df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG,loc);
                sdf.applyPattern("yyyy. M. d a h'\uc2dc' mm'\ubd84' ss'\ucd08'");
                if( !sdf.format(now).equals(df.format(now))){
                 result++;
                 System.out.println("error at " + sdf.format(now));
                 }
                df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL,loc);
                sdf.applyPattern("yy. M. d a h'\uc2dc' mm'\ubd84' ss'\ucd08' z");
                if( !sdf.format(now).equals(df.format(now))){
                 result++;
                 System.out.println("error at " + sdf.format(now));
                 }
           if(result > 0) throw new RuntimeException();
}}
