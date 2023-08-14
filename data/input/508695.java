public class Test_pargsreturn extends DxTestCase {
    public void testN1() {
        assertEquals(1234, new T1().run());
    }
    public void testN2() {
        assertEquals(1234, new T2().run(1234));
    }
    public void testN3() {
        T3 t = new T3();
        t.run(1234);
        assertEquals(1234, t.i1);
    }
    public void testN4() {
        T4 t = new T4();
        t.run(1234);
        assertEquals(50000000000l, t.j1);
        assertEquals(1234, t.i1);
    }
}
