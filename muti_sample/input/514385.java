public class TestGrouping {
    SortedSet<Class<? extends TestCase>> testCaseClasses;
    public static final Comparator<Class<? extends TestCase>> SORT_BY_SIMPLE_NAME
            = new SortBySimpleName();
    public static final Comparator<Class<? extends TestCase>> SORT_BY_FULLY_QUALIFIED_NAME
            = new SortByFullyQualifiedName();
    protected String firstIncludedPackage = null;
    private ClassLoader classLoader;
    public TestGrouping(Comparator<Class<? extends TestCase>> comparator) {
        testCaseClasses = new TreeSet<Class<? extends TestCase>>(comparator);
    }
    public List<TestMethod> getTests() {
        List<TestMethod> testMethods = new ArrayList<TestMethod>();
        for (Class<? extends TestCase> testCase : testCaseClasses) {
            for (Method testMethod : getTestMethods(testCase)) {
                testMethods.add(new TestMethod(testMethod, testCase));
            }
        }
        return testMethods;
    }
    protected List<Method> getTestMethods(Class<? extends TestCase> testCaseClass) {
        List<Method> methods = Arrays.asList(testCaseClass.getMethods());
        return select(methods, new TestMethodPredicate());
    }
    SortedSet<Class<? extends TestCase>> getTestCaseClasses() {
        return testCaseClasses;
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestGrouping other = (TestGrouping) o;
        if (!this.testCaseClasses.equals(other.testCaseClasses)) {
            return false;
        }
        return this.testCaseClasses.comparator().equals(other.testCaseClasses.comparator());
    }
    public int hashCode() {
        return testCaseClasses.hashCode();
    }
    public TestGrouping addPackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            List<Class<? extends TestCase>> addedClasses = testCaseClassesInPackage(packageName);
            if (addedClasses.isEmpty()) {
                Log.w("TestGrouping", "Invalid Package: '" + packageName
                        + "' could not be found or has no tests");
            }
            testCaseClasses.addAll(addedClasses);
            if (firstIncludedPackage == null) {
                firstIncludedPackage = packageName;
            }
        }
        return this;
    }
    public TestGrouping removePackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            testCaseClasses.removeAll(testCaseClassesInPackage(packageName));
        }
        return this;
    }
    public String getFirstIncludedPackage() {
        return firstIncludedPackage;
    }
    private List<Class<? extends TestCase>> testCaseClassesInPackage(String packageName) {
        ClassPathPackageInfoSource source = PackageInfoSources.forClassPath(classLoader);
        ClassPathPackageInfo packageInfo = source.getPackageInfo(packageName);
        return selectTestClasses(packageInfo.getTopLevelClassesRecursive());
    }
    @SuppressWarnings("unchecked")
    private List<Class<? extends TestCase>> selectTestClasses(Set<Class<?>> allClasses) {
        List<Class<? extends TestCase>> testClasses = new ArrayList<Class<? extends TestCase>>();
        for (Class<?> testClass : select(allClasses,
                new TestCasePredicate())) {
            testClasses.add((Class<? extends TestCase>) testClass);
        }
        return testClasses;
    }
    private <T> List<T> select(Collection<T> items, Predicate<T> predicate) {
        ArrayList<T> selectedItems = new ArrayList<T>();
        for (T item : items) {
            if (predicate.apply(item)) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    private static class SortBySimpleName
            implements Comparator<Class<? extends TestCase>>, Serializable {
        public int compare(Class<? extends TestCase> class1,
                Class<? extends TestCase> class2) {
            int result = class1.getSimpleName().compareTo(class2.getSimpleName());
            if (result != 0) {
                return result;
            }
            return class1.getName().compareTo(class2.getName());
        }
    }
    private static class SortByFullyQualifiedName
            implements Comparator<Class<? extends TestCase>>, Serializable {
        public int compare(Class<? extends TestCase> class1,
                Class<? extends TestCase> class2) {
            return class1.getName().compareTo(class2.getName());
        }
    }
    private static class TestCasePredicate implements Predicate<Class<?>> {
        public boolean apply(Class aClass) {
            int modifiers = ((Class<?>) aClass).getModifiers();
            return TestCase.class.isAssignableFrom((Class<?>) aClass)
                    && Modifier.isPublic(modifiers)
                    && !Modifier.isAbstract(modifiers)
                    && hasValidConstructor((Class<?>) aClass);
        }
        @SuppressWarnings("unchecked")
        private boolean hasValidConstructor(java.lang.Class<?> aClass) {
            Constructor<? extends TestCase>[] constructors
                    = (Constructor<? extends TestCase>[]) aClass.getConstructors();
            for (Constructor<? extends TestCase> constructor : constructors) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    java.lang.Class[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0 ||
                            (parameterTypes.length == 1 && parameterTypes[0] == String.class)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    private static class TestMethodPredicate implements Predicate<Method> {
        public boolean apply(Method method) {
            return ((method.getParameterTypes().length == 0) &&
                    (method.getName().startsWith("test")) &&
                    (method.getReturnType().getSimpleName().equals("void")));
        }
    }
}
