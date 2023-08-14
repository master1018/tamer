@TestTargetClass(Compiler.class) 
public class CompilerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "command",
        args = {java.lang.Object.class}
    )
    public void test_commandLjava_lang_Object() {
        if(System.getProperty("java.compiler") != null) {
            try {
                assertNull("Incorrect behavior.", Compiler.command(new Object()));
            } catch (Exception e) {
                fail("Exception during test : " + e.getMessage());
            }
            Compiler.command(null);
        } else {
            Compiler.command("");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compileClass",
        args = {java.lang.Class.class}
    )
    public void test_compileClassLjava_lang_Class() {
        try {
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test.");
        }
        Compiler.compileClass((Class) null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compileClasses",
        args = {java.lang.String.class}
    )
    public void test_compileClassesLjava_lang_String() {
        try {
            Compiler.compileClasses("Compiler");
        } catch (Exception e) {
            fail("Exception during test.");
        }
        Compiler.compileClasses((String) null);
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Doesn't verify that disable() method causes the Compiler to cease operation.",
        method = "disable",
        args = {}
    )
    public void test_disable() {
        try {
            Compiler.disable();
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Doesn't verify that enable() method causes the Compiler to resume operation.",
        method = "enable",
        args = {}
    )
    public void test_enable() {
        try {
            Compiler.disable();
            Compiler.enable();
            Compiler.compileClass(Compiler.class);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
}
