public class DeskClock extends Activity {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "DeskClock";
    private static final String ACTION_MIDNIGHT = "com.android.deskclock.MIDNIGHT";
    private final long QUERY_WEATHER_DELAY = 60 * 60 * 1000; 
    private static final String DOCK_SETTINGS_ACTION = "com.android.settings.DOCK_SETTINGS";
    private final long SCREEN_SAVER_TIMEOUT = 5 * 60 * 1000; 
    private final long SCREEN_SAVER_MOVE_DELAY = 60 * 1000; 
    private final int SCREEN_SAVER_COLOR = 0xFF308030;
    private final int SCREEN_SAVER_COLOR_DIM = 0xFF183018;
    private final float DIM_BEHIND_AMOUNT_NORMAL = 0.4f;
    private final float DIM_BEHIND_AMOUNT_DIMMED = 0.8f; 
    private final int QUERY_WEATHER_DATA_MSG     = 0x1000;
    private final int UPDATE_WEATHER_DISPLAY_MSG = 0x1001;
    private final int SCREEN_SAVER_TIMEOUT_MSG   = 0x2000;
    private final int SCREEN_SAVER_MOVE_MSG      = 0x2001;
    private static final String GENIE_PACKAGE_ID = "com.google.android.apps.genie.geniewidget";
    private static final String WEATHER_CONTENT_AUTHORITY = GENIE_PACKAGE_ID + ".weather";
    private static final String WEATHER_CONTENT_PATH = "/weather/current";
    private static final String[] WEATHER_CONTENT_COLUMNS = new String[] {
            "location",
            "timestamp",
            "temperature",
            "highTemperature",
            "lowTemperature",
            "iconUrl",
            "iconResId",
            "description",
        };
    private static final String ACTION_GENIE_REFRESH = "com.google.android.apps.genie.REFRESH";
    private DigitalClock mTime;
    private TextView mDate;
    private TextView mNextAlarm = null;
    private TextView mBatteryDisplay;
    private TextView mWeatherCurrentTemperature;
    private TextView mWeatherHighTemperature;
    private TextView mWeatherLowTemperature;
    private TextView mWeatherLocation;
    private ImageView mWeatherIcon;
    private String mWeatherCurrentTemperatureString;
    private String mWeatherHighTemperatureString;
    private String mWeatherLowTemperatureString;
    private String mWeatherLocationString;
    private Drawable mWeatherIconDrawable;
    private Resources mGenieResources = null;
    private boolean mDimmed = false;
    private boolean mScreenSaverMode = false;
    private String mDateFormat;
    private int mBatteryLevel = -1;
    private boolean mPluggedIn = false;
    private boolean mLaunchedFromDock = false;
    private Random mRNG;
    private PendingIntent mMidnightIntent;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(LOG_TAG, "mIntentReceiver.onReceive: action=" + action + ", intent=" + intent);
            if (Intent.ACTION_DATE_CHANGED.equals(action) || ACTION_MIDNIGHT.equals(action)) {
                refreshDate();
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                handleBatteryUpdate(
                    intent.getIntExtra("status", BATTERY_STATUS_UNKNOWN),
                    intent.getIntExtra("level", 0));
            } else if (UiModeManager.ACTION_EXIT_DESK_MODE.equals(action)) {
                if (mLaunchedFromDock) {
                    finish();
                }
                mLaunchedFromDock = false;
            }
        }
    };
    private final Handler mHandy = new Handler() {
        @Override
        public void handleMessage(Message m) {
            if (m.what == QUERY_WEATHER_DATA_MSG) {
                new Thread() { public void run() { queryWeatherData(); } }.start();
                scheduleWeatherQueryDelayed(QUERY_WEATHER_DELAY);
            } else if (m.what == UPDATE_WEATHER_DISPLAY_MSG) {
                updateWeatherDisplay();
            } else if (m.what == SCREEN_SAVER_TIMEOUT_MSG) {
                saveScreen();
            } else if (m.what == SCREEN_SAVER_MOVE_MSG) {
                moveScreenSaver();
            }
        }
    };
    private final ContentObserver mContentObserver = new ContentObserver(mHandy) {
        @Override
        public void onChange(boolean selfChange) {
            if (DEBUG) Log.d(LOG_TAG, "content observer notified that weather changed");
            refreshWeather();
        }
    };
    private void moveScreenSaver() {
        moveScreenSaverTo(-1,-1);
    }
    private void moveScreenSaverTo(int x, int y) {
        if (!mScreenSaverMode) return;
        final View saver_view = findViewById(R.id.saver_view);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (x < 0 || y < 0) {
            int myWidth = saver_view.getMeasuredWidth();
            int myHeight = saver_view.getMeasuredHeight();
            x = (int)(mRNG.nextFloat()*(metrics.widthPixels - myWidth));
            y = (int)(mRNG.nextFloat()*(metrics.heightPixels - myHeight));
        }
        if (DEBUG) Log.d(LOG_TAG, String.format("screen saver: %d: jumping to (%d,%d)",
                System.currentTimeMillis(), x, y));
        saver_view.setLayoutParams(new AbsoluteLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            x,
            y));
        mHandy.sendEmptyMessageDelayed(SCREEN_SAVER_MOVE_MSG,
            SCREEN_SAVER_MOVE_DELAY +
            (1000 - (System.currentTimeMillis() % 1000)));
    }
    private void setWakeLock(boolean hold) {
        if (DEBUG) Log.d(LOG_TAG, (hold ? "hold" : " releas") + "ing wake lock");
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (hold)
            winParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        else
            winParams.flags &= (~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.setAttributes(winParams);
    }
    private void scheduleScreenSaver() {
        mHandy.removeMessages(SCREEN_SAVER_TIMEOUT_MSG);
        mHandy.sendMessageDelayed(
            Message.obtain(mHandy, SCREEN_SAVER_TIMEOUT_MSG),
            SCREEN_SAVER_TIMEOUT);
    }
    private void restoreScreen() {
        if (!mScreenSaverMode) return;
        if (DEBUG) Log.d(LOG_TAG, "restoreScreen");
        mScreenSaverMode = false;
        initViews();
        doDim(false); 
        if (mPluggedIn) requestWeatherDataFetch();
        scheduleScreenSaver();
        refreshAll();
    }
    private void saveScreen() {
        if (mScreenSaverMode) return;
        if (DEBUG) Log.d(LOG_TAG, "saveScreen");
        final View oldTimeDate = findViewById(R.id.time_date);
        int oldLoc[] = new int[2];
        oldTimeDate.getLocationOnScreen(oldLoc);
        mScreenSaverMode = true;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);
        final View focused = getCurrentFocus();
        if (focused != null) focused.clearFocus();
        setContentView(R.layout.desk_clock_saver);
        mTime = (DigitalClock) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mNextAlarm = (TextView) findViewById(R.id.nextAlarm);
        final int color = mDimmed ? SCREEN_SAVER_COLOR_DIM : SCREEN_SAVER_COLOR;
        ((TextView)findViewById(R.id.timeDisplay)).setTextColor(color);
        ((TextView)findViewById(R.id.am_pm)).setTextColor(color);
        mDate.setTextColor(color);
        mNextAlarm.setTextColor(color);
        mNextAlarm.setCompoundDrawablesWithIntrinsicBounds(
            getResources().getDrawable(mDimmed
                ? R.drawable.ic_lock_idle_alarm_saver_dim
                : R.drawable.ic_lock_idle_alarm_saver),
            null, null, null);
        mBatteryDisplay =
        mWeatherCurrentTemperature =
        mWeatherHighTemperature =
        mWeatherLowTemperature =
        mWeatherLocation = null;
        mWeatherIcon = null;
        refreshDate();
        refreshAlarm();
        moveScreenSaverTo(oldLoc[0], oldLoc[1]);
    }
    @Override
    public void onUserInteraction() {
        if (mScreenSaverMode)
            restoreScreen();
    }
    private void requestWeatherDataFetch() {
        if (DEBUG) Log.d(LOG_TAG, "forcing the Genie widget to update weather now...");
        sendBroadcast(new Intent(ACTION_GENIE_REFRESH).putExtra("requestWeather", true));
    }
    private boolean supportsWeather() {
        return (mGenieResources != null);
    }
    private void scheduleWeatherQueryDelayed(long delay) {
        unscheduleWeatherQuery();
        if (DEBUG) Log.d(LOG_TAG, "scheduling weather fetch message for " + delay + "ms from now");
        mHandy.sendEmptyMessageDelayed(QUERY_WEATHER_DATA_MSG, delay);
    }
    private void unscheduleWeatherQuery() {
        mHandy.removeMessages(QUERY_WEATHER_DATA_MSG);
    }
    private void queryWeatherData() {
        if (mGenieResources == null) return;
        Uri queryUri = new Uri.Builder()
            .scheme(android.content.ContentResolver.SCHEME_CONTENT)
            .authority(WEATHER_CONTENT_AUTHORITY)
            .path(WEATHER_CONTENT_PATH)
            .appendPath(new Long(System.currentTimeMillis()).toString())
            .build();
        if (DEBUG) Log.d(LOG_TAG, "querying genie: " + queryUri);
        Cursor cur;
        try {
            cur = getContentResolver().query(
                queryUri,
                WEATHER_CONTENT_COLUMNS,
                null,
                null,
                null);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Weather query failed", e);
            cur = null;
        }
        if (cur != null && cur.moveToFirst()) {
            if (DEBUG) {
                java.lang.StringBuilder sb =
                    new java.lang.StringBuilder("Weather query result: {");
                for(int i=0; i<cur.getColumnCount(); i++) {
                    if (i>0) sb.append(", ");
                    sb.append(cur.getColumnName(i))
                        .append("=")
                        .append(cur.getString(i));
                }
                sb.append("}");
                Log.d(LOG_TAG, sb.toString());
            }
            mWeatherIconDrawable = mGenieResources.getDrawable(cur.getInt(
                cur.getColumnIndexOrThrow("iconResId")));
            mWeatherLocationString = cur.getString(
                cur.getColumnIndexOrThrow("location"));
            final int colTemp = cur.getColumnIndexOrThrow("temperature");
            final int colHigh = cur.getColumnIndexOrThrow("highTemperature");
            final int colLow = cur.getColumnIndexOrThrow("lowTemperature");
            mWeatherCurrentTemperatureString =
                cur.isNull(colTemp)
                    ? "\u2014"
                    : String.format("%d\u00b0", cur.getInt(colTemp));
            mWeatherHighTemperatureString =
                cur.isNull(colHigh)
                    ? "\u2014"
                    : String.format("%d\u00b0", cur.getInt(colHigh));
            mWeatherLowTemperatureString =
                cur.isNull(colLow)
                    ? "\u2014"
                    : String.format("%d\u00b0", cur.getInt(colLow));
        } else {
            Log.w(LOG_TAG, "No weather information available (cur="
                + cur +")");
            mWeatherIconDrawable = null;
            mWeatherLocationString = getString(R.string.weather_fetch_failure);
            mWeatherCurrentTemperatureString =
                mWeatherHighTemperatureString =
                mWeatherLowTemperatureString = "";
        }
        if (cur != null) {
            cur.close();
        }
        mHandy.sendEmptyMessage(UPDATE_WEATHER_DISPLAY_MSG);
    }
    private void refreshWeather() {
        if (supportsWeather())
            scheduleWeatherQueryDelayed(0);
        updateWeatherDisplay(); 
    }
    private void updateWeatherDisplay() {
        if (mWeatherCurrentTemperature == null) return;
        mWeatherCurrentTemperature.setText(mWeatherCurrentTemperatureString);
        mWeatherHighTemperature.setText(mWeatherHighTemperatureString);
        mWeatherLowTemperature.setText(mWeatherLowTemperatureString);
        mWeatherLocation.setText(mWeatherLocationString);
        mWeatherIcon.setImageDrawable(mWeatherIconDrawable);
    }
    private void handleBatteryUpdate(int plugStatus, int batteryLevel) {
        final boolean pluggedIn = (plugStatus == BATTERY_STATUS_CHARGING || plugStatus == BATTERY_STATUS_FULL);
        if (pluggedIn != mPluggedIn) {
            setWakeLock(pluggedIn);
            if (pluggedIn) {
                requestWeatherDataFetch();
            }
        }
        if (pluggedIn != mPluggedIn || batteryLevel != mBatteryLevel) {
            mBatteryLevel = batteryLevel;
            mPluggedIn = pluggedIn;
            refreshBattery();
        }
    }
    private void refreshBattery() {
        if (mBatteryDisplay == null) return;
        if (mPluggedIn ) {
            mBatteryDisplay.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, android.R.drawable.ic_lock_idle_charging, 0);
            mBatteryDisplay.setText(
                getString(R.string.battery_charging_level, mBatteryLevel));
            mBatteryDisplay.setVisibility(View.VISIBLE);
        } else {
            mBatteryDisplay.setVisibility(View.INVISIBLE);
        }
    }
    private void refreshDate() {
        final Date now = new Date();
        if (DEBUG) Log.d(LOG_TAG, "refreshing date..." + now);
        mDate.setText(DateFormat.format(mDateFormat, now));
    }
    private void refreshAlarm() {
        if (mNextAlarm == null) return;
        String nextAlarm = Settings.System.getString(getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            mNextAlarm.setText(nextAlarm);
            mNextAlarm.setVisibility(View.VISIBLE);
        } else {
            mNextAlarm.setVisibility(View.INVISIBLE);
        }
    }
    private void refreshAll() {
        refreshDate();
        refreshAlarm();
        refreshBattery();
        refreshWeather();
    }
    private void doDim(boolean fade) {
        View tintView = findViewById(R.id.window_tint);
        if (tintView == null) return;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        winParams.flags |= (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        if (mDimmed) {
            winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            winParams.dimAmount = DIM_BEHIND_AMOUNT_DIMMED;
            winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
            tintView.startAnimation(AnimationUtils.loadAnimation(this,
                fade ? R.anim.dim
                     : R.anim.dim_instant));
        } else {
            winParams.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            winParams.dimAmount = DIM_BEHIND_AMOUNT_NORMAL;
            winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            tintView.startAnimation(AnimationUtils.loadAnimation(this,
                fade ? R.anim.undim
                     : R.anim.undim_instant));
        }
        win.setAttributes(winParams);
    }
    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        if (DEBUG) Log.d(LOG_TAG, "onNewIntent with intent: " + newIntent);
        setIntent(newIntent);
    }
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction(ACTION_MIDNIGHT);
        registerReceiver(mIntentReceiver, filter);
    }
    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mIntentReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(LOG_TAG, "onResume with intent: " + getIntent());
        mDateFormat = getString(R.string.full_wday_month_day_no_year);
        Uri weatherNotificationUri = new Uri.Builder()
            .scheme(android.content.ContentResolver.SCHEME_CONTENT)
            .authority(WEATHER_CONTENT_AUTHORITY)
            .path(WEATHER_CONTENT_PATH)
            .build();
        getContentResolver().registerContentObserver(
            weatherNotificationUri, true, mContentObserver);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.add(Calendar.DATE, 1);
        long alarmTimeUTC = today.getTimeInMillis();
        mMidnightIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_MIDNIGHT), 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, alarmTimeUTC, AlarmManager.INTERVAL_DAY, mMidnightIntent);
        if (DEBUG) Log.d(LOG_TAG, "set repeating midnight event at UTC: "
            + alarmTimeUTC + " ("
            + (alarmTimeUTC - System.currentTimeMillis())
            + " ms from now) repeating every "
            + AlarmManager.INTERVAL_DAY + " with intent: " + mMidnightIntent);
        if (mTime != null && mTime.getWindowVisibility() != View.VISIBLE) {
            mDimmed = false;
        }
        doDim(false);
        restoreScreen(); 
        refreshAll(); 
        setWakeLock(mPluggedIn);
        scheduleScreenSaver();
        final boolean launchedFromDock
            = getIntent().hasCategory(Intent.CATEGORY_DESK_DOCK);
        if (supportsWeather() && launchedFromDock && !mLaunchedFromDock) {
            if (DEBUG) Log.d(LOG_TAG, "Device now docked; forcing weather to refresh right now");
            requestWeatherDataFetch();
        }
        mLaunchedFromDock = launchedFromDock;
    }
    @Override
    public void onPause() {
        if (DEBUG) Log.d(LOG_TAG, "onPause");
        mHandy.removeMessages(SCREEN_SAVER_TIMEOUT_MSG);
        restoreScreen();
        getContentResolver().unregisterContentObserver(mContentObserver);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(mMidnightIntent);
        unscheduleWeatherQuery();
        super.onPause();
    }
    private void initViews() {
        final View focused = getCurrentFocus();
        if (focused != null) focused.clearFocus();
        setContentView(R.layout.desk_clock);
        mTime = (DigitalClock) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mBatteryDisplay = (TextView) findViewById(R.id.battery);
        mTime.getRootView().requestFocus();
        mWeatherCurrentTemperature = (TextView) findViewById(R.id.weather_temperature);
        mWeatherHighTemperature = (TextView) findViewById(R.id.weather_high_temperature);
        mWeatherLowTemperature = (TextView) findViewById(R.id.weather_low_temperature);
        mWeatherLocation = (TextView) findViewById(R.id.weather_location);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);
        final View.OnClickListener alarmClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(DeskClock.this, AlarmClock.class));
            }
        };
        mNextAlarm = (TextView) findViewById(R.id.nextAlarm);
        mNextAlarm.setOnClickListener(alarmClickListener);
        final ImageButton alarmButton = (ImageButton) findViewById(R.id.alarm_button);
        alarmButton.setOnClickListener(alarmClickListener);
        final ImageButton galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            .putExtra("slideshow", true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } catch (android.content.ActivityNotFoundException e) {
                    Log.e(LOG_TAG, "Couldn't launch image browser", e);
                }
            }
        });
        final ImageButton musicButton = (ImageButton) findViewById(R.id.music_button);
        musicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } catch (android.content.ActivityNotFoundException e) {
                    Log.e(LOG_TAG, "Couldn't launch music browser", e);
                }
            }
        });
        final ImageButton homeButton = (ImageButton) findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(
                    new Intent(Intent.ACTION_MAIN)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addCategory(Intent.CATEGORY_HOME));
            }
        });
        final ImageButton nightmodeButton = (ImageButton) findViewById(R.id.nightmode_button);
        nightmodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDimmed = ! mDimmed;
                doDim(true);
            }
        });
        nightmodeButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                saveScreen();
                return true;
            }
        });
        final View weatherView = findViewById(R.id.weather);
        weatherView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!supportsWeather()) return;
                Intent genieAppQuery = getPackageManager()
                    .getLaunchIntentForPackage(GENIE_PACKAGE_ID)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (genieAppQuery != null) {
                    startActivity(genieAppQuery);
                }
            }
        });
        final View tintView = findViewById(R.id.window_tint);
        tintView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mDimmed && event.getAction() == MotionEvent.ACTION_DOWN) {
                    Rect r = new Rect();
                    nightmodeButton.getHitRect(r);
                    int[] gloc = new int[2];
                    nightmodeButton.getLocationInWindow(gloc);
                    r.offsetTo(gloc[0], gloc[1]); 
                    if (!r.contains((int) event.getX(), (int) event.getY())) {
                        mDimmed = false;
                        doDim(true);
                    }
                }
                return false; 
            }
        });
        final ViewTreeObserver vto = alarmButton.getViewTreeObserver();
        vto.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (oldFocus == null && newFocus == nightmodeButton) {
                    alarmButton.requestFocus();
                }
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mScreenSaverMode) {
            moveScreenSaver();
        } else {
            initViews();
            doDim(false);
            refreshAll();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_alarms:
                startActivity(new Intent(DeskClock.this, AlarmClock.class));
                return true;
            case R.id.menu_item_add_alarm:
                startActivity(new Intent(this, SetAlarm.class));
                return true;
            case R.id.menu_item_dock_settings:
                startActivity(new Intent(DOCK_SETTINGS_ACTION));
                return true;
            default:
                return false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.desk_clock_menu, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mRNG = new Random();
        try {
            mGenieResources = getPackageManager().getResourcesForApplication(GENIE_PACKAGE_ID);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(LOG_TAG, "Can't find "+GENIE_PACKAGE_ID+". Weather forecast will not be available.");
        }
        initViews();
    }
}
