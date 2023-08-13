public class AliasActivityStub extends AliasActivity {
    public static boolean isOnCreateCalled = false;
    public static boolean isFinished = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOnCreateCalled = true;
    }
    @Override
    public void finish() {
        super.finish();
        isFinished = true;
    }
}
