@TestTargetClass(Pack200.class)
public class Pack200Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPacker",
        args = {}
    )
    @KnownFailure("No Implementation in Android!")
    public void testNewPacker() {
        Method[] methodsInt = Pack200.Packer.class.getDeclaredMethods();
        Method[] methodsImpl = Pack200.newPacker().getClass()
                .getDeclaredMethods();
        Field[] fieldsInt = Pack200.Packer.class.getFields();
        Field[] fieldsImpl = Pack200.newPacker().getClass().getFields();
        int i, k;
        boolean flag;
        for (i = 0; i < methodsInt.length; i++) {
            flag = false;
            for (k = 0; k < methodsImpl.length; k++) {
                if (methodsInt[i].getName().equals(methodsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all methods were implemented", flag);
        }
        for (i = 0; i < fieldsInt.length; i++) {
            flag = false;
            for (k = 0; k < fieldsImpl.length; k++) {
                if (fieldsInt[i].getName().equals(fieldsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all fields were existed", flag);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newUnpacker",
        args = {}
    )
    @KnownFailure("No Implementation in Android!")
    public void testNewUnpacker() {
        assertNotNull(Pack200.newUnpacker().getClass());
        Method[] methodsInt = Pack200.Unpacker.class.getDeclaredMethods();
        Method[] methodsImpl = Pack200.newUnpacker().getClass()
                .getDeclaredMethods();
        Field[] fieldsInt = Pack200.Unpacker.class.getFields();
        Field[] fieldsImpl = Pack200.newUnpacker().getClass().getFields();
        int i, k;
        boolean flag;
        for (i = 0; i < methodsInt.length; i++) {
            flag = false;
            for (k = 0; k < methodsImpl.length; k++) {
                if (methodsInt[i].getName().equals(methodsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all methods were implemented", flag);
        }
        for (i = 0; i < fieldsInt.length; i++) {
            flag = false;
            for (k = 0; k < fieldsImpl.length; k++) {
                if (fieldsInt[i].getName().equals(fieldsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all fields were existed", flag);
        }
    }
}
