public class cInterpreter extends VMObject {
  private static final boolean DEBUG = true;
  private static AddressField bcpField;
  private static AddressField localsField;
  private static AddressField constantsField;
  private static AddressField methodField;
  private static AddressField stackField;       
  private static AddressField stackBaseField;   
  private static AddressField stackLimitField;  
  private static AddressField monitorBaseField;
  private static CIntegerField messageField;
  private static AddressField prevFieldField;
  private static AddressField wrapperField;
  private static AddressField prevField;
  private static int NO_REQUEST;
  private static int INITIALIZE;
  private static int METHOD_ENTRY;
  private static int METHOD_RESUME;
  private static int GOT_MONITORS;
  private static int RETHROW_EXCEPTION;
  private static int CALL_METHOD;
  private static int RETURN_FROM_METHOD;
  private static int RETRY_METHOD;
  private static int MORE_MONITORS;
  private static int THROWING_EXCEPTION;
  private static int POPPING_FRAME;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type cInterpreterType = db.lookupType("cInterpreter");
    bcpField = cInterpreterType.getAddressField("_bcp");
    localsField = cInterpreterType.getAddressField("_locals");
    constantsField = cInterpreterType.getAddressField("_constants");
    methodField = cInterpreterType.getAddressField("_method");
    stackField = cInterpreterType.getAddressField("_stack");
    stackBaseField = cInterpreterType.getAddressField("_stack_base");
    stackLimitField = cInterpreterType.getAddressField("_stack_limit");
    monitorBaseField = cInterpreterType.getAddressField("_monitor_base");
    messageField = null;
    wrapperField = cInterpreterType.getAddressField("_wrapper");
    prevField = cInterpreterType.getAddressField("_prev_link");
  }
  public cInterpreter(Address addr) {
    super(addr);
  }
  public Address prev() {
    return prevField.getValue(addr);
  }
  public Address locals() {
    Address val = localsField.getValue(addr);
    return val;
  }
  public Address localsAddr() {
    Address localsAddr = localsField.getValue(addr);
    return localsAddr;
  }
  public Address bcp() {
    Address val = bcpField.getValue(addr);
    return val;
  }
  public Address bcpAddr() {
    Address bcpAddr = addr.addOffsetTo(bcpField.getOffset());
    return bcpAddr;
  }
  public Address constants() {
    Address val = constantsField.getValue(addr);
    return val;
  }
  public Address constantsAddr() {
    Address constantsAddr = constantsField.getValue(addr);
    return constantsAddr;
  }
  public Address method() {
    Address val = methodField.getValue(addr);
    return val;
  }
  public Address methodAddr() {
    Address methodAddr = addr.addOffsetTo(methodField.getOffset());
    return methodAddr;
  }
  public Address stack() {
    Address val = stackField.getValue(addr);
    return val;
  }
  public Address stackBase() {
    Address val = stackBaseField.getValue(addr);
    return val;
  }
  public Address stackLimit() {
    Address val = stackLimitField.getValue(addr);
    return val;
  }
  public Address monitorBase() {
    Address val = monitorBaseField.getValue(addr);
    return val;
  }
  public Address wrapper() {
    return wrapperField.getValue(addr);
  }
  public int message() {
    int val = (int) messageField.getValue(addr);
    return val;
  }
}
