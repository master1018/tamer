public class SimpleApplication {
    private static SimpleApplication myApp;      
    private static String            myAppName;  
    private static int               myPort;     
    private static ServerSocket      mySS;       
    protected SimpleApplication() {
        myAppName = getClass().getName();
    }
    final public static SimpleApplication getMyApp() {
        return myApp;
    }
    final public static void setMyApp(SimpleApplication _myApp) {
        myApp = _myApp;
    }
    final public void doMyAppFinish(String[] args) throws Exception {
        System.out.println("INFO: " + myAppName + " is waiting on port: " +
            myPort);
        System.out.flush();
        Socket s = mySS.accept();
        s.close();
        mySS.close();
        System.out.println("INFO: " + myAppName + " is shutting down.");
        System.out.flush();
    }
    final public void doMyAppStart(String[] args) throws Exception {
        if (args.length < 1) {
            throw new RuntimeException("Usage: " + myAppName +
                " port-file [arg(s)]");
        }
        mySS = new ServerSocket(0);
        myPort = mySS.getLocalPort();
        File f = new File(args[0]);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write( Integer.toString(myPort).getBytes("UTF-8") );
        fos.close();
        System.out.println("INFO: " + myAppName + " created socket on port: " +
            myPort);
        System.out.flush();
    }
    public void doMyAppWork(String[] args) throws Exception {
    }
    public static void main(String[] args) throws Exception {
        if (myApp == null) {
            myApp = new SimpleApplication();
        }
        myApp.doMyAppStart(args);   
        System.out.println("INFO: " + myAppName + " is calling doMyAppWork()");
        System.out.flush();
        myApp.doMyAppWork(args);    
        System.out.println("INFO: " + myAppName + " returned from" +
            " doMyAppWork()");
        System.out.flush();
        myApp.doMyAppFinish(args);  
        System.exit(0);
    }
}
