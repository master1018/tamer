public class EasyMock {
    public static final String ENABLE_THREAD_SAFETY_CHECK_BY_DEFAULT = "easymock.enableThreadSafetyCheckByDefault";
    public static final String NOT_THREAD_SAFE_BY_DEFAULT = "easymock.notThreadSafeByDefault";
    public static <T> T createStrictMock(Class<T> toMock) {
        return createStrictControl().createMock(toMock);
    }
    public static <T> T createStrictMock(String name, Class<T> toMock) {
        return createStrictControl().createMock(name, toMock);
    }
    public static <T> T createMock(Class<T> toMock) {
        return createControl().createMock(toMock);
    }
    public static <T> T createMock(String name, Class<T> toMock) {
        return createControl().createMock(name, toMock);
    }
    public static <T> T createNiceMock(Class<T> toMock) {
        return createNiceControl().createMock(toMock);
    }
    public static <T> T createNiceMock(String name, Class<T> toMock) {
        return createNiceControl().createMock(name, toMock);
    }
    public static IMocksControl createStrictControl() {
        return new MocksControl(MocksControl.MockType.STRICT);
    }
    public static IMocksControl createControl() {
        return new MocksControl(MocksControl.MockType.DEFAULT);
    }
    public static IMocksControl createNiceControl() {
        return new MocksControl(MocksControl.MockType.NICE);
    }
    public static <T> IExpectationSetters<T> expect(T value) {
        return EasyMock.getControlForLastCall();
    }
    public static <T> IExpectationSetters<T> expectLastCall() {
        return getControlForLastCall();
    }
    @SuppressWarnings("unchecked")
    private static <T> IExpectationSetters<T> getControlForLastCall() {
        MocksControl lastControl = LastControl.lastControl();
        if (lastControl == null) {
            throw new IllegalStateException("no last call on a mock available");
        }
        return (IExpectationSetters<T>) lastControl;
    }
    public static boolean anyBoolean() {
        reportMatcher(Any.ANY);
        return false;
    }
    public static byte anyByte() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static char anyChar() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static int anyInt() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static long anyLong() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static float anyFloat() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static double anyDouble() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static short anyShort() {
        reportMatcher(Any.ANY);
        return 0;
    }
    public static <T> T anyObject() {
        reportMatcher(Any.ANY);
        return null;
    }
    public static <T extends Comparable<T>> T geq(Comparable<T> value) {
        reportMatcher(new GreaterOrEqual<T>(value));
        return null;
    }
    public static byte geq(byte value) {
        reportMatcher(new GreaterOrEqual<Byte>(value));
        return 0;
    }
    public static double geq(double value) {
        reportMatcher(new GreaterOrEqual<Double>(value));
        return 0;
    }
    public static float geq(float value) {
        reportMatcher(new GreaterOrEqual<Float>(value));
        return 0;
    }
    public static int geq(int value) {
        reportMatcher(new GreaterOrEqual<Integer>(value));
        return 0;
    }
    public static long geq(long value) {
        reportMatcher(new GreaterOrEqual<Long>(value));
        return 0;
    }
    public static short geq(short value) {
        reportMatcher(new GreaterOrEqual<Short>(value));
        return 0;
    }
    public static <T extends Comparable<T>> T leq(Comparable<T> value) {
        reportMatcher(new LessOrEqual<T>(value));
        return null;
    }
    public static byte leq(byte value) {
        reportMatcher(new LessOrEqual<Byte>(value));
        return 0;
    }
    public static double leq(double value) {
        reportMatcher(new LessOrEqual<Double>(value));
        return 0;
    }
    public static float leq(float value) {
        reportMatcher(new LessOrEqual<Float>(value));
        return 0;
    }
    public static int leq(int value) {
        reportMatcher(new LessOrEqual<Integer>(value));
        return 0;
    }
    public static long leq(long value) {
        reportMatcher(new LessOrEqual<Long>(value));
        return 0;
    }
    public static short leq(short value) {
        reportMatcher(new LessOrEqual<Short>(value));
        return 0;
    }
    public static <T extends Comparable<T>> T gt(Comparable<T> value) {
        reportMatcher(new GreaterThan<T>(value));
        return null;
    }
    public static byte gt(byte value) {
        reportMatcher(new GreaterThan<Byte>(value));
        return 0;
    }
    public static double gt(double value) {
        reportMatcher(new GreaterThan<Double>(value));
        return 0;
    }
    public static float gt(float value) {
        reportMatcher(new GreaterThan<Float>(value));
        return 0;
    }
    public static int gt(int value) {
        reportMatcher(new GreaterThan<Integer>(value));
        return 0;
    }
    public static long gt(long value) {
        reportMatcher(new GreaterThan<Long>(value));
        return 0;
    }
    public static short gt(short value) {
        reportMatcher(new GreaterThan<Short>(value));
        return 0;
    }
    public static <T extends Comparable<T>> T lt(Comparable<T> value) {
        reportMatcher(new LessThan<T>(value));
        return null;
    }
    public static byte lt(byte value) {
        reportMatcher(new LessThan<Byte>(value));
        return 0;
    }
    public static double lt(double value) {
        reportMatcher(new LessThan<Double>(value));
        return 0;
    }
    public static float lt(float value) {
        reportMatcher(new LessThan<Float>(value));
        return 0;
    }
    public static int lt(int value) {
        reportMatcher(new LessThan<Integer>(value));
        return 0;
    }
    public static long lt(long value) {
        reportMatcher(new LessThan<Long>(value));
        return 0;
    }
    public static short lt(short value) {
        reportMatcher(new LessThan<Short>(value));
        return 0;
    }
    public static <T> T isA(Class<T> clazz) {
        reportMatcher(new InstanceOf(clazz));
        return null;
    }
    public static String contains(String substring) {
        reportMatcher(new Contains(substring));
        return null;
    }
    public static boolean and(boolean first, boolean second) {
        LastControl.reportAnd(2);
        return false;
    }
    public static byte and(byte first, byte second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static char and(char first, char second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static double and(double first, double second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static float and(float first, float second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static int and(int first, int second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static long and(long first, long second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static short and(short first, short second) {
        LastControl.reportAnd(2);
        return 0;
    }
    public static <T> T and(T first, T second) {
        LastControl.reportAnd(2);
        return null;
    }
    public static boolean or(boolean first, boolean second) {
        LastControl.reportOr(2);
        return false;
    }
    public static byte or(byte first, byte second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static char or(char first, char second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static double or(double first, double second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static float or(float first, float second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static int or(int first, int second) {
        LastControl.reportOr(2);
        return first;
    }
    public static long or(long first, long second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static short or(short first, short second) {
        LastControl.reportOr(2);
        return 0;
    }
    public static <T> T or(T first, T second) {
        LastControl.reportOr(2);
        return null;
    }
    public static boolean not(boolean first) {
        LastControl.reportNot();
        return false;
    }
    public static byte not(byte first) {
        LastControl.reportNot();
        return 0;
    }
    public static char not(char first) {
        LastControl.reportNot();
        return 0;
    }
    public static double not(double first) {
        LastControl.reportNot();
        return 0;
    }
    public static float not(float first) {
        LastControl.reportNot();
        return first;
    }
    public static int not(int first) {
        LastControl.reportNot();
        return 0;
    }
    public static long not(long first) {
        LastControl.reportNot();
        return 0;
    }
    public static short not(short first) {
        LastControl.reportNot();
        return 0;
    }
    public static <T> T not(T first) {
        LastControl.reportNot();
        return null;
    }
    public static boolean eq(boolean value) {
        reportMatcher(new Equals(value));
        return false;
    }
    public static byte eq(byte value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static char eq(char value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static double eq(double value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static float eq(float value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static int eq(int value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static long eq(long value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static short eq(short value) {
        reportMatcher(new Equals(value));
        return 0;
    }
    public static <T> T eq(T value) {
        reportMatcher(new Equals(value));
        return null;
    }
    public static boolean[] aryEq(boolean[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static byte[] aryEq(byte[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static char[] aryEq(char[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static double[] aryEq(double[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static float[] aryEq(float[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static int[] aryEq(int[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static long[] aryEq(long[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static short[] aryEq(short[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static <T> T[] aryEq(T[] value) {
        reportMatcher(new ArrayEquals(value));
        return null;
    }
    public static <T> T isNull() {
        reportMatcher(Null.NULL);
        return null;
    }
    public static <T> T notNull() {
        reportMatcher(NotNull.NOT_NULL);
        return null;
    }
    public static String find(String regex) {
        reportMatcher(new Find(regex));
        return null;
    }
    public static String matches(String regex) {
        reportMatcher(new Matches(regex));
        return null;
    }
    public static String startsWith(String prefix) {
        reportMatcher(new StartsWith(prefix));
        return null;
    }
    public static String endsWith(String suffix) {
        reportMatcher(new EndsWith(suffix));
        return null;
    }
    public static double eq(double value, double delta) {
        reportMatcher(new EqualsWithDelta(value, delta));
        return 0;
    }
    public static float eq(float value, float delta) {
        reportMatcher(new EqualsWithDelta(value, delta));
        return 0;
    }
    public static <T> T same(T value) {
        reportMatcher(new Same(value));
        return null;
    }
    public static <T extends Comparable<T>> T cmpEq(Comparable<T> value) {
        reportMatcher(new CompareEqual<T>(value));
        return null;
    }
    public static <T> T cmp(T value, Comparator<? super T> comparator, LogicalOperator operator) {
        reportMatcher(new Compare<T>(value, comparator, operator));
        return null;
    }
    public static <T> T capture(Capture<T> captured) {
        reportMatcher(new Captures<T>(captured));
        return null;
    }
    public static void replay(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).replay();
        }
    }
    public static void reset(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).reset();
        }
    }
    public static void resetToNice(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).resetToNice();
        }
    }
    public static void resetToDefault(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).resetToDefault();
        }
    }
    public static void resetToStrict(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).resetToStrict();
        }
    }
    public static void verify(Object... mocks) {
        for (Object mock : mocks) {
            getControl(mock).verify();
        }
    }
    public static void checkOrder(Object mock, boolean state) {
        getControl(mock).checkOrder(state);
    }
    public static void reportMatcher(IArgumentMatcher matcher) {
        LastControl.reportMatcher(matcher);
    }
    private static MocksControl getControl(Object mock) {
        return ((ObjectMethodsFilter) Proxy
        .getInvocationHandler(mock)).getDelegate().getControl();
    }
    public static Object[] getCurrentArguments() {
        Invocation result = LastControl.getCurrentInvocation();
        if (result == null) {
            throw new IllegalStateException(
                    "current arguments are only available when executing callback methods");
        }
        return result.getArguments();
    }
    public static void makeThreadSafe(Object mock, boolean threadSafe) {
        getControl(mock).makeThreadSafe(threadSafe);
    }
    public static void checkIsUsedInOneThread(Object mock,
            boolean shouldBeUsedInOneThread) {
        getControl(mock).checkIsUsedInOneThread(shouldBeUsedInOneThread);
    }
    public static String getEasyMockProperty(String key) {
        return EasyMockProperties.getInstance().getProperty(key);
    }
    public static String setEasyMockProperty(String key, String value) {
        return EasyMockProperties.getInstance().setProperty(key, value);
    }
    protected EasyMock() {
    }
}
