public class ToastTest extends TestActivity
{
    private final static String TAG = "ToastTest";
    Handler mHandler = new Handler();
    Toast mToast1;
    Toast mToast2;
    @Override
    protected String tag() {
        return TAG;
    }
    @Override
    protected Test[] tests() {
        return mTests;
    }
    private static String readFile(String fn) {
        FileReader f;
        int len;
        f = null;
        try {
            f = new FileReader(fn);
            String s = "";
            char[] cbuf = new char[200];
            while ((len = f.read(cbuf, 0, cbuf.length)) >= 0) {
                s += String.valueOf(cbuf, 0, len);
            }
            return s;
        } catch (IOException ex) {
            return "ERROR";
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ex) {
                    return "ERROR!";
                }
            }
        }
    }
    private Test[] mTests = new Test[] {
        new Test("Read lights") {
            public void run()
            {
                String text = "freq=" + readFile("/sys/class/leds/red/device/grpfreq")
                        + "\npwm=" + readFile("/sys/class/leds/red/device/grppwm");
                mToast1 = Toast.makeText(ToastTest.this, text, Toast.LENGTH_SHORT);
                mToast1.show();
            }
        },
        new Test("Make Toast #1") {
            public void run()
            {
                mToast1 = Toast.makeText(ToastTest.this, "hi 1", Toast.LENGTH_SHORT);
            }
        },
        new Test("Show Toast #1") {
            public void run()
            {
                mToast1.show();
            }
        },
        new Test("Update Toast #1") {
            public void run()
            {
                TextView view = new TextView(ToastTest.this);
                view.setText("replaced!");
                mToast1.setView(view);
                mToast1.show();
            }
        },
        new Test("Make Toast #2") {
            public void run()
            {
                mToast2 = Toast.makeText(ToastTest.this, "hi 2", Toast.LENGTH_SHORT);
            }
        },
        new Test("Show Toast #2") {
            public void run()
            {
                mToast2.show();
            }
        },
        new Test("Gravity Toast LEFT") {
            public void run()
            {
                Toast toast = Toast.makeText(ToastTest.this, "LEFT", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.LEFT, 0, 0);
                toast.show();
            }
        },
        new Test("Gravity Toast FILL_HORIZONTAL") {
            public void run()
            {
                Toast toast = Toast.makeText(ToastTest.this, "FILL_HORIZONTAL",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
                toast.show();
            }
        },
    };
}
