public class TestClassReplacement extends TestCase {
    public void testClassReplacements() {
        final String[] classes = new String[] {
                "android.graphics.Paint",               "android.graphics._Original_Paint"
        };
        final int count = classes.length;
        for (int i = 0 ; i < count ; i += 2) {
            loadAndCompareClasses(classes[i], classes[i+1]);
        }
    }
    private void loadAndCompareClasses(String newClassName, String oldClassName) {
        try {
            Class<?> newClass = TestClassReplacement.class.getClassLoader().loadClass(newClassName);
            Class<?> oldClass = TestClassReplacement.class.getClassLoader().loadClass(oldClassName);
            compare(newClass, oldClass);
        } catch (ClassNotFoundException e) {
            fail("Failed to load class: " + e.getMessage());
        }
    }
    private void compare(Class<?> newClass, Class<?> oldClass) {
        Method[] newClassMethods = newClass.getDeclaredMethods();
        Method[] oldClassMethods = oldClass.getDeclaredMethods();
        for (Method oldMethod : oldClassMethods) {
            if (oldMethod.getName().startsWith("native")) {
                continue;
            }
            boolean found = false;
            for (Method newMethod : newClassMethods) {
                if (compareMethods(newClass, newMethod, oldClass, oldMethod)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                fail(String.format("Unable to find %1$s", oldMethod.toGenericString()));
            }
        }
    }
    private boolean compareMethods(Class<?> newClass, Method newMethod,
            Class<?> oldClass, Method oldMethod) {
        if (newMethod.getName().equals(oldMethod.getName()) == false) {
            return false;
        }
        Class<?> oldReturnType = oldMethod.getReturnType();
        oldReturnType = adapt(oldReturnType, newClass, oldClass);
        Class<?> newReturnType = newMethod.getReturnType();
        if (newReturnType.equals(oldReturnType) == false) {
            return false;
        }
        Class<?>[] oldParameters = oldMethod.getParameterTypes();
        Class<?>[] newParemeters = newMethod.getParameterTypes();
        if (oldParameters.length != newParemeters.length) {
            return false;
        }
        for (int i = 0 ; i < oldParameters.length ; i++) {
            if (newParemeters[i].equals(adapt(oldParameters[i], newClass, oldClass)) == false) {
                return false;
            }
        }
        return true;
    }
    private Class<?> adapt(Class<?> theClass, Class<?> newClass, Class<?> oldClass) {
        if (theClass.isPrimitive() == false) {
            String n = theClass.getName().replace(oldClass.getName(), newClass.getName());
            try {
                return Class.forName(n);
            } catch (ClassNotFoundException e) {
                fail("Missing class: " + n);
            }
        }
        return theClass;
    }
}
