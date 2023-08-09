public class OSThread extends VMObject {
    private static JIntField interruptedField;
    static {
        VM.registerVMInitializedObserver(new Observer() {
            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }
    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("OSThread");
        interruptedField = type.getJIntField("_interrupted");
    }
    public OSThread(Address addr) {
        super(addr);
    }
    public boolean interrupted() {
        return ((int)interruptedField.getValue(addr)) != 0;
    }
}
