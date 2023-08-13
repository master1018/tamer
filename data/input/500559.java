class ClassWithPrivateConstructor {
    private ClassWithPrivateConstructor() {
    }
}
public class ClassTest extends TestCase {
    @SmallTest
    public void testClass() throws Exception {
        Class helloClass = Class.forName(ClassTest.class.getName());
        Object instance = helloClass.newInstance();
        assertNotNull(instance);
        try {
            Class.forName("this.class.DoesNotExist");
            fail("unexpected success");
        } catch (ClassNotFoundException ex) {
        }
        try {
            Class.forName("android.core.ClassWithPrivateConstructor").newInstance();
            fail("unexpected success");
        } catch (IllegalAccessException ex) {
        }
        Method method = helloClass.getDeclaredMethod("method", (Class[]) null);
        method.invoke(new ClassTest(), (Object[]) null);
        method = helloClass.getDeclaredMethod("methodWithArgs", Object.class);
        Object invokeArgs[] = new Object[1];
        invokeArgs[0] = "Hello";
        Object ret = method.invoke(new ClassTest(), invokeArgs);
        assertEquals(ret, invokeArgs[0]);
        method = helloClass.getDeclaredMethod("privateMethod", (Class[]) null);
        method.invoke(new ClassTest(), (Object[]) null);
        Class objectClass = Class.forName("java.lang.Object");
        assertEquals(helloClass.getSuperclass().getSuperclass().getSuperclass(), objectClass);
        assertTrue(objectClass.isAssignableFrom(helloClass));
        assertFalse(helloClass.isAssignableFrom(objectClass));
        Constructor constructor = helloClass.getConstructor((Class[]) null);
        assertNotNull(constructor);
        assertTrue(Modifier.isPublic(helloClass.getModifiers()));
        helloClass.getMethod("method", (Class[]) null);
        try {
            Class[] argTypes = new Class[1];
            argTypes[0] = helloClass;
            helloClass.getMethod("method", argTypes);
            fail("unexpected success");
        } catch (NoSuchMethodException ex) {
        }
        SimpleClass obj = new SimpleClass();
        Field field = obj.getClass().getDeclaredField("str");
        field.set(obj, null);
    }
    public class SimpleClass {
        public String str;
    }
    public Object methodWithArgs(Object o) {
        return o;
    }
    boolean methodInvoked;
    public void method() {
        methodInvoked = true;
    }
    boolean privateMethodInvoked;
    public void privateMethod() {
        privateMethodInvoked = true;
    }
    @MediumTest
    public void testClassGetMethodsNoDupes() {
        Method[] methods = Button.class.getMethods();
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < methods.length; i++) {
            String signature = methods[i].toString();
            int par = signature.indexOf('(');
            int dot = signature.lastIndexOf('.', par);
            signature = signature.substring(dot + 1);
            assertFalse("Duplicate " + signature, set.contains(signature));
            set.add(signature);
        }
    }
    interface MyInterface {
        void foo();
    }
    interface MyOtherInterface extends MyInterface {
        void bar();
    }
    abstract class MyClass implements MyOtherInterface {
        public void gabba() {
        }
        public void hey() {
        }
    }
    @SmallTest
    public void testGetMethodsInterfaces() {
        Method[] methods = MyInterface.class.getMethods();
        assertTrue("Interface method must be there", hasMethod(methods, ".foo("));
        methods = MyOtherInterface.class.getMethods();
        assertTrue("Interface method must be there", hasMethod(methods, ".foo("));
        assertTrue("Interface method must be there", hasMethod(methods, ".bar("));
        methods = MyClass.class.getMethods();
        assertTrue("Interface method must be there", hasMethod(methods, ".foo("));
        assertTrue("Interface method must be there", hasMethod(methods, ".bar("));
        assertTrue("Declared method must be there", hasMethod(methods, ".gabba("));
        assertTrue("Declared method must be there", hasMethod(methods, ".hey("));
        assertTrue("Inherited method must be there", hasMethod(methods, ".toString("));
    }
    private boolean hasMethod(Method[] methods, String signature) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].toString().contains(signature)) {
                return true;
            }
        }
        return false;
    }
    @SmallTest
    public void testClassGetPackage() {
        assertNotNull("Package must be non-null", getClass().getPackage());
        assertEquals("Package must have expected name", "android.core", getClass().getPackage().getName());
        assertEquals("Package must have expected title", "Unknown", getClass().getPackage().getSpecificationTitle());
        Package p = java.lang.Object.class.getPackage();
        assertNotNull("Package must be non-null", p);
        assertEquals("Package must have expected name", "java.lang", p.getName());
        assertSame("Package object must be same for each call", p, java.lang.Object.class.getPackage());
    }
    private class MemberClass {
    }
    private class Mi$o$oup {
    }
    @SmallTest
    public void testVariousClassNames() {
        Class<?> clazz = this.getClass();
        String pkg = (clazz.getPackage() == null ? "" : clazz.getPackage().getName() + ".");
        assertEquals("Top-level class name must be correct", pkg + "ClassTest", clazz.getName());
        assertEquals("Top-level class simple name must be correct", "ClassTest", clazz.getSimpleName());
        assertEquals("Top-level class canonical name must be correct", pkg + "ClassTest", clazz.getCanonicalName());
        clazz = MemberClass.class;
        assertEquals("Member class name must be correct", pkg + "ClassTest$MemberClass", clazz.getName());
        assertEquals("Member class simple name must be correct", "MemberClass", clazz.getSimpleName());
        assertEquals("Member class canonical name must be correct", pkg + "ClassTest.MemberClass", clazz.getCanonicalName());
        class LocalClass {
        }
        clazz = LocalClass.class;
        assertEquals("Local class name must be correct", pkg + "ClassTest$1LocalClass", clazz.getName());
        assertEquals("Local class simple name must be correct", "LocalClass", clazz.getSimpleName());
        assertNull("Local class canonical name must be null", clazz.getCanonicalName());
        clazz = new Object() { }.getClass();
        assertEquals("Anonymous class name must be correct", pkg + "ClassTest$1", clazz.getName());
        assertEquals("Anonymous class simple name must be empty", "", clazz.getSimpleName());
        assertNull("Anonymous class canonical name must be null", clazz.getCanonicalName());
        clazz = Mou$$aka.class;
        assertEquals("Top-level class name must be correct", pkg + "Mou$$aka", clazz.getName());
        assertEquals("Top-level class simple name must be correct", "Mou$$aka", clazz.getSimpleName());
        assertEquals("Top-level class canonical name must be correct", pkg + "Mou$$aka", clazz.getCanonicalName());
        clazz = Mi$o$oup.class;
        assertEquals("Member class name must be correct", pkg + "ClassTest$Mi$o$oup", clazz.getName());
        assertEquals("Member class simple name must be correct", "Mi$o$oup", clazz.getSimpleName());
        assertEquals("Member class canonical name must be correct", pkg + "ClassTest.Mi$o$oup", clazz.getCanonicalName());
        class Ma$hedPotatoe$ {
        }
        clazz = Ma$hedPotatoe$.class;
        assertEquals("Member class name must be correct", pkg + "ClassTest$1Ma$hedPotatoe$", clazz.getName());
        assertEquals("Member class simple name must be correct", "Ma$hedPotatoe$", clazz.getSimpleName());
        assertNull("Member class canonical name must be null", clazz.getCanonicalName());
    }
    @SmallTest
    public void testLocalMemberClass() {
        Class<?> clazz = this.getClass();
        assertFalse("Class must not be member", clazz.isMemberClass());  
        assertFalse("Class must not be local", clazz.isLocalClass());  
        clazz = MemberClass.class;
        assertTrue("Class must be member", clazz.isMemberClass());  
        assertFalse("Class must not be local", clazz.isLocalClass());  
        class OtherLocalClass {
        }
        clazz = OtherLocalClass.class;
        assertFalse("Class must not be member", clazz.isMemberClass());  
        assertTrue("Class must be local", clazz.isLocalClass());  
        clazz = new Object() { }.getClass();
        assertFalse("Class must not be member", clazz.isMemberClass());  
        assertFalse("Class must not be local", clazz.isLocalClass());  
    }
}
class Mou$$aka {
}
