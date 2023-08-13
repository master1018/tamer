public class MediaScanner
{
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    private final static String TAG = "MediaScanner";
    private static final String[] AUDIO_PROJECTION = new String[] {
            Audio.Media._ID, 
            Audio.Media.DATA, 
            Audio.Media.DATE_MODIFIED, 
    };
    private static final int ID_AUDIO_COLUMN_INDEX = 0;
    private static final int PATH_AUDIO_COLUMN_INDEX = 1;
    private static final int DATE_MODIFIED_AUDIO_COLUMN_INDEX = 2;
    private static final String[] VIDEO_PROJECTION = new String[] {
            Video.Media._ID, 
            Video.Media.DATA, 
            Video.Media.DATE_MODIFIED, 
    };
    private static final int ID_VIDEO_COLUMN_INDEX = 0;
    private static final int PATH_VIDEO_COLUMN_INDEX = 1;
    private static final int DATE_MODIFIED_VIDEO_COLUMN_INDEX = 2;
    private static final String[] IMAGES_PROJECTION = new String[] {
            Images.Media._ID, 
            Images.Media.DATA, 
            Images.Media.DATE_MODIFIED, 
    };
    private static final int ID_IMAGES_COLUMN_INDEX = 0;
    private static final int PATH_IMAGES_COLUMN_INDEX = 1;
    private static final int DATE_MODIFIED_IMAGES_COLUMN_INDEX = 2;
    private static final String[] PLAYLISTS_PROJECTION = new String[] {
            Audio.Playlists._ID, 
            Audio.Playlists.DATA, 
            Audio.Playlists.DATE_MODIFIED, 
    };
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = new String[] {
            Audio.Playlists.Members.PLAYLIST_ID, 
     };
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String[] GENRE_LOOKUP_PROJECTION = new String[] {
            Audio.Genres._ID, 
            Audio.Genres.NAME, 
    };
    private static final String RINGTONES_DIR = "/ringtones/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final String ALARMS_DIR = "/alarms/";
    private static final String MUSIC_DIR = "/music/";
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String[] ID3_GENRES = {
        "Blues",
        "Classic Rock",
        "Country",
        "Dance",
        "Disco",
        "Funk",
        "Grunge",
        "Hip-Hop",
        "Jazz",
        "Metal",
        "New Age",
        "Oldies",
        "Other",
        "Pop",
        "R&B",
        "Rap",
        "Reggae",
        "Rock",
        "Techno",
        "Industrial",
        "Alternative",
        "Ska",
        "Death Metal",
        "Pranks",
        "Soundtrack",
        "Euro-Techno",
        "Ambient",
        "Trip-Hop",
        "Vocal",
        "Jazz+Funk",
        "Fusion",
        "Trance",
        "Classical",
        "Instrumental",
        "Acid",
        "House",
        "Game",
        "Sound Clip",
        "Gospel",
        "Noise",
        "AlternRock",
        "Bass",
        "Soul",
        "Punk",
        "Space",
        "Meditative",
        "Instrumental Pop",
        "Instrumental Rock",
        "Ethnic",
        "Gothic",
        "Darkwave",
        "Techno-Industrial",
        "Electronic",
        "Pop-Folk",
        "Eurodance",
        "Dream",
        "Southern Rock",
        "Comedy",
        "Cult",
        "Gangsta",
        "Top 40",
        "Christian Rap",
        "Pop/Funk",
        "Jungle",
        "Native American",
        "Cabaret",
        "New Wave",
        "Psychadelic",
        "Rave",
        "Showtunes",
        "Trailer",
        "Lo-Fi",
        "Tribal",
        "Acid Punk",
        "Acid Jazz",
        "Polka",
        "Retro",
        "Musical",
        "Rock & Roll",
        "Hard Rock",
        "Folk",
        "Folk-Rock",
        "National Folk",
        "Swing",
        "Fast Fusion",
        "Bebob",
        "Latin",
        "Revival",
        "Celtic",
        "Bluegrass",
        "Avantgarde",
        "Gothic Rock",
        "Progressive Rock",
        "Psychedelic Rock",
        "Symphonic Rock",
        "Slow Rock",
        "Big Band",
        "Chorus",
        "Easy Listening",
        "Acoustic",
        "Humour",
        "Speech",
        "Chanson",
        "Opera",
        "Chamber Music",
        "Sonata",
        "Symphony",
        "Booty Bass",
        "Primus",
        "Porn Groove",
        "Satire",
        "Slow Jam",
        "Club",
        "Tango",
        "Samba",
        "Folklore",
        "Ballad",
        "Power Ballad",
        "Rhythmic Soul",
        "Freestyle",
        "Duet",
        "Punk Rock",
        "Drum Solo",
        "A capella",
        "Euro-House",
        "Dance Hall"
    };
    private int mNativeContext;
    private Context mContext;
    private IContentProvider mMediaProvider;
    private Uri mAudioUri;
    private Uri mVideoUri;
    private Uri mImagesUri;
    private Uri mThumbsUri;
    private Uri mGenresUri;
    private Uri mPlaylistsUri;
    private boolean mProcessPlaylists, mProcessGenres;
    private int mOriginalCount;
    private boolean mDefaultRingtoneSet;
    private boolean mDefaultNotificationSet;
    private boolean mDefaultAlarmSet;
    private String mDefaultRingtoneFilename;
    private String mDefaultNotificationFilename;
    private String mDefaultAlarmAlertFilename;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private boolean mCaseInsensitivePaths;
    private BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    private static class FileCacheEntry {
        Uri mTableUri;
        long mRowId;
        String mPath;
        long mLastModified;
        boolean mSeenInFileSystem;
        boolean mLastModifiedChanged;
        FileCacheEntry(Uri tableUri, long rowId, String path, long lastModified) {
            mTableUri = tableUri;
            mRowId = rowId;
            mPath = path;
            mLastModified = lastModified;
            mSeenInFileSystem = false;
            mLastModifiedChanged = false;
        }
        @Override
        public String toString() {
            return mPath;
        }
    }
    private HashMap<String, FileCacheEntry> mFileCache;
    private ArrayList<FileCacheEntry> mPlayLists;
    private HashMap<String, Uri> mGenreCache;
    public MediaScanner(Context c) {
        native_setup();
        mContext = c;
        mBitmapOptions.inSampleSize = 1;
        mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
    }
    private void setDefaultRingtoneFileNames() {
        mDefaultRingtoneFilename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX
                + Settings.System.RINGTONE);
        mDefaultNotificationFilename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX
                + Settings.System.NOTIFICATION_SOUND);
        mDefaultAlarmAlertFilename = SystemProperties.get(DEFAULT_RINGTONE_PROPERTY_PREFIX
                + Settings.System.ALARM_ALERT);
    }
    private MyMediaScannerClient mClient = new MyMediaScannerClient();
    private class MyMediaScannerClient implements MediaScannerClient {
        private String mArtist;
        private String mAlbumArtist;    
        private String mAlbum;
        private String mTitle;
        private String mComposer;
        private String mGenre;
        private String mMimeType;
        private int mFileType;
        private int mTrack;
        private int mYear;
        private int mDuration;
        private String mPath;
        private long mLastModified;
        private long mFileSize;
        private String mWriter;
        public FileCacheEntry beginFile(String path, String mimeType, long lastModified, long fileSize) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
                if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                    return null;
                }
                if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                    if (path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10) ||
                            path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                        return null;
                    }
                    int length = path.length() - lastSlash - 1;
                    if ((length == 17 && path.regionMatches(true, lastSlash + 1, "AlbumArtSmall", 0, 13)) ||
                            (length == 10 && path.regionMatches(true, lastSlash + 1, "Folder", 0, 6))) {
                        return null;
                    }
                }
            }
            mMimeType = null;
            if (mimeType != null) {
                mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                if (mFileType != 0) {
                    mMimeType = mimeType;
                }
            }
            mFileSize = fileSize;
            if (mMimeType == null) {
                MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                if (mediaFileType != null) {
                    mFileType = mediaFileType.fileType;
                    mMimeType = mediaFileType.mimeType;
                }
            }
            String key = path;
            if (mCaseInsensitivePaths) {
                key = path.toLowerCase();
            }
            FileCacheEntry entry = mFileCache.get(key);
            if (entry == null) {
                entry = new FileCacheEntry(null, 0, path, 0);
                mFileCache.put(key, entry);
            }
            entry.mSeenInFileSystem = true;
            long delta = lastModified - entry.mLastModified;
            if (delta > 1 || delta < -1) {
                entry.mLastModified = lastModified;
                entry.mLastModifiedChanged = true;
            }
            if (mProcessPlaylists && MediaFile.isPlayListFileType(mFileType)) {
                mPlayLists.add(entry);
                return null;
            }
            mArtist = null;
            mAlbumArtist = null;
            mAlbum = null;
            mTitle = null;
            mComposer = null;
            mGenre = null;
            mTrack = 0;
            mYear = 0;
            mDuration = 0;
            mPath = path;
            mLastModified = lastModified;
            mWriter = null;
            return entry;
        }
        public void scanFile(String path, long lastModified, long fileSize) {
            doScanFile(path, null, lastModified, fileSize, false);
        }
        public void scanFile(String path, String mimeType, long lastModified, long fileSize) {
            doScanFile(path, mimeType, lastModified, fileSize, false);
        }
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean scanAlways) {
            Uri result = null;
            try {
                FileCacheEntry entry = beginFile(path, mimeType, lastModified, fileSize);
                if (entry != null && (entry.mLastModifiedChanged || scanAlways)) {
                    String lowpath = path.toLowerCase();
                    boolean ringtones = (lowpath.indexOf(RINGTONES_DIR) > 0);
                    boolean notifications = (lowpath.indexOf(NOTIFICATIONS_DIR) > 0);
                    boolean alarms = (lowpath.indexOf(ALARMS_DIR) > 0);
                    boolean podcasts = (lowpath.indexOf(PODCAST_DIR) > 0);
                    boolean music = (lowpath.indexOf(MUSIC_DIR) > 0) ||
                        (!ringtones && !notifications && !alarms && !podcasts);
                    if (!MediaFile.isImageFileType(mFileType)) {
                        processFile(path, mimeType, this);
                    }
                    result = endFile(entry, ringtones, notifications, alarms, music, podcasts);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            }
            return result;
        }
        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) return defaultValue;
            char ch = s.charAt(start++);
            if (ch < '0' || ch > '9') return defaultValue;
            int result = ch - '0';
            while (start < length) {
                ch = s.charAt(start++);
                if (ch < '0' || ch > '9') return result;
                result = result * 10 + (ch - '0');
            }
            return result;
        }
        public void handleStringTag(String name, String value) {
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;")) {
                mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                mAlbum = value.trim();
            } else if (name.equalsIgnoreCase("composer") || name.startsWith("composer;")) {
                mComposer = value.trim();
            } else if (name.equalsIgnoreCase("genre") || name.startsWith("genre;")) {
                if (value.length() > 0) {
                    int genreCode = -1;
                    char ch = value.charAt(0);
                    if (ch == '(') {
                        genreCode = parseSubstring(value, 1, -1);
                    } else if (ch >= '0' && ch <= '9') {
                        genreCode = parseSubstring(value, 0, -1);
                    }
                    if (genreCode >= 0 && genreCode < ID3_GENRES.length) {
                        value = ID3_GENRES[genreCode];
                    } else if (genreCode == 255) {
                        value = null;
                    }
                }
                mGenre = value;
            } else if (name.equalsIgnoreCase("year") || name.startsWith("year;")) {
                mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                int num = parseSubstring(value, 0, 0);
                mTrack = (mTrack / 1000) * 1000 + num;
            } else if (name.equalsIgnoreCase("discnumber") ||
                    name.equals("set") || name.startsWith("set;")) {
                int num = parseSubstring(value, 0, 0);
                mTrack = (num * 1000) + (mTrack % 1000);
            } else if (name.equalsIgnoreCase("duration")) {
                mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                mWriter = value.trim();
            }
        }
        public void setMimeType(String mimeType) {
            if ("audio/mp4".equals(mMimeType) &&
                    mimeType.startsWith("video")) {
                return;
            }
            mMimeType = mimeType;
            mFileType = MediaFile.getFileTypeForMimeType(mimeType);
        }
        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put(MediaStore.MediaColumns.DATA, mPath);
            map.put(MediaStore.MediaColumns.TITLE, mTitle);
            map.put(MediaStore.MediaColumns.DATE_MODIFIED, mLastModified);
            map.put(MediaStore.MediaColumns.SIZE, mFileSize);
            map.put(MediaStore.MediaColumns.MIME_TYPE, mMimeType);
            if (MediaFile.isVideoFileType(mFileType)) {
                map.put(Video.Media.ARTIST, (mArtist != null && mArtist.length() > 0 ? mArtist : MediaStore.UNKNOWN_STRING));
                map.put(Video.Media.ALBUM, (mAlbum != null && mAlbum.length() > 0 ? mAlbum : MediaStore.UNKNOWN_STRING));
                map.put(Video.Media.DURATION, mDuration);
            } else if (MediaFile.isImageFileType(mFileType)) {
            } else if (MediaFile.isAudioFileType(mFileType)) {
                map.put(Audio.Media.ARTIST, (mArtist != null && mArtist.length() > 0) ?
                        mArtist : MediaStore.UNKNOWN_STRING);
                map.put(Audio.Media.ALBUM_ARTIST, (mAlbumArtist != null &&
                        mAlbumArtist.length() > 0) ? mAlbumArtist : null);
                map.put(Audio.Media.ALBUM, (mAlbum != null && mAlbum.length() > 0) ?
                        mAlbum : MediaStore.UNKNOWN_STRING);
                map.put(Audio.Media.COMPOSER, mComposer);
                if (mYear != 0) {
                    map.put(Audio.Media.YEAR, mYear);
                }
                map.put(Audio.Media.TRACK, mTrack);
                map.put(Audio.Media.DURATION, mDuration);
            }
            return map;
        }
        private Uri endFile(FileCacheEntry entry, boolean ringtones, boolean notifications,
                boolean alarms, boolean music, boolean podcasts)
                throws RemoteException {
            Uri tableUri;
            boolean isAudio = MediaFile.isAudioFileType(mFileType);
            boolean isVideo = MediaFile.isVideoFileType(mFileType);
            boolean isImage = MediaFile.isImageFileType(mFileType);
            if (isVideo) {
                tableUri = mVideoUri;
            } else if (isImage) {
                tableUri = mImagesUri;
            } else if (isAudio) {
                tableUri = mAudioUri;
            } else {
                return null;
            }
            entry.mTableUri = tableUri;
            if (mArtist == null || mArtist.length() == 0) {
                mArtist = mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString(MediaStore.MediaColumns.TITLE);
            if (title == null || TextUtils.isEmpty(title.trim())) {
                title = values.getAsString(MediaStore.MediaColumns.DATA);
                int lastSlash = title.lastIndexOf('/');
                if (lastSlash >= 0) {
                    lastSlash++;
                    if (lastSlash < title.length()) {
                        title = title.substring(lastSlash);
                    }
                }
                int lastDot = title.lastIndexOf('.');
                if (lastDot > 0) {
                    title = title.substring(0, lastDot);
                }
                values.put(MediaStore.MediaColumns.TITLE, title);
            }
            String album = values.getAsString(Audio.Media.ALBUM);
            if (MediaStore.UNKNOWN_STRING.equals(album)) {
                album = values.getAsString(MediaStore.MediaColumns.DATA);
                int lastSlash = album.lastIndexOf('/');
                if (lastSlash >= 0) {
                    int previousSlash = 0;
                    while (true) {
                        int idx = album.indexOf('/', previousSlash + 1);
                        if (idx < 0 || idx >= lastSlash) {
                            break;
                        }
                        previousSlash = idx;
                    }
                    if (previousSlash != 0) {
                        album = album.substring(previousSlash + 1, lastSlash);
                        values.put(Audio.Media.ALBUM, album);
                    }
                }
            }
            long rowId = entry.mRowId;
            if (isAudio && rowId == 0) {
                values.put(Audio.Media.IS_RINGTONE, ringtones);
                values.put(Audio.Media.IS_NOTIFICATION, notifications);
                values.put(Audio.Media.IS_ALARM, alarms);
                values.put(Audio.Media.IS_MUSIC, music);
                values.put(Audio.Media.IS_PODCAST, podcasts);
            } else if (mFileType == MediaFile.FILE_TYPE_JPEG) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(entry.mPath);
                } catch (IOException ex) {
                }
                if (exif != null) {
                    float[] latlng = new float[2];
                    if (exif.getLatLong(latlng)) {
                        values.put(Images.Media.LATITUDE, latlng[0]);
                        values.put(Images.Media.LONGITUDE, latlng[1]);
                    }
                    long time = exif.getGpsDateTime();
                    if (time != -1) {
                        values.put(Images.Media.DATE_TAKEN, time);
                    }
                    int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        int degree;
                        switch(orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                degree = 90;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                degree = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                degree = 270;
                                break;
                            default:
                                degree = 0;
                                break;
                        }
                        values.put(Images.Media.ORIENTATION, degree);
                    }
                }
            }
            Uri result = null;
            if (rowId == 0) {
                result = mMediaProvider.insert(tableUri, values);
                if (result != null) {
                    rowId = ContentUris.parseId(result);
                    entry.mRowId = rowId;
                }
            } else {
                result = ContentUris.withAppendedId(tableUri, rowId);
                mMediaProvider.update(result, values, null, null);
            }
            if (mProcessGenres && mGenre != null) {
                String genre = mGenre;
                Uri uri = mGenreCache.get(genre);
                if (uri == null) {
                    Cursor cursor = null;
                    try {
                        cursor = mMediaProvider.query(
                                mGenresUri,
                                GENRE_LOOKUP_PROJECTION, MediaStore.Audio.Genres.NAME + "=?",
                                        new String[] { genre }, null);
                        if (cursor == null || cursor.getCount() == 0) {
                            values.clear();
                            values.put(MediaStore.Audio.Genres.NAME, genre);
                            uri = mMediaProvider.insert(mGenresUri, values);
                        } else {
                            cursor.moveToNext();
                            uri = ContentUris.withAppendedId(mGenresUri, cursor.getLong(0));
                        }
                        if (uri != null) {
                            uri = Uri.withAppendedPath(uri, Genres.Members.CONTENT_DIRECTORY);
                            mGenreCache.put(genre, uri);
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                if (uri != null) {
                    values.clear();
                    values.put(MediaStore.Audio.Genres.Members.AUDIO_ID, Long.valueOf(rowId));
                    mMediaProvider.insert(uri, values);
                }
            }
            if (notifications && !mDefaultNotificationSet) {
                if (TextUtils.isEmpty(mDefaultNotificationFilename) ||
                        doesPathHaveFilename(entry.mPath, mDefaultNotificationFilename)) {
                    setSettingIfNotSet(Settings.System.NOTIFICATION_SOUND, tableUri, rowId);
                    mDefaultNotificationSet = true;
                }
            } else if (ringtones && !mDefaultRingtoneSet) {
                if (TextUtils.isEmpty(mDefaultRingtoneFilename) ||
                        doesPathHaveFilename(entry.mPath, mDefaultRingtoneFilename)) {
                    setSettingIfNotSet(Settings.System.RINGTONE, tableUri, rowId);
                    mDefaultRingtoneSet = true;
                }
            } else if (alarms && !mDefaultAlarmSet) {
                if (TextUtils.isEmpty(mDefaultAlarmAlertFilename) ||
                        doesPathHaveFilename(entry.mPath, mDefaultAlarmAlertFilename)) {
                    setSettingIfNotSet(Settings.System.ALARM_ALERT, tableUri, rowId);
                    mDefaultAlarmSet = true;
                }
            }
            return result;
        }
        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            return path.regionMatches(pathFilenameStart, filename, 0, filenameLength) &&
                    pathFilenameStart + filenameLength == path.length();
        }
        private void setSettingIfNotSet(String settingName, Uri uri, long rowId) {
            String existingSettingValue = Settings.System.getString(mContext.getContentResolver(),
                    settingName);
            if (TextUtils.isEmpty(existingSettingValue)) {
                Settings.System.putString(mContext.getContentResolver(), settingName,
                        ContentUris.withAppendedId(uri, rowId).toString());
            }
        }
        public void addNoMediaFolder(String path) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.ImageColumns.DATA, "");
            String [] pathSpec = new String[] {path + '%'};
            try {
                mMediaProvider.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
                        MediaStore.Images.ImageColumns.DATA + " LIKE ?", pathSpec);
                mMediaProvider.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values,
                        MediaStore.Images.ImageColumns.DATA + " LIKE ?", pathSpec);
            } catch (RemoteException e) {
                throw new RuntimeException();
            }
        }
    }; 
    private void prescan(String filePath) throws RemoteException {
        Cursor c = null;
        String where = null;
        String[] selectionArgs = null;
        if (mFileCache == null) {
            mFileCache = new HashMap<String, FileCacheEntry>();
        } else {
            mFileCache.clear();
        }
        if (mPlayLists == null) {
            mPlayLists = new ArrayList<FileCacheEntry>();
        } else {
            mPlayLists.clear();
        }
        try {
            if (filePath != null) {
                where = MediaStore.Audio.Media.DATA + "=?";
                selectionArgs = new String[] { filePath };
            }
            c = mMediaProvider.query(mAudioUri, AUDIO_PROJECTION, where, selectionArgs, null);
            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        long rowId = c.getLong(ID_AUDIO_COLUMN_INDEX);
                        String path = c.getString(PATH_AUDIO_COLUMN_INDEX);
                        long lastModified = c.getLong(DATE_MODIFIED_AUDIO_COLUMN_INDEX);
                        if (path.startsWith("/")) {
                            String key = path;
                            if (mCaseInsensitivePaths) {
                                key = path.toLowerCase();
                            }
                            mFileCache.put(key, new FileCacheEntry(mAudioUri, rowId, path,
                                    lastModified));
                        }
                    }
                } finally {
                    c.close();
                    c = null;
                }
            }
            if (filePath != null) {
                where = MediaStore.Video.Media.DATA + "=?";
            } else {
                where = null;
            }
            c = mMediaProvider.query(mVideoUri, VIDEO_PROJECTION, where, selectionArgs, null);
            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        long rowId = c.getLong(ID_VIDEO_COLUMN_INDEX);
                        String path = c.getString(PATH_VIDEO_COLUMN_INDEX);
                        long lastModified = c.getLong(DATE_MODIFIED_VIDEO_COLUMN_INDEX);
                        if (path.startsWith("/")) {
                            String key = path;
                            if (mCaseInsensitivePaths) {
                                key = path.toLowerCase();
                            }
                            mFileCache.put(key, new FileCacheEntry(mVideoUri, rowId, path,
                                    lastModified));
                        }
                    }
                } finally {
                    c.close();
                    c = null;
                }
            }
            if (filePath != null) {
                where = MediaStore.Images.Media.DATA + "=?";
            } else {
                where = null;
            }
            mOriginalCount = 0;
            c = mMediaProvider.query(mImagesUri, IMAGES_PROJECTION, where, selectionArgs, null);
            if (c != null) {
                try {
                    mOriginalCount = c.getCount();
                    while (c.moveToNext()) {
                        long rowId = c.getLong(ID_IMAGES_COLUMN_INDEX);
                        String path = c.getString(PATH_IMAGES_COLUMN_INDEX);
                       long lastModified = c.getLong(DATE_MODIFIED_IMAGES_COLUMN_INDEX);
                       if (path.startsWith("/")) {
                           String key = path;
                           if (mCaseInsensitivePaths) {
                               key = path.toLowerCase();
                           }
                           mFileCache.put(key, new FileCacheEntry(mImagesUri, rowId, path,
                                   lastModified));
                       }
                    }
                } finally {
                    c.close();
                    c = null;
                }
            }
            if (mProcessPlaylists) {
                if (filePath != null) {
                    where = MediaStore.Audio.Playlists.DATA + "=?";
                } else {
                    where = null;
                }
                c = mMediaProvider.query(mPlaylistsUri, PLAYLISTS_PROJECTION, where, selectionArgs, null);
                if (c != null) {
                    try {
                        while (c.moveToNext()) {
                            String path = c.getString(PATH_PLAYLISTS_COLUMN_INDEX);
                            if (path != null && path.length() > 0) {
                                long rowId = c.getLong(ID_PLAYLISTS_COLUMN_INDEX);
                                long lastModified = c.getLong(DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX);
                                String key = path;
                                if (mCaseInsensitivePaths) {
                                    key = path.toLowerCase();
                                }
                                mFileCache.put(key, new FileCacheEntry(mPlaylistsUri, rowId, path,
                                        lastModified));
                            }
                        }
                    } finally {
                        c.close();
                        c = null;
                    }
                }
            }
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }
    private boolean inScanDirectory(String path, String[] directories) {
        for (int i = 0; i < directories.length; i++) {
            if (path.startsWith(directories[i])) {
                return true;
            }
        }
        return false;
    }
    private void pruneDeadThumbnailFiles() {
        HashSet<String> existingFiles = new HashSet<String>();
        String directory = "/sdcard/DCIM/.thumbnails";
        String [] files = (new File(directory)).list();
        if (files == null)
            files = new String[0];
        for (int i = 0; i < files.length; i++) {
            String fullPathString = directory + "/" + files[i];
            existingFiles.add(fullPathString);
        }
        try {
            Cursor c = mMediaProvider.query(
                    mThumbsUri,
                    new String [] { "_data" },
                    null,
                    null,
                    null);
            Log.v(TAG, "pruneDeadThumbnailFiles... " + c);
            if (c != null && c.moveToFirst()) {
                do {
                    String fullPathString = c.getString(0);
                    existingFiles.remove(fullPathString);
                } while (c.moveToNext());
            }
            for (String fileToDelete : existingFiles) {
                if (Config.LOGV)
                    Log.v(TAG, "fileToDelete is " + fileToDelete);
                try {
                    (new File(fileToDelete)).delete();
                } catch (SecurityException ex) {
                }
            }
            Log.v(TAG, "/pruneDeadThumbnailFiles... " + c);
            if (c != null) {
                c.close();
            }
        } catch (RemoteException e) {
        }
    }
    private void postscan(String[] directories) throws RemoteException {
        Iterator<FileCacheEntry> iterator = mFileCache.values().iterator();
        while (iterator.hasNext()) {
            FileCacheEntry entry = iterator.next();
            String path = entry.mPath;
            boolean fileMissing = false;
            if (!entry.mSeenInFileSystem) {
                if (inScanDirectory(path, directories)) {
                    fileMissing = true;
                } else {
                    File testFile = new File(path);
                    if (!testFile.exists()) {
                        fileMissing = true;
                    }
                }
            }
            if (fileMissing) {
                MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);
                if (MediaFile.isPlayListFileType(fileType)) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Playlists.DATA, "");
                    values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, 0);
                    mMediaProvider.update(ContentUris.withAppendedId(mPlaylistsUri, entry.mRowId), values, null, null);
                } else {
                    mMediaProvider.delete(ContentUris.withAppendedId(entry.mTableUri, entry.mRowId), null, null);
                    iterator.remove();
                }
            }
        }
        if (mProcessPlaylists) {
            processPlayLists();
        }
        if (mOriginalCount == 0 && mImagesUri.equals(Images.Media.getContentUri("external")))
            pruneDeadThumbnailFiles();
        mGenreCache = null;
        mPlayLists = null;
        mFileCache = null;
        mMediaProvider = null;
    }
    private void initialize(String volumeName) {
        mMediaProvider = mContext.getContentResolver().acquireProvider("media");
        mAudioUri = Audio.Media.getContentUri(volumeName);
        mVideoUri = Video.Media.getContentUri(volumeName);
        mImagesUri = Images.Media.getContentUri(volumeName);
        mThumbsUri = Images.Thumbnails.getContentUri(volumeName);
        if (!volumeName.equals("internal")) {
            mProcessPlaylists = true;
            mProcessGenres = true;
            mGenreCache = new HashMap<String, Uri>();
            mGenresUri = Genres.getContentUri(volumeName);
            mPlaylistsUri = Playlists.getContentUri(volumeName);
            if ( Process.supportsProcesses()) {
                mCaseInsensitivePaths = true;
            }
        }
    }
    public void scanDirectories(String[] directories, String volumeName) {
        try {
            long start = System.currentTimeMillis();
            initialize(volumeName);
            prescan(null);
            long prescan = System.currentTimeMillis();
            for (int i = 0; i < directories.length; i++) {
                processDirectory(directories[i], MediaFile.sFileExtensions, mClient);
            }
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
            if (Config.LOGD) {
                Log.d(TAG, " prescan time: " + (prescan - start) + "ms\n");
                Log.d(TAG, "    scan time: " + (scan - prescan) + "ms\n");
                Log.d(TAG, "postscan time: " + (end - scan) + "ms\n");
                Log.d(TAG, "   total time: " + (end - start) + "ms\n");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
        }
    }
    public Uri scanSingleFile(String path, String volumeName, String mimeType) {
        try {
            initialize(volumeName);
            prescan(path);
            File file = new File(path);
            return mClient.doScanFile(path, mimeType, file.lastModified(), file.length(), true);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        }
    }
    private int matchPaths(String path1, String path2) {
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf('/', end1 - 1);
            int slash2 = path2.lastIndexOf('/', end2 - 1);
            int backSlash1 = path1.lastIndexOf('\\', end1 - 1);
            int backSlash2 = path2.lastIndexOf('\\', end2 - 1);
            int start1 = (slash1 > backSlash1 ? slash1 : backSlash1);
            int start2 = (slash2 > backSlash2 ? slash2 : backSlash2);
            if (start1 < 0) start1 = 0; else start1++;
            if (start2 < 0) start2 = 0; else start2++;
            int length = end1 - start1;
            if (end2 - start2 != length) break;
            if (path1.regionMatches(true, start1, path2, start2, length)) {
                result++;
                end1 = start1 - 1;
                end2 = start2 - 1;
            } else break;
        }
        return result;
    }
    private boolean addPlayListEntry(String entry, String playListDirectory,
            Uri uri, ContentValues values, int index) {
        int entryLength = entry.length();
        while (entryLength > 0 && Character.isWhitespace(entry.charAt(entryLength - 1))) entryLength--;
        if (entryLength < 3) return false;
        if (entryLength < entry.length()) entry = entry.substring(0, entryLength);
        char ch1 = entry.charAt(0);
        boolean fullPath = (ch1 == '/' ||
                (Character.isLetter(ch1) && entry.charAt(1) == ':' && entry.charAt(2) == '\\'));
        if (!fullPath)
            entry = playListDirectory + entry;
        FileCacheEntry bestMatch = null;
        int bestMatchLength = 0;
        Iterator<FileCacheEntry> iterator = mFileCache.values().iterator();
        while (iterator.hasNext()) {
            FileCacheEntry cacheEntry = iterator.next();
            String path = cacheEntry.mPath;
            if (path.equalsIgnoreCase(entry)) {
                bestMatch = cacheEntry;
                break;    
            }
            int matchLength = matchPaths(path, entry);
            if (matchLength > bestMatchLength) {
                bestMatch = cacheEntry;
                bestMatchLength = matchLength;
            }
        }
        if (bestMatch == null || ! mAudioUri.equals(bestMatch.mTableUri)) {
            return false;
        }
        try {
            values.clear();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(index));
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, Long.valueOf(bestMatch.mRowId));
            mMediaProvider.insert(uri, values);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.addPlayListEntry()", e);
            return false;
        }
        return true;
    }
    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values) {
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f)), 8192);
                String line = reader.readLine();
                int index = 0;
                while (line != null) {
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        values.clear();
                        if (addPlayListEntry(line, playListDirectory, uri, values, index))
                            index++;
                    }
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
            }
        }
    }
    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values) {
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f)), 8192);
                String line = reader.readLine();
                int index = 0;
                while (line != null) {
                    if (line.startsWith("File")) {
                        int equals = line.indexOf('=');
                        if (equals > 0) {
                            values.clear();
                            if (addPlayListEntry(line.substring(equals + 1), playListDirectory, uri, values, index))
                                index++;
                        }
                    }
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
            }
        }
    }
    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;
        Uri uri;
        ContentValues values = new ContentValues();
        int index = 0;
        public WplHandler(String playListDirectory, Uri uri) {
            this.playListDirectory = playListDirectory;
            this.uri = uri;
            RootElement root = new RootElement("smil");
            Element body = root.getChild("body");
            Element seq = body.getChild("seq");
            Element media = seq.getChild("media");
            media.setElementListener(this);
            this.handler = root.getContentHandler();
        }
        public void start(Attributes attributes) {
            String path = attributes.getValue("", "src");
            if (path != null) {
                values.clear();
                if (addPlayListEntry(path, playListDirectory, uri, values, index)) {
                    index++;
                }
            }
        }
       public void end() {
       }
        ContentHandler getContentHandler() {
            return handler;
        }
    }
    private void processWplPlayList(String path, String playListDirectory, Uri uri) {
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                fis = new FileInputStream(f);
                Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri).getContentHandler());
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e);
            }
        }
    }
    private void processPlayLists() throws RemoteException {
        Iterator<FileCacheEntry> iterator = mPlayLists.iterator();
        while (iterator.hasNext()) {
            FileCacheEntry entry = iterator.next();
            String path = entry.mPath;
            if (entry.mLastModifiedChanged) {
                ContentValues values = new ContentValues();
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash < 0) throw new IllegalArgumentException("bad path " + path);
                Uri uri, membersUri;
                long rowId = entry.mRowId;
                if (rowId == 0) {
                    int lastDot = path.lastIndexOf('.');
                    String name = (lastDot < 0 ? path.substring(lastSlash + 1) : path.substring(lastSlash + 1, lastDot));
                    values.put(MediaStore.Audio.Playlists.NAME, name);
                    values.put(MediaStore.Audio.Playlists.DATA, path);
                    values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, entry.mLastModified);
                    uri = mMediaProvider.insert(mPlaylistsUri, values);
                    rowId = ContentUris.parseId(uri);
                    membersUri = Uri.withAppendedPath(uri, Playlists.Members.CONTENT_DIRECTORY);
                } else {
                    uri = ContentUris.withAppendedId(mPlaylistsUri, rowId);
                    values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, entry.mLastModified);
                    mMediaProvider.update(uri, values, null, null);
                    membersUri = Uri.withAppendedPath(uri, Playlists.Members.CONTENT_DIRECTORY);
                    mMediaProvider.delete(membersUri, null, null);
                }
                String playListDirectory = path.substring(0, lastSlash + 1);
                MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);
                if (fileType == MediaFile.FILE_TYPE_M3U)
                    processM3uPlayList(path, playListDirectory, membersUri, values);
                else if (fileType == MediaFile.FILE_TYPE_PLS)
                    processPlsPlayList(path, playListDirectory, membersUri, values);
                else if (fileType == MediaFile.FILE_TYPE_WPL)
                    processWplPlayList(path, playListDirectory, membersUri);
                Cursor cursor = mMediaProvider.query(membersUri, PLAYLIST_MEMBERS_PROJECTION, null,
                        null, null);
                try {
                    if (cursor == null || cursor.getCount() == 0) {
                        Log.d(TAG, "playlist is empty - deleting");
                        mMediaProvider.delete(uri, null, null);
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
        }
    }
    private native void processDirectory(String path, String extensions, MediaScannerClient client);
    private native void processFile(String path, String mimeType, MediaScannerClient client);
    public native void setLocale(String locale);
    public native byte[] extractAlbumArt(FileDescriptor fd);
    private static native final void native_init();
    private native final void native_setup();
    private native final void native_finalize();
    @Override
    protected void finalize() {
        mContext.getContentResolver().releaseProvider(mMediaProvider);
        native_finalize();
    }
}
