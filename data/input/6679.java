public class StartTest {
    public static void main(String[] args) throws Exception {
        System.out.println(
            ">>> Test on timer start method with past notifications.");
        System.out.println(">>> Create a Timer object.");
        Timer timer = new Timer();
        System.out.println(
            ">>> Set the flag (setSendPastNotification) to true.");
        timer.setSendPastNotifications(true);
        timer.addNotificationListener(myListener, null, null);
        System.out.println(">>> Add notifications: " + SENT);
        Date date = new Date();
        for (int i = 0; i < SENT; i++) {
            timer.addNotification(
                "testType" + i, "testMsg" + i, "testData" + i, date);
        }
        System.out.println(">>> The notifications should be sent at " + date);
        System.out.println(">>> Sleep 100 ms to have past notifications.");
        Thread.sleep(100);
        System.out.println(">>> Start the timer at " + new Date());
        timer.start();
        System.out.println(">>> Stop the timer.");
        Thread.sleep(100);
        stopping = true;
        timer.stop();
        if (received != SENT) {
            throw new RuntimeException(
                "Expected to receive " + SENT + " but got " + received);
        }
        System.out.println(">>> Received all expected notifications.");
        System.out.println(">>> Bye bye!");
    }
    private static NotificationListener myListener =
        new NotificationListener() {
            public void handleNotification(Notification n, Object hb) {
                if (!stopping) {
                    received++;
                    System.out.println(
                        ">>> myListener-handleNotification: received " +
                        n.getSequenceNumber());
            }
        }
    };
    private static int SENT = 10;
    private static volatile int received = 0;
    private static volatile boolean stopping = false;
}
