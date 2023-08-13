public class InstanceofTest extends TestCase {
    protected A mA;
    protected ChildOfAOne mOne;
    protected ChildOfAOne mTwo;
    protected ChildOfAOne mThree;
    protected ChildOfAOne mFour;
    protected ChildOfAFive mFive;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mA = new A();
        mOne = new ChildOfAOne();
        mTwo = new ChildOfATwo();
        mThree = new ChildOfAThree();
        mFour = new ChildOfAFour();
        mFive = new ChildOfAFive();
    }
    @MediumTest
    public void testNoInterface() throws Exception {
        A a = mA;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_a should not be a ChildOfAFive", a instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testDerivedOne() throws Exception {
        InterfaceOne one = mOne;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_one should not be a ChildOfAFive", one instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testDerivedTwo() throws Exception {
        InterfaceTwo two = mTwo;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_two should not be a ChildOfAFive", two instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testDerivedThree() throws Exception {
        InterfaceThree three = mThree;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_three should not be a ChildOfAFive", three instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testDerivedFour() throws Exception {
        InterfaceFour four = mFour;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_four should not be a ChildOfAFive", four instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testSuccessClass() throws Exception {
        ChildOfAOne five = mFive;
        for (int i = 0; i < 100000; i++) {
            assertTrue("m_five is suppose to be a ChildOfAFive", five instanceof ChildOfAFive);
        }
    }
    @MediumTest
    public void testSuccessInterface() throws Exception {
        ChildOfAFive five = mFive;
        for (int i = 0; i < 100000; i++) {
            assertTrue("m_five is suppose to be a InterfaceFour", five instanceof InterfaceFour);
        }
    }
    @MediumTest
    public void testFailInterface() throws Exception {
        InterfaceOne one = mFive;
        for (int i = 0; i < 100000; i++) {
            assertFalse("m_five does not implement InterfaceFive", one instanceof InterfaceFive);
        }
    }
    private interface InterfaceOne {
    }
    private interface InterfaceTwo {
    }
    private interface InterfaceThree {
    }
    private interface InterfaceFour {
    }
    private interface InterfaceFive {
    }
    private static class A {
    }
    private static class ChildOfAOne extends A implements InterfaceOne, InterfaceTwo, InterfaceThree, InterfaceFour {
    }
    private static class ChildOfATwo extends ChildOfAOne {
    }
    private static class ChildOfAThree extends ChildOfATwo {
    }
    private static class ChildOfAFour extends ChildOfAThree {
    }
    private static class ChildOfAFive extends ChildOfAFour {
    }
}
