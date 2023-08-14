public abstract class TempStorage {
    private static Log log = LogFactory.getLog(TempStorage.class);
    private static TempStorage inst = null;
    static {
        String clazz = System.getProperty("org.apache.james.mime4j.tempStorage");
        try {
            if (inst != null) {
                inst = (TempStorage) Class.forName(clazz).newInstance();
            }
        } catch (Throwable t) {
            log.warn("Unable to create or instantiate TempStorage class '" 
                      + clazz + "' using SimpleTempStorage instead", t);
        }
        if (inst == null) {
            inst = new SimpleTempStorage();            
        }
    }
    public abstract TempPath getRootTempPath();
    public static TempStorage getInstance() {
        return inst;
    }
    public static void setInstance(TempStorage inst) {
        if (inst == null) {
            throw new NullPointerException("inst");
        }
        TempStorage.inst = inst;
    }
}
