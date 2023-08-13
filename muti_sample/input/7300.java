public class SnmpEngineImpl implements SnmpEngine, Serializable {
    private static final long serialVersionUID = -2564301391365614725L;
    public static final int noAuthNoPriv = 0;
    public static final int authNoPriv = 1;
    public static final int authPriv = 3;
    public static final int reportableFlag = 4;
    public static final int authMask = 1;
    public static final int privMask = 2;
    public static final int authPrivMask = 3;
    private SnmpEngineId engineid = null;
    private SnmpEngineFactory factory = null;
    private long startTime = 0;
    private int boot = 0;
    private boolean checkOid = false;
    transient private SnmpUsmKeyHandler usmKeyHandler = null;
    transient private SnmpLcd lcd = null;
    transient private SnmpSecuritySubSystem securitySub = null;
    transient private SnmpMsgProcessingSubSystem messageSub = null;
    transient private SnmpAccessControlSubSystem accessSub = null;
    public synchronized int getEngineTime() {
        long delta = (System.currentTimeMillis() / 1000) - startTime;
        if(delta >  0x7FFFFFFF) {
            startTime = System.currentTimeMillis() / 1000;
            if(boot != 0x7FFFFFFF)
                boot += 1;
            storeNBBoots(boot);
        }
        return (int) ((System.currentTimeMillis() / 1000) - startTime);
    }
    public SnmpEngineId getEngineId() {
        return engineid;
    }
    public SnmpUsmKeyHandler getUsmKeyHandler() {
        return usmKeyHandler;
    }
    public SnmpLcd getLcd() {
        return lcd;
    }
    public int getEngineBoots() {
        return boot;
    }
    public SnmpEngineImpl(SnmpEngineFactory fact,
                          SnmpLcd lcd,
                          SnmpEngineId engineid) throws UnknownHostException {
        init(lcd, fact);
        initEngineID();
        if(this.engineid == null) {
            if(engineid != null)
                this.engineid = engineid;
            else
                this.engineid = SnmpEngineId.createEngineId();
        }
        lcd.storeEngineId(this.engineid);
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpEngineImpl.class.getName(),
                    "SnmpEngineImpl(SnmpEngineFactory,SnmpLcd,SnmpEngineId)",
                    "LOCAL ENGINE ID: " + this.engineid);
        }
    }
    public SnmpEngineImpl(SnmpEngineFactory fact,
                          SnmpLcd lcd,
                          InetAddress address,
                          int port) throws UnknownHostException {
        init(lcd, fact);
        initEngineID();
        if(engineid == null)
            engineid = SnmpEngineId.createEngineId(address, port);
        lcd.storeEngineId(engineid);
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpEngineImpl.class.getName(),
                    "SnmpEngineImpl(SnmpEngineFactory,SnmpLcd,InetAddress,int)",
                    "LOCAL ENGINE ID: " + engineid + " / " +
                    "LOCAL ENGINE NB BOOTS: " + boot + " / " +
                    "LOCAL ENGINE START TIME: " + getEngineTime());
        }
    }
    public SnmpEngineImpl(SnmpEngineFactory fact,
                          SnmpLcd lcd,
                          int port) throws UnknownHostException {
        init(lcd, fact);
        initEngineID();
        if(engineid == null)
           engineid = SnmpEngineId.createEngineId(port);
        lcd.storeEngineId(engineid);
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpEngineImpl.class.getName(),
                    "SnmpEngineImpl(SnmpEngineFactory,SnmpLcd,int)",
                    "LOCAL ENGINE ID: " + engineid + " / " +
                    "LOCAL ENGINE NB BOOTS: " + boot + " / " +
                    "LOCAL ENGINE START TIME: " + getEngineTime());
        }
    }
    public SnmpEngineImpl(SnmpEngineFactory fact,
                          SnmpLcd lcd) throws UnknownHostException {
        init(lcd, fact);
        initEngineID();
        if(engineid == null)
            engineid = SnmpEngineId.createEngineId();
        lcd.storeEngineId(engineid);
        if (SNMP_LOGGER.isLoggable(Level.FINER)) {
            SNMP_LOGGER.logp(Level.FINER, SnmpEngineImpl.class.getName(),
                    "SnmpEngineImpl(SnmpEngineFactory,SnmpLcd)",
                    "LOCAL ENGINE ID: " + engineid + " / " +
                    "LOCAL ENGINE NB BOOTS: " + boot + " / " +
                    "LOCAL ENGINE START TIME: " + getEngineTime());
        }
    }
    public synchronized void activateCheckOid() {
        checkOid = true;
    }
    public synchronized void deactivateCheckOid() {
        checkOid = false;
    }
    public synchronized boolean isCheckOidActivated() {
        return checkOid;
    }
    private void storeNBBoots(int boot) {
        if(boot < 0 || boot == 0x7FFFFFFF) {
            boot = 0x7FFFFFFF;
            lcd.storeEngineBoots(boot);
        }
        else
            lcd.storeEngineBoots(boot + 1);
    }
    private void init(SnmpLcd lcd, SnmpEngineFactory fact) {
        this.factory = fact;
        this.lcd = lcd;
        boot = lcd.getEngineBoots();
        if(boot == -1 || boot == 0)
            boot = 1;
        storeNBBoots(boot);
        startTime = System.currentTimeMillis() / 1000;
    }
    void setUsmKeyHandler(SnmpUsmKeyHandler usmKeyHandler) {
        this.usmKeyHandler = usmKeyHandler;
    }
    private void initEngineID() throws UnknownHostException {
        String id = lcd.getEngineId();
        if(id != null) {
            engineid = SnmpEngineId.createEngineId(id);
        }
    }
    public SnmpMsgProcessingSubSystem getMsgProcessingSubSystem() {
        return messageSub;
    }
    public void setMsgProcessingSubSystem(SnmpMsgProcessingSubSystem sys) {
        messageSub = sys;
    }
    public SnmpSecuritySubSystem getSecuritySubSystem() {
        return securitySub;
    }
    public void setSecuritySubSystem(SnmpSecuritySubSystem sys) {
        securitySub = sys;
    }
    public void setAccessControlSubSystem(SnmpAccessControlSubSystem sys) {
        accessSub = sys;
    }
    public SnmpAccessControlSubSystem getAccessControlSubSystem() {
        return accessSub;
    }
    public static void checkSecurityLevel(byte msgFlags)
        throws SnmpBadSecurityLevelException {
        int secLevel = msgFlags & SnmpDefinitions.authPriv;
        if((secLevel & SnmpDefinitions.privMask) != 0)
            if((secLevel & SnmpDefinitions.authMask) == 0) {
                throw new SnmpBadSecurityLevelException("Security level:"+
                                                        " noAuthPriv!!!");
            }
    }
}
