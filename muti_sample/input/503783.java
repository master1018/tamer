public class Test_aget_object extends DxTestCase {
    public void testN1() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("a", t.run(arr, 0));
    }
    public void testN2() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("b", t.run(arr, 1));
    }
    public void testN3() {
        String[] arr = new String[] {"a", "b"};
        T_aget_object_8 t = new T_aget_object_8();
        try {
            t.run(arr, 3.14f);
        } catch (Throwable e) {
        }
    }    
    public void testE1() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }
    public void testE2() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }
    public void testE3() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = null;
        try {
            t.run(arr, 0);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try { 
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.aget_object.d.T_aget_object_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
