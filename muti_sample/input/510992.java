@TestTargetClass(Number.class) 
public class NumberTest extends junit.framework.TestCase {
    class MockNumber extends Number {
        @Override
        public double doubleValue() {
            return 0;
        }
        @Override
        public float floatValue() {
            return 0;
        }
        @Override
        public int intValue() {
            return 0;
        }
        @Override
        public long longValue() {
            return 0;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Number",
        args = {}
    )
    public void test_Number() {
        MockNumber number = new MockNumber();
        assertEquals(0, number.longValue());
        assertEquals(0, number.shortValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "byteValue",
        args = {}
    )
    public void test_byteValue() {
        int number = 1231243;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = 0;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = -1;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
        number = -84109328;
        assertTrue("Incorrect byte returned for: " + number,
                ((byte) new Integer(number).intValue()) == new Integer(number)
                        .byteValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "shortValue",
        args = {}
    )
    public void test_shortValue() {
        int number = 1231243;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = 0;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = -1;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
        number = -84109328;
        assertTrue("Incorrect byte returned for: " + number,
                ((short) new Integer(number).intValue()) == new Integer(number)
                        .shortValue());
    }
}
