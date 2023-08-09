public class RelationNotificationSeqNoTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName relSvcName = new ObjectName("a:type=relationService");
        RelationServiceMBean relSvc =
                JMX.newMBeanProxy(mbs, relSvcName, RelationServiceMBean.class);
        mbs.createMBean("javax.management.relation.RelationService",
                        relSvcName,
                        new Object[] {Boolean.TRUE},
                        new String[] {"boolean"});
        final BlockingQueue<Notification> q =
                new ArrayBlockingQueue<Notification>(100);
        NotificationListener qListener = new NotificationListener() {
            public void handleNotification(Notification notification,
                                           Object handback) {
                q.add(notification);
            }
        };
        mbs.addNotificationListener(relSvcName, qListener, null, null);
        RoleInfo leftInfo =
            new RoleInfo("left", "javax.management.timer.TimerMBean");
        RoleInfo rightInfo =
            new RoleInfo("right", "javax.management.timer.Timer");
        relSvc.createRelationType("typeName", new RoleInfo[] {leftInfo, rightInfo});
        ObjectName timer1 = new ObjectName("a:type=timer,number=1");
        ObjectName timer2 = new ObjectName("a:type=timer,number=2");
        mbs.createMBean("javax.management.timer.Timer", timer1);
        mbs.createMBean("javax.management.timer.Timer", timer2);
        Role leftRole =
            new Role("left", Arrays.asList(new ObjectName[] {timer1}));
        Role rightRole =
            new Role("right", Arrays.asList(new ObjectName[] {timer2}));
        RoleList roles =
            new RoleList(Arrays.asList(new Role[] {leftRole, rightRole}));
        final int NREPEAT = 10;
        for (int i = 0; i < NREPEAT; i++) {
            relSvc.createRelation("relationName", "typeName", roles);
            relSvc.removeRelation("relationName");
        }
        Notification firstNotif = q.remove();
        long seqNo = firstNotif.getSequenceNumber();
        for (int i = 0; i < NREPEAT * 2 - 1; i++) {
            Notification n = q.remove();
            long nSeqNo = n.getSequenceNumber();
            if (nSeqNo != seqNo + 1) {
                throw new Exception(
                        "TEST FAILED: expected seqNo " + (seqNo + 1) + "; got " +
                        nSeqNo);
            }
            seqNo++;
        }
        System.out.println("TEST PASSED: got " + (NREPEAT * 2) + " notifications " +
                "with contiguous sequence numbers");
    }
}
