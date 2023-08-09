public class HelloJni extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView  tv = new TextView(this);
        tv.setText( stringFromJNI() );
        setContentView(tv);
    }
    public native String  stringFromJNI();
    public native String  unimplementedStringFromJNI();
    static {
        System.loadLibrary("hello-jni");
    }
}
