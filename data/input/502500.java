public class SslLoad extends Activity implements OnClickListener, Runnable {
    private static final String TAG = SslLoad.class.getSimpleName();
    private Button button;
    private boolean running = false;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Thread requestThread = new Thread(this);
        requestThread.setDaemon(true);
        requestThread.start();
        button = new Button(this);
        button.setText("GO");
        button.setOnClickListener(this);
        setContentView(button);
    }
    @Override
    protected void onStop() {
        super.onStop();
        synchronized (this) {
            running = false;
        }
    }
    public void onClick(View v) {
        synchronized (this) {
            running = !running;
            button.setText(running ? "STOP" : "GO");
            if (running) {
                this.notifyAll();
            }
        }
    }
    public void run() {
        boolean error = false;
        while (true) {
            synchronized (this) {
                while (!running) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {  }
                }
            }
            AndroidHttpClient client = AndroidHttpClient.newInstance(
                    "Mozilla/5.001 (windows; U; NT4.0; en-us) Gecko/25250101");
            try {
                String url = error ? "https:
                        : "https:
                client.execute(new HttpGet(url),
                        new ResponseHandler<Void>() {
                            public Void handleResponse(HttpResponse response) {
                                return null;
                            }
                        });
                Log.i(TAG, "Request succeeded.");
            } catch (IOException e) {
                Log.w(TAG, "Request failed.", e);
            }
            client.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {  }
            error = !error;
        }
    }
}
