public class ParentLoggersTest {
    static final LogManager  logMgr     = LogManager.getLogManager();
    static final PrintStream out        = System.out;
    static final boolean     PASSED     = true;
    static final boolean     FAILED     = false;
    static final String      MSG_PASSED = "ParentLoggersTest: passed";
    static final String      MSG_FAILED = "ParentLoggersTest: failed";
    static final String TST_SRC_PROP    = "test.src";
    static final String CFG_FILE_PROP   = "java.util.logging.config.file";
    static final String LM_PROP_FNAME   = "ParentLoggersTest.props";
    static final String PARENT_NAME_1   = "myParentLogger";
    static final String PARENT_NAME_2   = "abc.xyz.foo";
    static final String LOGGER_NAME_1   = PARENT_NAME_1 + ".myLogger";
    static final String LOGGER_NAME_2   = PARENT_NAME_2 + ".myBar.myLogger";
    static final List<String> initialLoggerNames = new ArrayList<String>();
    public static void main(String args[]) throws Exception {
        Enumeration<String> e = logMgr.getLoggerNames();
        List<String> defaultLoggers = getDefaultLoggerNames();
        while (e.hasMoreElements()) {
            String logger = e.nextElement();
            if (!defaultLoggers.contains(logger)) {
                initialLoggerNames.add(logger);
            }
        };
        String tstSrc = System.getProperty(TST_SRC_PROP);
        File   fname  = new File(tstSrc, LM_PROP_FNAME);
        String prop   = fname.getCanonicalPath();
        System.setProperty(CFG_FILE_PROP, prop);
        logMgr.readConfiguration();
        System.out.println();
        if (checkLoggers() == PASSED) {
            System.out.println(MSG_PASSED);
        } else {
            System.out.println(MSG_FAILED);
            throw new Exception(MSG_FAILED);
        }
    }
    public static List<String> getDefaultLoggerNames() {
        List<String> expectedLoggerNames = new ArrayList<String>();
        expectedLoggerNames.add("");       
        expectedLoggerNames.add("global"); 
        return expectedLoggerNames;
    }
    public static boolean checkLoggers() {
        String failMsg = "# checkLoggers: getLoggerNames() returned unexpected loggers";
        Vector<String> expectedLoggerNames = new Vector<String>(getDefaultLoggerNames());
        Logger logger1 = Logger.getLogger(LOGGER_NAME_1);
        expectedLoggerNames.addElement(PARENT_NAME_1);
        expectedLoggerNames.addElement(LOGGER_NAME_1);
        Logger logger2 = Logger.getLogger(LOGGER_NAME_2);
        expectedLoggerNames.addElement(PARENT_NAME_2);
        expectedLoggerNames.addElement(LOGGER_NAME_2);
        Enumeration<String> returnedLoggersEnum = logMgr.getLoggerNames();
        Vector<String>      returnedLoggerNames = new Vector<String>(0);
        while (returnedLoggersEnum.hasMoreElements()) {
           String logger = returnedLoggersEnum.nextElement();
            if (!initialLoggerNames.contains(logger)) {
                returnedLoggerNames.addElement(logger);
            }
        };
        return checkNames(expectedLoggerNames, returnedLoggerNames, failMsg);
    }
    private static boolean checkNames(Vector<String> expNames,
                                      Vector<String> retNames,
                                      String failMsg) {
        boolean status = PASSED;
        if (expNames.size() != retNames.size()) {
            status = FAILED;
        } else {
            boolean checked[] = new boolean[retNames.size()];
            for (int i = 0; i < expNames.size(); i++) {
                 int j = 0;
                for (; j < retNames.size(); j++) {
                    if (!checked[j] &&
                        expNames.elementAt(i).equals(retNames.elementAt(j))) {
                        checked[j] = true;
                        break;
                    }
                }
                if (j >= retNames.size()) {
                    status = FAILED;
                    break;
                }
            }
        }
        if (!status) {
            printFailMsg(expNames, retNames, failMsg);
        }
        return status;
    }
    private static void printFailMsg(Vector<String> expNames,
                                     Vector<String> retNames,
                                     String failMsg) {
        out.println();
        out.println(failMsg);
        if (expNames.size() == 0) {
            out.println("# there are NO expected logger names");
        } else {
            out.println("# expected logger names (" + expNames.size() + "):");
            for (int i = 0; i < expNames.size(); i++) {
               out.println(" expNames[" + i + "] = " + expNames.elementAt(i));
            }
        }
        if (retNames.size() == 0) {
            out.println("# there are NO returned logger names");
        } else {
            out.println("# returned logger names (" + retNames.size() + "):");
            for (int i = 0; i < retNames.size(); i++) {
               out.println("  retNames[" + i + "] = " + retNames.elementAt(i));
            }
        }
    }
}
