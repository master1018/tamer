public class NewDatabasePerformanceTests {
  final static int kMultiplier = 1;
  public static class PerformanceBase extends TestCase
          implements PerformanceTestCase {
    protected static final int CURRENT_DATABASE_VERSION = 42;
    protected SQLiteDatabase mDatabase;
    protected File mDatabaseFile;
    public void setUp() {
      mDatabaseFile = new File("/sdcard", "perf_database_test.db");
      if (mDatabaseFile.exists()) {
        mDatabaseFile.delete();
      }
      mDatabase =
        SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(),
            null);
      assertTrue(mDatabase != null);
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
  public static class Insert1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private String[] statements = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.execSQL(statements[i]);
      }
    }
  }
  public static class InsertIndexed1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private String[] statements = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.execSQL(statements[i]);
      }
    }
  }
  public static class Select100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"count(*)", "avg(b)"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectStringComparison100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"count(*)", "avg(b)"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectIndex100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"count(*)", "avg(b)"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class InnerJoin100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"t1.a"};
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      mDatabase.query("t1 INNER JOIN t2 ON t1.b = t2.b", COLUMNS, null,
          null, null, null, null);
    }
  }
  public static class InnerJoinOneSide100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"t1.a"};
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      mDatabase.query("t1 INNER JOIN t2 ON t1.b = t2.b", COLUMNS, null,
          null, null, null, null);
    }
  }
  public static class InnerJoinNoIndex100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"t1.a"};
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      mDatabase.query("t1 INNER JOIN t2 ON t1.c = t2.c", COLUMNS, null,
          null, null, null, null);
    }
  }
  public static class SelectSubQIndex100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"t1.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectIndexStringComparison100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"count(*)", "avg(b)"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectInteger100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"b"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
      Random random = new Random(42);
      mDatabase
      .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
      for (int i = 0; i < SIZE; i++) {
        int r = random.nextInt(100000);
        mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
            + numberName(r) + "')");
      }
    }
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t1", COLUMNS, null, null, null, null, null);
      }
    }
  }
  public static class SelectString100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"c"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
      Random random = new Random(42);
      mDatabase
      .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
      for (int i = 0; i < SIZE; i++) {
        int r = random.nextInt(100000);
        mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
            + numberName(r) + "')");
      }
    }
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t1", COLUMNS, null, null, null, null, null);
      }
    }
  }
  public static class SelectIntegerIndex100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"b"};
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t1", COLUMNS, null, null, null, null, null);
      }
    }
  }
  public static class SelectIndexString100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"c"};      
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t1", COLUMNS, null, null, null, null, null);
      }
    }
  }
  public static class SelectStringStartsWith100 extends PerformanceBase {
    private static final int SIZE = 1 * kMultiplier;
    private static final String[] COLUMNS = {"c"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase
        .query("t1", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class DeleteIndexed1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private static final String[] COLUMNS = {"c"};
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.delete("t1", null, null);
      }
    }
  }
  public static class Delete1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private static final String[] COLUMNS = {"c"};       
    @Override
    public void setUp() {
      super.setUp();
      Random random = new Random(42);
      mDatabase
      .execSQL("CREATE TABLE t1(a INTEGER, b INTEGER, c VARCHAR(100))");
      for (int i = 0; i < SIZE; i++) {
        int r = random.nextInt(100000);
        mDatabase.execSQL("INSERT INTO t1 VALUES(" + i + "," + r + ",'"
            + numberName(r) + "')");
      }
    }
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.delete("t1", null, null);
      }
    }
  }
  public static class DeleteWhere1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.delete("t1", where[i], null);
      }
    }
  }
  public static class DeleteIndexWhere1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.delete("t1", where[i], null);
      }
    }
  }
  public static class UpdateIndexWhere1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;
    private String[] where = new String[SIZE];
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.update("t1", mValues[i], where[i], null);
      }
    }
  }
  public static class UpdateWhere1000 extends PerformanceBase {
    private static final int SIZE = 10 * kMultiplier;       
    private String[] where = new String[SIZE];
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.update("t1", mValues[i], where[i], null);
      }
    }
  }
  public static class InsertInteger10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.insert("t1", null, mValues[i]);
      }
    }
  }
  public static class InsertIntegerIndex10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.insert("t1", null, mValues[i]);
      }
    }
  }
  public static class InsertString10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.insert("t1", null, mValues[i]);
      }
    }
  }
  public static class InsertStringIndexed10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;       
    ContentValues[] mValues = new ContentValues[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.insert("t1", null, mValues[i]);
      }
    }
  }
  public static class SelectStringStartsWith10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t3.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectStringIndexedStartsWith10000 extends
  PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t3.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectInteger10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t4.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t4", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectIntegerIndexed10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t4.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t4", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectStringContains10000 extends PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t3.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
      for (int i = 0; i < SIZE; i++) {
        mDatabase.query("t3", COLUMNS, where[i], null, null, null, null);
      }
    }
  }
  public static class SelectStringIndexedContains10000 extends
  PerformanceBase {
    private static final int SIZE = 100 * kMultiplier;
    private static final String[] COLUMNS = {"t3.a"};
    private String[] where = new String[SIZE];
    @Override
    public void setUp() {
      super.setUp();
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
    public void testRun() {
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
