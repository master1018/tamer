public class DayOfMonthCursorTest extends TestCase {
    @SmallTest
    public void testMonthRows() {
        DayOfMonthCursor mc = new DayOfMonthCursor(2007,
                Calendar.SEPTEMBER, 11, Calendar.SUNDAY);
        assertArraysEqual(new int[]{26, 27, 28, 29, 30, 31, 1},
                mc.getDigitsForRow(0));
        assertArraysEqual(new int[]{2, 3, 4, 5, 6, 7, 8},
                mc.getDigitsForRow(1));
        assertArraysEqual(new int[]{30, 1, 2, 3, 4, 5, 6},
                mc.getDigitsForRow(5));
    }
    @SmallTest
    public void testMoveLeft() {
        DayOfMonthCursor mc = new DayOfMonthCursor(2007,
                Calendar.SEPTEMBER, 3, Calendar.SUNDAY);
        assertEquals(Calendar.SEPTEMBER, mc.getMonth());
        assertEquals(3, mc.getSelectedDayOfMonth());
        assertEquals(1, mc.getSelectedRow());
        assertEquals(1, mc.getSelectedColumn());
        assertFalse(mc.left());
        assertEquals(2, mc.getSelectedDayOfMonth());
        assertEquals(1, mc.getSelectedRow());
        assertEquals(0, mc.getSelectedColumn());
        assertFalse(mc.left());
        assertEquals(1, mc.getSelectedDayOfMonth());
        assertEquals(0, mc.getSelectedRow());
        assertEquals(6, mc.getSelectedColumn());
        assertTrue(mc.left());
        assertEquals(Calendar.AUGUST, mc.getMonth());
        assertEquals(31, mc.getSelectedDayOfMonth());
        assertEquals(4, mc.getSelectedRow());
        assertEquals(5, mc.getSelectedColumn());
    }
    @SmallTest
    public void testMoveRight() {
        DayOfMonthCursor mc = new DayOfMonthCursor(2007,
                Calendar.SEPTEMBER, 28, Calendar.SUNDAY);
        assertEquals(Calendar.SEPTEMBER, mc.getMonth());
        assertEquals(28, mc.getSelectedDayOfMonth());
        assertEquals(4, mc.getSelectedRow());
        assertEquals(5, mc.getSelectedColumn());
        assertFalse(mc.right());
        assertEquals(29, mc.getSelectedDayOfMonth());
        assertEquals(4, mc.getSelectedRow());
        assertEquals(6, mc.getSelectedColumn());
        assertFalse(mc.right());
        assertEquals(30, mc.getSelectedDayOfMonth());
        assertEquals(5, mc.getSelectedRow());
        assertEquals(0, mc.getSelectedColumn());
        assertTrue(mc.right());
        assertEquals(Calendar.OCTOBER, mc.getMonth());
        assertEquals(1, mc.getSelectedDayOfMonth());
        assertEquals(0, mc.getSelectedRow());
        assertEquals(1, mc.getSelectedColumn());
    }
    @SmallTest
    public void testMoveUp() {
        DayOfMonthCursor mc = new DayOfMonthCursor(2007,
                Calendar.SEPTEMBER, 13, Calendar.SUNDAY);
        assertEquals(Calendar.SEPTEMBER, mc.getMonth());
        assertEquals(13, mc.getSelectedDayOfMonth());
        assertEquals(2, mc.getSelectedRow());
        assertEquals(4, mc.getSelectedColumn());
        assertFalse(mc.up());
        assertEquals(6, mc.getSelectedDayOfMonth());
        assertEquals(1, mc.getSelectedRow());
        assertEquals(4, mc.getSelectedColumn());
        assertTrue(mc.up());
        assertEquals(Calendar.AUGUST, mc.getMonth());
        assertEquals(30, mc.getSelectedDayOfMonth());
        assertEquals(4, mc.getSelectedRow());
        assertEquals(4, mc.getSelectedColumn());
    }
    @SmallTest
    public void testMoveDown() {
        DayOfMonthCursor mc = new DayOfMonthCursor(2007,
                Calendar.SEPTEMBER, 23, Calendar.SUNDAY);
        assertEquals(Calendar.SEPTEMBER, mc.getMonth());
        assertEquals(23, mc.getSelectedDayOfMonth());
        assertEquals(4, mc.getSelectedRow());
        assertEquals(0, mc.getSelectedColumn());
        assertFalse(mc.down());
        assertEquals(30, mc.getSelectedDayOfMonth());
        assertEquals(5, mc.getSelectedRow());
        assertEquals(0, mc.getSelectedColumn());
        assertTrue(mc.down());
        assertEquals(Calendar.OCTOBER, mc.getMonth());
        assertEquals(7, mc.getSelectedDayOfMonth());
        assertEquals(1, mc.getSelectedRow());
        assertEquals(0, mc.getSelectedColumn());
    }
    private void assertArraysEqual(int[] expected, int[] actual) {
        assertEquals("array length", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("index " + i,
                    expected[i], actual[i]);
        }
    }
}
