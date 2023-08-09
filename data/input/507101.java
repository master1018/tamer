public class RemoteService extends Service {
    final RemoteCallbackList<IRemoteServiceCallback> mCallbacks
            = new RemoteCallbackList<IRemoteServiceCallback>();
    int mValue = 0;
    NotificationManager mNM;
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        mHandler.sendEmptyMessage(REPORT_MSG);
    }
    @Override
    public void onDestroy() {
        mNM.cancel(R.string.remote_service_started);
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
        mCallbacks.kill();
        mHandler.removeMessages(REPORT_MSG);
    }
    @Override
    public IBinder onBind(Intent intent) {
        if (IRemoteService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        return null;
    }
    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }
        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };
    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        public int getPid() {
            return Process.myPid();
        }
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString) {
        }
    };
    private static final int REPORT_MSG = 1;
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_MSG: {
                    int value = ++mValue;
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).valueChanged(value);
                        } catch (RemoteException e) {
                        }
                    }
                    mCallbacks.finishBroadcast();
                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1*1000);
                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private void showNotification() {
        CharSequence text = getText(R.string.remote_service_started);
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);
        notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
                       text, contentIntent);
        mNM.notify(R.string.remote_service_started, notification);
    }
    public static class Controller extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.remote_service_controller);
            Button button = (Button)findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = (Button)findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }
        private OnClickListener mStartListener = new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(
                        "com.example.android.apis.app.REMOTE_SERVICE"));
            }
        };
        private OnClickListener mStopListener = new OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(
                        "com.example.android.apis.app.REMOTE_SERVICE"));
            }
        };
    }
    public static class Binding extends Activity {
        IRemoteService mService = null;
        ISecondary mSecondaryService = null;
        Button mKillButton;
        TextView mCallbackText;
        private boolean mIsBound;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.remote_service_binding);
            Button button = (Button)findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button)findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
            mKillButton = (Button)findViewById(R.id.kill);
            mKillButton.setOnClickListener(mKillListener);
            mKillButton.setEnabled(false);
            mCallbackText = (TextView)findViewById(R.id.callback);
            mCallbackText.setText("Not attached.");
        }
        private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                    IBinder service) {
                mService = IRemoteService.Stub.asInterface(service);
                mKillButton.setEnabled(true);
                mCallbackText.setText("Attached.");
                try {
                    mService.registerCallback(mCallback);
                } catch (RemoteException e) {
                }
                Toast.makeText(Binding.this, R.string.remote_service_connected,
                        Toast.LENGTH_SHORT).show();
            }
            public void onServiceDisconnected(ComponentName className) {
                mService = null;
                mKillButton.setEnabled(false);
                mCallbackText.setText("Disconnected.");
                Toast.makeText(Binding.this, R.string.remote_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };
        private ServiceConnection mSecondaryConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                    IBinder service) {
                mSecondaryService = ISecondary.Stub.asInterface(service);
                mKillButton.setEnabled(true);
            }
            public void onServiceDisconnected(ComponentName className) {
                mSecondaryService = null;
                mKillButton.setEnabled(false);
            }
        };
        private OnClickListener mBindListener = new OnClickListener() {
            public void onClick(View v) {
                bindService(new Intent(IRemoteService.class.getName()),
                        mConnection, Context.BIND_AUTO_CREATE);
                bindService(new Intent(ISecondary.class.getName()),
                        mSecondaryConnection, Context.BIND_AUTO_CREATE);
                mIsBound = true;
                mCallbackText.setText("Binding.");
            }
        };
        private OnClickListener mUnbindListener = new OnClickListener() {
            public void onClick(View v) {
                if (mIsBound) {
                    if (mService != null) {
                        try {
                            mService.unregisterCallback(mCallback);
                        } catch (RemoteException e) {
                        }
                    }
                    unbindService(mConnection);
                    unbindService(mSecondaryConnection);
                    mKillButton.setEnabled(false);
                    mIsBound = false;
                    mCallbackText.setText("Unbinding.");
                }
            }
        };
        private OnClickListener mKillListener = new OnClickListener() {
            public void onClick(View v) {
                if (mSecondaryService != null) {
                    try {
                        int pid = mSecondaryService.getPid();
                        Process.killProcess(pid);
                        mCallbackText.setText("Killed service process.");
                    } catch (RemoteException ex) {
                        Toast.makeText(Binding.this,
                                R.string.remote_call_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
            public void valueChanged(int value) {
                mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
            }
        };
        private static final int BUMP_MSG = 1;
        private Handler mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BUMP_MSG:
                        mCallbackText.setText("Received from service: " + msg.arg1);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }
}
