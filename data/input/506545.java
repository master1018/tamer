public class MatrixCursorTest extends TestCase {
    public void testEmptyCursor() {
        Cursor cursor = new MatrixCursor(new String[] { "a" });
        assertEquals(0, cursor.getCount());
    }
    public void testNullValue() {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a" });
        cursor.newRow().add(null);
        cursor.moveToNext();
        assertTrue(cursor.isNull(0));
        assertNull(cursor.getString(0));
        assertEquals(0, cursor.getShort(0));
        assertEquals(0, cursor.getInt(0));
        assertEquals(0L, cursor.getLong(0));
        assertEquals(0.0f, cursor.getFloat(0));
        assertEquals(0.0d, cursor.getDouble(0));
    }
    public void testMatrixCursor() {
        MatrixCursor cursor = newMatrixCursor();
        cursor.newRow()
                .add("a")
                .add(1)
                .add(2)
                .add(3)
                .add(4)
                .add(5);
        cursor.moveToNext();
        checkValues(cursor);
        cursor.newRow()
                .add("a")
                .add("1")
                .add("2")
                .add("3")
                .add("4")
                .add("5");
        cursor.moveToNext();
        checkValues(cursor);
        cursor.moveToPrevious();
        checkValues(cursor);
    }
    public void testAddArray() {
        MatrixCursor cursor = newMatrixCursor();
        cursor.addRow(new Object[] { "a", 1, 2, 3, 4, 5 });
        cursor.moveToNext();
        checkValues(cursor);
        try {
            cursor.addRow(new Object[0]);
            fail();
        } catch (IllegalArgumentException e) {  }
    }
    public void testAddIterable() {
        MatrixCursor cursor = newMatrixCursor();
        cursor.addRow(Arrays.asList("a", 1, 2, 3, 4, 5));
        cursor.moveToNext();
        checkValues(cursor);
        try {
            cursor.addRow(Collections.emptyList());
            fail();
        } catch (IllegalArgumentException e) {  }
        try {
            cursor.addRow(Arrays.asList("a", 1, 2, 3, 4, 5, "Too many!"));
            fail();
        } catch (IllegalArgumentException e) {  }
    }
    public void testAddArrayList() {
        MatrixCursor cursor = newMatrixCursor();
        cursor.addRow(new NonIterableArrayList<Object>(
                Arrays.asList("a", 1, 2, 3, 4, 5)));
        cursor.moveToNext();
        checkValues(cursor);
        try {
            cursor.addRow(new NonIterableArrayList<Object>());
            fail();
        } catch (IllegalArgumentException e) {  }
        try {
            cursor.addRow(new NonIterableArrayList<Object>(
                    Arrays.asList("a", 1, 2, 3, 4, 5, "Too many!")));
            fail();
        } catch (IllegalArgumentException e) {  }
    }
    static class NonIterableArrayList<T> extends ArrayList<T> {
        NonIterableArrayList() {}
        NonIterableArrayList(Collection<? extends T> ts) {
            super(ts);
        }
        @Override
        public Iterator<T> iterator() {
            throw new AssertionError();
        }
    }
    private MatrixCursor newMatrixCursor() {
        return new MatrixCursor(new String[] {
                "string", "short", "int", "long", "float", "double" });
    }
    private void checkValues(MatrixCursor cursor) {
        assertEquals("a", cursor.getString(0));
        assertEquals(1, cursor.getShort(1));
        assertEquals(2, cursor.getInt(2));
        assertEquals(3, cursor.getLong(3));
        assertEquals(4.0f, cursor.getFloat(4));
        assertEquals(5.0D, cursor.getDouble(5));
    }
}
