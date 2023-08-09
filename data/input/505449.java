public class RecycleAccessibilityEventTest extends TestCase {
    private static final String CLASS_NAME = "foo.bar.baz.Test";
    private static final String PACKAGE_NAME = "foo.bar.baz";
    private static final String TEXT = "Some stuff";
    private static final String CONTENT_DESCRIPTION = "Content description";
    private static final int ITEM_COUNT = 10;
    private static final int CURRENT_ITEM_INDEX = 1;
    private static final int FROM_INDEX = 1;
    private static final int ADDED_COUNT = 2;
    private static final int REMOVED_COUNT = 1;
    @MediumTest
    public void testAccessibilityEventViewTextChangedType() {
        AccessibilityEvent first =
            AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        assertNotNull(first);
        first.setClassName(CLASS_NAME);
        first.setPackageName(PACKAGE_NAME);
        first.getText().add(TEXT);
        first.setFromIndex(FROM_INDEX);
        first.setAddedCount(ADDED_COUNT);
        first.setRemovedCount(REMOVED_COUNT);
        first.setChecked(true);
        first.setContentDescription(CONTENT_DESCRIPTION);
        first.setItemCount(ITEM_COUNT);
        first.setCurrentItemIndex(CURRENT_ITEM_INDEX);
        first.setEnabled(true);
        first.setPassword(true);
        first.recycle();
        assertNotNull(first);
        assertNull(first.getClassName());
        assertNull(first.getPackageName());
        assertEquals(0, first.getText().size());
        assertFalse(first.isChecked());
        assertNull(first.getContentDescription());
        assertEquals(0, first.getItemCount());
        assertEquals(AccessibilityEvent.INVALID_POSITION, first.getCurrentItemIndex());
        assertFalse(first.isEnabled());
        assertFalse(first.isPassword());
        assertEquals(0, first.getFromIndex());
        assertEquals(0, first.getAddedCount());
        assertEquals(0, first.getRemovedCount());
        AccessibilityEvent second = AccessibilityEvent.obtain();
        assertEquals(first, second);
    }
}
