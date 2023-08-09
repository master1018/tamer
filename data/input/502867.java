public class MockControl<T> implements Serializable {
    private static final long serialVersionUID = 8741244302173698092L;
    private final T mock;
    private final MocksControl ctrl;
    protected MockControl(MocksControl ctrl, Class<T> toMock) {
        this.ctrl = ctrl;
        this.mock = ctrl.createMock(toMock);
    }
    public static <T> MockControl<T> createControl(Class<T> toMock) {
        return new MockControl<T>((MocksControl) EasyMock.createControl(),
                toMock);
    }
    public static <T> MockControl<T> createStrictControl(Class<T> toMock) {
        return new MockControl<T>(
                (MocksControl) EasyMock.createStrictControl(), toMock);
    }
    public static <T> MockControl<T> createNiceControl(Class<T> toMock) {
        return new MockControl<T>((MocksControl) EasyMock.createNiceControl(),
                toMock);
    }
    public T getMock() {
        return mock;
    }
    public final void reset() {
        ctrl.reset();
    }
    public void replay() {
        ctrl.replay();
    }
    public void verify() {
        ctrl.verify();
    }
    public void setVoidCallable() {
        expectLastCall(
                "method call on the mock needed before setting void callable")
                .once();
    }
    public void setThrowable(Throwable throwable) {
        expectLastCall(
                "method call on the mock needed before setting Throwable")
                .andThrow(throwable).once();
    }
    public void setReturnValue(Object value) {
        expectLastCall(
                "method call on the mock needed before setting return value")
                .andReturn(value).once();
    }
    public void setVoidCallable(int times) {
        expectLastCall(
                "method call on the mock needed before setting void callable")
                .times(times);
    }
    public void setThrowable(Throwable throwable, int times) {
        expectLastCall(
                "method call on the mock needed before setting Throwable")
                .andThrow(throwable).times(times);
    }
    public void setReturnValue(Object value, int times) {
        expectLastCall(
                "method call on the mock needed before setting return value")
                .andReturn(value).times(times);
    }
    public void setReturnValue(Object value, Range range) {
        IExpectationSetters<Object> setter = expectLastCall(
                "method call on the mock needed before setting return value")
                .andReturn(value);
        callWithConvertedRange(setter, range);
    }
    public void setDefaultVoidCallable() {
        ((MocksControl) expectLastCall("method call on the mock needed before setting default void callable"))
                .setLegacyDefaultVoidCallable();
    }
    public void setDefaultThrowable(Throwable throwable) {
        ctrl.setLegacyDefaultThrowable(throwable);
    }
    public void setDefaultReturnValue(Object value) {
        ctrl.setLegacyDefaultReturnValue(value);
    }
    public void setMatcher(ArgumentsMatcher matcher) {
        ctrl.setLegacyMatcher(matcher);
    }
    public void setVoidCallable(int minCount, int maxCount) {
        expectLastCall(
                "method call on the mock needed before setting void callable")
                .times(minCount, maxCount);
    }
    public void setVoidCallable(Range range) {
        IExpectationSetters<Object> setter = expectLastCall("method call on the mock needed before setting void callable");
        callWithConvertedRange(setter, range);
    }
    public void setThrowable(Throwable throwable, int minCount, int maxCount) {
        expectLastCall(
                "method call on the mock needed before setting Throwable")
                .andThrow(throwable).times(minCount, maxCount);
    }
    public void setThrowable(Throwable throwable, Range range) {
        IExpectationSetters<Object> setter = expectLastCall(
                "method call on the mock needed before setting Throwable")
                .andThrow(throwable);
        callWithConvertedRange(setter, range);
    }
    public void setReturnValue(Object value, int minCount, int maxCount) {
        expectLastCall(
                "method call on the mock needed before setting return value")
                .andReturn(value).times(minCount, maxCount);
    }
    public static final Range ONE = MocksControl.ONCE;
    public static final Range ONE_OR_MORE = MocksControl.AT_LEAST_ONCE;
    public static final Range ZERO_OR_MORE = MocksControl.ZERO_OR_MORE;
    public static final ArgumentsMatcher EQUALS_MATCHER = new EqualsMatcher();
    public static final ArgumentsMatcher ALWAYS_MATCHER = new AlwaysMatcher();
    public static final ArgumentsMatcher ARRAY_MATCHER = new ArrayMatcher();
    public void setDefaultMatcher(ArgumentsMatcher matcher) {
        ctrl.setLegacyDefaultMatcher(matcher);
    }
    public <V1, V2 extends V1> void expectAndReturn(V1 ignored, V2 value) {
        EasyMock.expectLastCall().andReturn(value).once();
    }
    public void expectAndReturn(int ignored, int value) {
        this.expectAndReturn((Object) ignored, (Object) value);
    }
    public <V1, V2 extends V1> void expectAndReturn(V1 ignored, V2 value,
            Range range) {
        IExpectationSetters<Object> expectAndReturn = EasyMock.expectLastCall()
                .andReturn(value);
        callWithConvertedRange(expectAndReturn, range);
    }
    public void expectAndReturn(int ignored, int value, Range range) {
        this.expectAndReturn((Object) ignored, (Object) value, range);
    }
    public <V1, V2 extends V1> void expectAndReturn(V1 ignored, V2 value,
            int count) {
        EasyMock.expectLastCall().andReturn(value).times(count);
    }
    public void expectAndReturn(int ignored, int value, int count) {
        this.expectAndReturn((Object) ignored, (Object) value, count);
    }
    public <V1, V2 extends V1> void expectAndReturn(V1 ignored, V2 value,
            int min, int max) {
        EasyMock.expectLastCall().andReturn(value).times(min, max);
    }
    public void expectAndReturn(int ignored, int value, int min, int max) {
        this.expectAndReturn((Object) ignored, (Object) value, min, max);
    }
    public void expectAndThrow(Object ignored, Throwable throwable) {
        EasyMock.expect(ignored).andThrow(throwable).once();
    }
    public void expectAndThrow(Object ignored, Throwable throwable, Range range) {
        IExpectationSetters<Object> setter = EasyMock.expect(ignored).andThrow(
                throwable);
        callWithConvertedRange(setter, range);
    }
    public void expectAndThrow(Object ignored, Throwable throwable, int count) {
        expect(ignored).andThrow(throwable).times(count);
    }
    public void expectAndThrow(Object ignored, Throwable throwable, int min,
            int max) {
        expect(ignored).andThrow(throwable).times(min, max);
    }
    public <V1, V2 extends V1> void expectAndDefaultReturn(V1 ignored, V2 value) {
        EasyMock.expectLastCall().andStubReturn(value);
    }
    public void expectAndDefaultThrow(Object ignored, Throwable throwable) {
        expectLastCall(
                "method call on the mock needed before setting default Throwable")
                .andStubThrow(throwable);
    }
    private IExpectationSetters<Object> expectLastCall(String failureMessage) {
        try {
            return EasyMock.expectLastCall();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(failureMessage);
        }
    }
    private void callWithConvertedRange(IExpectationSetters<Object> setter, Range range) {
        if (range == ONE) {
            setter.once();
        } else if (range == ONE_OR_MORE) {
            setter.atLeastOnce();
        } else if (range == ZERO_OR_MORE) {
            setter.anyTimes();
        } else {
            throw new IllegalArgumentException("Unexpected Range");
        }
    }
}