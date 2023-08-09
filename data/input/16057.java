public abstract class SnmpLcd {
    class SubSysLcdManager {
        private Hashtable<Integer, SnmpModelLcd> models =
                new Hashtable<Integer, SnmpModelLcd>();
        public void addModelLcd(int id,
                                SnmpModelLcd usmlcd) {
            models.put(new Integer(id), usmlcd);
        }
        public SnmpModelLcd getModelLcd(int id) {
            return models.get(new Integer(id));
        }
        public SnmpModelLcd removeModelLcd(int id) {
            return models.remove(new Integer(id));
        }
    }
    private Hashtable<SnmpSubSystem, SubSysLcdManager> subs =
            new Hashtable<SnmpSubSystem, SubSysLcdManager>();
    public abstract int getEngineBoots();
    public abstract String getEngineId();
    public abstract void storeEngineBoots(int i);
    public abstract void  storeEngineId(SnmpEngineId id);
    public void addModelLcd(SnmpSubSystem sys,
                            int id,
                            SnmpModelLcd lcd) {
        SubSysLcdManager subsys = subs.get(sys);
        if( subsys == null ) {
            subsys = new SubSysLcdManager();
            subs.put(sys, subsys);
        }
        subsys.addModelLcd(id, lcd);
    }
    public void removeModelLcd(SnmpSubSystem sys,
                                int id)
        throws SnmpUnknownModelLcdException, SnmpUnknownSubSystemException {
        SubSysLcdManager subsys = subs.get(sys);
        if( subsys != null ) {
            SnmpModelLcd lcd = subsys.removeModelLcd(id);
            if(lcd == null) {
                throw new SnmpUnknownModelLcdException("Model : " + id);
            }
        }
        else
            throw new SnmpUnknownSubSystemException(sys.toString());
    }
    public SnmpModelLcd getModelLcd(SnmpSubSystem sys,
                                    int id) {
        SubSysLcdManager subsys = subs.get(sys);
        if(subsys == null) return null;
        return subsys.getModelLcd(id);
    }
}
