public class PlatformLoggingMXBeanTest
{
    ObjectName objectName = null;
    static String LOGGER_NAME_1 = "com.sun.management.Logger1";
    static String LOGGER_NAME_2 = "com.sun.management.Logger2";
    public PlatformLoggingMXBeanTest() throws Exception {
    }
    private void runTest(PlatformLoggingMXBean mBean) throws Exception {
        System.out.println( "***************************************************" );
        System.out.println( "********** PlatformLoggingMXBean Unit Test **********" );
        System.out.println( "***************************************************" );
        System.out.println( "" );
        System.out.println( "*******************************" );
        System.out.println( "*********** Phase 1 ***********" );
        System.out.println( "*******************************" );
        System.out.println( "    Creating MBeanServer " );
        System.out.print( "    Register PlatformLoggingMXBean: " );
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        String[] list = new String[0];
        try {
            objectName = new ObjectName(LogManager.LOGGING_MXBEAN_NAME);
            mbs.registerMBean( mBean, objectName );
        }
        catch ( Exception e ) {
            System.out.println( "FAILED" );
            throw e;
        }
        System.out.println( "PASSED" );
        System.out.println("");
        System.out.println( "*******************************" );
        System.out.println( "*********** Phase 2 ***********" );
        System.out.println( "*******************************" );
        System.out.println( "   Test Logger Name retrieval (getLoggerNames) " );
        try {
            list = (String[]) mbs.getAttribute( objectName,  "LoggerNames" );
        }
        catch ( Exception e ) {
            System.out.println("    : FAILED" );
            throw e;
        }
        Object[] params =  new Object[1];
        String[] signature =  new String[1];
        Level l;
        if ( list == null ) {
            System.out.println("    : PASSED.  No Standard Loggers Present" );
            System.out.println("");
        }
        else {
            System.out.println("    : PASSED. There are " + list.length + " Loggers Present" );
            System.out.println("");
            System.out.println( "*******************************" );
            System.out.println( "*********** Phase 2B **********" );
            System.out.println( "*******************************" );
            System.out.println( " Examine Existing Loggers" );
            for ( int i = 0; i < list.length; i++ ) {
                try {
                    params[0] = list[i];
                    signature[0] = "java.lang.String";
                    String levelName = (String) mbs.invoke(  objectName, "getLoggerLevel", params, signature );
                    System.out.println("    : Logger #" + i + " = " + list[i] );
                    System.out.println("    : Level = " + levelName );
                }
                catch ( Exception e ) {
                    System.out.println("    : FAILED" );
                    throw e;
                }
            }
            System.out.println("    : PASSED" );
        }
        System.out.println("");
        System.out.println( "*******************************" );
        System.out.println( "*********** Phase 3 ***********" );
        System.out.println( "*******************************" );
        System.out.println( " Create and test new Loggers" );
        Logger logger1 = Logger.getLogger( LOGGER_NAME_1 );
        Logger logger2 = Logger.getLogger( LOGGER_NAME_2 );
        try {
            list = (String[]) mbs.getAttribute( objectName,  "LoggerNames" );
        }
        catch ( Exception e ) {
            System.out.println("    : FAILED" );
            throw e;
        }
        boolean log1 = false, log2 = false;
        if ( list == null || list.length < 2 ) {
            System.out.println("    : FAILED.  Could not Detect the presense of the new Loggers" );
            throw new RuntimeException(
                "Could not Detect the presense of the new Loggers");
        }
        else {
            for ( int i = 0; i < list.length; i++ ) {
                if ( list[i].equals( LOGGER_NAME_1 ) ) {
                    log1 = true;
                    System.out.println( "    : Found new Logger : " + list[i] );
                }
                if ( list[i].equals( LOGGER_NAME_2 ) ) {
                    log2 = true;
                    System.out.println( "    : Found new Logger : " + list[i] );
                }
            }
            if ( log1 && log2 )
                System.out.println( "    : PASSED." );
            else {
                System.out.println( "    : FAILED.  Could not Detect the new Loggers." );
                throw new RuntimeException(
                    "Could not Detect the presense of the new Loggers");
            }
        }
        System.out.println("");
        System.out.println( "*******************************" );
        System.out.println( "*********** Phase 4 ***********" );
        System.out.println( "*******************************" );
        System.out.println( " Set and Check the Logger Level" );
        log1 = false;
        log2 = false;
        try {
            params = new Object[2];
            signature =  new String[2];
            params[0] = LOGGER_NAME_1;
            params[1] = Level.ALL.getName();
            signature[0] = "java.lang.String";
            signature[1] = "java.lang.String";
            mbs.invoke(  objectName, "setLoggerLevel", params, signature );
            params[0] = LOGGER_NAME_2;
            params[1] = Level.FINER.getName();
            mbs.invoke(  objectName, "setLoggerLevel", params, signature );
            params =  new Object[1];
            signature =  new String[1];
            params[0] = LOGGER_NAME_1;
            signature[0] = "java.lang.String";
            String levelName = (String) mbs.invoke(  objectName, "getLoggerLevel", params, signature );
            l = Level.parse(levelName);
            System.out.print("    Logger1: " );
            if ( l.equals( l.ALL ) ) {
                System.out.println("Level Set to ALL: PASSED" );
                log1 = true;
            }
            else {
                System.out.println("Level Set to ALL: FAILED" );
                throw new RuntimeException(
                    "Level Set to ALL but returned " + l.toString());
            }
            params =  new Object[1];
            signature =  new String[1];
            params[0] = LOGGER_NAME_2;
            signature[0] = "java.lang.String";
            levelName = (String) mbs.invoke(  objectName, "getLoggerLevel", params, signature );
            l = Level.parse(levelName);
            System.out.print("    Logger2: " );
            if ( l.equals( l.FINER ) ) {
                System.out.println("Level Set to FINER: PASSED" );
                log2 = true;
            }
            else {
                System.out.println("Level Set to FINER: FAILED" );
                throw new RuntimeException(
                    "Level Set to FINER but returned " + l.toString());
            }
        }
        catch ( Exception e ) {
            throw e;
        }
        System.out.println( "" );
        System.out.println( "***************************************************" );
        System.out.println( "***************** All Tests Passed ****************" );
        System.out.println( "***************************************************" );
    }
    public static void main(String[] argv) throws Exception {
        PlatformLoggingMXBean mbean =
            ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        ObjectName objname = mbean.getObjectName();
        if (!objname.equals(new ObjectName(LogManager.LOGGING_MXBEAN_NAME))) {
            throw new RuntimeException("Invalid ObjectName " + objname);
        }
        MBeanServer platformMBS = ManagementFactory.getPlatformMBeanServer();
        ObjectName objName = new ObjectName(LogManager.LOGGING_MXBEAN_NAME);
        platformMBS.getMBeanInfo(objName);
        if (!platformMBS.isInstanceOf(objName, "java.lang.management.PlatformLoggingMXBean") ||
            !platformMBS.isInstanceOf(objName, "java.util.logging.LoggingMXBean")) {
            throw new RuntimeException(objName + " is of unexpected type");
        }
        PlatformLoggingMXBeanTest test = new PlatformLoggingMXBeanTest();
        test.runTest(mbean);
    }
}
