public class MemoryHandler extends Handler {
    private static final int DEFAULT_SIZE = 1000;
    private Handler target;
    private int size = DEFAULT_SIZE;
    private Level push = Level.SEVERE;
    private final LogManager manager = LogManager.getLogManager();
    private LogRecord[] buffer;
    private int cursor;
    public MemoryHandler() {
        super();
        String className = this.getClass().getName();
        final String targetName = manager.getProperty(className + ".target"); 
        try {
            Class<?> targetClass = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                        public Class<?> run() throws Exception {
                            ClassLoader loader = Thread.currentThread()
                                    .getContextClassLoader();
                            if (loader == null) {
                                loader = ClassLoader.getSystemClassLoader();
                            }
                            return loader.loadClass(targetName);
                        }
                    });
            target = (Handler) targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("logging.10", 
                    targetName));
        }
        String sizeString = manager.getProperty(className + ".size"); 
        if (null != sizeString) {
            try {
                size = Integer.parseInt(sizeString);
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }
            } catch (Exception e) {
                printInvalidPropMessage(className + ".size", sizeString, e); 
            }
        }
        String pushName = manager.getProperty(className + ".push"); 
        if (null != pushName) {
            try {
                push = Level.parse(pushName);
            } catch (Exception e) {
                printInvalidPropMessage(className + ".push", pushName, e); 
            }
        }
        initProperties("ALL", null, "java.util.logging.SimpleFormatter", null); 
        buffer = new LogRecord[size];
    }
    public MemoryHandler(Handler target, int size, Level pushLevel) {
        if (size <= 0) {
            throw new IllegalArgumentException(Messages.getString("logging.11")); 
        }
        target.getLevel();
        pushLevel.intValue();
        this.target = target;
        this.size = size;
        this.push = pushLevel;
        initProperties("ALL", null, "java.util.logging.SimpleFormatter", null); 
        buffer = new LogRecord[size];
    }
    @Override
    public void close() {
        manager.checkAccess();
        target.close();
        setLevel(Level.OFF);
    }
    @Override
    public void flush() {
        target.flush();
    }
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (cursor >= size) {
            cursor = 0;
        }
        buffer[cursor++] = record;
        if (record.getLevel().intValue() >= push.intValue()) {
            push();
        }
        return;
    }
    public Level getPushLevel() {
        return push;
    }
    @Override
    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record);
    }
    public void push() {
        for (int i = cursor; i < size; i++) {
            if (null != buffer[i]) {
                target.publish(buffer[i]);
            }
            buffer[i] = null;
        }
        for (int i = 0; i < cursor; i++) {
            if (null != buffer[i]) {
                target.publish(buffer[i]);
            }
            buffer[i] = null;
        }
        cursor = 0;
    }
    public void setPushLevel(Level newLevel) {
        manager.checkAccess();
        newLevel.intValue();
        this.push = newLevel;
    }
}
