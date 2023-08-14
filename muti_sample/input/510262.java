@TestTargetClass(AbstractCursor.class)
public class AbstractCursor_SelfContentObserverTest extends AndroidTestCase{
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor of SelfContentObserver",
        method = "SelfContentObserver",
        args = {}
    )
    public void testConstructor() {
        MockAbstractCursor mac = new MockAbstractCursor();
        mac.getMockSelfContentObserver();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test deliverSelfNotifications of SelfContentObserver",
        method = "deliverSelfNotifications",
        args = {}
    )
    public void testDeliverSelfNotifications() {
        MockAbstractCursor mac = new MockAbstractCursor();
        MockAbstractCursor.MockSelfContentObserver msc = mac.getMockSelfContentObserver();
        assertFalse(msc.deliverSelfNotifications());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onChange of SelfContentObserver",
        method = "onChange",
        args = {boolean.class}
    )
    public void testOnChange() {
        MockAbstractCursor mockCursor = new MockAbstractCursor();
        MockAbstractCursor.MockSelfContentObserver msc = mockCursor.getMockSelfContentObserver();
        mockCursor.registerContentObserver(msc);
        mockCursor.onChange(false);
        assertTrue(msc.mIsOnChangeCalled);
        assertFalse(msc.mOnChangeResult);
        msc.mIsOnChangeCalled = false;
        mockCursor.onChange(true);
        assertFalse(msc.mIsOnChangeCalled);
        assertFalse(msc.mOnChangeResult);
        msc.mIsOnChangeCalled = false;
        msc.setDeliverSelfNotificationsValue(true);
        mockCursor.onChange(true);
        assertTrue(msc.mIsOnChangeCalled);
        assertTrue(msc.mOnChangeResult);
    }
    class MockAbstractCursor extends AbstractCursor {
        @Override
        public String[] getColumnNames() {
            return null;
        }
        @Override
        public int getCount() {
            return 0;
        }
        @Override
        public double getDouble(int column) {
            return 0;
        }
        @Override
        public float getFloat(int column) {
            return 0;
        }
        @Override
        public int getInt(int column) {
            return 0;
        }
        @Override
        public long getLong(int column) {
            return 0;
        }
        @Override
        public short getShort(int column) {
            return 0;
        }
        @Override
        public String getString(int column) {
            return null;
        }
        @Override
        public boolean isNull(int column) {
            return false;
        }
        public MockSelfContentObserver getMockSelfContentObserver() {
            return new MockSelfContentObserver(new MockAbstractCursor());
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }
        public class MockSelfContentObserver extends AbstractCursor.SelfContentObserver {
            public MockAbstractCursor mMockAbstractCursor;
            public boolean mIsOnChangeCalled;
            public boolean mOnChangeResult;
            private boolean mIsTrue;
            public MockSelfContentObserver(AbstractCursor cursor) {
                super(cursor);
                mMockAbstractCursor = (MockAbstractCursor) cursor;
            }
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mOnChangeResult = selfChange;
                mIsOnChangeCalled = true;
            }
            @Override
            public boolean deliverSelfNotifications() {
                if (mIsTrue) {
                    return true;
                }
                return super.deliverSelfNotifications();
            }
            public void setDeliverSelfNotificationsValue(boolean isTrue) {
                mIsTrue = isTrue;
            }
        }
    }
}
