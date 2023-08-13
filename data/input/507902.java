public class DatabaseTestUtils {
    public static void assertSchemaEquals(SQLiteDatabase expectedDb, SQLiteDatabase db) {
        Set<String> expectedSchema = getSchemaSet(expectedDb);
        Set<String> schema = getSchemaSet(db);
        MoreAsserts.assertEquals(expectedSchema, schema);
    }
    private static Set<String> getSchemaSet(SQLiteDatabase db) {
        Set<String> schemaSet = Sets.newHashSet();
        Cursor entityCursor = db.rawQuery("SELECT sql FROM sqlite_master", null);
        try {
            while (entityCursor.moveToNext()) {
                String sql = entityCursor.getString(0);
                schemaSet.add(sql);
            }
        } finally {
            entityCursor.close();
        }
        return schemaSet;
    }
}
