public class MessengerServiceActivities {
    public static class Binding extends Activity {
        Messenger mService = null;
        boolean mIsBound;
        TextView mCallbackText;
        class IncomingHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessengerService.MSG_SET_VALUE:
                        mCallbackText.setText("Received from service: " + msg.arg1);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
        final Messenger mMessenger = new Messenger(new IncomingHandler());
        private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                    IBinder service) {
                mService = new Messenger(service);
                mCallbackText.setText("Attached.");
                try {
                    Message msg = Message.obtain(null,
                            MessengerService.MSG_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                    msg = Message.obtain(null,
                            MessengerService.MSG_SET_VALUE, this.hashCode(), 0);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
                Toast.makeText(Binding.this, R.string.remote_service_connected,
                        Toast.LENGTH_SHORT).show();
            }
            public void onServiceDisconnected(ComponentName className) {
                mService = null;
                mCallbackText.setText("Disconnected.");
                Toast.makeText(Binding.this, R.string.remote_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };
        void doBindService() {
            bindService(new Intent(Binding.this, 
                    MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding.");
        }
        void doUnbindService() {
            if (mIsBound) {
                if (mService != null) {
                    try {
                        Message msg = Message.obtain(null,
                                MessengerService.MSG_UNREGISTER_CLIENT);
                        msg.replyTo = mMessenger;
                        mService.send(msg);
                    } catch (RemoteException e) {
                    }
                }
                unbindService(mConnection);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.messenger_service_binding);
            Button button = (Button)findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button)findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
            mCallbackText = (TextView)findViewById(R.id.callback);
            mCallbackText.setText("Not attached.");
        }
        private OnClickListener mBindListener = new OnClickListener() {
            public void onClick(View v) {
                doBindService();
            }
        };
        private OnClickListener mUnbindListener = new OnClickListener() {
            public void onClick(View v) {
                doUnbindService();
            }
        };
    }
}
