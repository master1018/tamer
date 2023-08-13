@TestTargetClass(EventListenerProxy.class)
public class EventListenerProxyTest extends TestCase {
    class Mock_EventListener implements EventListener {
    }
    class Mock_EventListenerProxy extends EventListenerProxy {
        public Mock_EventListenerProxy(EventListener listener) {
            super(listener);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EventListenerProxy",
        args = {java.util.EventListener.class}
    )
    public void testEventListenerProxy() {
        assertNotNull(new Mock_EventListenerProxy(null));
        assertNotNull(new Mock_EventListenerProxy(new Mock_EventListener()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getListener",
        args = {}
    )
    public void testGetListener() {
        EventListener el = new Mock_EventListener();
        EventListenerProxy elp = new Mock_EventListenerProxy(el);
        assertSame(el, elp.getListener());
    }
}
