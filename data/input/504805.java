public class DriverManager {
    private static PrintStream thePrintStream;
    private static PrintWriter thePrintWriter;
    private static int loginTimeout = 0;
    private static final List<Driver> theDrivers = new ArrayList<Driver>(10);
    private static final SQLPermission logPermission = new SQLPermission(
            "setLog"); 
    static {
        loadInitialDrivers();
    }
    private static void loadInitialDrivers() {
        String theDriverList = AccessController
                .doPrivileged(new PriviAction<String>("jdbc.drivers", null)); 
        if (theDriverList == null) {
            return;
        }
        String[] theDriverNames = theDriverList.split(":"); 
        for (String element : theDriverNames) {
            try {
                Class
                        .forName(element, true, ClassLoader
                                .getSystemClassLoader());
            } catch (Throwable t) {
            }
        }
    }
    private DriverManager() {
        super();
    }
    public static void deregisterDriver(Driver driver) throws SQLException {
        if (driver == null) {
            return;
        }
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        if (!DriverManager.isClassFromClassLoader(driver, callerClassLoader)) {
            throw new SecurityException(Messages.getString("sql.1")); 
        } 
        synchronized (theDrivers) {
            theDrivers.remove(driver);
        }
    }
    public static Connection getConnection(String url) throws SQLException {
        return getConnection(url, new Properties());
    }
    public static Connection getConnection(String url, Properties info)
            throws SQLException {
        String sqlState = "08001"; 
        if (url == null) {
            throw new SQLException(Messages.getString("sql.5"), sqlState); 
        }
        synchronized (theDrivers) {
            for (Driver theDriver : theDrivers) {
                Connection theConnection = theDriver.connect(url, info);
                if (theConnection != null) {
                    return theConnection;
                }
            }
        }
        throw new SQLException(Messages.getString("sql.6"), sqlState); 
    }
    public static Connection getConnection(String url, String user,
            String password) throws SQLException {
        Properties theProperties = new Properties();
        if (null != user) {
            theProperties.setProperty("user", user); 
        }
        if (null != password) {
            theProperties.setProperty("password", password); 
        }
        return getConnection(url, theProperties);
    }
    public static Driver getDriver(String url) throws SQLException {
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        synchronized (theDrivers) {
            Iterator<Driver> theIterator = theDrivers.iterator();
            while (theIterator.hasNext()) {
                Driver theDriver = theIterator.next();
                if (theDriver.acceptsURL(url)
                        && DriverManager.isClassFromClassLoader(theDriver,
                                callerClassLoader)) {
                    return theDriver;
                }
            }
        }
        throw new SQLException(Messages.getString("sql.6"), "08001"); 
    }
    public static Enumeration<Driver> getDrivers() {
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        synchronized (theDrivers) {
            Vector<Driver> theVector = new Vector<Driver>();
            Iterator<Driver> theIterator = theDrivers.iterator();
            while (theIterator.hasNext()) {
                Driver theDriver = theIterator.next();
                if (DriverManager.isClassFromClassLoader(theDriver,
                        callerClassLoader)) {
                    theVector.add(theDriver);
                }
            }
            return theVector.elements();
        }
    }
    public static int getLoginTimeout() {
        return loginTimeout;
    }
    @Deprecated
    public static PrintStream getLogStream() {
        return thePrintStream;
    }
    public static PrintWriter getLogWriter() {
        return thePrintWriter;
    }
    public static void println(String message) {
        if (thePrintWriter != null) {
            thePrintWriter.println(message);
            thePrintWriter.flush();
        } else if (thePrintStream != null) {
            thePrintStream.println(message);
            thePrintStream.flush();
        }
        return;
    }
    public static void registerDriver(Driver driver) throws SQLException {
        if (driver == null) {
            throw new NullPointerException();
        }
        synchronized (theDrivers) {
            theDrivers.add(driver);
        }
    }
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
        return;
    }
    @Deprecated
    public static void setLogStream(PrintStream out) {
        checkLogSecurity();
        thePrintStream = out;
    }
    public static void setLogWriter(PrintWriter out) {
        checkLogSecurity();
        thePrintWriter = out;
    }
    private static void checkLogSecurity() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(logPermission);
        }
    }
    private static boolean isClassFromClassLoader(Object theObject,
            ClassLoader theClassLoader) {
        if ((theObject == null) || (theClassLoader == null)) {
            return false;
        }
        Class<?> objectClass = theObject.getClass();
        try {
            Class<?> checkClass = Class.forName(objectClass.getName(), true,
                    theClassLoader);
            if (checkClass == objectClass) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }
}
