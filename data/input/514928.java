public class JNIExample extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        int sum = Native.add(2, 3);
        tv.setText("Native Code test: 2 + 3 = " + Integer.toString(sum));
        setContentView(tv);
    }
}
class Native {
    static {
        System.loadLibrary("native");
    }
    static native int add(int a, int b);
}
