public class AnonLoggerWeakRefLeak extends SimpleApplication {
    public static int DEFAULT_LOOP_TIME = 120;  
    public void doMyAppWork(String[] args) throws Exception {
        int loop_time = 0;
        int max_loop_time = DEFAULT_LOOP_TIME;
        if (args.length < 2) {
            System.out.println("INFO: using default time of "
                + max_loop_time + " seconds.");
        } else {
            try {
                max_loop_time = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("Error: '" + args[1]
                    + "': is not a valid seconds value.");
            }
        }
        long count = 0;
        long now = 0;
        long startTime = System.currentTimeMillis();
        while (now < (startTime + (max_loop_time * 1000))) {
            if ((count % 1000) == 0) {
                System.out.println("INFO: call count = " + count);
            }
            for (int i = 0; i < 100; i++) {
                java.util.logging.Logger.getAnonymousLogger();
                count++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
            now = System.currentTimeMillis();
        }
        System.out.println("INFO: final loop count = " + count);
    }
    public static void main(String[] args) throws Exception {
        AnonLoggerWeakRefLeak myApp = new AnonLoggerWeakRefLeak();
        SimpleApplication.setMyApp(myApp);
        SimpleApplication.main(args);
    }
}
