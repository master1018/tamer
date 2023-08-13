public class SleeperApplication extends SimpleApplication {
    public static int DEFAULT_SLEEP_TIME = 60;  
    public void doMyAppWork(String[] args) throws Exception {
        int sleep_time = DEFAULT_SLEEP_TIME;
        if (args.length < 2) {
            System.out.println("INFO: using default sleep time of "
                + sleep_time + " seconds.");
        } else {
            try {
                sleep_time = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("Error: '" + args[1] +
                    "': is not a valid seconds value.");
            }
        }
        Thread.sleep(sleep_time * 1000);  
    }
    public static void main(String[] args) throws Exception {
        SleeperApplication myApp = new SleeperApplication();
        SimpleApplication.setMyApp(myApp);
        SimpleApplication.main(args);
    }
}
