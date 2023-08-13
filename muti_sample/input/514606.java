public class MediaMetadataRetrieverTest extends AndroidTestCase {
    private static final String TAG         = "MediaMetadataRetrieverTest";
    @MediumTest
    public static void testAlbumArt() throws Exception {
        Log.v(TAG, "testAlbumArt starts.");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        boolean supportWMA = MediaProfileReader.getWMAEnable();
        boolean hasFailed = false;
        boolean supportWMV = MediaProfileReader.getWMVEnable();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        for (int i = 0, n = MediaNames.ALBUMART_TEST_FILES.length; i < n; ++i) {
            try {
                Log.v(TAG, "File " + i + ": " + MediaNames.ALBUMART_TEST_FILES[i]);
                if ((MediaNames.ALBUMART_TEST_FILES[i].endsWith(".wma") && !supportWMA) ||
                    (MediaNames.ALBUMART_TEST_FILES[i].endsWith(".wmv") && !supportWMV)
                   ) {
                    Log.v(TAG, "windows media is not supported and thus we will skip the test for this file");
                    continue;
                }
                retriever.setDataSource(MediaNames.ALBUMART_TEST_FILES[i]);
                byte[] albumArt = retriever.extractAlbumArt();
                if (albumArt == null) {  
                    Log.e(TAG, "Fails to extract album art for " + MediaNames.ALBUMART_TEST_FILES[i]);
                    hasFailed = true;
                }
            } catch(Exception e) {
                Log.e(TAG, "Fails to setDataSource for " + MediaNames.ALBUMART_TEST_FILES[i]);
                hasFailed = true;
            }
            Thread.yield();  
        }
        retriever.release();
        Log.v(TAG, "testAlbumArt completes.");
        assertTrue(!hasFailed);
    }
    @LargeTest
    public static void testThumbnailCapture() throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        boolean supportWMA = MediaProfileReader.getWMAEnable();
        boolean supportWMV = MediaProfileReader.getWMVEnable();
        boolean hasFailed = false;
        Log.v(TAG, "Thumbnail processing starts");
        long startedAt = System.currentTimeMillis();
        for(int i = 0, n = MediaNames.THUMBNAIL_CAPTURE_TEST_FILES.length; i < n; ++i) {
            try {
                Log.v(TAG, "File " + i + ": " + MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i]);
                if ((MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i].endsWith(".wma") && !supportWMA) ||
                    (MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i].endsWith(".wmv") && !supportWMV)
                   ) {
                    Log.v(TAG, "windows media is not supported and thus we will skip the test for this file");
                    continue;
                }
                retriever.setDataSource(MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i]);
                Bitmap bitmap = retriever.captureFrame();
                assertTrue(bitmap != null);
                try {
                    java.io.OutputStream stream = new FileOutputStream(MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i] + ".jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    stream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Fails to convert the bitmap to a JPEG file for " + MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i]);
                    hasFailed = true;
                }
            } catch(Exception e) {
                Log.e(TAG, "Fails to setDataSource for file " + MediaNames.THUMBNAIL_CAPTURE_TEST_FILES[i]);
                hasFailed = true;
            }
            Thread.yield();  
        }
        long endedAt = System.currentTimeMillis();
        retriever.release();
        assertTrue(!hasFailed);
        Log.v(TAG, "Average processing time per thumbnail: " + (endedAt - startedAt)/MediaNames.THUMBNAIL_CAPTURE_TEST_FILES.length + " ms");
    }
    @LargeTest
    public static void testMetadataRetrieval() throws Exception {
        boolean supportWMA = MediaProfileReader.getWMAEnable();
        boolean supportWMV = MediaProfileReader.getWMVEnable();
        boolean hasFailed = false;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        for(int i = 0, n = MediaNames.METADATA_RETRIEVAL_TEST_FILES.length; i < n; ++i) {
            try {
                Log.v(TAG, "File " + i + ": " + MediaNames.METADATA_RETRIEVAL_TEST_FILES[i]);
                if ((MediaNames.METADATA_RETRIEVAL_TEST_FILES[i].endsWith(".wma") && !supportWMA) ||
                    (MediaNames.METADATA_RETRIEVAL_TEST_FILES[i].endsWith(".wmv") && !supportWMV)
                   ) {
                    Log.v(TAG, "windows media is not supported and thus we will skip the test for this file");
                    continue;
                }
                retriever.setDataSource(MediaNames.METADATA_RETRIEVAL_TEST_FILES[i]);
                extractAllSupportedMetadataValues(retriever);
            } catch(Exception e) {
                Log.e(TAG, "Fails to setDataSource for file " + MediaNames.METADATA_RETRIEVAL_TEST_FILES[i]);
                hasFailed = true;
            }
            Thread.yield();  
        }
        retriever.release();
        assertTrue(!hasFailed);
    }
    @MediumTest
    public static void testBasicNormalMethodCallSequence() throws Exception {
        boolean hasFailed = false;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        try {
            retriever.setDataSource(MediaNames.TEST_PATH_1);
            extractAllSupportedMetadataValues(retriever);
        } catch(Exception e) {
            Log.e(TAG, "Fails to setDataSource for " + MediaNames.TEST_PATH_1, e);
            hasFailed = true;
        }
        retriever.release();
        assertTrue(!hasFailed);
    }
    @MediumTest
    public static void testBasicAbnormalMethodCallSequence() {
        boolean hasFailed = false;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null) {
            Log.e(TAG, "No album metadata expected, but is available");
            hasFailed = true;
        }
        if (retriever.captureFrame() != null) {
            Log.e(TAG, "No frame expected, but is available");
            hasFailed = true;
        }
        assertTrue(!hasFailed);
    }
    @MediumTest
    public static void testSetDataSource() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        boolean hasFailed = false;
        try {
            String path = null;
            retriever.setDataSource(path);
            Log.e(TAG, "IllegalArgumentException failed to be thrown.");
            hasFailed = true;
        } catch(Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                Log.e(TAG, "Expected a IllegalArgumentException, but got a different exception");
                hasFailed = true;
            }
        }
        try {
            retriever.setDataSource(MediaNames.TEST_PATH_5);
            Log.e(TAG, "IllegalArgumentException failed to be thrown.");
            hasFailed = true;
        } catch(Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                Log.e(TAG, "Expected a IllegalArgumentException, but got a different exception");
                hasFailed = true;
            }
        }
        try {
            retriever.setDataSource(MediaNames.TEST_PATH_4);
            Log.e(TAG, "RuntimeException failed to be thrown.");
            hasFailed = true;
        } catch(Exception e) {
            if (!(e instanceof RuntimeException)) {
                Log.e(TAG, "Expected a RuntimeException, but got a different exception");
                hasFailed = true;
            }
        }
        try {
            retriever.setDataSource(MediaNames.TEST_PATH_3);
            Log.e(TAG, "RuntimeException failed to be thrown.");
            hasFailed = true;
        } catch(Exception e) {
            if (!(e instanceof RuntimeException)) {
                Log.e(TAG, "Expected a RuntimeException, but got a different exception");
                hasFailed = true;
            }
        }
        retriever.release();
        assertTrue(!hasFailed);
    }
    @MediumTest
    public static void testIntendedUsage() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        boolean hasFailed = false;
        retriever.setMode(MediaMetadataRetriever.MODE_CAPTURE_FRAME_ONLY & MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        retriever.setDataSource(MediaNames.TEST_PATH_1);
        if (retriever.captureFrame() != null) {
            Log.e(TAG, "No frame expected, but is available");
            hasFailed = true;
        }
        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS) != null) {
            Log.e(TAG, "No num track metadata expected, but is available");
            hasFailed = true;
        }
        retriever.setMode(MediaMetadataRetriever.MODE_GET_METADATA_ONLY);
        retriever.setDataSource(MediaNames.TEST_PATH_1);
        if (retriever.captureFrame() != null) {
            Log.e(TAG, "No frame expected, but is available");
            hasFailed = true;
        }
        retriever.release();
        assertTrue(!hasFailed);
    }
    private static void extractAllSupportedMetadataValues(MediaMetadataRetriever retriever) {
        String value = null;
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)) == null? "not found": value);
        Log.v(TAG, (value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)) == null? "not found": value);
    }
}
