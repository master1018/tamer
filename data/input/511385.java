public class CountDownTimerTestStub extends Activity {
    public boolean onFinished;
    public ArrayList<Long> tickTimes;
    public final long MILLISINFUTURE = 4500;
    public final long INTERVAL = 1000;
    public CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimes = new ArrayList<Long>();
        countDownTimer = new CountDownTimer(MILLISINFUTURE, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                tickTimes.add(System.currentTimeMillis());
            }
            @Override
            public void onFinish() {
                onFinished = true;
            }
        };
    }
}
