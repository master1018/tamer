public class RelationNotificationSourceTest {
    public static void main(String[] args) throws Exception {
        ObjectName name1 = new ObjectName("a:n=1");
        ObjectName name2 = new ObjectName("a:n=2");
        ObjectName name = new ObjectName("a:b=c");
        Notification n1 =
            new RelationNotification(RELATION_BASIC_REMOVAL,
                                     name,
                                     1234L,
                                     System.currentTimeMillis(),
                                     "message",
                                     "id",
                                     "typeName",
                                     name1,
                                     Collections.singletonList(name2));
        if (!name.equals(n1.getSource()))
            throw new Exception("FAILED: source is " + n1.getSource());
        Notification n2 =
            new RelationNotification(RELATION_BASIC_UPDATE,
                                     name,
                                     1234L,
                                     System.currentTimeMillis(),
                                     "message",
                                     "id",
                                     "typeName",
                                     name1,
                                     "role",
                                     Collections.singletonList(name2),
                                     Collections.singletonList(name2));
        if (!name.equals(n2.getSource()))
            throw new Exception("FAILED: source is " + n2.getSource());
        System.out.println("TEST PASSED");
    }
}
