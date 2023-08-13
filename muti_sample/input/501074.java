public class GroupMessagingListenerUnitTests extends TestCase {
    public void testAddRemove() {
        GroupMessagingListener groupListener = new GroupMessagingListener();
        MessagingListener listener1 = new MessagingListener();
        MessagingListener listener2 = new MessagingListener();
        groupListener.addListener(listener1);
        groupListener.addListener(listener2);
        groupListener.removeListener(listener1);
        groupListener.removeListener(listener2);
    }
    public void testIsActiveListener() {
        GroupMessagingListener groupListener = new GroupMessagingListener();
        MessagingListener listener1 = new MessagingListener();
        MessagingListener listener2 = new MessagingListener();
        assertFalse(groupListener.isActiveListener(listener1));
        assertFalse(groupListener.isActiveListener(listener2));
        groupListener.addListener(listener1);
        assertTrue(groupListener.isActiveListener(listener1));
        assertFalse(groupListener.isActiveListener(listener2));
        groupListener.addListener(listener2);
        assertTrue(groupListener.isActiveListener(listener1));
        assertTrue(groupListener.isActiveListener(listener2));
        groupListener.removeListener(listener1);
        assertFalse(groupListener.isActiveListener(listener1));
        assertTrue(groupListener.isActiveListener(listener2));
        groupListener.removeListener(listener2);
        assertFalse(groupListener.isActiveListener(listener1));
        assertFalse(groupListener.isActiveListener(listener2));
    }
}
