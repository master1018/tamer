public class MediaStoreAudioTestHelper {
    public static abstract class MockAudioMediaInfo {
        public abstract ContentValues getContentValues(boolean isInternal);
        public Uri insertToInternal(ContentResolver contentResolver) {
            Uri uri = contentResolver.insert(Media.INTERNAL_CONTENT_URI, getContentValues(true));
            Assert.assertNotNull(uri);
            return uri;
        }
        public Uri insertToExternal(ContentResolver contentResolver) {
            Uri uri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, getContentValues(false));
            Assert.assertNotNull(uri);
            return uri;
        }
        public int delete(ContentResolver contentResolver, Uri uri) {
            return contentResolver.delete(uri, null, null);
        }
    }
    public static class Audio1 extends MockAudioMediaInfo {
        private Audio1() {
        }
        private static Audio1 sInstance = new Audio1();
        public static Audio1 getInstance() {
            return sInstance;
        }
        public static final int IS_RINGTONE = 0;
        public static final int IS_NOTIFICATION = 0;
        public static final int IS_ALARM = 0;
        public static final int IS_MUSIC = 1;
        public static final int YEAR = 1992;
        public static final int TRACK = 1;
        public static final int DURATION = 340000;
        public static final String COMPOSER = "Bruce Swedien";
        public static final String ARTIST = "Michael Jackson";
        public static final String ALBUM = "Dangerous";
        public static final String TITLE = "Jam";
        public static final int SIZE = 2737870;
        public static final String MIME_TYPE = "audio/x-mpeg";
        public static final String DISPLAY_NAME = "Jam -Michael Jackson";
        public static final String INTERNAL_DATA =
            "/data/data/com.android.cts.stub/files/Jam.mp3";
        public static final String FILE_NAME = "Jam.mp3";
        public static final String EXTERNAL_DATA = Environment.getExternalStorageDirectory() +
                "/" + FILE_NAME;
        public static final long DATE_MODIFIED = System.currentTimeMillis();
        public static final String GENRE = "POP";
        @Override
        public ContentValues getContentValues(boolean isInternal) {
            ContentValues values = new ContentValues();
            values.put(Media.DATA, isInternal ? INTERNAL_DATA : EXTERNAL_DATA);
            values.put(Media.DATE_MODIFIED, DATE_MODIFIED);
            values.put(Media.DISPLAY_NAME, DISPLAY_NAME);
            values.put(Media.MIME_TYPE, MIME_TYPE);
            values.put(Media.SIZE, SIZE);
            values.put(Media.TITLE, TITLE);
            values.put(Media.ALBUM, ALBUM);
            values.put(Media.ARTIST, ARTIST);
            values.put(Media.COMPOSER, COMPOSER);
            values.put(Media.DURATION, DURATION);
            values.put(Media.TRACK, TRACK);
            values.put(Media.YEAR, YEAR);
            values.put(Media.IS_MUSIC, IS_MUSIC);
            values.put(Media.IS_ALARM, IS_ALARM);
            values.put(Media.IS_NOTIFICATION, IS_NOTIFICATION);
            values.put(Media.IS_RINGTONE, IS_RINGTONE);
            return values;
        }
    }
    public static class Audio2 extends MockAudioMediaInfo {
        private Audio2() {
        }
        private static Audio2 sInstance = new Audio2();
        public static Audio2 getInstance() {
            return sInstance;
        }
        public static final int IS_RINGTONE = 1;
        public static final int IS_NOTIFICATION = 0;
        public static final int IS_ALARM = 0;
        public static final int IS_MUSIC = 0;
        public static final int YEAR = 1992;
        public static final int TRACK = 1001;
        public static final int DURATION = 338000;
        public static final String COMPOSER = "Bruce Swedien";
        public static final String ARTIST =
            "Michael Jackson - Live And Dangerous - National Stadium Bucharest";
        public static final String ALBUM =
            "Michael Jackson - Live And Dangerous - National Stadium Bucharest";
        public static final String TITLE = "Jam";
        public static final int SIZE = 2737321;
        public static final String MIME_TYPE = "audio/x-mpeg";
        public static final String DISPLAY_NAME = "Jam(Live)-Michael Jackson";
        public static final String FILE_NAME = "Jam_live.mp3";
        public static final String EXTERNAL_DATA =
            Environment.getExternalStorageDirectory().getPath() + "/" + FILE_NAME;
        public static final String INTERNAL_DATA =
            "/data/data/com.android.cts.stub/files/Jam_live.mp3";
        public static final long DATE_MODIFIED = System.currentTimeMillis();
        @Override
        public ContentValues getContentValues(boolean isInternal) {
            ContentValues values = new ContentValues();
            values.put(Media.DATA, isInternal ? INTERNAL_DATA : EXTERNAL_DATA);
            values.put(Media.DATE_MODIFIED, DATE_MODIFIED);
            values.put(Media.DISPLAY_NAME, DISPLAY_NAME);
            values.put(Media.MIME_TYPE, MIME_TYPE);
            values.put(Media.SIZE, SIZE);
            values.put(Media.TITLE, TITLE);
            values.put(Media.ALBUM, ALBUM);
            values.put(Media.ARTIST, ARTIST);
            values.put(Media.COMPOSER, COMPOSER);
            values.put(Media.DURATION, DURATION);
            values.put(Media.TRACK, TRACK);
            values.put(Media.YEAR, YEAR);
            values.put(Media.IS_MUSIC, IS_MUSIC);
            values.put(Media.IS_ALARM, IS_ALARM);
            values.put(Media.IS_NOTIFICATION, IS_NOTIFICATION);
            values.put(Media.IS_RINGTONE, IS_RINGTONE);
            return values;
        }
    }
    public static final String EXTERNAL_VOLUME_NAME = "external";
    public static final String INTERNAL_VOLUME_NAME = "internal";
}
