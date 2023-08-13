public class SQLiteContentHelper {
    public static AssetFileDescriptor getBlobColumnAsAssetFile(SQLiteDatabase db, String sql,
            String[] selectionArgs) throws FileNotFoundException {
        try {
            MemoryFile file = simpleQueryForBlobMemoryFile(db, sql, selectionArgs);
            if (file == null) {
                throw new FileNotFoundException("No results.");
            }
            return AssetFileDescriptor.fromMemoryFile(file);
        } catch (IOException ex) {
            throw new FileNotFoundException(ex.toString());
        }
    }
    private static MemoryFile simpleQueryForBlobMemoryFile(SQLiteDatabase db, String sql,
            String[] selectionArgs) throws IOException {
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            byte[] bytes = cursor.getBlob(0);
            if (bytes == null) {
                return null;
            }
            MemoryFile file = new MemoryFile(null, bytes.length);
            file.writeBytes(bytes, 0, 0, bytes.length);
            file.deactivate();
            return file;
        } finally {
            cursor.close();
        }
    }
}
