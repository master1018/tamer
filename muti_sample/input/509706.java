@TestTargetClass(AbsSavedState.class)
public class AbsSavedStateTest extends InstrumentationTestCase {
    public static final int TEST_NUMBER = 1;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor and describeContents of AbsSavedState",
            method = "AbsSavedState",
            args = {android.os.Parcelable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor and describeContents of AbsSavedState",
            method = "AbsSavedState",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor and describeContents of AbsSavedState",
            method = "describeContents",
            args = {}
        )
    })
    public void testConstructor() {
        MockParcelable superState = new MockParcelable();
        assertNotNull(superState);
        new MockAbsSavedState(superState);
        Parcel source = Parcel.obtain();
        new MockAbsSavedState(source);
        MockAbsSavedState savedState = new MockAbsSavedState(source);
        assertEquals(0, savedState.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSuperState function",
        method = "getSuperState",
        args = {}
    )
    public void testGetSuperState() {
        MockParcelable superState = new MockParcelable();
        assertNotNull(superState);
        MockAbsSavedState savedState = new MockAbsSavedState(superState);
        assertSame(superState, savedState.getSuperState());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel function",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        MockParcelable superState = new MockParcelable();
        assertNotNull(superState);
        MockAbsSavedState savedState = new MockAbsSavedState(superState);
        Parcel dest = Parcel.obtain();
        int flags = 2;
        savedState.writeToParcel(dest, flags);
        assertEquals(TEST_NUMBER, superState.writeToParcelRunSymbol());
        assertEquals(flags, superState.getFlags());
    }
    static class MockAbsSavedState extends AbsSavedState {
        public MockAbsSavedState(Parcelable superState) {
            super(superState);
        }
        public MockAbsSavedState(Parcel source) {
            super(source);
        }
    }
    static class MockParcelable implements Parcelable {
        private int mTest;
        private int mFlags;
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            mTest = TEST_NUMBER;
            mFlags = flags;
        }
        public int writeToParcelRunSymbol() {
            return mTest;
        }
        public int getFlags() {
            return mFlags;
        }
    }
}
