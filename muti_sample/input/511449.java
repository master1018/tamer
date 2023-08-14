public class JavaPerformanceTests {
    public static String[] children() {
        return new String[] {
                StringTest.class.getName(),
                HashMapPerformanceTest.class.getName(),
                ArrayListPerformanceTest.class.getName(),
                TreeMapPerformanceTest.class.getName(),
                TreeSetTest.class.getName(),
                HashSetTest.class.getName(),
                HashtableTest.class.getName(),
                VectorTest.class.getName(),
                LinkedListTest.class.getName(),
                MathPerformanceTest.class.getName(),
        };
    }
}
