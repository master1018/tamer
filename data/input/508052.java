@TestTargetClass(SparseBooleanArray.class)
public class SparseBooleanArrayTest extends AndroidTestCase {
    private static final int[] KEYS   = {12, 23, 4, 6, 8, 1, 3, -12, 0, -3, 11, 14, -23};
    private static final boolean[] VALUES =
        {true,  false, true, false, false, true, true, true, true, false, false, false, false};
    private static final int NON_EXISTED_KEY = 123;
    private static final boolean VALUE_FOR_NON_EXISTED_KEY = true;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SparseBooleanArray",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "append",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "delete",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "indexOfKey",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "indexOfValue",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "keyAt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "put",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "size",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "valueAt",
            args = {int.class}
        )
    })
    public void testSparseBooleanArrayWithDefaultCapacity() {
        SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
        assertEquals(0, sparseBooleanArray.size());
        int length = VALUES.length;
        for (int i = 0; i < length; i++) {
            sparseBooleanArray.put(KEYS[i], VALUES[i]);
            assertEquals(i + 1, sparseBooleanArray.size());
        }
        for (int i = 0; i < length; i++) {
            assertEquals(VALUES[i], sparseBooleanArray.get(KEYS[i]));
        }
        int truePos = sparseBooleanArray.indexOfValue(true);
        int falsePos = sparseBooleanArray.indexOfValue(false);
        int expectPos;
        int keyIndex;
        for (int i = 0; i < length; i++) {
            keyIndex = sparseBooleanArray.indexOfKey(KEYS[i]);
            assertEquals(VALUES[i], sparseBooleanArray.valueAt(keyIndex));
            expectPos = VALUES[i] ? truePos : falsePos;
            assertEquals(expectPos, sparseBooleanArray.indexOfValue(VALUES[i]));
        }
        int existKey = KEYS[0];
        boolean oldValue = VALUES[0]; 
        boolean newValue = false;
        assertEquals(oldValue, sparseBooleanArray.get(existKey));
        assertEquals(13, sparseBooleanArray.size());
        sparseBooleanArray.put(existKey, newValue);
        assertEquals(newValue, sparseBooleanArray.get(existKey));
        assertEquals(13, sparseBooleanArray.size());
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                     sparseBooleanArray.get(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(false, sparseBooleanArray.get(NON_EXISTED_KEY)); 
        int size = sparseBooleanArray.size();
        sparseBooleanArray.append(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY);
        assertEquals(size + 1, sparseBooleanArray.size());
        assertEquals(size, sparseBooleanArray.indexOfKey(NON_EXISTED_KEY));
        expectPos = VALUE_FOR_NON_EXISTED_KEY ? truePos : falsePos;
        assertEquals(expectPos, sparseBooleanArray.indexOfValue(VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(NON_EXISTED_KEY, sparseBooleanArray.keyAt(size));
        assertEquals(VALUE_FOR_NON_EXISTED_KEY, sparseBooleanArray.valueAt(size));
        assertEquals(VALUES[1], sparseBooleanArray.get(KEYS[1]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[1]);
        sparseBooleanArray.delete(KEYS[1]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseBooleanArray.get(KEYS[1], VALUE_FOR_NON_EXISTED_KEY));
        sparseBooleanArray.clear();
        assertEquals(0, sparseBooleanArray.size());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SparseBooleanArray",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "append",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "delete",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "indexOfKey",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "indexOfValue",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "keyAt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "put",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "size",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "valueAt",
            args = {int.class}
        )
    })
    public void testSparseBooleanArrayWithSpecifiedCapacity() {
        SparseBooleanArray sparseBooleanArray = new SparseBooleanArray(5);
        assertEquals(0, sparseBooleanArray.size());
        int length = VALUES.length;
        for (int i = 0; i < length; i++) {
            sparseBooleanArray.put(KEYS[i], VALUES[i]);
            assertEquals(i + 1, sparseBooleanArray.size());
        }
        for (int i = 0; i < length; i++) {
            assertEquals(VALUES[i], sparseBooleanArray.get(KEYS[i]));
        }
        int truePos = sparseBooleanArray.indexOfValue(true);
        int falsePos = sparseBooleanArray.indexOfValue(false);
        int expectPos;
        int keyIndex;
        for (int i = 0; i < length; i++) {
            keyIndex = sparseBooleanArray.indexOfKey(KEYS[i]);
            assertEquals(VALUES[i], sparseBooleanArray.valueAt(keyIndex));
            expectPos = VALUES[i] ? truePos : falsePos;
            assertEquals(expectPos, sparseBooleanArray.indexOfValue(VALUES[i]));
        }
        int existKey = KEYS[0];
        boolean oldValue = VALUES[0]; 
        boolean newValue = false;
        assertEquals(oldValue, sparseBooleanArray.get(existKey));
        assertEquals(13, sparseBooleanArray.size());
        sparseBooleanArray.put(existKey, newValue);
        assertEquals(newValue, sparseBooleanArray.get(existKey));
        assertEquals(13, sparseBooleanArray.size());
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                     sparseBooleanArray.get(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(false, sparseBooleanArray.get(NON_EXISTED_KEY)); 
        int size = sparseBooleanArray.size();
        sparseBooleanArray.append(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY);
        assertEquals(size + 1, sparseBooleanArray.size());
        assertEquals(size, sparseBooleanArray.indexOfKey(NON_EXISTED_KEY));
        expectPos = VALUE_FOR_NON_EXISTED_KEY ? truePos : falsePos;
        assertEquals(expectPos, sparseBooleanArray.indexOfValue(VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(NON_EXISTED_KEY, sparseBooleanArray.keyAt(size));
        assertEquals(VALUE_FOR_NON_EXISTED_KEY, sparseBooleanArray.valueAt(size));
        assertEquals(VALUES[1], sparseBooleanArray.get(KEYS[1]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[1]);
        sparseBooleanArray.delete(KEYS[1]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseBooleanArray.get(KEYS[1], VALUE_FOR_NON_EXISTED_KEY));
        sparseBooleanArray.clear();
        assertEquals(0, sparseBooleanArray.size());
    }
}
