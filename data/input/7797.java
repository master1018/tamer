public class Bug4442855
{
public static void main(String[] argv){
        int result = 0;
        Bug4442855 testsuite = new Bug4442855();
        if( !testsuite.TestAD()) result ++;
        if( !testsuite.TestBC()) result ++;
        if( result > 0 ) throw new RuntimeException();
}
private boolean TestAD(){
        Locale zhTWloc = new Locale("zh", "TW");
        SimpleDateFormat sdf = new SimpleDateFormat("G", zhTWloc);
        return Test(sdf.format(new Date()), "\u897f\u5143", "AD");
}
private boolean TestBC(){
        Locale zhTWloc = new Locale("zh", "TW");
        SimpleDateFormat sdf = new SimpleDateFormat("G", zhTWloc);
        Calendar cdar = sdf.getCalendar();
        cdar.set(-2000, 1, 1);
        return Test(sdf.format(cdar.getTime()), "\u897f\u5143\u524d", "BC");
}
private boolean Test(String parent, String child, String info){
        boolean retval = true;
        if(!parent.equals(child)){
                System.out.println("Error translation for " + info + " in TCH: " + parent);
                System.out.println("Which should be: " + child );
                retval = false;
        }
        return retval;
}
}
