public class TestPredicates {
    public static final Predicate<TestMethod> SELECT_INSTRUMENTATION =
            new AssignableFrom(InstrumentationTestCase.class);
    public static final Predicate<TestMethod> REJECT_INSTRUMENTATION =
            Predicates.not(SELECT_INSTRUMENTATION);
    public static final Predicate<TestMethod> SELECT_SMOKE = new HasAnnotation(Smoke.class);
    public static final Predicate<TestMethod> SELECT_SMALL = new HasAnnotation(SmallTest.class);
    public static final Predicate<TestMethod> SELECT_MEDIUM = new HasAnnotation(MediumTest.class);
    public static final Predicate<TestMethod> SELECT_LARGE = new HasAnnotation(LargeTest.class);
    public static final Predicate<TestMethod> REJECT_SUPPRESSED =
            Predicates.not(new HasAnnotation(Suppress.class));
    public static final Predicate<TestMethod> REJECT_PERFORMANCE =
            Predicates.not(new AssignableFrom(PerformanceTestBase.class));
}
