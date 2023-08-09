public class MediaProvider extends ContentProvider {
    private static final Uri MEDIA_URI = Uri.parse("content:
    private static final Uri ALBUMART_URI = Uri.parse("content:
    private static final int ALBUM_THUMB = 1;
    private static final int IMAGE_THUMB = 2;
    private static final HashMap<String, String> sArtistAlbumsMap = new HashMap<String, String>();
    private static final HashMap<String, String> sFolderArtMap = new HashMap<String, String>();
    private HashSet mPendingThumbs = new HashSet();
    private Stack mThumbRequestStack = new Stack();
    private MediaThumbRequest mCurrentThumbRequest = null;
    private PriorityQueue<MediaThumbRequest> mMediaThumbQueue =
            new PriorityQueue<MediaThumbRequest>(MediaThumbRequest.PRIORITY_NORMAL,
            MediaThumbRequest.getComparator());
    private String[] mSearchColsLegacy = new String[] {
            android.provider.BaseColumns._ID,
            MediaStore.Audio.Media.MIME_TYPE,
            "(CASE WHEN grouporder=1 THEN " + R.drawable.ic_search_category_music_artist +
            " ELSE CASE WHEN grouporder=2 THEN " + R.drawable.ic_search_category_music_album +
            " ELSE " + R.drawable.ic_search_category_music_song + " END END" +
            ") AS " + SearchManager.SUGGEST_COLUMN_ICON_1,
            "0 AS " + SearchManager.SUGGEST_COLUMN_ICON_2,
            "text1 AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
            "text1 AS " + SearchManager.SUGGEST_COLUMN_QUERY,
            "CASE when grouporder=1 THEN data1 ELSE artist END AS data1",
            "CASE when grouporder=1 THEN data2 ELSE " +
                "CASE WHEN grouporder=2 THEN NULL ELSE album END END AS data2",
            "match as ar",
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            "grouporder",
            "NULL AS itemorder" 
    };
    private String[] mSearchColsFancy = new String[] {
            android.provider.BaseColumns._ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Media.TITLE,
            "data1",
            "data2",
    };
    private String[] mSearchColsBasic = new String[] {
            android.provider.BaseColumns._ID,
            MediaStore.Audio.Media.MIME_TYPE,
            "(CASE WHEN grouporder=1 THEN " + R.drawable.ic_search_category_music_artist +
            " ELSE CASE WHEN grouporder=2 THEN " + R.drawable.ic_search_category_music_album +
            " ELSE " + R.drawable.ic_search_category_music_song + " END END" +
            ") AS " + SearchManager.SUGGEST_COLUMN_ICON_1,
            "text1 AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
            "text1 AS " + SearchManager.SUGGEST_COLUMN_QUERY,
            "(CASE WHEN grouporder=1 THEN '%1'" +  
            " ELSE CASE WHEN grouporder=3 THEN artist || ' - ' || album" +
            " ELSE CASE WHEN text2!='" + MediaStore.UNKNOWN_STRING + "' THEN text2" +
            " ELSE NULL END END END) AS " + SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };
    private final int SEARCH_COLUMN_BASIC_TEXT2 = 5;
    private Uri mAlbumArtBaseUri = Uri.parse("content:
    private BroadcastReceiver mUnmountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
                detachVolume(Uri.parse("content:
                sFolderArtMap.clear();
                MiniThumbFile.reset();
            }
        }
    };
    private static final class DatabaseHelper extends SQLiteOpenHelper {
        final Context mContext;
        final boolean mInternal;  
        HashMap<String, Long> mArtistCache = new HashMap<String, Long>();
        HashMap<String, Long> mAlbumCache = new HashMap<String, Long>();
        public DatabaseHelper(Context context, String name, boolean internal) {
            super(context, name, null, DATABASE_VERSION);
            mContext = context;
            mInternal = internal;
        }
        @Override
        public void onCreate(final SQLiteDatabase db) {
            updateDatabase(db, mInternal, 0, DATABASE_VERSION);
        }
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldV, final int newV) {
            updateDatabase(db, mInternal, oldV, newV);
        }
        @Override
        public void onOpen(SQLiteDatabase db) {
            if (mInternal) return;  
            File file = new File(db.getPath());
            long now = System.currentTimeMillis();
            file.setLastModified(now);
            String[] databases = mContext.databaseList();
            int count = databases.length;
            int limit = MAX_EXTERNAL_DATABASES;
            long twoMonthsAgo = now - OBSOLETE_DATABASE_DB;
            for (int i = 0; i < databases.length; i++) {
                File other = mContext.getDatabasePath(databases[i]);
                if (INTERNAL_DATABASE_NAME.equals(databases[i]) || file.equals(other)) {
                    databases[i] = null;
                    count--;
                    if (file.equals(other)) {
                        limit--;
                    }
                } else {
                    long time = other.lastModified();
                    if (time < twoMonthsAgo) {
                        if (LOCAL_LOGV) Log.v(TAG, "Deleting old database " + databases[i]);
                        mContext.deleteDatabase(databases[i]);
                        databases[i] = null;
                        count--;
                    }
                }
            }
            while (count > limit) {
                int lruIndex = -1;
                long lruTime = 0;
                for (int i = 0; i < databases.length; i++) {
                    if (databases[i] != null) {
                        long time = mContext.getDatabasePath(databases[i]).lastModified();
                        if (lruTime == 0 || time < lruTime) {
                            lruIndex = i;
                            lruTime = time;
                        }
                    }
                }
                if (lruIndex != -1) {
                    if (LOCAL_LOGV) Log.v(TAG, "Deleting old database " + databases[lruIndex]);
                    mContext.deleteDatabase(databases[lruIndex]);
                    databases[lruIndex] = null;
                    count--;
                }
            }
        }
    }
    @Override
    public boolean onCreate() {
        sArtistAlbumsMap.put(MediaStore.Audio.Albums._ID, "audio.album_id AS " +
                MediaStore.Audio.Albums._ID);
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.ALBUM, "album");
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.ALBUM_KEY, "album_key");
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.FIRST_YEAR, "MIN(year) AS " +
                MediaStore.Audio.Albums.FIRST_YEAR);
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.LAST_YEAR, "MAX(year) AS " +
                MediaStore.Audio.Albums.LAST_YEAR);
        sArtistAlbumsMap.put(MediaStore.Audio.Media.ARTIST, "artist");
        sArtistAlbumsMap.put(MediaStore.Audio.Media.ARTIST_ID, "artist");
        sArtistAlbumsMap.put(MediaStore.Audio.Media.ARTIST_KEY, "artist_key");
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.NUMBER_OF_SONGS, "count(*) AS " +
                MediaStore.Audio.Albums.NUMBER_OF_SONGS);
        sArtistAlbumsMap.put(MediaStore.Audio.Albums.ALBUM_ART, "album_art._data AS " +
                MediaStore.Audio.Albums.ALBUM_ART);
        mSearchColsBasic[SEARCH_COLUMN_BASIC_TEXT2] =
                mSearchColsBasic[SEARCH_COLUMN_BASIC_TEXT2].replaceAll(
                        "%1", getContext().getString(R.string.artist_label));
        mDatabases = new HashMap<String, DatabaseHelper>();
        attachVolume(INTERNAL_VOLUME);
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        iFilter.addDataScheme("file");
        getContext().registerReceiver(mUnmountReceiver, iFilter);
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            attachVolume(EXTERNAL_VOLUME);
        }
        HandlerThread ht = new HandlerThread("thumbs thread", Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        mThumbHandler = new Handler(ht.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == IMAGE_THUMB) {
                    synchronized (mMediaThumbQueue) {
                        mCurrentThumbRequest = mMediaThumbQueue.poll();
                    }
                    if (mCurrentThumbRequest == null) {
                        Log.w(TAG, "Have message but no request?");
                    } else {
                        try {
                            File origFile = new File(mCurrentThumbRequest.mPath);
                            if (origFile.exists() && origFile.length() > 0) {
                                mCurrentThumbRequest.execute();
                            } else {
                                synchronized (mMediaThumbQueue) {
                                    Log.w(TAG, "original file hasn't been stored yet: " + mCurrentThumbRequest.mPath);
                                }
                            }
                        } catch (IOException ex) {
                            Log.w(TAG, ex);
                        } catch (UnsupportedOperationException ex) {
                            Log.w(TAG, ex);
                        } finally {
                            synchronized (mCurrentThumbRequest) {
                                mCurrentThumbRequest.mState = MediaThumbRequest.State.DONE;
                                mCurrentThumbRequest.notifyAll();
                            }
                        }
                    }
                } else if (msg.what == ALBUM_THUMB) {
                    ThumbData d;
                    synchronized (mThumbRequestStack) {
                        d = (ThumbData)mThumbRequestStack.pop();
                    }
                    makeThumbInternal(d);
                    synchronized (mPendingThumbs) {
                        mPendingThumbs.remove(d.path);
                    }
                }
            }
        };
        return true;
    }
    private static void updateDatabase(SQLiteDatabase db, boolean internal,
            int fromVersion, int toVersion) {
        if (toVersion != DATABASE_VERSION) {
            Log.e(TAG, "Illegal update request. Got " + toVersion + ", expected " +
                    DATABASE_VERSION);
            throw new IllegalArgumentException();
        } else if (fromVersion > toVersion) {
            Log.e(TAG, "Illegal update request: can't downgrade from " + fromVersion +
                    " to " + toVersion + ". Did you forget to wipe data?");
            throw new IllegalArgumentException();
        }
        if (fromVersion < 63 || (fromVersion >= 84 && fromVersion <= 89)) {
            fromVersion = 63;
            Log.i(TAG, "Upgrading media database from version " +
                    fromVersion + " to " + toVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS images");
            db.execSQL("DROP TRIGGER IF EXISTS images_cleanup");
            db.execSQL("DROP TABLE IF EXISTS thumbnails");
            db.execSQL("DROP TRIGGER IF EXISTS thumbnails_cleanup");
            db.execSQL("DROP TABLE IF EXISTS audio_meta");
            db.execSQL("DROP TABLE IF EXISTS artists");
            db.execSQL("DROP TABLE IF EXISTS albums");
            db.execSQL("DROP TABLE IF EXISTS album_art");
            db.execSQL("DROP VIEW IF EXISTS artist_info");
            db.execSQL("DROP VIEW IF EXISTS album_info");
            db.execSQL("DROP VIEW IF EXISTS artists_albums_map");
            db.execSQL("DROP TRIGGER IF EXISTS audio_meta_cleanup");
            db.execSQL("DROP TABLE IF EXISTS audio_genres");
            db.execSQL("DROP TABLE IF EXISTS audio_genres_map");
            db.execSQL("DROP TRIGGER IF EXISTS audio_genres_cleanup");
            db.execSQL("DROP TABLE IF EXISTS audio_playlists");
            db.execSQL("DROP TABLE IF EXISTS audio_playlists_map");
            db.execSQL("DROP TRIGGER IF EXISTS audio_playlists_cleanup");
            db.execSQL("DROP TRIGGER IF EXISTS albumart_cleanup1");
            db.execSQL("DROP TRIGGER IF EXISTS albumart_cleanup2");
            db.execSQL("DROP TABLE IF EXISTS video");
            db.execSQL("DROP TRIGGER IF EXISTS video_cleanup");
            db.execSQL("CREATE TABLE IF NOT EXISTS images (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT," +
                    "_size INTEGER," +
                    "_display_name TEXT," +
                    "mime_type TEXT," +
                    "title TEXT," +
                    "date_added INTEGER," +
                    "date_modified INTEGER," +
                    "description TEXT," +
                    "picasa_id TEXT," +
                    "isprivate INTEGER," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "datetaken INTEGER," +
                    "orientation INTEGER," +
                    "mini_thumb_magic INTEGER," +
                    "bucket_id TEXT," +
                    "bucket_display_name TEXT" +
                   ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS mini_thumb_magic_index on images(mini_thumb_magic);");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS images_cleanup DELETE ON images " +
                    "BEGIN " +
                        "DELETE FROM thumbnails WHERE image_id = old._id;" +
                        "SELECT _DELETE_FILE(old._data);" +
                    "END");
            db.execSQL("CREATE TABLE IF NOT EXISTS thumbnails (" +
                       "_id INTEGER PRIMARY KEY," +
                       "_data TEXT," +
                       "image_id INTEGER," +
                       "kind INTEGER," +
                       "width INTEGER," +
                       "height INTEGER" +
                       ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS image_id_index on thumbnails(image_id);");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS thumbnails_cleanup DELETE ON thumbnails " +
                    "BEGIN " +
                        "SELECT _DELETE_FILE(old._data);" +
                    "END");
            db.execSQL("CREATE TABLE IF NOT EXISTS audio_meta (" +
                       "_id INTEGER PRIMARY KEY," +
                       "_data TEXT UNIQUE NOT NULL," +
                       "_display_name TEXT," +
                       "_size INTEGER," +
                       "mime_type TEXT," +
                       "date_added INTEGER," +
                       "date_modified INTEGER," +
                       "title TEXT NOT NULL," +
                       "title_key TEXT NOT NULL," +
                       "duration INTEGER," +
                       "artist_id INTEGER," +
                       "composer TEXT," +
                       "album_id INTEGER," +
                       "track INTEGER," +    
                       "year INTEGER CHECK(year!=0)," +
                       "is_ringtone INTEGER," +
                       "is_music INTEGER," +
                       "is_alarm INTEGER," +
                       "is_notification INTEGER" +
                       ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS artists (" +
                        "artist_id INTEGER PRIMARY KEY," +
                        "artist_key TEXT NOT NULL UNIQUE," +
                        "artist TEXT NOT NULL" +
                       ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS albums (" +
                        "album_id INTEGER PRIMARY KEY," +
                        "album_key TEXT NOT NULL UNIQUE," +
                        "album TEXT NOT NULL" +
                       ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS album_art (" +
                    "album_id INTEGER PRIMARY KEY," +
                    "_data TEXT" +
                   ");");
            recreateAudioView(db);
            db.execSQL("CREATE VIEW IF NOT EXISTS artist_info AS " +
                        "SELECT artist_id AS _id, artist, artist_key, " +
                        "COUNT(DISTINCT album) AS number_of_albums, " +
                        "COUNT(*) AS number_of_tracks FROM audio WHERE is_music=1 "+
                        "GROUP BY artist_key;");
            db.execSQL("CREATE VIEW IF NOT EXISTS album_info AS " +
                    "SELECT audio.album_id AS _id, album, album_key, " +
                    "MIN(year) AS minyear, " +
                    "MAX(year) AS maxyear, artist, artist_id, artist_key, " +
                    "count(*) AS " + MediaStore.Audio.Albums.NUMBER_OF_SONGS +
                    ",album_art._data AS album_art" +
                    " FROM audio LEFT OUTER JOIN album_art ON audio.album_id=album_art.album_id" +
                    " WHERE is_music=1 GROUP BY audio.album_id;");
            db.execSQL("CREATE VIEW IF NOT EXISTS artists_albums_map AS " +
                    "SELECT DISTINCT artist_id, album_id FROM audio_meta;");
            if (!internal) {
                db.execSQL("CREATE TRIGGER IF NOT EXISTS audio_meta_cleanup DELETE ON audio_meta " +
                           "BEGIN " +
                               "DELETE FROM audio_genres_map WHERE audio_id = old._id;" +
                               "DELETE FROM audio_playlists_map WHERE audio_id = old._id;" +
                           "END");
                db.execSQL("CREATE TABLE IF NOT EXISTS audio_genres (" +
                           "_id INTEGER PRIMARY KEY," +
                           "name TEXT NOT NULL" +
                           ");");
                db.execSQL("CREATE TABLE IF NOT EXISTS audio_genres_map (" +
                           "_id INTEGER PRIMARY KEY," +
                           "audio_id INTEGER NOT NULL," +
                           "genre_id INTEGER NOT NULL" +
                           ");");
                db.execSQL("CREATE TRIGGER IF NOT EXISTS audio_genres_cleanup DELETE ON audio_genres " +
                           "BEGIN " +
                               "DELETE FROM audio_genres_map WHERE genre_id = old._id;" +
                           "END");
                db.execSQL("CREATE TABLE IF NOT EXISTS audio_playlists (" +
                           "_id INTEGER PRIMARY KEY," +
                           "_data TEXT," +  
                           "name TEXT NOT NULL," +
                           "date_added INTEGER," +
                           "date_modified INTEGER" +
                           ");");
                db.execSQL("CREATE TABLE IF NOT EXISTS audio_playlists_map (" +
                           "_id INTEGER PRIMARY KEY," +
                           "audio_id INTEGER NOT NULL," +
                           "playlist_id INTEGER NOT NULL," +
                           "play_order INTEGER NOT NULL" +
                           ");");
                db.execSQL("CREATE TRIGGER IF NOT EXISTS audio_playlists_cleanup DELETE ON audio_playlists " +
                           "BEGIN " +
                               "DELETE FROM audio_playlists_map WHERE playlist_id = old._id;" +
                               "SELECT _DELETE_FILE(old._data);" +
                           "END");
                db.execSQL("CREATE TRIGGER IF NOT EXISTS albumart_cleanup1 DELETE ON albums " +
                        "BEGIN " +
                            "DELETE FROM album_art WHERE album_id = old.album_id;" +
                        "END");
                db.execSQL("CREATE TRIGGER IF NOT EXISTS albumart_cleanup2 DELETE ON album_art " +
                        "BEGIN " +
                            "SELECT _DELETE_FILE(old._data);" +
                        "END");
            }
            db.execSQL("CREATE TABLE IF NOT EXISTS video (" +
                       "_id INTEGER PRIMARY KEY," +
                       "_data TEXT NOT NULL," +
                       "_display_name TEXT," +
                       "_size INTEGER," +
                       "mime_type TEXT," +
                       "date_added INTEGER," +
                       "date_modified INTEGER," +
                       "title TEXT," +
                       "duration INTEGER," +
                       "artist TEXT," +
                       "album TEXT," +
                       "resolution TEXT," +
                       "description TEXT," +
                       "isprivate INTEGER," +   
                       "tags TEXT," +           
                       "category TEXT," +       
                       "language TEXT," +       
                       "mini_thumb_data TEXT," +
                       "latitude DOUBLE," +
                       "longitude DOUBLE," +
                       "datetaken INTEGER," +
                       "mini_thumb_magic INTEGER" +
                       ");");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS video_cleanup DELETE ON video " +
                    "BEGIN " +
                        "SELECT _DELETE_FILE(old._data);" +
                    "END");
        }
        if (fromVersion < 64) {
            db.execSQL("CREATE INDEX IF NOT EXISTS sort_index on images(datetaken ASC, _id ASC);");
        }
        if (fromVersion < 65) {
            db.execSQL("CREATE INDEX IF NOT EXISTS titlekey_index on audio_meta(title_key);");
        }
        if (fromVersion < 67) {
            db.execSQL("CREATE INDEX IF NOT EXISTS albumkey_index on albums(album_key);");
            db.execSQL("CREATE INDEX IF NOT EXISTS artistkey_index on artists(artist_key);");
        }
        if (fromVersion < 68) {
            db.execSQL("ALTER TABLE video ADD COLUMN bucket_id TEXT;");
            db.execSQL("ALTER TABLE video ADD COLUMN bucket_display_name TEXT");
        }
        if (fromVersion < 69) {
            updateDisplayName(db, "images");
        }
        if (fromVersion < 70) {
            db.execSQL("ALTER TABLE video ADD COLUMN bookmark INTEGER;");
        }
        if (fromVersion < 71) {
            db.execSQL("UPDATE audio_meta SET date_modified=0 WHERE _id IN (" +
                    "SELECT _id FROM audio where mime_type='audio/mp4' AND " +
                    "artist='" + MediaStore.UNKNOWN_STRING + "' AND " +
                    "album='" + MediaStore.UNKNOWN_STRING + "'" +
                    ");");
        }
        if (fromVersion < 72) {
            db.execSQL("ALTER TABLE audio_meta ADD COLUMN is_podcast INTEGER;");
            db.execSQL("UPDATE audio_meta SET is_podcast=1 WHERE _data LIKE '%/podcasts/%';");
            db.execSQL("UPDATE audio_meta SET is_music=0 WHERE is_podcast=1" +
                    " AND _data NOT LIKE '%/music/%';");
            db.execSQL("ALTER TABLE audio_meta ADD COLUMN bookmark INTEGER;");
            recreateAudioView(db);
        }
        if (fromVersion < 73) {
            db.execSQL("UPDATE audio_meta SET is_music=1 WHERE is_music=0 AND " +
                    "_data LIKE '%/music/%';");
            db.execSQL("UPDATE audio_meta SET is_ringtone=1 WHERE is_ringtone=0 AND " +
                    "_data LIKE '%/ringtones/%';");
            db.execSQL("UPDATE audio_meta SET is_notification=1 WHERE is_notification=0 AND " +
                    "_data LIKE '%/notifications/%';");
            db.execSQL("UPDATE audio_meta SET is_alarm=1 WHERE is_alarm=0 AND " +
                    "_data LIKE '%/alarms/%';");
            db.execSQL("UPDATE audio_meta SET is_podcast=1 WHERE is_podcast=0 AND " +
                    "_data LIKE '%/podcasts/%';");
        }
        if (fromVersion < 74) {
            db.execSQL("CREATE VIEW IF NOT EXISTS searchhelpertitle AS SELECT * FROM audio " +
                    "ORDER BY title_key;");
            db.execSQL("CREATE VIEW IF NOT EXISTS search AS " +
                    "SELECT _id," +
                    "'artist' AS mime_type," +
                    "artist," +
                    "NULL AS album," +
                    "NULL AS title," +
                    "artist AS text1," +
                    "NULL AS text2," +
                    "number_of_albums AS data1," +
                    "number_of_tracks AS data2," +
                    "artist_key AS match," +
                    "'content:
                    "1 AS grouporder " +
                    "FROM artist_info WHERE (artist!='" + MediaStore.UNKNOWN_STRING + "') " +
                "UNION ALL " +
                    "SELECT _id," +
                    "'album' AS mime_type," +
                    "artist," +
                    "album," +
                    "NULL AS title," +
                    "album AS text1," +
                    "artist AS text2," +
                    "NULL AS data1," +
                    "NULL AS data2," +
                    "artist_key||' '||album_key AS match," +
                    "'content:
                    "2 AS grouporder " +
                    "FROM album_info WHERE (album!='" + MediaStore.UNKNOWN_STRING + "') " +
                "UNION ALL " +
                    "SELECT searchhelpertitle._id AS _id," +
                    "mime_type," +
                    "artist," +
                    "album," +
                    "title," +
                    "title AS text1," +
                    "artist AS text2," +
                    "NULL AS data1," +
                    "NULL AS data2," +
                    "artist_key||' '||album_key||' '||title_key AS match," +
                    "'content:
                    "suggest_intent_data," +
                    "3 AS grouporder " +
                    "FROM searchhelpertitle WHERE (title != '') "
                    );
        }
        if (fromVersion < 75) {
            db.execSQL("UPDATE audio_meta SET date_modified=0;");
            db.execSQL("DELETE FROM albums");
        }
        if (fromVersion < 76) {
            db.execSQL("UPDATE audio_meta SET title_key=" +
                    "REPLACE(title_key,x'081D08C29F081D',x'081D') " +
                    "WHERE title_key LIKE '%'||x'081D08C29F081D'||'%';");
            db.execSQL("UPDATE albums SET album_key=" +
                    "REPLACE(album_key,x'081D08C29F081D',x'081D') " +
                    "WHERE album_key LIKE '%'||x'081D08C29F081D'||'%';");
            db.execSQL("UPDATE artists SET artist_key=" +
                    "REPLACE(artist_key,x'081D08C29F081D',x'081D') " +
                    "WHERE artist_key LIKE '%'||x'081D08C29F081D'||'%';");
        }
        if (fromVersion < 77) {
            db.execSQL("CREATE TABLE IF NOT EXISTS videothumbnails (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT," +
                    "video_id INTEGER," +
                    "kind INTEGER," +
                    "width INTEGER," +
                    "height INTEGER" +
                    ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS video_id_index on videothumbnails(video_id);");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS videothumbnails_cleanup DELETE ON videothumbnails " +
                    "BEGIN " +
                        "SELECT _DELETE_FILE(old._data);" +
                    "END");
        }
        if (fromVersion < 78) {
            db.execSQL("UPDATE video SET date_modified=0;");
        }
        if (fromVersion < 79) {
            String storageroot = Environment.getExternalStorageDirectory().getAbsolutePath();
            String oldthumbspath = storageroot + "/albumthumbs";
            String newthumbspath = storageroot + "/" + ALBUM_THUMB_FOLDER;
            File thumbsfolder = new File(oldthumbspath);
            if (thumbsfolder.exists()) {
                File newthumbsfolder = new File(newthumbspath);
                newthumbsfolder.getParentFile().mkdirs();
                if(thumbsfolder.renameTo(newthumbsfolder)) {
                    db.execSQL("UPDATE album_art SET _data=REPLACE(_data, '" +
                            oldthumbspath + "','" + newthumbspath + "');");
                }
            }
        }
        if (fromVersion < 80) {
            db.execSQL("UPDATE images SET date_modified=0;");
        }
        if (fromVersion < 81 && !internal) {
            db.execSQL("UPDATE audio_playlists SET _data='
            db.execSQL("UPDATE images SET _data='
            db.execSQL("UPDATE video SET _data='
            db.execSQL("UPDATE videothumbnails SET _data='
            db.execSQL("UPDATE thumbnails SET _data='
            db.execSQL("UPDATE album_art SET _data='
            db.execSQL("UPDATE audio_meta SET _data='
            db.execSQL("DELETE FROM audio_playlists WHERE _data IS '
            db.execSQL("DELETE FROM images WHERE _data IS '
            db.execSQL("DELETE FROM video WHERE _data IS '
            db.execSQL("DELETE FROM videothumbnails WHERE _data IS '
            db.execSQL("DELETE FROM thumbnails WHERE _data IS '
            db.execSQL("DELETE FROM audio_meta WHERE _data  IS '
            db.execSQL("DELETE FROM album_art WHERE _data  IS '
            db.execSQL("UPDATE audio_meta" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE audio_playlists" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE images" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE video" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE videothumbnails" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE thumbnails" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("UPDATE album_art" +
                    " SET _data='/mnt/sdcard'||SUBSTR(_data,8) WHERE _data LIKE '/sdcard/%';");
            db.execSQL("DELETE from albums");
            db.execSQL("DELETE from artists");
            db.execSQL("UPDATE audio_meta SET date_modified=0;");
        }
        if (fromVersion < 82) {
            db.execSQL("DROP VIEW IF EXISTS artist_info");
            db.execSQL("CREATE VIEW IF NOT EXISTS artist_info AS " +
                        "SELECT artist_id AS _id, artist, artist_key, " +
                        "COUNT(DISTINCT album_key) AS number_of_albums, " +
                        "COUNT(*) AS number_of_tracks FROM audio WHERE is_music=1 "+
                        "GROUP BY artist_key;");
        }
        if (fromVersion < 87) {
            db.execSQL("CREATE INDEX IF NOT EXISTS title_idx on audio_meta(title);");
            db.execSQL("CREATE INDEX IF NOT EXISTS artist_idx on artists(artist);");
            db.execSQL("CREATE INDEX IF NOT EXISTS album_idx on albums(album);");
        }
        if (fromVersion < 88) {
            db.execSQL("DROP TRIGGER IF EXISTS albums_update1;");
            db.execSQL("DROP TRIGGER IF EXISTS albums_update2;");
            db.execSQL("DROP TRIGGER IF EXISTS albums_update3;");
            db.execSQL("DROP TRIGGER IF EXISTS albums_update4;");
            db.execSQL("DROP TRIGGER IF EXISTS artist_update1;");
            db.execSQL("DROP TRIGGER IF EXISTS artist_update2;");
            db.execSQL("DROP TRIGGER IF EXISTS artist_update3;");
            db.execSQL("DROP TRIGGER IF EXISTS artist_update4;");
            db.execSQL("DROP VIEw IF EXISTS album_artists;");
            db.execSQL("CREATE INDEX IF NOT EXISTS album_id_idx on audio_meta(album_id);");
            db.execSQL("CREATE INDEX IF NOT EXISTS artist_id_idx on audio_meta(artist_id);");
            db.execSQL("CREATE VIEW IF NOT EXISTS artists_albums_map AS " +
                    "SELECT DISTINCT artist_id, album_id FROM audio_meta;");
        }
        if (fromVersion < 89) {
            updateBucketNames(db, "images");
            updateBucketNames(db, "video");
        }
        sanityCheck(db, fromVersion);
    }
    private static void sanityCheck(SQLiteDatabase db, int fromVersion) {
        Cursor c1 = db.query("audio_meta", new String[] {"count(*)"},
                null, null, null, null, null);
        Cursor c2 = db.query("audio_meta", new String[] {"count(distinct _data)"},
                null, null, null, null, null);
        c1.moveToFirst();
        c2.moveToFirst();
        int num1 = c1.getInt(0);
        int num2 = c2.getInt(0);
        c1.close();
        c2.close();
        if (num1 != num2) {
            Log.e(TAG, "audio_meta._data column is not unique while upgrading" +
                    " from schema " +fromVersion + " : " + num1 +"/" + num2);
            db.execSQL("DELETE FROM audio_meta;");
        }
    }
    private static void recreateAudioView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS audio");
        db.execSQL("DROP TRIGGER IF EXISTS audio_delete");
        db.execSQL("CREATE VIEW IF NOT EXISTS audio as SELECT * FROM audio_meta " +
                    "LEFT OUTER JOIN artists ON audio_meta.artist_id=artists.artist_id " +
                    "LEFT OUTER JOIN albums ON audio_meta.album_id=albums.album_id;");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS audio_delete INSTEAD OF DELETE ON audio " +
                "BEGIN " +
                    "DELETE from audio_meta where _id=old._id;" +
                    "DELETE from audio_playlists_map where audio_id=old._id;" +
                    "DELETE from audio_genres_map where audio_id=old._id;" +
                "END");
    }
    private static void updateBucketNames(SQLiteDatabase db, String tableName) {
        db.beginTransaction();
        try {
            String[] columns = {BaseColumns._ID, MediaColumns.DATA};
            Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
            try {
                final int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                final int dataColumnIndex = cursor.getColumnIndex(MediaColumns.DATA);
                while (cursor.moveToNext()) {
                    String data = cursor.getString(dataColumnIndex);
                    ContentValues values = new ContentValues();
                    computeBucketValues(data, values);
                    int rowId = cursor.getInt(idColumnIndex);
                    db.update(tableName, values, "_id=" + rowId, null);
                }
            } finally {
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    private static void updateDisplayName(SQLiteDatabase db, String tableName) {
        db.beginTransaction();
        try {
            String[] columns = {BaseColumns._ID, MediaColumns.DATA, MediaColumns.DISPLAY_NAME};
            Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
            try {
                final int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                final int dataColumnIndex = cursor.getColumnIndex(MediaColumns.DATA);
                final int displayNameIndex = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
                ContentValues values = new ContentValues();
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(displayNameIndex);
                    if (displayName == null) {
                        String data = cursor.getString(dataColumnIndex);
                        values.clear();
                        computeDisplayName(data, values);
                        int rowId = cursor.getInt(idColumnIndex);
                        db.update(tableName, values, "_id=" + rowId, null);
                    }
                }
            } finally {
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    private static void computeBucketValues(String data, ContentValues values) {
        File parentFile = new File(data).getParentFile();
        if (parentFile == null) {
            parentFile = new File("/");
        }
        String path = parentFile.toString().toLowerCase();
        String name = parentFile.getName();
        values.put(ImageColumns.BUCKET_ID, path.hashCode());
        values.put(ImageColumns.BUCKET_DISPLAY_NAME, name);
    }
    private static void computeDisplayName(String data, ContentValues values) {
        String s = (data == null ? "" : data.toString());
        int idx = s.lastIndexOf('/');
        if (idx >= 0) {
            s = s.substring(idx + 1);
        }
        values.put("_display_name", s);
    }
    private static void computeTakenTime(ContentValues values) {
        if (! values.containsKey(Images.Media.DATE_TAKEN)) {
            Long lastModified = values.getAsLong(MediaColumns.DATE_MODIFIED);
            if (lastModified != null) {
                values.put(Images.Media.DATE_TAKEN, lastModified * 1000);
            }
        }
    }
    private boolean waitForThumbnailReady(Uri origUri) {
        Cursor c = this.query(origUri, new String[] { ImageColumns._ID, ImageColumns.DATA,
                ImageColumns.MINI_THUMB_MAGIC}, null, null, null);
        if (c == null) return false;
        boolean result = false;
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            String path = c.getString(1);
            long magic = c.getLong(2);
            MediaThumbRequest req = requestMediaThumbnail(path, origUri,
                    MediaThumbRequest.PRIORITY_HIGH, magic);
            if (req == null) {
                return false;
            }
            synchronized (req) {
                try {
                    while (req.mState == MediaThumbRequest.State.WAIT) {
                        req.wait();
                    }
                } catch (InterruptedException e) {
                    Log.w(TAG, e);
                }
                if (req.mState == MediaThumbRequest.State.DONE) {
                    result = true;
                }
            }
        }
        c.close();
        return result;
    }
    private boolean matchThumbRequest(MediaThumbRequest req, int pid, long id, long gid,
            boolean isVideo) {
        boolean cancelAllOrigId = (id == -1);
        boolean cancelAllGroupId = (gid == -1);
        return (req.mCallingPid == pid) &&
                (cancelAllGroupId || req.mGroupId == gid) &&
                (cancelAllOrigId || req.mOrigId == id) &&
                (req.mIsVideo == isVideo);
    }
    private boolean queryThumbnail(SQLiteQueryBuilder qb, Uri uri, String table,
            String column, boolean hasThumbnailId) {
        qb.setTables(table);
        if (hasThumbnailId) {
            qb.appendWhere("_id = " + uri.getPathSegments().get(3));
            return true;
        }
        String origId = uri.getQueryParameter("orig_id");
        if (origId == null) {
            return true;
        }
        boolean needBlocking = "1".equals(uri.getQueryParameter("blocking"));
        boolean cancelRequest = "1".equals(uri.getQueryParameter("cancel"));
        Uri origUri = uri.buildUpon().encodedPath(
                uri.getPath().replaceFirst("thumbnails", "media"))
                .appendPath(origId).build();
        if (needBlocking && !waitForThumbnailReady(origUri)) {
            Log.w(TAG, "original media doesn't exist or it's canceled.");
            return false;
        } else if (cancelRequest) {
            String groupId = uri.getQueryParameter("group_id");
            boolean isVideo = "video".equals(uri.getPathSegments().get(1));
            int pid = Binder.getCallingPid();
            long id = -1;
            long gid = -1;
            try {
                id = Long.parseLong(origId);
                gid = Long.parseLong(groupId);
            } catch (NumberFormatException ex) {
                return false;
            }
            synchronized (mMediaThumbQueue) {
                if (mCurrentThumbRequest != null &&
                        matchThumbRequest(mCurrentThumbRequest, pid, id, gid, isVideo)) {
                    synchronized (mCurrentThumbRequest) {
                        mCurrentThumbRequest.mState = MediaThumbRequest.State.CANCEL;
                        mCurrentThumbRequest.notifyAll();
                    }
                }
                for (MediaThumbRequest mtq : mMediaThumbQueue) {
                    if (matchThumbRequest(mtq, pid, id, gid, isVideo)) {
                        synchronized (mtq) {
                            mtq.mState = MediaThumbRequest.State.CANCEL;
                            mtq.notifyAll();
                        }
                        mMediaThumbQueue.remove(mtq);
                    }
                }
            }
        }
        if (origId != null) {
            qb.appendWhere(column + " = " + origId);
        }
        return true;
    }
    @SuppressWarnings("fallthrough")
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        int table = URI_MATCHER.match(uri);
        if (table == MEDIA_SCANNER) {
            if (mMediaScannerVolume == null) {
                return null;
            } else {
                MatrixCursor c = new MatrixCursor(new String[] {MediaStore.MEDIA_SCANNER_VOLUME});
                c.addRow(new String[] {mMediaScannerVolume});
                return c;
            }
        }
        if (table == FS_ID) {
            MatrixCursor c = new MatrixCursor(new String[] {"fsid"});
            c.addRow(new Integer[] {mVolumeId});
            return c;
        }
        String groupBy = null;
        DatabaseHelper database = getDatabaseForUri(uri);
        if (database == null) {
            return null;
        }
        SQLiteDatabase db = database.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = uri.getQueryParameter("limit");
        boolean hasThumbnailId = false;
        switch (table) {
            case IMAGES_MEDIA:
                qb.setTables("images");
                if (uri.getQueryParameter("distinct") != null)
                    qb.setDistinct(true);
                break;
            case IMAGES_MEDIA_ID:
                qb.setTables("images");
                if (uri.getQueryParameter("distinct") != null)
                    qb.setDistinct(true);
                qb.appendWhere("_id = " + uri.getPathSegments().get(3));
                break;
            case IMAGES_THUMBNAILS_ID:
                hasThumbnailId = true;
            case IMAGES_THUMBNAILS:
                if (!queryThumbnail(qb, uri, "thumbnails", "image_id", hasThumbnailId)) {
                    return null;
                }
                break;
            case AUDIO_MEDIA:
                if (projectionIn != null && projectionIn.length == 1 &&  selectionArgs == null
                        && (selection == null || selection.equalsIgnoreCase("is_music=1")
                          || selection.equalsIgnoreCase("is_podcast=1") )
                        && projectionIn[0].equalsIgnoreCase("count(*)") ) {
                    qb.setTables("audio_meta");
                } else {
                    qb.setTables("audio");
                }
                break;
            case AUDIO_MEDIA_ID:
                qb.setTables("audio");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_MEDIA_ID_GENRES:
                qb.setTables("audio_genres");
                qb.appendWhere("_id IN (SELECT genre_id FROM " +
                        "audio_genres_map WHERE audio_id = " +
                        uri.getPathSegments().get(3) + ")");
                break;
            case AUDIO_MEDIA_ID_GENRES_ID:
                qb.setTables("audio_genres");
                qb.appendWhere("_id=" + uri.getPathSegments().get(5));
                break;
            case AUDIO_MEDIA_ID_PLAYLISTS:
                qb.setTables("audio_playlists");
                qb.appendWhere("_id IN (SELECT playlist_id FROM " +
                        "audio_playlists_map WHERE audio_id = " +
                        uri.getPathSegments().get(3) + ")");
                break;
            case AUDIO_MEDIA_ID_PLAYLISTS_ID:
                qb.setTables("audio_playlists");
                qb.appendWhere("_id=" + uri.getPathSegments().get(5));
                break;
            case AUDIO_GENRES:
                qb.setTables("audio_genres");
                break;
            case AUDIO_GENRES_ID:
                qb.setTables("audio_genres");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_GENRES_ID_MEMBERS:
                qb.setTables("audio");
                qb.appendWhere("_id IN (SELECT audio_id FROM " +
                        "audio_genres_map WHERE genre_id = " +
                        uri.getPathSegments().get(3) + ")");
                break;
            case AUDIO_GENRES_ID_MEMBERS_ID:
                qb.setTables("audio");
                qb.appendWhere("_id=" + uri.getPathSegments().get(5));
                break;
            case AUDIO_PLAYLISTS:
                qb.setTables("audio_playlists");
                break;
            case AUDIO_PLAYLISTS_ID:
                qb.setTables("audio_playlists");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_PLAYLISTS_ID_MEMBERS:
                if (projectionIn != null) {
                    for (int i = 0; i < projectionIn.length; i++) {
                        if (projectionIn[i].equals("_id")) {
                            projectionIn[i] = "audio_playlists_map._id AS _id";
                        }
                    }
                }
                qb.setTables("audio_playlists_map, audio");
                qb.appendWhere("audio._id = audio_id AND playlist_id = "
                        + uri.getPathSegments().get(3));
                break;
            case AUDIO_PLAYLISTS_ID_MEMBERS_ID:
                qb.setTables("audio");
                qb.appendWhere("_id=" + uri.getPathSegments().get(5));
                break;
            case VIDEO_MEDIA:
                qb.setTables("video");
                break;
            case VIDEO_MEDIA_ID:
                qb.setTables("video");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case VIDEO_THUMBNAILS_ID:
                hasThumbnailId = true;
            case VIDEO_THUMBNAILS:
                if (!queryThumbnail(qb, uri, "videothumbnails", "video_id", hasThumbnailId)) {
                    return null;
                }
                break;
            case AUDIO_ARTISTS:
                if (projectionIn != null && projectionIn.length == 1 &&  selectionArgs == null
                        && (selection == null || selection.length() == 0)
                        && projectionIn[0].equalsIgnoreCase("count(*)") ) {
                    qb.setTables("audio_meta");
                    projectionIn[0] = "count(distinct artist_id)";
                    qb.appendWhere("is_music=1");
                } else {
                    qb.setTables("artist_info");
                }
                break;
            case AUDIO_ARTISTS_ID:
                qb.setTables("artist_info");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_ARTISTS_ID_ALBUMS:
                String aid = uri.getPathSegments().get(3);
                qb.setTables("audio LEFT OUTER JOIN album_art ON" +
                        " audio.album_id=album_art.album_id");
                qb.appendWhere("is_music=1 AND audio.album_id IN (SELECT album_id FROM " +
                        "artists_albums_map WHERE artist_id = " +
                         aid + ")");
                groupBy = "audio.album_id";
                sArtistAlbumsMap.put(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST,
                        "count(CASE WHEN artist_id==" + aid + " THEN 'foo' ELSE NULL END) AS " +
                        MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
                qb.setProjectionMap(sArtistAlbumsMap);
                break;
            case AUDIO_ALBUMS:
                if (projectionIn != null && projectionIn.length == 1 &&  selectionArgs == null
                        && (selection == null || selection.length() == 0)
                        && projectionIn[0].equalsIgnoreCase("count(*)") ) {
                    qb.setTables("audio_meta");
                    projectionIn[0] = "count(distinct album_id)";
                    qb.appendWhere("is_music=1");
                } else {
                    qb.setTables("album_info");
                }
                break;
            case AUDIO_ALBUMS_ID:
                qb.setTables("album_info");
                qb.appendWhere("_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_ALBUMART_ID:
                qb.setTables("album_art");
                qb.appendWhere("album_id=" + uri.getPathSegments().get(3));
                break;
            case AUDIO_SEARCH_LEGACY:
                Log.w(TAG, "Legacy media search Uri used. Please update your code.");
            case AUDIO_SEARCH_FANCY:
            case AUDIO_SEARCH_BASIC:
                return doAudioSearch(db, qb, uri, projectionIn, selection, selectionArgs, sort,
                        table, limit);
            default:
                throw new IllegalStateException("Unknown URL: " + uri.toString());
        }
        Cursor c = qb.query(db, projectionIn, selection,
                selectionArgs, groupBy, null, sort, limit);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }
    private Cursor doAudioSearch(SQLiteDatabase db, SQLiteQueryBuilder qb,
            Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sort, int mode,
            String limit) {
        String mSearchString = uri.getPath().endsWith("/") ? "" : uri.getLastPathSegment();
        mSearchString = mSearchString.replaceAll("  ", " ").trim().toLowerCase();
        String [] searchWords = mSearchString.length() > 0 ?
                mSearchString.split(" ") : new String[0];
        String [] wildcardWords = new String[searchWords.length];
        Collator col = Collator.getInstance();
        col.setStrength(Collator.PRIMARY);
        int len = searchWords.length;
        for (int i = 0; i < len; i++) {
            wildcardWords[i] =
                (searchWords[i].equals("a") || searchWords[i].equals("an") ||
                        searchWords[i].equals("the")) ? "%" :
                '%' + MediaStore.Audio.keyFor(searchWords[i]) + '%';
        }
        String where = "";
        for (int i = 0; i < searchWords.length; i++) {
            if (i == 0) {
                where = "match LIKE ?";
            } else {
                where += " AND match LIKE ?";
            }
        }
        qb.setTables("search");
        String [] cols;
        if (mode == AUDIO_SEARCH_FANCY) {
            cols = mSearchColsFancy;
        } else if (mode == AUDIO_SEARCH_BASIC) {
            cols = mSearchColsBasic;
        } else {
            cols = mSearchColsLegacy;
        }
        return qb.query(db, cols, where, wildcardWords, null, null, null, limit);
    }
    @Override
    public String getType(Uri url)
    {
        switch (URI_MATCHER.match(url)) {
            case IMAGES_MEDIA_ID:
            case AUDIO_MEDIA_ID:
            case AUDIO_GENRES_ID_MEMBERS_ID:
            case AUDIO_PLAYLISTS_ID_MEMBERS_ID:
            case VIDEO_MEDIA_ID:
                Cursor c = null;
                try {
                    c = query(url, MIME_TYPE_PROJECTION, null, null, null);
                    if (c != null && c.getCount() == 1) {
                        c.moveToFirst();
                        String mimeType = c.getString(1);
                        c.deactivate();
                        return mimeType;
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
                break;
            case IMAGES_MEDIA:
            case IMAGES_THUMBNAILS:
                return Images.Media.CONTENT_TYPE;
            case IMAGES_THUMBNAILS_ID:
                return "image/jpeg";
            case AUDIO_MEDIA:
            case AUDIO_GENRES_ID_MEMBERS:
            case AUDIO_PLAYLISTS_ID_MEMBERS:
                return Audio.Media.CONTENT_TYPE;
            case AUDIO_GENRES:
            case AUDIO_MEDIA_ID_GENRES:
                return Audio.Genres.CONTENT_TYPE;
            case AUDIO_GENRES_ID:
            case AUDIO_MEDIA_ID_GENRES_ID:
                return Audio.Genres.ENTRY_CONTENT_TYPE;
            case AUDIO_PLAYLISTS:
            case AUDIO_MEDIA_ID_PLAYLISTS:
                return Audio.Playlists.CONTENT_TYPE;
            case AUDIO_PLAYLISTS_ID:
            case AUDIO_MEDIA_ID_PLAYLISTS_ID:
                return Audio.Playlists.ENTRY_CONTENT_TYPE;
            case VIDEO_MEDIA:
                return Video.Media.CONTENT_TYPE;
        }
        throw new IllegalStateException("Unknown URL");
    }
    private ContentValues ensureFile(boolean internal, ContentValues initialValues,
            String preferredExtension, String directoryName) {
        ContentValues values;
        String file = initialValues.getAsString("_data");
        if (TextUtils.isEmpty(file)) {
            file = generateFileName(internal, preferredExtension, directoryName);
            values = new ContentValues(initialValues);
            values.put("_data", file);
        } else {
            values = initialValues;
        }
        if (!ensureFileExists(file)) {
            throw new IllegalStateException("Unable to create new file: " + file);
        }
        return values;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues values[]) {
        int match = URI_MATCHER.match(uri);
        if (match == VOLUMES) {
            return super.bulkInsert(uri, values);
        }
        DatabaseHelper database = getDatabaseForUri(uri);
        if (database == null) {
            throw new UnsupportedOperationException(
                    "Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        if (match == AUDIO_PLAYLISTS_ID || match == AUDIO_PLAYLISTS_ID_MEMBERS) {
            return playlistBulkInsert(db, uri, values);
        }
        db.beginTransaction();
        int numInserted = 0;
        try {
            int len = values.length;
            for (int i = 0; i < len; i++) {
                insertInternal(uri, values[i]);
            }
            numInserted = len;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return numInserted;
    }
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        Uri newUri = insertInternal(uri, initialValues);
        if (newUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newUri;
    }
    private int playlistBulkInsert(SQLiteDatabase db, Uri uri, ContentValues values[]) {
        DatabaseUtils.InsertHelper helper =
            new DatabaseUtils.InsertHelper(db, "audio_playlists_map");
        int audioidcolidx = helper.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        int playlistididx = helper.getColumnIndex(Audio.Playlists.Members.PLAYLIST_ID);
        int playorderidx = helper.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER);
        long playlistId = Long.parseLong(uri.getPathSegments().get(3));
        db.beginTransaction();
        int numInserted = 0;
        try {
            int len = values.length;
            for (int i = 0; i < len; i++) {
                helper.prepareForInsert();
                long audioid = ((Number) values[i].get(
                        MediaStore.Audio.Playlists.Members.AUDIO_ID)).longValue();
                helper.bind(audioidcolidx, audioid);
                helper.bind(playlistididx, playlistId);
                int playorder = ((Number) values[i].get(
                        MediaStore.Audio.Playlists.Members.PLAY_ORDER)).intValue();
                helper.bind(playorderidx, playorder);
                helper.execute();
            }
            numInserted = len;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            helper.close();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return numInserted;
    }
    private Uri insertInternal(Uri uri, ContentValues initialValues) {
        long rowId;
        int match = URI_MATCHER.match(uri);
        if (match == MEDIA_SCANNER) {
            mMediaScannerVolume = initialValues.getAsString(MediaStore.MEDIA_SCANNER_VOLUME);
            return MediaStore.getMediaScannerUri();
        }
        Uri newUri = null;
        DatabaseHelper database = getDatabaseForUri(uri);
        if (database == null && match != VOLUMES) {
            throw new UnsupportedOperationException(
                    "Unknown URI: " + uri);
        }
        SQLiteDatabase db = (match == VOLUMES ? null : database.getWritableDatabase());
        if (initialValues == null) {
            initialValues = new ContentValues();
        }
        switch (match) {
            case IMAGES_MEDIA: {
                ContentValues values = ensureFile(database.mInternal, initialValues, ".jpg", "DCIM/Camera");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                String data = values.getAsString(MediaColumns.DATA);
                if (! values.containsKey(MediaColumns.DISPLAY_NAME)) {
                    computeDisplayName(data, values);
                }
                computeBucketValues(data, values);
                computeTakenTime(values);
                rowId = db.insert("images", "name", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(
                            Images.Media.getContentUri(uri.getPathSegments().get(0)), rowId);
                    requestMediaThumbnail(data, newUri, MediaThumbRequest.PRIORITY_NORMAL, 0);
                }
                break;
            }
            case IMAGES_THUMBNAILS: {
                ContentValues values = ensureFile(database.mInternal, initialValues, ".jpg",
                        "DCIM/.thumbnails");
                rowId = db.insert("thumbnails", "name", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Images.Thumbnails.
                            getContentUri(uri.getPathSegments().get(0)), rowId);
                }
                break;
            }
            case VIDEO_THUMBNAILS: {
                ContentValues values = ensureFile(database.mInternal, initialValues, ".jpg",
                        "DCIM/.thumbnails");
                rowId = db.insert("videothumbnails", "name", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Video.Thumbnails.
                            getContentUri(uri.getPathSegments().get(0)), rowId);
                }
                break;
            }
            case AUDIO_MEDIA: {
                ContentValues values = new ContentValues(initialValues);
                values.remove(MediaStore.Audio.Media.ALBUM_ARTIST);
                Object so = values.get("artist");
                String s = (so == null ? "" : so.toString());
                values.remove("artist");
                long artistRowId;
                HashMap<String, Long> artistCache = database.mArtistCache;
                String path = values.getAsString("_data");
                synchronized(artistCache) {
                    Long temp = artistCache.get(s);
                    if (temp == null) {
                        artistRowId = getKeyIdForName(db, "artists", "artist_key", "artist",
                                s, s, path, 0, null, artistCache, uri);
                    } else {
                        artistRowId = temp.longValue();
                    }
                }
                String artist = s;
                so = values.get("album");
                s = (so == null ? "" : so.toString());
                values.remove("album");
                long albumRowId;
                HashMap<String, Long> albumCache = database.mAlbumCache;
                synchronized(albumCache) {
                    int albumhash = path.substring(0, path.lastIndexOf('/')).hashCode();
                    String cacheName = s + albumhash;
                    Long temp = albumCache.get(cacheName);
                    if (temp == null) {
                        albumRowId = getKeyIdForName(db, "albums", "album_key", "album",
                                s, cacheName, path, albumhash, artist, albumCache, uri);
                    } else {
                        albumRowId = temp;
                    }
                }
                values.put("artist_id", Integer.toString((int)artistRowId));
                values.put("album_id", Integer.toString((int)albumRowId));
                so = values.getAsString("title");
                s = (so == null ? "" : so.toString());
                values.put("title_key", MediaStore.Audio.keyFor(s));
                values.remove("title");
                values.put("title", s.trim());
                computeDisplayName(values.getAsString("_data"), values);
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                rowId = db.insert("audio_meta", "duration", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Audio.Media.getContentUri(uri.getPathSegments().get(0)), rowId);
                }
                break;
            }
            case AUDIO_MEDIA_ID_GENRES: {
                Long audioId = Long.parseLong(uri.getPathSegments().get(2));
                ContentValues values = new ContentValues(initialValues);
                values.put(Audio.Genres.Members.AUDIO_ID, audioId);
                rowId = db.insert("audio_genres_map", "genre_id", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(uri, rowId);
                }
                break;
            }
            case AUDIO_MEDIA_ID_PLAYLISTS: {
                Long audioId = Long.parseLong(uri.getPathSegments().get(2));
                ContentValues values = new ContentValues(initialValues);
                values.put(Audio.Playlists.Members.AUDIO_ID, audioId);
                rowId = db.insert("audio_playlists_map", "playlist_id",
                        values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(uri, rowId);
                }
                break;
            }
            case AUDIO_GENRES: {
                rowId = db.insert("audio_genres", "audio_id", initialValues);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Audio.Genres.getContentUri(uri.getPathSegments().get(0)), rowId);
                }
                break;
            }
            case AUDIO_GENRES_ID_MEMBERS: {
                Long genreId = Long.parseLong(uri.getPathSegments().get(3));
                ContentValues values = new ContentValues(initialValues);
                values.put(Audio.Genres.Members.GENRE_ID, genreId);
                rowId = db.insert("audio_genres_map", "genre_id", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(uri, rowId);
                }
                break;
            }
            case AUDIO_PLAYLISTS: {
                ContentValues values = new ContentValues(initialValues);
                values.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis() / 1000);
                rowId = db.insert("audio_playlists", "name", initialValues);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Audio.Playlists.getContentUri(uri.getPathSegments().get(0)), rowId);
                }
                break;
            }
            case AUDIO_PLAYLISTS_ID:
            case AUDIO_PLAYLISTS_ID_MEMBERS: {
                Long playlistId = Long.parseLong(uri.getPathSegments().get(3));
                ContentValues values = new ContentValues(initialValues);
                values.put(Audio.Playlists.Members.PLAYLIST_ID, playlistId);
                rowId = db.insert("audio_playlists_map", "playlist_id", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(uri, rowId);
                }
                break;
            }
            case VIDEO_MEDIA: {
                ContentValues values = ensureFile(database.mInternal, initialValues, ".3gp", "video");
                String data = values.getAsString("_data");
                computeDisplayName(data, values);
                computeBucketValues(data, values);
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                computeTakenTime(values);
                rowId = db.insert("video", "artist", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(Video.Media.getContentUri(
                            uri.getPathSegments().get(0)), rowId);
                    requestMediaThumbnail(data, newUri, MediaThumbRequest.PRIORITY_NORMAL, 0);
                }
                break;
            }
            case AUDIO_ALBUMART:
                if (database.mInternal) {
                    throw new UnsupportedOperationException("no internal album art allowed");
                }
                ContentValues values = null;
                try {
                    values = ensureFile(false, initialValues, "", ALBUM_THUMB_FOLDER);
                } catch (IllegalStateException ex) {
                    values = initialValues;
                }
                rowId = db.insert("album_art", "_data", values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(uri, rowId);
                }
                break;
            case VOLUMES:
                return attachVolume(initialValues.getAsString("name"));
            default:
                throw new UnsupportedOperationException("Invalid URI " + uri);
        }
        return newUri;
    }
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
                throws OperationApplicationException {
        DatabaseHelper ihelper = getDatabaseForUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        DatabaseHelper ehelper = getDatabaseForUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        SQLiteDatabase idb = ihelper.getWritableDatabase();
        idb.beginTransaction();
        SQLiteDatabase edb = null;
        if (ehelper != null) {
            edb = ehelper.getWritableDatabase();
            edb.beginTransaction();
        }
        try {
            ContentProviderResult[] result = super.applyBatch(operations);
            idb.setTransactionSuccessful();
            if (edb != null) {
                edb.setTransactionSuccessful();
            }
            ContentResolver res = getContext().getContentResolver();
            res.notifyChange(Uri.parse("content:
            return result;
        } finally {
            idb.endTransaction();
            if (edb != null) {
                edb.endTransaction();
            }
        }
    }
    private MediaThumbRequest requestMediaThumbnail(String path, Uri uri, int priority, long magic) {
        synchronized (mMediaThumbQueue) {
            MediaThumbRequest req = null;
            try {
                req = new MediaThumbRequest(
                        getContext().getContentResolver(), path, uri, priority, magic);
                mMediaThumbQueue.add(req);
                Message msg = mThumbHandler.obtainMessage(IMAGE_THUMB);
                msg.sendToTarget();
            } catch (Throwable t) {
                Log.w(TAG, t);
            }
            return req;
        }
    }
    private String generateFileName(boolean internal, String preferredExtension, String directoryName)
    {
        String name = String.valueOf(System.currentTimeMillis());
        if (internal) {
            throw new UnsupportedOperationException("Writing to internal storage is not supported.");
        } else {
            return Environment.getExternalStorageDirectory()
                + "/" + directoryName + "/" + name + preferredExtension;
        }
    }
    private boolean ensureFileExists(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            int secondSlash = path.indexOf('/', 1);
            if (secondSlash < 1) return false;
            String directoryPath = path.substring(0, secondSlash);
            File directory = new File(directoryPath);
            if (!directory.exists())
                return false;
            file.getParentFile().mkdirs();
            try {
                return file.createNewFile();
            } catch(IOException ioe) {
                Log.e(TAG, "File creation failed", ioe);
            }
            return false;
        }
    }
    private static final class GetTableAndWhereOutParameter {
        public String table;
        public String where;
    }
    static final GetTableAndWhereOutParameter sGetTableAndWhereParam =
            new GetTableAndWhereOutParameter();
    private void getTableAndWhere(Uri uri, int match, String userWhere,
            GetTableAndWhereOutParameter out) {
        String where = null;
        switch (match) {
            case IMAGES_MEDIA:
                out.table = "images";
                break;
            case IMAGES_MEDIA_ID:
                out.table = "images";
                where = "_id = " + uri.getPathSegments().get(3);
                break;
            case IMAGES_THUMBNAILS_ID:
                where = "_id=" + uri.getPathSegments().get(3);
            case IMAGES_THUMBNAILS:
                out.table = "thumbnails";
                break;
            case AUDIO_MEDIA:
                out.table = "audio";
                break;
            case AUDIO_MEDIA_ID:
                out.table = "audio";
                where = "_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_MEDIA_ID_GENRES:
                out.table = "audio_genres";
                where = "audio_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_MEDIA_ID_GENRES_ID:
                out.table = "audio_genres";
                where = "audio_id=" + uri.getPathSegments().get(3) +
                        " AND genre_id=" + uri.getPathSegments().get(5);
               break;
            case AUDIO_MEDIA_ID_PLAYLISTS:
                out.table = "audio_playlists";
                where = "audio_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_MEDIA_ID_PLAYLISTS_ID:
                out.table = "audio_playlists";
                where = "audio_id=" + uri.getPathSegments().get(3) +
                        " AND playlists_id=" + uri.getPathSegments().get(5);
                break;
            case AUDIO_GENRES:
                out.table = "audio_genres";
                break;
            case AUDIO_GENRES_ID:
                out.table = "audio_genres";
                where = "_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_GENRES_ID_MEMBERS:
                out.table = "audio_genres";
                where = "genre_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_GENRES_ID_MEMBERS_ID:
                out.table = "audio_genres";
                where = "genre_id=" + uri.getPathSegments().get(3) +
                        " AND audio_id =" + uri.getPathSegments().get(5);
                break;
            case AUDIO_PLAYLISTS:
                out.table = "audio_playlists";
                break;
            case AUDIO_PLAYLISTS_ID:
                out.table = "audio_playlists";
                where = "_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_PLAYLISTS_ID_MEMBERS:
                out.table = "audio_playlists_map";
                where = "playlist_id=" + uri.getPathSegments().get(3);
                break;
            case AUDIO_PLAYLISTS_ID_MEMBERS_ID:
                out.table = "audio_playlists_map";
                where = "playlist_id=" + uri.getPathSegments().get(3) +
                        " AND _id=" + uri.getPathSegments().get(5);
                break;
            case AUDIO_ALBUMART_ID:
                out.table = "album_art";
                where = "album_id=" + uri.getPathSegments().get(3);
                break;
            case VIDEO_MEDIA:
                out.table = "video";
                break;
            case VIDEO_MEDIA_ID:
                out.table = "video";
                where = "_id=" + uri.getPathSegments().get(3);
                break;
            case VIDEO_THUMBNAILS_ID:
                where = "_id=" + uri.getPathSegments().get(3);
            case VIDEO_THUMBNAILS:
                out.table = "videothumbnails";
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unknown or unsupported URL: " + uri.toString());
        }
        if (!TextUtils.isEmpty(userWhere)) {
            if (!TextUtils.isEmpty(where)) {
                out.where = where + " AND (" + userWhere + ")";
            } else {
                out.where = userWhere;
            }
        } else {
            out.where = where;
        }
    }
    @Override
    public int delete(Uri uri, String userWhere, String[] whereArgs) {
        int count;
        int match = URI_MATCHER.match(uri);
        if (match == MEDIA_SCANNER) {
            if (mMediaScannerVolume == null) {
                return 0;
            }
            mMediaScannerVolume = null;
            return 1;
        }
        if (match != VOLUMES_ID) {
            DatabaseHelper database = getDatabaseForUri(uri);
            if (database == null) {
                throw new UnsupportedOperationException(
                        "Unknown URI: " + uri);
            }
            SQLiteDatabase db = database.getWritableDatabase();
            synchronized (sGetTableAndWhereParam) {
                getTableAndWhere(uri, match, userWhere, sGetTableAndWhereParam);
                switch (match) {
                    case AUDIO_MEDIA:
                    case AUDIO_MEDIA_ID:
                        count = db.delete("audio_meta",
                                sGetTableAndWhereParam.where, whereArgs);
                        break;
                    default:
                        count = db.delete(sGetTableAndWhereParam.table,
                                sGetTableAndWhereParam.where, whereArgs);
                        break;
                }
                getContext().getContentResolver().notifyChange(uri, null);
            }
        } else {
            detachVolume(uri);
            count = 1;
        }
        return count;
    }
    @Override
    public int update(Uri uri, ContentValues initialValues, String userWhere,
            String[] whereArgs) {
        int count;
        int match = URI_MATCHER.match(uri);
        DatabaseHelper database = getDatabaseForUri(uri);
        if (database == null) {
            throw new UnsupportedOperationException(
                    "Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        synchronized (sGetTableAndWhereParam) {
            getTableAndWhere(uri, match, userWhere, sGetTableAndWhereParam);
            switch (match) {
                case AUDIO_MEDIA:
                case AUDIO_MEDIA_ID:
                    {
                        ContentValues values = new ContentValues(initialValues);
                        values.remove(MediaStore.Audio.Media.ALBUM_ARTIST);
                        String artist = values.getAsString("artist");
                        values.remove("artist");
                        if (artist != null) {
                            long artistRowId;
                            HashMap<String, Long> artistCache = database.mArtistCache;
                            synchronized(artistCache) {
                                Long temp = artistCache.get(artist);
                                if (temp == null) {
                                    artistRowId = getKeyIdForName(db, "artists", "artist_key", "artist",
                                            artist, artist, null, 0, null, artistCache, uri);
                                } else {
                                    artistRowId = temp.longValue();
                                }
                            }
                            values.put("artist_id", Integer.toString((int)artistRowId));
                        }
                        String so = values.getAsString("album");
                        values.remove("album");
                        if (so != null) {
                            String path = values.getAsString("_data");
                            int albumHash = 0;
                            if (path == null) {
                                Log.w(TAG, "Update without specified path.");
                            } else {
                                albumHash = path.substring(0, path.lastIndexOf('/')).hashCode();
                            }
                            String s = so.toString();
                            long albumRowId;
                            HashMap<String, Long> albumCache = database.mAlbumCache;
                            synchronized(albumCache) {
                                String cacheName = s + albumHash;
                                Long temp = albumCache.get(cacheName);
                                if (temp == null) {
                                    albumRowId = getKeyIdForName(db, "albums", "album_key", "album",
                                            s, cacheName, path, albumHash, artist, albumCache, uri);
                                } else {
                                    albumRowId = temp.longValue();
                                }
                            }
                            values.put("album_id", Integer.toString((int)albumRowId));
                        }
                        values.remove("title_key");
                        so = values.getAsString("title");
                        if (so != null) {
                            String s = so.toString();
                            values.put("title_key", MediaStore.Audio.keyFor(s));
                            values.remove("title");
                            values.put("title", s.trim());
                        }
                        count = db.update("audio_meta", values, sGetTableAndWhereParam.where,
                                whereArgs);
                    }
                    break;
                case IMAGES_MEDIA:
                case IMAGES_MEDIA_ID:
                case VIDEO_MEDIA:
                case VIDEO_MEDIA_ID:
                    {
                        ContentValues values = new ContentValues(initialValues);
                        values.remove(ImageColumns.BUCKET_ID);
                        values.remove(ImageColumns.BUCKET_DISPLAY_NAME);
                        String data = values.getAsString(MediaColumns.DATA);
                        if (data != null) {
                            computeBucketValues(data, values);
                        }
                        computeTakenTime(values);
                        count = db.update(sGetTableAndWhereParam.table, values,
                                sGetTableAndWhereParam.where, whereArgs);
                        if (count > 0 && values.getAsString(MediaStore.MediaColumns.DATA) != null) {
                            Cursor c = db.query(sGetTableAndWhereParam.table,
                                    READY_FLAG_PROJECTION, sGetTableAndWhereParam.where,
                                    whereArgs, null, null, null);
                            if (c != null) {
                                try {
                                    while (c.moveToNext()) {
                                        long magic = c.getLong(2);
                                        if (magic == 0) {
                                            requestMediaThumbnail(c.getString(1), uri,
                                                    MediaThumbRequest.PRIORITY_NORMAL, 0);
                                        }
                                    }
                                } finally {
                                    c.close();
                                }
                            }
                        }
                    }
                    break;
                case AUDIO_PLAYLISTS_ID_MEMBERS_ID:
                    String moveit = uri.getQueryParameter("move");
                    if (moveit != null) {
                        String key = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
                        if (initialValues.containsKey(key)) {
                            int newpos = initialValues.getAsInteger(key);
                            List <String> segments = uri.getPathSegments();
                            long playlist = Long.valueOf(segments.get(3));
                            int oldpos = Integer.valueOf(segments.get(5));
                            return movePlaylistEntry(db, playlist, oldpos, newpos);
                        }
                        throw new IllegalArgumentException("Need to specify " + key +
                                " when using 'move' parameter");
                    }
                default:
                    count = db.update(sGetTableAndWhereParam.table, initialValues,
                        sGetTableAndWhereParam.where, whereArgs);
                    break;
            }
        }
        if (count > 0 && !db.inTransaction()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
    private int movePlaylistEntry(SQLiteDatabase db, long playlist, int from, int to) {
        if (from == to) {
            return 0;
        }
        db.beginTransaction();
        try {
            int numlines = 0;
            db.execSQL("UPDATE audio_playlists_map SET play_order=-1" +
                    " WHERE play_order=" + from +
                    " AND playlist_id=" + playlist);
            if (from  < to) {
                db.execSQL("UPDATE audio_playlists_map SET play_order=play_order-1" +
                        " WHERE play_order<=" + to + " AND play_order>" + from +
                        " AND playlist_id=" + playlist);
                numlines = to - from + 1;
            } else {
                db.execSQL("UPDATE audio_playlists_map SET play_order=play_order+1" +
                        " WHERE play_order>=" + to + " AND play_order<" + from +
                        " AND playlist_id=" + playlist);
                numlines = from - to + 1;
            }
            db.execSQL("UPDATE audio_playlists_map SET play_order=" + to +
                    " WHERE play_order=-1 AND playlist_id=" + playlist);
            db.setTransactionSuccessful();
            Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
                    .buildUpon().appendEncodedPath(String.valueOf(playlist)).build();
            getContext().getContentResolver().notifyChange(uri, null);
            return numlines;
        } finally {
            db.endTransaction();
        }
    }
    private static final String[] openFileColumns = new String[] {
        MediaStore.MediaColumns.DATA,
    };
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        ParcelFileDescriptor pfd = null;
        if (URI_MATCHER.match(uri) == AUDIO_ALBUMART_FILE_ID) {
            DatabaseHelper database = getDatabaseForUri(uri);
            if (database == null) {
                throw new IllegalStateException("Couldn't open database for " + uri);
            }
            SQLiteDatabase db = database.getReadableDatabase();
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            int songid = Integer.parseInt(uri.getPathSegments().get(3));
            qb.setTables("audio_meta");
            qb.appendWhere("_id=" + songid);
            Cursor c = qb.query(db,
                    new String [] {
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID },
                    null, null, null, null, null);
            if (c.moveToFirst()) {
                String audiopath = c.getString(0);
                int albumid = c.getInt(1);
                Uri newUri = ContentUris.withAppendedId(ALBUMART_URI, albumid);
                try {
                    pfd = openFile(newUri, mode);  
                } catch (FileNotFoundException ex) {
                    pfd = getThumb(db, audiopath, albumid, null);
                }
            }
            c.close();
            return pfd;
        }
        try {
            pfd = openFileHelper(uri, mode);
        } catch (FileNotFoundException ex) {
            if (mode.contains("w")) {
                throw ex;
            }
            if (URI_MATCHER.match(uri) == AUDIO_ALBUMART_ID) {
                DatabaseHelper database = getDatabaseForUri(uri);
                if (database == null) {
                    throw ex;
                }
                SQLiteDatabase db = database.getReadableDatabase();
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                int albumid = Integer.parseInt(uri.getPathSegments().get(3));
                qb.setTables("audio_meta");
                qb.appendWhere("album_id=" + albumid);
                Cursor c = qb.query(db,
                        new String [] {
                            MediaStore.Audio.Media.DATA },
                        null, null, null, null, null);
                if (c.moveToFirst()) {
                    String audiopath = c.getString(0);
                    pfd = getThumb(db, audiopath, albumid, uri);
                }
                c.close();
            }
            if (pfd == null) {
                throw ex;
            }
        }
        return pfd;
    }
    private class ThumbData {
        SQLiteDatabase db;
        String path;
        long album_id;
        Uri albumart_uri;
    }
    private void makeThumbAsync(SQLiteDatabase db, String path, long album_id) {
        synchronized (mPendingThumbs) {
            if (mPendingThumbs.contains(path)) {
                return;
            }
            mPendingThumbs.add(path);
        }
        ThumbData d = new ThumbData();
        d.db = db;
        d.path = path;
        d.album_id = album_id;
        d.albumart_uri = ContentUris.withAppendedId(mAlbumArtBaseUri, album_id);
        synchronized (mThumbRequestStack) {
            mThumbRequestStack.push(d);
        }
        Message msg = mThumbHandler.obtainMessage(ALBUM_THUMB);
        msg.sendToTarget();
    }
    private static byte[] getCompressedAlbumArt(Context context, String path) {
        byte[] compressed = null;
        try {
            File f = new File(path);
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            MediaScanner scanner = new MediaScanner(context);
            compressed = scanner.extractAlbumArt(pfd.getFileDescriptor());
            pfd.close();
            if (compressed == null && path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash > 0) {
                    String artPath = path.substring(0, lastSlash);
                    String sdroot = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String dwndir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    String bestmatch = null;
                    synchronized (sFolderArtMap) {
                        if (sFolderArtMap.containsKey(artPath)) {
                            bestmatch = sFolderArtMap.get(artPath);
                        } else if (!artPath.equalsIgnoreCase(sdroot) &&
                                !artPath.equalsIgnoreCase(dwndir)) {
                            File dir = new File(artPath);
                            String [] entrynames = dir.list();
                            if (entrynames == null) {
                                return null;
                            }
                            bestmatch = null;
                            int matchlevel = 1000;
                            for (int i = entrynames.length - 1; i >=0; i--) {
                                String entry = entrynames[i].toLowerCase();
                                if (entry.equals("albumart.jpg")) {
                                    bestmatch = entrynames[i];
                                    break;
                                } else if (entry.startsWith("albumart")
                                        && entry.endsWith("large.jpg")
                                        && matchlevel > 1) {
                                    bestmatch = entrynames[i];
                                    matchlevel = 1;
                                } else if (entry.contains("albumart")
                                        && entry.endsWith(".jpg")
                                        && matchlevel > 2) {
                                    bestmatch = entrynames[i];
                                    matchlevel = 2;
                                } else if (entry.endsWith(".jpg") && matchlevel > 3) {
                                    bestmatch = entrynames[i];
                                    matchlevel = 3;
                                } else if (entry.endsWith(".png") && matchlevel > 4) {
                                    bestmatch = entrynames[i];
                                    matchlevel = 4;
                                }
                            }
                            sFolderArtMap.put(artPath, bestmatch);
                        }
                    }
                    if (bestmatch != null) {
                        File file = new File(artPath, bestmatch);
                        if (file.exists()) {
                            compressed = new byte[(int)file.length()];
                            FileInputStream stream = null;
                            try {
                                stream = new FileInputStream(file);
                                stream.read(compressed);
                            } catch (IOException ex) {
                                compressed = null;
                            } finally {
                                if (stream != null) {
                                    stream.close();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
        return compressed;
    }
    Uri getAlbumArtOutputUri(SQLiteDatabase db, long album_id, Uri albumart_uri) {
        Uri out = null;
        if (albumart_uri != null) {
            Cursor c = query(albumart_uri, new String [] { "_data" },
                    null, null, null);
            if (c.moveToFirst()) {
                String albumart_path = c.getString(0);
                if (ensureFileExists(albumart_path)) {
                    out = albumart_uri;
                }
            } else {
                albumart_uri = null;
            }
            c.close();
        }
        if (albumart_uri == null){
            ContentValues initialValues = new ContentValues();
            initialValues.put("album_id", album_id);
            try {
                ContentValues values = ensureFile(false, initialValues, "", ALBUM_THUMB_FOLDER);
                long rowId = db.insert("album_art", "_data", values);
                if (rowId > 0) {
                    out = ContentUris.withAppendedId(ALBUMART_URI, rowId);
                }
            } catch (IllegalStateException ex) {
                Log.e(TAG, "error creating album thumb file");
            }
        }
        return out;
    }
    private void writeAlbumArt(
            boolean need_to_recompress, Uri out, byte[] compressed, Bitmap bm) {
        boolean success = false;
        try {
            OutputStream outstream = getContext().getContentResolver().openOutputStream(out);
            if (!need_to_recompress) {
                outstream.write(compressed);
                success = true;
            } else {
                success = bm.compress(Bitmap.CompressFormat.JPEG, 75, outstream);
            }
            outstream.close();
        } catch (FileNotFoundException ex) {
            Log.e(TAG, "error creating file", ex);
        } catch (IOException ex) {
            Log.e(TAG, "error creating file", ex);
        }
        if (!success) {
            getContext().getContentResolver().delete(out, null, null);
        }
    }
    private ParcelFileDescriptor getThumb(SQLiteDatabase db, String path, long album_id,
            Uri albumart_uri) {
        ThumbData d = new ThumbData();
        d.db = db;
        d.path = path;
        d.album_id = album_id;
        d.albumart_uri = albumart_uri;
        return makeThumbInternal(d);
    }
    private ParcelFileDescriptor makeThumbInternal(ThumbData d) {
        byte[] compressed = getCompressedAlbumArt(getContext(), d.path);
        if (compressed == null) {
            return null;
        }
        Bitmap bm = null;
        boolean need_to_recompress = true;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            opts.inSampleSize = 1;
            BitmapFactory.decodeByteArray(compressed, 0, compressed.length, opts);
            while (opts.outHeight > 320 || opts.outWidth > 320) {
                opts.outHeight /= 2;
                opts.outWidth /= 2;
                opts.inSampleSize *= 2;
            }
            if (opts.inSampleSize == 1) {
                need_to_recompress = false;
            } else {
                opts.inJustDecodeBounds = false;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                bm = BitmapFactory.decodeByteArray(compressed, 0, compressed.length, opts);
                if (bm != null && bm.getConfig() == null) {
                    Bitmap nbm = bm.copy(Bitmap.Config.RGB_565, false);
                    if (nbm != null && nbm != bm) {
                        bm.recycle();
                        bm = nbm;
                    }
                }
            }
        } catch (Exception e) {
        }
        if (need_to_recompress && bm == null) {
            return null;
        }
        if (d.albumart_uri == null) {
            try {
                MemoryFile file = new MemoryFile("albumthumb", compressed.length);
                file.writeBytes(compressed, 0, 0, compressed.length);
                file.deactivate();
                return file.getParcelFileDescriptor();
            } catch (IOException e) {
            }
        } else {
            d.db.beginTransaction();
            try {
                Uri out = getAlbumArtOutputUri(d.db, d.album_id, d.albumart_uri);
                if (out != null) {
                    writeAlbumArt(need_to_recompress, out, compressed, bm);
                    getContext().getContentResolver().notifyChange(MEDIA_URI, null);
                    ParcelFileDescriptor pfd = openFileHelper(out, "r");
                    d.db.setTransactionSuccessful();
                    return pfd;
                }
            } catch (FileNotFoundException ex) {
            } catch (UnsupportedOperationException ex) {
            } finally {
                d.db.endTransaction();
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
        return null;
    }
    private long getKeyIdForName(SQLiteDatabase db, String table, String keyField, String nameField,
            String rawName, String cacheName, String path, int albumHash,
            String artist, HashMap<String, Long> cache, Uri srcuri) {
        long rowId;
        if (rawName == null || rawName.length() == 0) {
            return -1;
        }
        String k = MediaStore.Audio.keyFor(rawName);
        if (k == null) {
            return -1;
        }
        boolean isAlbum = table.equals("albums");
        boolean isUnknown = MediaStore.UNKNOWN_STRING.equals(rawName);
        if (isAlbum) {
            k = k + albumHash;
            if (isUnknown) {
                k = k + artist;
            }
        }
        String [] selargs = { k };
        Cursor c = db.query(table, null, keyField + "=?", selargs, null, null, null);
        try {
            switch (c.getCount()) {
                case 0: {
                        ContentValues otherValues = new ContentValues();
                        otherValues.put(keyField, k);
                        otherValues.put(nameField, rawName);
                        rowId = db.insert(table, "duration", otherValues);
                        if (path != null && isAlbum && ! isUnknown) {
                            makeThumbAsync(db, path, rowId);
                        }
                        if (rowId > 0) {
                            String volume = srcuri.toString().substring(16, 24); 
                            Uri uri = Uri.parse("content:
                            getContext().getContentResolver().notifyChange(uri, null);
                        }
                    }
                    break;
                case 1: {
                        c.moveToFirst();
                        rowId = c.getLong(0);
                        String currentFancyName = c.getString(2);
                        String bestName = makeBestName(rawName, currentFancyName);
                        if (!bestName.equals(currentFancyName)) {
                            ContentValues newValues = new ContentValues();
                            newValues.put(nameField, bestName);
                            db.update(table, newValues, "rowid="+Integer.toString((int)rowId), null);
                            String volume = srcuri.toString().substring(16, 24); 
                            Uri uri = Uri.parse("content:
                            getContext().getContentResolver().notifyChange(uri, null);
                        }
                    }
                    break;
                default:
                    Log.e(TAG, "Multiple entries in table " + table + " for key " + k);
                    rowId = -1;
                    break;
            }
        } finally {
            if (c != null) c.close();
        }
        if (cache != null && ! isUnknown) {
            cache.put(cacheName, rowId);
        }
        return rowId;
    }
    String makeBestName(String one, String two) {
        String name;
        if (one.length() > two.length()) {
            name = one;
        } else {
            if (one.toLowerCase().compareTo(two.toLowerCase()) > 0) {
                name = one;
            } else {
                name = two;
            }
        }
        if (name.endsWith(", the") || name.endsWith(",the") ||
            name.endsWith(", an") || name.endsWith(",an") ||
            name.endsWith(", a") || name.endsWith(",a")) {
            String fix = name.substring(1 + name.lastIndexOf(','));
            name = fix.trim() + " " + name.substring(0, name.lastIndexOf(','));
        }
        return name;
    }
    private DatabaseHelper getDatabaseForUri(Uri uri) {
        synchronized (mDatabases) {
            if (uri.getPathSegments().size() > 1) {
                return mDatabases.get(uri.getPathSegments().get(0));
            }
        }
        return null;
    }
    private Uri attachVolume(String volume) {
        if (Process.supportsProcesses() && Binder.getCallingPid() != Process.myPid()) {
            throw new SecurityException(
                    "Opening and closing databases not allowed.");
        }
        synchronized (mDatabases) {
            if (mDatabases.get(volume) != null) {  
                return Uri.parse("content:
            }
            DatabaseHelper db;
            if (INTERNAL_VOLUME.equals(volume)) {
                db = new DatabaseHelper(getContext(), INTERNAL_DATABASE_NAME, true);
            } else if (EXTERNAL_VOLUME.equals(volume)) {
                String path = Environment.getExternalStorageDirectory().getPath();
                int volumeID = FileUtils.getFatVolumeId(path);
                if (LOCAL_LOGV) Log.v(TAG, path + " volume ID: " + volumeID);
                String dbName = "external-" + Integer.toHexString(volumeID) + ".db";
                db = new DatabaseHelper(getContext(), dbName, false);
                mVolumeId = volumeID;
            } else {
                throw new IllegalArgumentException("There is no volume named " + volume);
            }
            mDatabases.put(volume, db);
            if (!db.mInternal) {
                File[] files = new File(
                        Environment.getExternalStorageDirectory(),
                        ALBUM_THUMB_FOLDER).listFiles();
                HashSet<String> fileSet = new HashSet();
                for (int i = 0; files != null && i < files.length; i++) {
                    fileSet.add(files[i].getPath());
                }
                Cursor cursor = query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Audio.Albums.ALBUM_ART }, null, null, null);
                try {
                    while (cursor != null && cursor.moveToNext()) {
                        fileSet.remove(cursor.getString(0));
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
                Iterator<String> iterator = fileSet.iterator();
                while (iterator.hasNext()) {
                    String filename = iterator.next();
                    if (LOCAL_LOGV) Log.v(TAG, "deleting obsolete album art " + filename);
                    new File(filename).delete();
                }
            }
        }
        if (LOCAL_LOGV) Log.v(TAG, "Attached volume: " + volume);
        return Uri.parse("content:
    }
    private void detachVolume(Uri uri) {
        if (Process.supportsProcesses() && Binder.getCallingPid() != Process.myPid()) {
            throw new SecurityException(
                    "Opening and closing databases not allowed.");
        }
        String volume = uri.getPathSegments().get(0);
        if (INTERNAL_VOLUME.equals(volume)) {
            throw new UnsupportedOperationException(
                    "Deleting the internal volume is not allowed");
        } else if (!EXTERNAL_VOLUME.equals(volume)) {
            throw new IllegalArgumentException(
                    "There is no volume named " + volume);
        }
        synchronized (mDatabases) {
            DatabaseHelper database = mDatabases.get(volume);
            if (database == null) return;
            try {
                File file = new File(database.getReadableDatabase().getPath());
                file.setLastModified(System.currentTimeMillis());
            } catch (SQLException e) {
                Log.e(TAG, "Can't touch database file", e);
            }
            mDatabases.remove(volume);
            database.close();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (LOCAL_LOGV) Log.v(TAG, "Detached volume: " + volume);
    }
    private static String TAG = "MediaProvider";
    private static final boolean LOCAL_LOGV = true;
    private static final int DATABASE_VERSION = 90;
    private static final String INTERNAL_DATABASE_NAME = "internal.db";
    private static final int MAX_EXTERNAL_DATABASES = 3;
    private static final long OBSOLETE_DATABASE_DB = 5184000000L;
    private HashMap<String, DatabaseHelper> mDatabases;
    private Handler mThumbHandler;
    private String mMediaScannerVolume;
    private int mVolumeId;
    static final String INTERNAL_VOLUME = "internal";
    static final String EXTERNAL_VOLUME = "external";
    static final String ALBUM_THUMB_FOLDER = "Android/data/com.android.providers.media/albumthumbs";
    private String mTempDatabasePath;
    private static final int IMAGES_MEDIA = 1;
    private static final int IMAGES_MEDIA_ID = 2;
    private static final int IMAGES_THUMBNAILS = 3;
    private static final int IMAGES_THUMBNAILS_ID = 4;
    private static final int AUDIO_MEDIA = 100;
    private static final int AUDIO_MEDIA_ID = 101;
    private static final int AUDIO_MEDIA_ID_GENRES = 102;
    private static final int AUDIO_MEDIA_ID_GENRES_ID = 103;
    private static final int AUDIO_MEDIA_ID_PLAYLISTS = 104;
    private static final int AUDIO_MEDIA_ID_PLAYLISTS_ID = 105;
    private static final int AUDIO_GENRES = 106;
    private static final int AUDIO_GENRES_ID = 107;
    private static final int AUDIO_GENRES_ID_MEMBERS = 108;
    private static final int AUDIO_GENRES_ID_MEMBERS_ID = 109;
    private static final int AUDIO_PLAYLISTS = 110;
    private static final int AUDIO_PLAYLISTS_ID = 111;
    private static final int AUDIO_PLAYLISTS_ID_MEMBERS = 112;
    private static final int AUDIO_PLAYLISTS_ID_MEMBERS_ID = 113;
    private static final int AUDIO_ARTISTS = 114;
    private static final int AUDIO_ARTISTS_ID = 115;
    private static final int AUDIO_ALBUMS = 116;
    private static final int AUDIO_ALBUMS_ID = 117;
    private static final int AUDIO_ARTISTS_ID_ALBUMS = 118;
    private static final int AUDIO_ALBUMART = 119;
    private static final int AUDIO_ALBUMART_ID = 120;
    private static final int AUDIO_ALBUMART_FILE_ID = 121;
    private static final int VIDEO_MEDIA = 200;
    private static final int VIDEO_MEDIA_ID = 201;
    private static final int VIDEO_THUMBNAILS = 202;
    private static final int VIDEO_THUMBNAILS_ID = 203;
    private static final int VOLUMES = 300;
    private static final int VOLUMES_ID = 301;
    private static final int AUDIO_SEARCH_LEGACY = 400;
    private static final int AUDIO_SEARCH_BASIC = 401;
    private static final int AUDIO_SEARCH_FANCY = 402;
    private static final int MEDIA_SCANNER = 500;
    private static final int FS_ID = 600;
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String[] ID_PROJECTION = new String[] {
        MediaStore.MediaColumns._ID
    };
    private static final String[] MIME_TYPE_PROJECTION = new String[] {
            MediaStore.MediaColumns._ID, 
            MediaStore.MediaColumns.MIME_TYPE, 
    };
    private static final String[] READY_FLAG_PROJECTION = new String[] {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            Images.Media.MINI_THUMB_MAGIC
    };
    private static final String[] EXTERNAL_DATABASE_TABLES = new String[] {
        "images",
        "thumbnails",
        "audio_meta",
        "artists",
        "albums",
        "audio_genres",
        "audio_genres_map",
        "audio_playlists",
        "audio_playlists_map",
        "video",
    };
    static
    {
        URI_MATCHER.addURI("media", "*/images/media", IMAGES_MEDIA);
        URI_MATCHER.addURI("media", "*/images/media/#", IMAGES_MEDIA_ID);
        URI_MATCHER.addURI("media", "*/images/thumbnails", IMAGES_THUMBNAILS);
        URI_MATCHER.addURI("media", "*/images/thumbnails/#", IMAGES_THUMBNAILS_ID);
        URI_MATCHER.addURI("media", "*/audio/media", AUDIO_MEDIA);
        URI_MATCHER.addURI("media", "*/audio/media/#", AUDIO_MEDIA_ID);
        URI_MATCHER.addURI("media", "*/audio/media/#/genres", AUDIO_MEDIA_ID_GENRES);
        URI_MATCHER.addURI("media", "*/audio/media/#/genres/#", AUDIO_MEDIA_ID_GENRES_ID);
        URI_MATCHER.addURI("media", "*/audio/media/#/playlists", AUDIO_MEDIA_ID_PLAYLISTS);
        URI_MATCHER.addURI("media", "*/audio/media/#/playlists/#", AUDIO_MEDIA_ID_PLAYLISTS_ID);
        URI_MATCHER.addURI("media", "*/audio/genres", AUDIO_GENRES);
        URI_MATCHER.addURI("media", "*/audio/genres/#", AUDIO_GENRES_ID);
        URI_MATCHER.addURI("media", "*/audio/genres/#/members", AUDIO_GENRES_ID_MEMBERS);
        URI_MATCHER.addURI("media", "*/audio/genres/#/members/#", AUDIO_GENRES_ID_MEMBERS_ID);
        URI_MATCHER.addURI("media", "*/audio/playlists", AUDIO_PLAYLISTS);
        URI_MATCHER.addURI("media", "*/audio/playlists/#", AUDIO_PLAYLISTS_ID);
        URI_MATCHER.addURI("media", "*/audio/playlists/#/members", AUDIO_PLAYLISTS_ID_MEMBERS);
        URI_MATCHER.addURI("media", "*/audio/playlists/#/members/#", AUDIO_PLAYLISTS_ID_MEMBERS_ID);
        URI_MATCHER.addURI("media", "*/audio/artists", AUDIO_ARTISTS);
        URI_MATCHER.addURI("media", "*/audio/artists/#", AUDIO_ARTISTS_ID);
        URI_MATCHER.addURI("media", "*/audio/artists/#/albums", AUDIO_ARTISTS_ID_ALBUMS);
        URI_MATCHER.addURI("media", "*/audio/albums", AUDIO_ALBUMS);
        URI_MATCHER.addURI("media", "*/audio/albums/#", AUDIO_ALBUMS_ID);
        URI_MATCHER.addURI("media", "*/audio/albumart", AUDIO_ALBUMART);
        URI_MATCHER.addURI("media", "*/audio/albumart/#", AUDIO_ALBUMART_ID);
        URI_MATCHER.addURI("media", "*/audio/media/#/albumart", AUDIO_ALBUMART_FILE_ID);
        URI_MATCHER.addURI("media", "*/video/media", VIDEO_MEDIA);
        URI_MATCHER.addURI("media", "*/video/media/#", VIDEO_MEDIA_ID);
        URI_MATCHER.addURI("media", "*/video/thumbnails", VIDEO_THUMBNAILS);
        URI_MATCHER.addURI("media", "*/video/thumbnails/#", VIDEO_THUMBNAILS_ID);
        URI_MATCHER.addURI("media", "*/media_scanner", MEDIA_SCANNER);
        URI_MATCHER.addURI("media", "*/fs_id", FS_ID);
        URI_MATCHER.addURI("media", "*", VOLUMES_ID);
        URI_MATCHER.addURI("media", null, VOLUMES);
        URI_MATCHER.addURI("media", "*/audio/" + SearchManager.SUGGEST_URI_PATH_QUERY,
                AUDIO_SEARCH_LEGACY);
        URI_MATCHER.addURI("media", "*/audio/" + SearchManager.SUGGEST_URI_PATH_QUERY + "audio/search/" + SearchManager.SUGGEST_URI_PATH_QUERY,
                AUDIO_SEARCH_BASIC);
        URI_MATCHER.addURI("media", "*/audio/search/" + SearchManager.SUGGEST_URI_PATH_QUERY +
                "audio/search/fancy", AUDIO_SEARCH_FANCY);
        URI_MATCHER.addURI("media", "*/audio/search/fancy/*", AUDIO_SEARCH_FANCY);
    }
}
