public class RemoteSubActivityScreen extends SubActivityScreen {
	Handler mHandler = new Handler();
	boolean mFirst = false;
    public RemoteSubActivityScreen() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        Intent intent = getIntent();
    	intent.setClass(this, SubActivityScreen.class);
        super.onCreate(icicle);
        boolean kill = intent.getBooleanExtra("kill", false);
        if (kill) {
	        if (icicle == null) {
		        mHandler.post(new Runnable() {
		        	public void run() {
		        		handleBeforeStopping();
		        		Process.killProcess(Process.myPid());
		        	}
		        });
	        }
        }
    }
}
