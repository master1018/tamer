@TestTargetClass(android.database.Observable.class)
public class ObservableTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test registerObserver(Object.class) and unregisterObserver(Object.class).",
            method = "registerObserver",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test registerObserver(Object.class) and unregisterObserver(Object.class).",
            method = "unregisterObserver",
            args = {java.lang.Object.class}
        )
    })
    public void testRegisterUnRegisterObserver() {
        MockObservable observable = new MockObservable();
        try {
            observable.registerObserver((null));
            fail("registerObserver should throw a IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        Object observer = new Object();
        try {
            observable.unregisterObserver(observer);
            fail("unregisterObserver should throw a IllegalStateException here.");
        } catch (IllegalStateException e) {
        }
        try {
            observable.unregisterObserver(null);
            fail("unregisterObserver should throw a IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        observable.registerObserver(observer);
        try {
            observable.registerObserver(observer);
            fail("registerObserver should throw a IllegalStateException here.");
        } catch (IllegalStateException e) {
        }
        observable.unregisterObserver(observer);
        try {
            observable.unregisterObserver(observer);
            fail("unregisterObserver should throw a IllegalStateException here.");
        } catch (IllegalStateException e) {
        }
        observable.registerObserver(observer);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test unregisterAll method.",
        method = "unregisterAll",
        args = {}
    )
    public void testUnregisterAll() {
        MockObservable observable = new MockObservable();
        Object observer1 = new Object();
        Object observer2 = new Object();
        observable.registerObserver(observer1);
        observable.registerObserver(observer2);
        try {
            observable.registerObserver(observer1);
            fail("registerObserver should throw a IllegalStateException here.");
        } catch (IllegalStateException e) {
        }
        try {
            observable.registerObserver(observer2);
            fail("registerObserver should throw a IllegalStateException here.");
        } catch (IllegalStateException e) {
        }
        observable.unregisterAll();
        observable.registerObserver(observer1);
        observable.registerObserver(observer2);
    }
    private class MockObservable extends Observable<Object> {
    }
}
