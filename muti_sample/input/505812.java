public class BatteryInfo extends Activity {
    private TextView mStatus;
    private TextView mPower;
    private TextView mLevel;
    private TextView mScale;
    private TextView mHealth;
    private TextView mVoltage;
    private TextView mTemperature;
    private TextView mTechnology;
    private TextView mUptime;
    private IBatteryStats mBatteryStats;
    private IPowerManager mScreenStats;
    private static final int EVENT_TICK = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_TICK:
                    updateBatteryStats();
                    sendEmptyMessageDelayed(EVENT_TICK, 1000);
                    break;
            }
        }
    };
    private final String tenthsToFixedString(int x) {
        int tens = x / 10;
        return Integer.toString(tens) + "." + (x - 10 * tens);
    }
    private IntentFilter   mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int plugType = intent.getIntExtra("plugged", 0);
                mLevel.setText("" + intent.getIntExtra("level", 0));
                mScale.setText("" + intent.getIntExtra("scale", 0));
                mVoltage.setText("" + intent.getIntExtra("voltage", 0) + " "
                        + getString(R.string.battery_info_voltage_units));
                mTemperature.setText("" + tenthsToFixedString(intent.getIntExtra("temperature", 0))
                        + getString(R.string.battery_info_temperature_units));
                mTechnology.setText("" + intent.getStringExtra("technology"));
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                String statusString;
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    statusString = getString(R.string.battery_info_status_charging);
                    if (plugType > 0) {
                        statusString = statusString + " " + getString(
                                (plugType == BatteryManager.BATTERY_PLUGGED_AC)
                                        ? R.string.battery_info_status_charging_ac
                                        : R.string.battery_info_status_charging_usb);
                    }
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    statusString = getString(R.string.battery_info_status_discharging);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    statusString = getString(R.string.battery_info_status_not_charging);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    statusString = getString(R.string.battery_info_status_full);
                } else {
                    statusString = getString(R.string.battery_info_status_unknown);
                }
                mStatus.setText(statusString);
                switch (plugType) {
                    case 0:
                        mPower.setText(getString(R.string.battery_info_power_unplugged));
                        break;
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        mPower.setText(getString(R.string.battery_info_power_ac));
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mPower.setText(getString(R.string.battery_info_power_usb));
                        break;
                    case (BatteryManager.BATTERY_PLUGGED_AC|BatteryManager.BATTERY_PLUGGED_USB):
                        mPower.setText(getString(R.string.battery_info_power_ac_usb));
                        break;
                    default:
                        mPower.setText(getString(R.string.battery_info_power_unknown));
                        break;
                }
                int health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                String healthString;
                if (health == BatteryManager.BATTERY_HEALTH_GOOD) {
                    healthString = getString(R.string.battery_info_health_good);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                    healthString = getString(R.string.battery_info_health_overheat);
                } else if (health == BatteryManager.BATTERY_HEALTH_DEAD) {
                    healthString = getString(R.string.battery_info_health_dead);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) {
                    healthString = getString(R.string.battery_info_health_over_voltage);
                } else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) {
                    healthString = getString(R.string.battery_info_health_unspecified_failure);
                } else {
                    healthString = getString(R.string.battery_info_health_unknown);
                }
                mHealth.setText(healthString);
            }
        }
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.battery_info);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    }
    @Override
    public void onResume() {
        super.onResume();
        mStatus = (TextView)findViewById(R.id.status);
        mPower = (TextView)findViewById(R.id.power);
        mLevel = (TextView)findViewById(R.id.level);
        mScale = (TextView)findViewById(R.id.scale);
        mHealth = (TextView)findViewById(R.id.health);
        mTechnology = (TextView)findViewById(R.id.technology);
        mVoltage = (TextView)findViewById(R.id.voltage);
        mTemperature = (TextView)findViewById(R.id.temperature);
        mUptime = (TextView) findViewById(R.id.uptime);
        mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        mScreenStats = IPowerManager.Stub.asInterface(ServiceManager.getService(POWER_SERVICE));
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
        registerReceiver(mIntentReceiver, mIntentFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
        unregisterReceiver(mIntentReceiver);
    }
    private void updateBatteryStats() {
        long uptime = SystemClock.elapsedRealtime();
        mUptime.setText(DateUtils.formatElapsedTime(uptime / 1000));
    }
}
