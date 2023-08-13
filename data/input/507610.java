@TestTargetClass(Observer.class)
public class ObserverTest extends TestCase {
    class Mock_Observer implements Observer {
        int updateCount = 0;
        public void update(Observable observed, Object arg) {
            ++updateCount;
        }
        public int getUpdateCount() {
            return updateCount;
        }
    }
    class TestObservable extends Observable {
        public void doChange() {
            setChanged();
        }
        public void clearChange() {
            clearChanged();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {java.util.Observable.class, java.lang.Object.class}
    )
    public void testUpdate() {
        TestObservable observable = new TestObservable();
        Mock_Observer observer = null;
        observable.addObserver(observer = new Mock_Observer());
        observable.notifyObservers();
        assertEquals("Notified when unchnaged", 0, observer.getUpdateCount());
        observable.doChange();
        observable.notifyObservers();
        assertEquals("Failed to notify", 1, observer.getUpdateCount());
    }
}
