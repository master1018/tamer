public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "SettingsProvider";
    private static final String DATABASE_NAME = "settings.db";
    private static final int DATABASE_VERSION = 56;
    private Context mContext;
    private static final HashSet<String> mValidTables = new HashSet<String>();
    static {
        mValidTables.add("system");
        mValidTables.add("secure");
        mValidTables.add("bluetooth_devices");
        mValidTables.add("bookmarks");
        mValidTables.add("favorites");
        mValidTables.add("gservices");
        mValidTables.add("old_favorites");
    }
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    public static boolean isValidTable(String name) {
        return mValidTables.contains(name);
    }
    private void createSecureTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE secure (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE ON CONFLICT REPLACE," +
                "value TEXT" +
                ");");
        db.execSQL("CREATE INDEX secureIndex1 ON secure (name);");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE system (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE ON CONFLICT REPLACE," +
                    "value TEXT" +
                    ");");
        db.execSQL("CREATE INDEX systemIndex1 ON system (name);");
        createSecureTable(db);
        db.execSQL("CREATE TABLE bluetooth_devices (" +
                    "_id INTEGER PRIMARY KEY," +
                    "name TEXT," +
                    "addr TEXT," +
                    "channel INTEGER," +
                    "type INTEGER" +
                    ");");
        db.execSQL("CREATE TABLE bookmarks (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "folder TEXT," +
                    "intent TEXT," +
                    "shortcut INTEGER," +
                    "ordering INTEGER" +
                    ");");
        db.execSQL("CREATE INDEX bookmarksIndex1 ON bookmarks (folder);");
        db.execSQL("CREATE INDEX bookmarksIndex2 ON bookmarks (shortcut);");
        loadBookmarks(db);
        loadVolumeLevels(db);
        loadSettings(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.w(TAG, "Upgrading settings database from version " + oldVersion + " to "
                + currentVersion);
        int upgradeVersion = oldVersion;
        if (upgradeVersion == 20) {
            loadVibrateSetting(db, true);
            upgradeVersion = 21;
        }
        if (upgradeVersion < 22) {
            upgradeVersion = 22;
            upgradeLockPatternLocation(db);
        }
        if (upgradeVersion < 23) {
            db.execSQL("UPDATE favorites SET iconResource=0 WHERE iconType=0");
            upgradeVersion = 23;
        }
        if (upgradeVersion == 23) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE favorites ADD spanX INTEGER");
                db.execSQL("ALTER TABLE favorites ADD spanY INTEGER");
                db.execSQL("UPDATE favorites SET spanX=1, spanY=1 WHERE itemType<=0");
                db.execSQL(
                    "UPDATE favorites SET spanX=2, spanY=2 WHERE itemType=1000 or itemType=1002");
                db.execSQL("UPDATE favorites SET spanX=4, spanY=1 WHERE itemType=1001");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 24;
        }
        if (upgradeVersion == 24) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM system WHERE name='network_preference'");
                db.execSQL("INSERT INTO system ('name', 'value') values ('network_preference', '" +
                        ConnectivityManager.DEFAULT_NETWORK_PREFERENCE + "')");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 25;
        }
        if (upgradeVersion == 25) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE favorites ADD uri TEXT");
                db.execSQL("ALTER TABLE favorites ADD displayMode INTEGER");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 26;
        }
        if (upgradeVersion == 26) {
            db.beginTransaction();
            try {
                createSecureTable(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 27;
        }
        if (upgradeVersion == 27) {
            String[] settingsToMove = {
                    Settings.Secure.ADB_ENABLED,
                    Settings.Secure.ANDROID_ID,
                    Settings.Secure.BLUETOOTH_ON,
                    Settings.Secure.DATA_ROAMING,
                    Settings.Secure.DEVICE_PROVISIONED,
                    Settings.Secure.HTTP_PROXY,
                    Settings.Secure.INSTALL_NON_MARKET_APPS,
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
                    Settings.Secure.LOGGING_ID,
                    Settings.Secure.NETWORK_PREFERENCE,
                    Settings.Secure.PARENTAL_CONTROL_ENABLED,
                    Settings.Secure.PARENTAL_CONTROL_LAST_UPDATE,
                    Settings.Secure.PARENTAL_CONTROL_REDIRECT_URL,
                    Settings.Secure.SETTINGS_CLASSNAME,
                    Settings.Secure.USB_MASS_STORAGE_ENABLED,
                    Settings.Secure.USE_GOOGLE_MAIL,
                    Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    Settings.Secure.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY,
                    Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT,
                    Settings.Secure.WIFI_ON,
                    Settings.Secure.WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE,
                    Settings.Secure.WIFI_WATCHDOG_AP_COUNT,
                    Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS,
                    Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED,
                    Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS,
                    Settings.Secure.WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT,
                    Settings.Secure.WIFI_WATCHDOG_MAX_AP_CHECKS,
                    Settings.Secure.WIFI_WATCHDOG_ON,
                    Settings.Secure.WIFI_WATCHDOG_PING_COUNT,
                    Settings.Secure.WIFI_WATCHDOG_PING_DELAY_MS,
                    Settings.Secure.WIFI_WATCHDOG_PING_TIMEOUT_MS,
                };
            moveFromSystemToSecure(db, settingsToMove);
            upgradeVersion = 28;
        }
        if (upgradeVersion == 28 || upgradeVersion == 29) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.MODE_RINGER_STREAMS_AFFECTED + "'");
                int newValue = (1 << AudioManager.STREAM_RING)
                        | (1 << AudioManager.STREAM_NOTIFICATION)
                        | (1 << AudioManager.STREAM_SYSTEM);
                db.execSQL("INSERT INTO system ('name', 'value') values ('"
                        + Settings.System.MODE_RINGER_STREAMS_AFFECTED + "', '"
                        + String.valueOf(newValue) + "')");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 30;
        }
        if (upgradeVersion == 30) {
            db.beginTransaction();
            try {
                db.execSQL("UPDATE bookmarks SET folder = '@quicklaunch'");
                db.execSQL("UPDATE bookmarks SET title = ''");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 31;
        }
        if (upgradeVersion == 31) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.WINDOW_ANIMATION_SCALE + "'");
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.TRANSITION_ANIMATION_SCALE + "'");
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadDefaultAnimationSettings(stmt);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 32;
        }
        if (upgradeVersion == 32) {
            String wifiWatchList = SystemProperties.get("ro.com.android.wifi-watchlist");
            if (!TextUtils.isEmpty(wifiWatchList)) {
                db.beginTransaction();
                try {
                    db.execSQL("INSERT OR IGNORE INTO secure(name,value) values('" +
                            Settings.Secure.WIFI_WATCHDOG_WATCH_LIST + "','" +
                            wifiWatchList + "');");
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            upgradeVersion = 33;
        }
        if (upgradeVersion == 33) {
            db.beginTransaction();
            try {
                db.execSQL("INSERT INTO system(name,value) values('zoom','2');");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 34;
        }
        if (upgradeVersion == 34) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO secure(name,value)"
                        + " VALUES(?,?);");
                loadSecure35Settings(stmt);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 35;
        }
        if (upgradeVersion == 35) {
            upgradeVersion = 36;
        }
        if (upgradeVersion == 36) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.MODE_RINGER_STREAMS_AFFECTED + "'");
                int newValue = (1 << AudioManager.STREAM_RING)
                        | (1 << AudioManager.STREAM_NOTIFICATION)
                        | (1 << AudioManager.STREAM_SYSTEM)
                        | (1 << AudioManager.STREAM_SYSTEM_ENFORCED);
                db.execSQL("INSERT INTO system ('name', 'value') values ('"
                        + Settings.System.MODE_RINGER_STREAMS_AFFECTED + "', '"
                        + String.valueOf(newValue) + "')");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 37;
        }
        if (upgradeVersion == 37) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO system(name,value)"
                        + " VALUES(?,?);");
                loadStringSetting(stmt, Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS,
                        R.string.airplane_mode_toggleable_radios);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 38;
        }
        if (upgradeVersion == 38) {
            db.beginTransaction();
            try {
                String value =
                        mContext.getResources().getBoolean(R.bool.assisted_gps_enabled) ? "1" : "0";
                db.execSQL("INSERT OR IGNORE INTO secure(name,value) values('" +
                        Settings.Secure.ASSISTED_GPS_ENABLED + "','" + value + "');");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 39;
        }
        if (upgradeVersion == 39) {
            db.beginTransaction();
            try {
                String value =
                        mContext.getResources().getBoolean(
                        R.bool.def_screen_brightness_automatic_mode) ? "1" : "0";
                db.execSQL("INSERT OR IGNORE INTO system(name,value) values('" +
                        Settings.System.SCREEN_BRIGHTNESS_MODE + "','" + value + "');");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 40;
        }
        if (upgradeVersion == 40) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.WINDOW_ANIMATION_SCALE + "'");
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.TRANSITION_ANIMATION_SCALE + "'");
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadDefaultAnimationSettings(stmt);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 41;
        }
        if (upgradeVersion == 41) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                db.execSQL("DELETE FROM system WHERE name='"
                        + Settings.System.HAPTIC_FEEDBACK_ENABLED + "'");
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadDefaultHapticSettings(stmt);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 42;
        }
        if (upgradeVersion == 42) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, Settings.System.NOTIFICATION_LIGHT_PULSE,
                        R.bool.def_notification_pulse);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 43;
        }
        if (upgradeVersion == 43) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO system(name,value)"
                        + " VALUES(?,?);");
                loadSetting(stmt, Settings.System.VOLUME_BLUETOOTH_SCO,
                        AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_BLUETOOTH_SCO]);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 44;
        }
        if (upgradeVersion == 44) {
            db.execSQL("DROP TABLE IF EXISTS gservices");
            db.execSQL("DROP INDEX IF EXISTS gservicesIndex1");
            upgradeVersion = 45;
        }
        if (upgradeVersion == 45) {
            db.beginTransaction();
            try {
                db.execSQL("INSERT INTO secure(name,value) values('" +
                        Settings.Secure.MOUNT_PLAY_NOTIFICATION_SND + "','1');");
                db.execSQL("INSERT INTO secure(name,value) values('" +
                        Settings.Secure.MOUNT_UMS_AUTOSTART + "','0');");
                db.execSQL("INSERT INTO secure(name,value) values('" +
                        Settings.Secure.MOUNT_UMS_PROMPT + "','1');");
                db.execSQL("INSERT INTO secure(name,value) values('" +
                        Settings.Secure.MOUNT_UMS_NOTIFY_ENABLED + "','1');");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 46;
        }
        if (upgradeVersion == 46) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM system WHERE name='lockscreen.password_type';");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
           upgradeVersion = 47;
       }
        if (upgradeVersion == 47) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM system WHERE name='lockscreen.password_type';");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
           upgradeVersion = 48;
       }
       if (upgradeVersion == 48) {
           upgradeVersion = 49;
       }
       if (upgradeVersion == 49) {
           db.beginTransaction();
           SQLiteStatement stmt = null;
           try {
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadUISoundEffectsSettings(stmt);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
           upgradeVersion = 50;
       }
       if (upgradeVersion == 50) {
           upgradeVersion = 51;
       }
       if (upgradeVersion == 51) {
           String[] settingsToMove = {
                   Secure.LOCK_PATTERN_ENABLED,
                   Secure.LOCK_PATTERN_VISIBLE,
                   Secure.LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED,
                   "lockscreen.password_type",
                   "lockscreen.lockoutattemptdeadline",
                   "lockscreen.patterneverchosen",
                   "lock_pattern_autolock",
                   "lockscreen.lockedoutpermanently",
                   "lockscreen.password_salt"
           };
           moveFromSystemToSecure(db, settingsToMove);
           upgradeVersion = 52;
       }
        if (upgradeVersion == 52) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, Settings.System.VIBRATE_IN_SILENT,
                        R.bool.def_vibrate_in_silent);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 53;
        }
        if (upgradeVersion == 53) {
            upgradeVersion = 54;
        }
        if (upgradeVersion == 54) {
            db.beginTransaction();
            try {
                upgradeScreenTimeoutFromNever(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 55;
        }
        if (upgradeVersion == 55) {
            String[] settingsToMove = {
                    Secure.SET_INSTALL_LOCATION,
                    Secure.DEFAULT_INSTALL_LOCATION
            };
            moveFromSystemToSecure(db, settingsToMove);
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT INTO system(name,value)"
                        + " VALUES(?,?);");
                loadSetting(stmt, Secure.SET_INSTALL_LOCATION, 0);
                loadSetting(stmt, Secure.DEFAULT_INSTALL_LOCATION,
                        PackageHelper.APP_INSTALL_AUTO);
                db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
                 if (stmt != null) stmt.close();
             }
            upgradeVersion = 56;
        }
        if (upgradeVersion != currentVersion) {
            Log.w(TAG, "Got stuck trying to upgrade from version " + upgradeVersion
                    + ", must wipe the settings provider");
            db.execSQL("DROP TABLE IF EXISTS system");
            db.execSQL("DROP INDEX IF EXISTS systemIndex1");
            db.execSQL("DROP TABLE IF EXISTS secure");
            db.execSQL("DROP INDEX IF EXISTS secureIndex1");
            db.execSQL("DROP TABLE IF EXISTS gservices");
            db.execSQL("DROP INDEX IF EXISTS gservicesIndex1");
            db.execSQL("DROP TABLE IF EXISTS bluetooth_devices");
            db.execSQL("DROP TABLE IF EXISTS bookmarks");
            db.execSQL("DROP INDEX IF EXISTS bookmarksIndex1");
            db.execSQL("DROP INDEX IF EXISTS bookmarksIndex2");
            db.execSQL("DROP TABLE IF EXISTS favorites");
            onCreate(db);
            String wipeReason = oldVersion + "/" + upgradeVersion + "/" + currentVersion;
            db.execSQL("INSERT INTO secure(name,value) values('" +
                    "wiped_db_reason" + "','" + wipeReason + "');");
        }
    }
    private void moveFromSystemToSecure(SQLiteDatabase db, String [] settingsToMove) {
        SQLiteStatement insertStmt = null;
        SQLiteStatement deleteStmt = null;
        db.beginTransaction();
        try {
            insertStmt =
                db.compileStatement("INSERT INTO secure (name,value) SELECT name,value FROM "
                    + "system WHERE name=?");
            deleteStmt = db.compileStatement("DELETE FROM system WHERE name=?");
            for (String setting : settingsToMove) {
                insertStmt.bindString(1, setting);
                insertStmt.execute();
                deleteStmt.bindString(1, setting);
                deleteStmt.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (insertStmt != null) {
                insertStmt.close();
            }
            if (deleteStmt != null) {
                deleteStmt.close();
            }
        }
    }
    private void upgradeLockPatternLocation(SQLiteDatabase db) {
        Cursor c = db.query("system", new String[] {"_id", "value"}, "name='lock_pattern'",
                null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String lockPattern = c.getString(1);
            if (!TextUtils.isEmpty(lockPattern)) {
                try {
                    LockPatternUtils lpu = new LockPatternUtils(mContext);
                    List<LockPatternView.Cell> cellPattern =
                            LockPatternUtils.stringToPattern(lockPattern);
                    lpu.saveLockPattern(cellPattern);
                } catch (IllegalArgumentException e) {
                }
            }
            c.close();
            db.delete("system", "name='lock_pattern'", null);
        } else {
            c.close();
        }
    }
    private void upgradeScreenTimeoutFromNever(SQLiteDatabase db) {
        Cursor c = db.query("system", new String[] { "_id", "value" }, "name=? AND value=?",
                new String[] { Settings.System.SCREEN_OFF_TIMEOUT, "-1" },
                null, null, null);
        SQLiteStatement stmt = null;
        if (c.getCount() > 0) {
            c.close();
            try {
                stmt = db.compileStatement("INSERT OR REPLACE INTO system(name,value)"
                        + " VALUES(?,?);");
                loadSetting(stmt, Settings.System.SCREEN_OFF_TIMEOUT,
                        Integer.toString(30 * 60 * 1000));
            } finally {
                if (stmt != null) stmt.close();
            }
        } else {
            c.close();
        }
    }
    private int loadBookmarks(SQLiteDatabase db, int startingIndex) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ContentValues values = new ContentValues();
        PackageManager packageManager = mContext.getPackageManager();
        int i = startingIndex;
        try {
            XmlResourceParser parser = mContext.getResources().getXml(R.xml.bookmarks);
            XmlUtils.beginDocument(parser, "bookmarks");
            final int depth = parser.getDepth();
            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (!"bookmark".equals(name)) {
                    break;
                }
                String pkg = parser.getAttributeValue(null, "package");
                String cls = parser.getAttributeValue(null, "class");
                String shortcutStr = parser.getAttributeValue(null, "shortcut");
                int shortcutValue = (int) shortcutStr.charAt(0);
                if (TextUtils.isEmpty(shortcutStr)) {
                    Log.w(TAG, "Unable to get shortcut for: " + pkg + "/" + cls);
                }
                ActivityInfo info = null;                
                ComponentName cn = new ComponentName(pkg, cls);
                try {
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    String[] packages = packageManager.canonicalToCurrentPackageNames(
                            new String[] { pkg });
                    cn = new ComponentName(packages[0], cls);
                    try {
                        info = packageManager.getActivityInfo(cn, 0);
                    } catch (PackageManager.NameNotFoundException e1) {
                        Log.w(TAG, "Unable to add bookmark: " + pkg + "/" + cls, e);
                    }
                }
                if (info != null) {
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    values.put(Settings.Bookmarks.INTENT, intent.toUri(0));
                    values.put(Settings.Bookmarks.TITLE,
                            info.loadLabel(packageManager).toString());
                    values.put(Settings.Bookmarks.SHORTCUT, shortcutValue);
                    db.insert("bookmarks", null, values);
                    i++;
                }
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got execption parsing bookmarks.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got execption parsing bookmarks.", e);
        }
        return i;
    }
    private void loadBookmarks(SQLiteDatabase db) {
        loadBookmarks(db, 0);
    }
    private void loadVolumeLevels(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO system(name,value)"
                    + " VALUES(?,?);");
            loadSetting(stmt, Settings.System.VOLUME_MUSIC,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_MUSIC]);
            loadSetting(stmt, Settings.System.VOLUME_RING,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_RING]);
            loadSetting(stmt, Settings.System.VOLUME_SYSTEM,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_SYSTEM]);
            loadSetting(
                    stmt,
                    Settings.System.VOLUME_VOICE,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_VOICE_CALL]);
            loadSetting(stmt, Settings.System.VOLUME_ALARM,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_ALARM]);
            loadSetting(
                    stmt,
                    Settings.System.VOLUME_NOTIFICATION,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_NOTIFICATION]);
            loadSetting(
                    stmt,
                    Settings.System.VOLUME_BLUETOOTH_SCO,
                    AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_BLUETOOTH_SCO]);
            loadSetting(stmt, Settings.System.MODE_RINGER,
                    AudioManager.RINGER_MODE_NORMAL);
            loadVibrateSetting(db, false);
            loadSetting(stmt, Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                    (1 << AudioManager.STREAM_RING) | (1 << AudioManager.STREAM_NOTIFICATION) |
                    (1 << AudioManager.STREAM_SYSTEM) | (1 << AudioManager.STREAM_SYSTEM_ENFORCED));
            loadSetting(stmt, Settings.System.MUTE_STREAMS_AFFECTED,
                    ((1 << AudioManager.STREAM_MUSIC) |
                     (1 << AudioManager.STREAM_RING) |
                     (1 << AudioManager.STREAM_NOTIFICATION) |
                     (1 << AudioManager.STREAM_SYSTEM)));
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    private void loadVibrateSetting(SQLiteDatabase db, boolean deleteOld) {
        if (deleteOld) {
            db.execSQL("DELETE FROM system WHERE name='" + Settings.System.VIBRATE_ON + "'");
        }
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO system(name,value)"
                    + " VALUES(?,?);");
            int vibrate = 0;
            vibrate = AudioService.getValueForVibrateSetting(vibrate,
                    AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            vibrate = AudioService.getValueForVibrateSetting(vibrate,
                    AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            loadSetting(stmt, Settings.System.VIBRATE_ON, vibrate);
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    private void loadSettings(SQLiteDatabase db) {
        loadSystemSettings(db);
        loadSecureSettings(db);
    }
    private void loadSystemSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO system(name,value)"
                    + " VALUES(?,?);");
            loadBooleanSetting(stmt, Settings.System.DIM_SCREEN,
                    R.bool.def_dim_screen);
            loadSetting(stmt, Settings.System.STAY_ON_WHILE_PLUGGED_IN,
                    "1".equals(SystemProperties.get("ro.kernel.qemu")) ? 1 : 0);
            loadIntegerSetting(stmt, Settings.System.SCREEN_OFF_TIMEOUT,
                    R.integer.def_screen_off_timeout);
            loadSetting(stmt, Settings.System.EMERGENCY_TONE, 0);
            loadSetting(stmt, Settings.System.CALL_AUTO_RETRY, 0);
            loadSetting(stmt, Settings.System.DTMF_TONE_TYPE_WHEN_DIALING, 0);
            loadSetting(stmt, Settings.System.HEARING_AID, 0);
            loadSetting(stmt, Settings.System.TTY_MODE, 0);
            loadBooleanSetting(stmt, Settings.System.AIRPLANE_MODE_ON,
                    R.bool.def_airplane_mode_on);
            loadStringSetting(stmt, Settings.System.AIRPLANE_MODE_RADIOS,
                    R.string.def_airplane_mode_radios);
            loadStringSetting(stmt, Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS,
                    R.string.airplane_mode_toggleable_radios);
            loadBooleanSetting(stmt, Settings.System.AUTO_TIME,
                    R.bool.def_auto_time); 
            loadIntegerSetting(stmt, Settings.System.SCREEN_BRIGHTNESS,
                    R.integer.def_screen_brightness);
            loadBooleanSetting(stmt, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    R.bool.def_screen_brightness_automatic_mode);
            loadDefaultAnimationSettings(stmt);
            loadBooleanSetting(stmt, Settings.System.ACCELEROMETER_ROTATION,
                    R.bool.def_accelerometer_rotation);
            loadDefaultHapticSettings(stmt);
            loadBooleanSetting(stmt, Settings.System.NOTIFICATION_LIGHT_PULSE,
                    R.bool.def_notification_pulse);
            loadSetting(stmt, Settings.Secure.SET_INSTALL_LOCATION, 0);
            loadSetting(stmt, Settings.Secure.DEFAULT_INSTALL_LOCATION,
                    PackageHelper.APP_INSTALL_AUTO);
            loadUISoundEffectsSettings(stmt);
            loadBooleanSetting(stmt, Settings.System.VIBRATE_IN_SILENT,
                    R.bool.def_vibrate_in_silent);
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    private void loadUISoundEffectsSettings(SQLiteStatement stmt) {
        loadIntegerSetting(stmt, Settings.System.POWER_SOUNDS_ENABLED,
            R.integer.def_power_sounds_enabled);
        loadStringSetting(stmt, Settings.System.LOW_BATTERY_SOUND,
            R.string.def_low_battery_sound);
        loadIntegerSetting(stmt, Settings.System.DOCK_SOUNDS_ENABLED,
            R.integer.def_dock_sounds_enabled);
        loadStringSetting(stmt, Settings.System.DESK_DOCK_SOUND,
            R.string.def_desk_dock_sound);
        loadStringSetting(stmt, Settings.System.DESK_UNDOCK_SOUND,
            R.string.def_desk_undock_sound);
        loadStringSetting(stmt, Settings.System.CAR_DOCK_SOUND,
            R.string.def_car_dock_sound);
        loadStringSetting(stmt, Settings.System.CAR_UNDOCK_SOUND,
            R.string.def_car_undock_sound);
        loadIntegerSetting(stmt, Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
            R.integer.def_lockscreen_sounds_enabled);
        loadStringSetting(stmt, Settings.System.LOCK_SOUND,
            R.string.def_lock_sound);
        loadStringSetting(stmt, Settings.System.UNLOCK_SOUND,
            R.string.def_unlock_sound);
    }
    private void loadDefaultAnimationSettings(SQLiteStatement stmt) {
        loadFractionSetting(stmt, Settings.System.WINDOW_ANIMATION_SCALE,
                R.fraction.def_window_animation_scale, 1);
        loadFractionSetting(stmt, Settings.System.TRANSITION_ANIMATION_SCALE,
                R.fraction.def_window_transition_scale, 1);
    }
    private void loadDefaultHapticSettings(SQLiteStatement stmt) {
        loadBooleanSetting(stmt, Settings.System.HAPTIC_FEEDBACK_ENABLED,
                R.bool.def_haptic_feedback);
    }
    private void loadSecureSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO secure(name,value)"
                    + " VALUES(?,?);");
            loadBooleanSetting(stmt, Settings.Secure.BLUETOOTH_ON,
                    R.bool.def_bluetooth_on);
            loadSetting(stmt, Settings.Secure.DATA_ROAMING,
                    "true".equalsIgnoreCase(
                            SystemProperties.get("ro.com.android.dataroaming",
                                    "false")) ? 1 : 0);
            loadBooleanSetting(stmt, Settings.Secure.INSTALL_NON_MARKET_APPS,
                    R.bool.def_install_non_market_apps);
            loadStringSetting(stmt, Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
                    R.string.def_location_providers_allowed);
            loadBooleanSetting(stmt, Settings.Secure.ASSISTED_GPS_ENABLED,
                    R.bool.assisted_gps_enabled);
            loadIntegerSetting(stmt, Settings.Secure.NETWORK_PREFERENCE,
                    R.integer.def_network_preference);
            loadBooleanSetting(stmt, Settings.Secure.USB_MASS_STORAGE_ENABLED,
                    R.bool.def_usb_mass_storage_enabled);
            loadBooleanSetting(stmt, Settings.Secure.WIFI_ON,
                    R.bool.def_wifi_on);
            loadBooleanSetting(stmt, Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    R.bool.def_networks_available_notification_on);
            String wifiWatchList = SystemProperties.get("ro.com.android.wifi-watchlist");
            if (!TextUtils.isEmpty(wifiWatchList)) {
                loadSetting(stmt, Settings.Secure.WIFI_WATCHDOG_WATCH_LIST, wifiWatchList);
            }
            int type = SystemProperties.getInt("ro.telephony.default_network",
                    RILConstants.PREFERRED_NETWORK_MODE);
            loadSetting(stmt, Settings.Secure.PREFERRED_NETWORK_MODE, type);
            loadSetting(stmt, Settings.Secure.CDMA_CELL_BROADCAST_SMS,
                    RILConstants.CDMA_CELL_BROADCAST_SMS_DISABLED);
            loadSetting(stmt, Settings.Secure.PREFERRED_CDMA_SUBSCRIPTION,
                    RILConstants.PREFERRED_CDMA_SUBSCRIPTION);
            loadSetting(stmt, Settings.Secure.ALLOW_MOCK_LOCATION,
                    "1".equals(SystemProperties.get("ro.allow.mock.location")) ? 1 : 0);
            loadSecure35Settings(stmt);
            loadBooleanSetting(stmt, Settings.Secure.MOUNT_PLAY_NOTIFICATION_SND,
                    R.bool.def_mount_play_notification_snd);
            loadBooleanSetting(stmt, Settings.Secure.MOUNT_UMS_AUTOSTART,
                    R.bool.def_mount_ums_autostart);
            loadBooleanSetting(stmt, Settings.Secure.MOUNT_UMS_PROMPT,
                    R.bool.def_mount_ums_prompt);
            loadBooleanSetting(stmt, Settings.Secure.MOUNT_UMS_NOTIFY_ENABLED,
                    R.bool.def_mount_ums_notify_enabled);
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    private void loadSecure35Settings(SQLiteStatement stmt) {
        loadBooleanSetting(stmt, Settings.Secure.BACKUP_ENABLED,
                R.bool.def_backup_enabled);
        loadStringSetting(stmt, Settings.Secure.BACKUP_TRANSPORT,
                R.string.def_backup_transport);
    }
    private void loadSetting(SQLiteStatement stmt, String key, Object value) {
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }
    private void loadStringSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mContext.getResources().getString(resid));
    }
    private void loadBooleanSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                mContext.getResources().getBoolean(resid) ? "1" : "0");
    }
    private void loadIntegerSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                Integer.toString(mContext.getResources().getInteger(resid)));
    }
    private void loadFractionSetting(SQLiteStatement stmt, String key, int resid, int base) {
        loadSetting(stmt, key,
                Float.toString(mContext.getResources().getFraction(resid, base, base)));
    }
}
