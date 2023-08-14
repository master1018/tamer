public class EnumTest extends TestCase {
    enum MyEnum {
        ZERO, ONE, TWO, THREE, FOUR {boolean isFour() {
        return true;
    }};
        boolean isFour() {
            return false;
        }
    }
    enum MyEnumTwo {
        FIVE, SIX
    }
    @SmallTest
    public void testEnum() throws Exception {
        assertTrue(MyEnum.ZERO.compareTo(MyEnum.ONE) < 0);
        assertEquals(MyEnum.ZERO, MyEnum.ZERO);
        assertTrue(MyEnum.TWO.compareTo(MyEnum.ONE) > 0);
        assertTrue(MyEnum.FOUR.compareTo(MyEnum.ONE) > 0);
        assertEquals("ONE", MyEnum.ONE.name());
        assertSame(MyEnum.ONE.getDeclaringClass(), MyEnum.class);
        assertSame(MyEnum.FOUR.getDeclaringClass(), MyEnum.class);
        assertTrue(MyEnum.FOUR.isFour());
        MyEnum e;
        e = MyEnum.ZERO;
        switch (e) {
            case ZERO:
                break;
            default:
                fail("wrong switch");
        }
    }
}
