public class CursorWindowTest extends TestCase implements PerformanceTestCase {
    public boolean isPerformanceOnly() {
        return false;
    }
    public int startPerformance(Intermediates intermediates) {
        return 1;
    }
    @SmallTest
    public void testWriteCursorToWindow() throws Exception {
        String[] colNames = new String[]{"name", "number", "profit"};
        int colsize = colNames.length;
        ArrayList<ArrayList> list = createTestList(10, colsize);
        AbstractCursor cursor = new ArrayListCursor(colNames, (ArrayList<ArrayList>) list);
        CursorWindow window = new CursorWindow(false);
        cursor.fillWindow(0, window);
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Integer> col = list.get(i);
            for (int j = 0; j < colsize; j++) {
                String s = window.getString(i, j);
                int r2 = col.get(j);
                int r1 = Integer.parseInt(s);
                assertEquals(r2, r1);
            }
        }
        window.clear();
        cursor.fillWindow(1, window);
        for (int i = 1; i < list.size(); i++) {
            ArrayList<Integer> col = list.get(i);
            for (int j = 0; j < colsize; j++) {
                String s = window.getString(i, j);
                int r2 = col.get(j);
                int r1 = Integer.parseInt(s);
                assertEquals(r2, r1);
            }
        }
        window.clear();
        assertEquals(0, window.getNumRows());
    }
    @SmallTest
    public void testValuesLocalWindow() {
        doTestValues(new CursorWindow(true));
    }
    @SmallTest
    public void testValuesRemoteWindow() {
        doTestValues(new CursorWindow(false));
    }
    private void doTestValues(CursorWindow window) {
        assertTrue(window.setNumColumns(7));
        assertTrue(window.allocRow());
        double db1 = 1.26;
        assertTrue(window.putDouble(db1, 0, 0));
        double db2 = window.getDouble(0, 0);
        assertEquals(db1, db2);
        long int1 = Long.MAX_VALUE;
        assertTrue(window.putLong(int1, 0, 1));
        long int2 = window.getLong(0, 1);
        assertEquals(int1, int2);
        assertTrue(window.putString("1198032740000", 0, 3));
        assertEquals("1198032740000", window.getString(0, 3));
        assertEquals(1198032740000L, window.getLong(0, 3));
        assertTrue(window.putString(Long.toString(1198032740000L), 0, 3));
        assertEquals(Long.toString(1198032740000L), window.getString(0, 3));
        assertEquals(1198032740000L, window.getLong(0, 3));
        assertTrue(window.putString(Double.toString(42.0), 0, 4));
        assertEquals(Double.toString(42.0), window.getString(0, 4));
        assertEquals(42.0, window.getDouble(0, 4));
        byte[] blob = new byte[1000];
        byte value = 99;
        Arrays.fill(blob, value);
        assertTrue(window.putBlob(blob, 0, 6));
        assertTrue(Arrays.equals(blob, window.getBlob(0, 6)));
    }
    @SmallTest
    public void testNull() {
        CursorWindow window = getOneByOneWindow();
        assertTrue(window.putNull(0, 0));
        assertNull(window.getString(0, 0));
        assertEquals(0, window.getLong(0, 0));
        assertEquals(0.0, window.getDouble(0, 0));
        assertNull(window.getBlob(0, 0));
    }
    @SmallTest
    public void testEmptyString() {
        CursorWindow window = getOneByOneWindow();
        assertTrue(window.putString("", 0, 0));
        assertEquals("", window.getString(0, 0));
        assertEquals(0, window.getLong(0, 0));
        assertEquals(0.0, window.getDouble(0, 0));
    }
    private CursorWindow getOneByOneWindow() {
        CursorWindow window = new CursorWindow(false);
        assertTrue(window.setNumColumns(1));
        assertTrue(window.allocRow());
        return window;
    }
    private static ArrayList<ArrayList> createTestList(int rows, int cols) {
        ArrayList<ArrayList> list = Lists.newArrayList();
        Random generator = new Random();
        for (int i = 0; i < rows; i++) {
            ArrayList<Integer> col = Lists.newArrayList();
            list.add(col);
            for (int j = 0; j < cols; j++) {
                Integer r = generator.nextInt();
                col.add(r);
            }
        }
        return list;
    }
}
