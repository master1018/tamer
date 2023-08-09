public class PhotoAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "PhotoAppWidgetProvider";
    private static final boolean LOGD = true;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PhotoDatabaseHelper helper = new PhotoDatabaseHelper(context);
        for (int appWidgetId : appWidgetIds) {
            int[] specificAppWidget = new int[] { appWidgetId };
            RemoteViews views = buildUpdate(context, appWidgetId, helper);
            if (LOGD) {
                Log.d(TAG, "sending out views=" + views + " for id=" + appWidgetId);
            }
            appWidgetManager.updateAppWidget(specificAppWidget, views);
        }
        helper.close();
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        PhotoDatabaseHelper helper = new PhotoDatabaseHelper(context);
        for (int appWidgetId : appWidgetIds) {
            helper.deletePhoto(appWidgetId);
        }
        helper.close();
    }
    static RemoteViews buildUpdate(Context context, int appWidgetId, PhotoDatabaseHelper helper) {
        RemoteViews views = null;
        Bitmap bitmap = helper.getPhoto(appWidgetId);
        if (bitmap != null) {
            views = new RemoteViews(context.getPackageName(), Res.layout.photo_frame);
            views.setImageViewBitmap(Res.id.photo, bitmap);
        }
        return views;
    }
    static class PhotoDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "launcher.db";
        private static final int DATABASE_VERSION = 2;
        static final String TABLE_PHOTOS = "photos";
        static final String FIELD_APPWIDGET_ID = "appWidgetId";
        static final String FIELD_PHOTO_BLOB = "photoBlob";
        PhotoDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_PHOTOS + " (" + FIELD_APPWIDGET_ID + " INTEGER PRIMARY KEY," + FIELD_PHOTO_BLOB
                    + " BLOB" + ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;
            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
                onCreate(db);
            }
        }
        public boolean setPhoto(int appWidgetId, Bitmap bitmap) {
            boolean success = false;
            try {
                int size = bitmap.getWidth() * bitmap.getHeight() * 4;
                ByteArrayOutputStream out = new ByteArrayOutputStream(size);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                ContentValues values = new ContentValues();
                values.put(PhotoDatabaseHelper.FIELD_APPWIDGET_ID, appWidgetId);
                values.put(PhotoDatabaseHelper.FIELD_PHOTO_BLOB, out.toByteArray());
                SQLiteDatabase db = getWritableDatabase();
                db.insertOrThrow(PhotoDatabaseHelper.TABLE_PHOTOS, null, values);
                success = true;
            } catch (SQLiteException e) {
                Log.e(TAG, "Could not open database", e);
            } catch (IOException e) {
                Log.e(TAG, "Could not serialize photo", e);
            }
            if (LOGD) {
                Log.d(TAG, "setPhoto success=" + success);
            }
            return success;
        }
        static final String[] PHOTOS_PROJECTION = { FIELD_PHOTO_BLOB, };
        static final int INDEX_PHOTO_BLOB = 0;
        public Bitmap getPhoto(int appWidgetId) {
            Cursor c = null;
            Bitmap bitmap = null;
            try {
                SQLiteDatabase db = getReadableDatabase();
                String selection = String.format("%s=%d", FIELD_APPWIDGET_ID, appWidgetId);
                c = db.query(TABLE_PHOTOS, PHOTOS_PROJECTION, selection, null, null, null, null, null);
                if (c != null && LOGD) {
                    Log.d(TAG, "getPhoto query count=" + c.getCount());
                }
                if (c != null && c.moveToFirst()) {
                    byte[] data = c.getBlob(INDEX_PHOTO_BLOB);
                    if (data != null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Could not load photo from database", e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return bitmap;
        }
        public void deletePhoto(int appWidgetId) {
            try {
                SQLiteDatabase db = getWritableDatabase();
                String whereClause = String.format("%s=%d", FIELD_APPWIDGET_ID, appWidgetId);
                db.delete(TABLE_PHOTOS, whereClause, null);
            } catch (SQLiteException e) {
                Log.e(TAG, "Could not delete photo from database", e);
            }
        }
    }
}
