@TestTargetClass(RowBuilder.class)
public class MatrixCursor_RowBuilderTest extends TestCase {
    private static final int COLUMN0_INDEX = 0;
    private static final int COLUMN1_INDEX = 1;
    private static final int COLUMN2_INDEX = 2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "add",
        args = {java.lang.Object.class}
    )
    public void testAdd() {
        MatrixCursor cursor = new MatrixCursor(new String[] { "column0", "column1", "column2" });
        assertEquals(0, cursor.getCount());
        RowBuilder builder = cursor.newRow();
        assertNotNull(builder);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertTrue(cursor.isNull(COLUMN0_INDEX));
        assertTrue(cursor.isNull(COLUMN1_INDEX));
        assertTrue(cursor.isNull(COLUMN2_INDEX));
        builder.add(Integer.MIN_VALUE);
        assertFalse(cursor.isNull(COLUMN0_INDEX));
        assertEquals(Integer.MIN_VALUE, cursor.getInt(COLUMN0_INDEX));
        assertTrue(cursor.isNull(COLUMN1_INDEX));
        assertTrue(cursor.isNull(COLUMN2_INDEX));
        builder.add(0);
        assertFalse(cursor.isNull(COLUMN0_INDEX));
        assertEquals(Integer.MIN_VALUE, cursor.getInt(COLUMN0_INDEX));
        assertFalse(cursor.isNull(COLUMN1_INDEX));
        assertEquals(0, cursor.getInt(COLUMN1_INDEX));
        assertTrue(cursor.isNull(COLUMN2_INDEX));
        builder.add(Integer.MAX_VALUE);
        assertFalse(cursor.isNull(COLUMN0_INDEX));
        assertEquals(Integer.MIN_VALUE, cursor.getInt(COLUMN0_INDEX));
        assertFalse(cursor.isNull(COLUMN1_INDEX));
        assertEquals(0, cursor.getInt(COLUMN1_INDEX));
        assertFalse(cursor.isNull(COLUMN2_INDEX));
        assertEquals(Integer.MAX_VALUE, cursor.getInt(COLUMN2_INDEX));
        try {
            builder.add(1);
            fail("Should throw CursorIndexOutOfBoundsException when adding too many values");
        } catch (CursorIndexOutOfBoundsException e) {
        }
        assertNotNull(cursor.newRow());
        assertEquals(2, cursor.getCount());
    }
}
