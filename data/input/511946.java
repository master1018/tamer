class DockObserver extends UEventObserver {
    private static final String TAG = DockObserver.class.getSimpleName();
    private static final boolean LOG = false;
    private static final String DOCK_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/dock";
    private static final String DOCK_STATE_PATH = "/sys/class/switch/dock/state";
    private static final int MSG_DOCK_STATE = 0;
    private int mDockState = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    private int mPreviousDockState = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    private boolean mSystemReady;
    private final Context mContext;
    private PowerManagerService mPowerManager;
    public DockObserver(Context context, PowerManagerService pm) {
        mContext = context;
        mPowerManager = pm;
        init();  
        startObserving(DOCK_UEVENT_MATCH);
    }
    @Override
    public void onUEvent(UEventObserver.UEvent event) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Slog.v(TAG, "Dock UEVENT: " + event.toString());
        }
        synchronized (this) {
            try {
                int newState = Integer.parseInt(event.get("SWITCH_STATE"));
                if (newState != mDockState) {
                    mPreviousDockState = mDockState;
                    mDockState = newState;
                    if (mSystemReady) {
                        if (mPreviousDockState != Intent.EXTRA_DOCK_STATE_DESK ||
                                mDockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                            mPowerManager.userActivityWithForce(SystemClock.uptimeMillis(),
                                    false, true);
                        }
                        update();
                    }
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "Could not parse switch state from event " + event);
            }
        }
    }
    private final void init() {
        char[] buffer = new char[1024];
        try {
            FileReader file = new FileReader(DOCK_STATE_PATH);
            int len = file.read(buffer, 0, 1024);
            mPreviousDockState = mDockState = Integer.valueOf((new String(buffer, 0, len)).trim());
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "This kernel does not have dock station support");
        } catch (Exception e) {
            Slog.e(TAG, "" , e);
        }
    }
    void systemReady() {
        synchronized (this) {
            if (mDockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                update();
            }
            mSystemReady = true;
        }
    }
    private final void update() {
        mHandler.sendEmptyMessage(MSG_DOCK_STATE);
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOCK_STATE:
                    synchronized (this) {
                        Slog.i(TAG, "Dock state changed: " + mDockState);
                        final ContentResolver cr = mContext.getContentResolver();
                        if (Settings.Secure.getInt(cr,
                                Settings.Secure.DEVICE_PROVISIONED, 0) == 0) {
                            Slog.i(TAG, "Device not provisioned, skipping dock broadcast");
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_DOCK_EVENT);
                        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                        intent.putExtra(Intent.EXTRA_DOCK_STATE, mDockState);
                        String address = BluetoothService.readDockBluetoothAddress();
                        if (address != null)
                            intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
                                    BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
                        if (Settings.System.getInt(cr,
                                Settings.System.DOCK_SOUNDS_ENABLED, 1) == 1)
                        {
                            String whichSound = null;
                            if (mDockState == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                                if (mPreviousDockState == Intent.EXTRA_DOCK_STATE_DESK) {
                                    whichSound = Settings.System.DESK_UNDOCK_SOUND;
                                } else if (mPreviousDockState == Intent.EXTRA_DOCK_STATE_CAR) {
                                    whichSound = Settings.System.CAR_UNDOCK_SOUND;
                                }
                            } else {
                                if (mDockState == Intent.EXTRA_DOCK_STATE_DESK) {
                                    whichSound = Settings.System.DESK_DOCK_SOUND;
                                } else if (mDockState == Intent.EXTRA_DOCK_STATE_CAR) {
                                    whichSound = Settings.System.CAR_DOCK_SOUND;
                                }
                            }
                            if (whichSound != null) {
                                final String soundPath = Settings.System.getString(cr, whichSound);
                                if (soundPath != null) {
                                    final Uri soundUri = Uri.parse("file:
                                    if (soundUri != null) {
                                        final Ringtone sfx = RingtoneManager.getRingtone(mContext, soundUri);
                                        if (sfx != null) {
                                            sfx.setStreamType(AudioManager.STREAM_SYSTEM);
                                            sfx.play();
                                        }
                                    }
                                }
                            }
                        }
                        mContext.sendStickyBroadcast(intent);
                    }
                    break;
            }
        }
    };
}
