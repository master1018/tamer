public class TwoLibs extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView  tv = new TextView(this);
        int       x  = 1000;
        int       y  = 42;
        System.loadLibrary("twolib-second");
        int  z = add(x, y);
        tv.setText( "The sum of " + x + " and " + y + " is " + z );
        setContentView(tv);
    }
    public native int add(int  x, int  y);
}
