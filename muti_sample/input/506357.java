public class PendingIntentStubActivity extends Activity {
    public static final int INVALIDATE = -1;
    public static final int ON_CREATE = 0;
    public static int status = INVALIDATE;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        status = ON_CREATE;
    }
}
