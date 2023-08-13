public class DatabasePerformanceTests {
    public static String[] children() {
        return new String[] {
            ContactReadingTest1.class.getName(),
            Perf1Test.class.getName(),
            Perf2Test.class.getName(),
            Perf3Test.class.getName(),
            Perf4Test.class.getName(),
            Perf5Test.class.getName(),
            Perf6Test.class.getName(),
            Perf7Test.class.getName(),
            Perf8Test.class.getName(),
            Perf9Test.class.getName(),
            Perf10Test.class.getName(),
            Perf11Test.class.getName(),
            Perf12Test.class.getName(),
            Perf13Test.class.getName(),
            Perf14Test.class.getName(),
            Perf15Test.class.getName(),
            Perf16Test.class.getName(),
            Perf17Test.class.getName(),
            Perf18Test.class.getName(),
            Perf19Test.class.getName(),
            Perf20Test.class.getName(),
            Perf21Test.class.getName(),
            Perf22Test.class.getName(),
            Perf23Test.class.getName(),
            Perf24Test.class.getName(),
            Perf25Test.class.getName(),
            Perf26Test.class.getName(),
            Perf27Test.class.getName(),
            Perf28Test.class.getName(),
            Perf29Test.class.getName(),
            Perf30Test.class.getName(),
            Perf31Test.class.getName(),
            };
    }
    public static abstract class PerformanceBase implements TestCase,
            PerformanceTestCase {
        protected static final int CURRENT_DATABASE_VERSION = 42;
        protected SQLiteDatabase mDatabase;
        protected File mDatabaseFile;
        protected Context mContext;
        public void setUp(Context c) {
            mContext = c;
            mDatabaseFile = new File("/tmp", "perf_database_test.db");
            if (mDatabaseFile.exists()) {
                mDatabaseFile.delete();
            }
            mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
            Assert.assertTrue(mDatabase != null);
            mDatabase.setVersion(CURRENT_DATABASE_VERSION);
        }
        public void tearDown() {
            mDatabase.close();
            mDatabaseFile.delete();
        }
        public boolean isPerformanceOnly() {
            return true;
        }
        public int startPerformance(Intermediates intermediates) {
            return 0;
        }
        public void run() {
        }
        public String numberName(int number) {
            String result = "";
            if (number >= 1000) {
                result += numberName((number / 1000)) + " thousand";
                number = (number % 1000);
                if (number > 0) result += " ";
            }
            if (number >= 100) {
                result += ONES[(number / 100)] + " hundred";
                number = (number % 100);
                if (number > 0) result += " ";
            }
            if (number >= 20) {
                result += TENS[(number / 10)];
                number = (number % 10);
                if (number > 0) result += " ";
            }
            if (number > 0) {
                result += ONES[number];
            }
            return result;
        }
    }
    public static class ContactReadingTest1 implements TestCase, PerformanceTestCase {
        private static final String[] PEOPLE_PROJECTION = new String[] {
               Contacts.People._ID, 
               Contacts.People.PRIMARY_PHONE_ID, 
               Contacts.People.TYPE, 
               Contacts.People.NUMBER, 
               Contacts.People.LABEL, 
               Contacts.People.NAME, 
               Contacts.People.PRESENCE_STATUS, 
        };
        private Cursor mCursor;
        public void setUp(Context c) {
            mCursor = c.getContentResolver().query(People.CONTENT_URI, PEOPLE_PROJECTION, null,
                    null, People.DEFAULT_SORT_ORDER);
        }
        public void tearDown() {
            mCursor.close();
        }
        public boolean isPerformanceOnly() {
            return true;
        }
        public int startPerformance(Intermediates intermediates) {
            return 0;
        }
        public void run() {
            while (mCursor.moveToNext()) {
                mCursor.getLong(0);
                mCursor.getLong(1);
                mCursor.getLong(2);
                mCursor.getString(3);
                mCursor.getString(4);
                mCursor.getString(5);
                mCursor.getLong(6);
            }
        }
    }
    public static class Perf1Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private String[] statements = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                statements[i] =
                        "INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                                + numberName(r) + "')";
            }
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.execSQL(statements[i]);
            }
        }
    }
    public static class Perf2Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private String[] statements = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                statements[i] =
                        "INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                                + numberName(r) + "')";
            }
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1c ON t1(c)");
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.execSQL(statements[i]);
            }
        }
    }
    public static class Perf3Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"count(*)", "avg(b)"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf4Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"count(*)", "avg(b)"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                where[i] = "c LIKE '" + numberName(i) + "'";
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf5Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"count(*)", "avg(b)"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b ON t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf6Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"t1.a"};
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase
              .execSQL("CREATE TABLE t2(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t2 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            mDatabase.query("t1 INNER JOIN t2 ON t1.b = t2.b", COLUMNS, null,
                    null, null, null, null);
        }
    }
    public static class Perf7Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"t1.a"};
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase
              .execSQL("CREATE TABLE t2(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b ON t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t2 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            mDatabase.query("t1 INNER JOIN t2 ON t1.b = t2.b", COLUMNS, null,
                    null, null, null, null);
        }
    }
    public static class Perf8Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"t1.a"};
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase
              .execSQL("CREATE TABLE t2(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b ON t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t2 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            mDatabase.query("t1 INNER JOIN t2 ON t1.c = t2.c", COLUMNS, null,
                    null, null, null, null);
        }
    }
    public static class Perf9Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"t1.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase
              .execSQL("CREATE TABLE t2(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i2b ON t2(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t2 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] =
                        "t1.b IN (SELECT t2.b FROM t2 WHERE t2.b >= " + lower
                                + " AND t2.b < " + upper + ")";
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf10Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"count(*)", "avg(b)"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i3c ON t1(c)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                where[i] = "c LIKE '" + numberName(i) + "'";
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf11Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"b"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t1", COLUMNS, null, null, null, null, null);
            }
        }
    }
    public static class Perf12Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"c"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t1", COLUMNS, null, null, null, null, null);
            }
        }
    }
    public static class Perf13Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"b"};
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b on t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t1", COLUMNS, null, null, null, null, null);
            }
        }
    }
    public static class Perf14Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"c"};      
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1c ON t1(c)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t1", COLUMNS, null, null, null, null, null);
            }
        }
    }
    public static class Perf15Test extends PerformanceBase {
        private static final int SIZE = 100;
        private static final String[] COLUMNS = {"c"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1c ON t1(c)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                where[i] = "c LIKE '" + numberName(r).substring(0, 1) + "*'";
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase
                        .query("t1", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf16Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private static final String[] COLUMNS = {"c"};
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i3c ON t1(c)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.delete("t1", null, null);
            }
        }
    }
    public static class Perf17Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private static final String[] COLUMNS = {"c"};       
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.delete("t1", null, null);
            }
        }
    }
    public static class Perf18Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.delete("t1", where[i], null);
            }
        }
    }
    public static class Perf19Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b ON t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.delete("t1", where[i], null);
            }
        }
    }
    public static class Perf20Test extends PerformanceBase {
        private static final int SIZE = 1000;
        private String[] where = new String[SIZE];
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1b ON t1(b)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
                ContentValues b = new ContentValues(1);
                b.put("b", upper);
                mValues[i] = b;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.update("t1", mValues[i], where[i], null);
            }
        }
    }
    public static class Perf21Test extends PerformanceBase {
        private static final int SIZE = 1000;       
        private String[] where = new String[SIZE];
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "b >= " + lower + " AND b < " + upper;
                ContentValues b = new ContentValues(1);
                b.put("b", upper);
                mValues[i] = b;
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.update("t1", mValues[i], where[i], null);
            }
        }
    }
    public static class Perf22Test extends PerformanceBase {
        private static final int SIZE = 10000;
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                ContentValues b = new ContentValues(1);
                b.put("a", r);
                mValues[i] = b;
            }
        }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.insert("t1", null, mValues[i]);
            }
        }
    }
    public static class Perf23Test extends PerformanceBase {
        private static final int SIZE = 10000;
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a INTEGER)");
            mDatabase.execSQL("CREATE INDEX i1a ON t1(a)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                ContentValues b = new ContentValues(1);
                b.put("a", r);
                mValues[i] = b;
            }
        }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.insert("t1", null, mValues[i]);
            }
        }
    }
    public static class Perf24Test extends PerformanceBase {
        private static final int SIZE = 10000;
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                ContentValues b = new ContentValues(1);
                b.put("a", numberName(r));
                mValues[i] = b;
            }
        }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.insert("t1", null, mValues[i]);
            }
        }
    }
    public static class Perf25Test extends PerformanceBase {
        private static final int SIZE = 10000;       
        ContentValues[] mValues = new ContentValues[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t1(a VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i1a ON t1(a)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                ContentValues b = new ContentValues(1);
                b.put("a", numberName(r));
                mValues[i] = b; 
            }
        }
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.insert("t1", null, mValues[i]);
            }
        }
    }
    public static class Perf26Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t3.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t3(a VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t3 VALUES('"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                where[i] = "a LIKE '" + numberName(r).substring(0, 1) + "*'";
            }
        }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf27Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t3.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t3(a VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i3a ON t3(a)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t3 VALUES('"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                where[i] = "a LIKE '" + numberName(r).substring(0, 1) + "*'";
            }                              
           }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf28Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t4.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t4(a INTEGER)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t4 VALUES(" + r + ")");
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "a >= " + lower + " AND a < " + upper;
            }
           }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t4", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf29Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t4.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t4(a INTEGER)");
           mDatabase.execSQL("CREATE INDEX i4a ON t4(a)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t4 VALUES(" + r + ")");
                int lower = i * 100;
                int upper = (i + 10) * 100;
                where[i] = "a >= " + lower + " AND a < " + upper;
            }
           }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t4", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf30Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t3.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t3(a VARCHAR(100))");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t3 VALUES('"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                 where[i] = "a LIKE '*e*'";
            }                              
           }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static class Perf31Test extends PerformanceBase {
        private static final int SIZE = 10000;
        private static final String[] COLUMNS = {"t3.a"};
        private String[] where = new String[SIZE];
        @Override
        public void setUp(Context c) {
            super.setUp(c);
            Random random = new Random(42);
            mDatabase
              .execSQL("CREATE TABLE t3(a VARCHAR(100))");
            mDatabase.execSQL("CREATE INDEX i3a ON t3(a)");
            for (int i = 0; i < SIZE; i++) {
                int r = random.nextInt(100000);
                mDatabase.execSQL("INSERT INTO t3 VALUES('"
                        + numberName(r) + "')");
            }
            for (int i = 0; i < SIZE; i++) {
                where[i] = "a LIKE '*e*'";
            }                              
           }        
        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
            }
        }
    }
    public static final String[] ONES =
            {"zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten", "eleven", "twelve", "thirteen",
                "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
                "nineteen"};
    public static final String[] TENS =
            {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty",
                "seventy", "eighty", "ninety"};
}
