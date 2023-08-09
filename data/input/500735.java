public class CalendarDatabaseHelperTest extends TestCase {
    private SQLiteDatabase mBadDb;
    private SQLiteDatabase mGoodDb;
    private DatabaseUtils.InsertHelper mBadEventsInserter;
    private DatabaseUtils.InsertHelper mGoodEventsInserter;
    @Override
    public void setUp() {
        mBadDb = SQLiteDatabase.create(null);
        assertNotNull(mBadDb);
        mGoodDb = SQLiteDatabase.create(null);
        assertNotNull(mGoodDb);
    }
    private void createVersion67EventsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Events (" +
                "_id INTEGER PRIMARY KEY," +
                "_sync_account TEXT," +
                "_sync_account_type TEXT," +
                "_sync_id TEXT," +
                "_sync_version TEXT," +
                "_sync_time TEXT," +            
                "_sync_local_id INTEGER," +
                "_sync_dirty INTEGER," +
                "_sync_mark INTEGER," + 
                "calendar_id INTEGER NOT NULL," +
                "htmlUri TEXT," +
                "title TEXT," +
                "eventLocation TEXT," +
                "description TEXT," +
                "eventStatus INTEGER," +
                "selfAttendeeStatus INTEGER NOT NULL DEFAULT 0," +
                "commentsUri TEXT," +
                "dtstart INTEGER," +               
                "dtend INTEGER," +                 
                "eventTimezone TEXT," +         
                "duration TEXT," +
                "allDay INTEGER NOT NULL DEFAULT 0," +
                "visibility INTEGER NOT NULL DEFAULT 0," +
                "transparency INTEGER NOT NULL DEFAULT 0," +
                "hasAlarm INTEGER NOT NULL DEFAULT 0," +
                "hasExtendedProperties INTEGER NOT NULL DEFAULT 0," +
                "rrule TEXT," +
                "rdate TEXT," +
                "exrule TEXT," +
                "exdate TEXT," +
                "originalEvent TEXT," +  
                "originalInstanceTime INTEGER," +  
                "originalAllDay INTEGER," +
                "lastDate INTEGER," +               
                "hasAttendeeData INTEGER NOT NULL DEFAULT 0," +
                "guestsCanModify INTEGER NOT NULL DEFAULT 0," +
                "guestsCanInviteOthers INTEGER NOT NULL DEFAULT 1," +
                "guestsCanSeeGuests INTEGER NOT NULL DEFAULT 1," +
                "organizer STRING," +
                "deleted INTEGER NOT NULL DEFAULT 0," +
                "dtstart2 INTEGER," + 
                "dtend2 INTEGER," + 
                "eventTimezone2 TEXT," + 
                "syncAdapterData TEXT" + 
                ");");
    }
    private void addVersion67Events() {
        mBadDb.execSQL("INSERT INTO Events (_id,dtstart,dtend,duration,dtstart2,dtend2," +
                "eventTimezone,eventTimezone2,allDay,calendar_id) " +
                "VALUES (1,1270454471000,1270540872000,'P10S'," +
                "1270454460000,1270540861000,'America/Los_Angeles','America/Los_Angeles',1,1);");
        mGoodDb.execSQL("INSERT INTO Events (_id,dtstart,dtend,duration,dtstart2,dtend2," +
                "eventTimezone,eventTimezone2,allDay,calendar_id) " +
                "VALUES (1,1270425600000,1270512000000,null," +
                "1270450800000,1270537200000,'UTC','America/Los_Angeles',1,1);");
        mBadDb.execSQL("INSERT INTO Events (_id,dtstart,dtend,duration,dtstart2,dtend2," +
                "eventTimezone,eventTimezone2,allDay,rrule,calendar_id) " +
                "VALUES (2,1270454462000,1270540863000," +
                "'P10S',1270454461000,1270540861000,'America/Los_Angeles','America/Los_Angeles',1," +
                "'WEEKLY:MON',1);");
        mGoodDb.execSQL("INSERT INTO Events (" +
                "_id,dtstart,dtend,duration,dtstart2,dtend2," +
                "eventTimezone,eventTimezone2,allDay,rrule,calendar_id)" +
                "VALUES (2,1270425600000,null,'P1D',1270450800000,null," +
                "'UTC','America/Los_Angeles',1," +
                "'WEEKLY:MON',1);");
        assertEquals(mBadDb.rawQuery("SELECT _id FROM Events;", null).getCount(), 2);
        assertEquals(mGoodDb.rawQuery("SELECT _id FROM Events;", null).getCount(), 2);
    }
    @MediumTest
    public void testUpgradeToVersion69() {
        createVersion67EventsTable(mBadDb);
        createVersion67EventsTable(mGoodDb);
        addVersion67Events();
        CalendarDatabaseHelper.upgradeToVersion69(mBadDb);
        Cursor badCursor = null;
        Cursor goodCursor = null;
        try {
            badCursor = mBadDb.rawQuery("SELECT _id,dtstart,dtend,duration,dtstart2,dtend2," +
                    "eventTimezone,eventTimezone2,rrule FROM Events WHERE allDay=?",
                    new String[] {"1"});
            goodCursor = mGoodDb.rawQuery("SELECT _id,dtstart,dtend,duration,dtstart2,dtend2," +
                    "eventTimezone,eventTimezone2,rrule FROM Events WHERE allDay=?",
                    new String[] {"1"});
            assertTrue(compareCursors(badCursor, goodCursor));
        } finally {
            if (badCursor != null) {
                badCursor.close();
            }
            if (goodCursor != null) {
                goodCursor.close();
            }
        }
    }
    private static boolean compareCursors(Cursor c1, Cursor c2) {
        if(c1 == null || c2 == null) {
            Log.d("CDBT","c1 is " + c1 + " and c2 is " + c2);
            return false;
        }
        int numColumns = c1.getColumnCount();
        if (numColumns != c2.getColumnCount()) {
            Log.d("CDBT","c1 has " + numColumns + " columns and c2 has " + c2.getColumnCount());
            return false;
        }
        if (c1.getCount() != c2.getCount()) {
            Log.d("CDBT","c1 has " + c1.getCount() + " rows and c2 has " + c2.getCount());
            return false;
        }
        c1.moveToPosition(-1);
        c2.moveToPosition(-1);
        while(c1.moveToNext() && c2.moveToNext()) {
            for(int i = 0; i < numColumns; i++) {
                if(!TextUtils.equals(c1.getString(i),c2.getString(i))) {
                    Log.d("CDBT", c1.getString(i) + "\n" + c2.getString(i));
                    return false;
                }
            }
        }
        return true;
    }
}
