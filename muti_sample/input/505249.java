public class ChildActivity extends Activity {
    public static boolean isStarted = false;
    @Override
    protected void onStart() {
        super.onStart();
        isStarted = true;
    }
}
