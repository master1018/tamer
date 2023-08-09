public abstract class SharedHeap extends CollectedHeap {
  private static AddressField permGenField;
  private static VirtualConstructor ctor;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("SharedHeap");
    permGenField        = type.getAddressField("_perm_gen");
    ctor = new VirtualConstructor(db);
    ctor.addMapping("CompactingPermGen", CompactingPermGen.class);
    ctor.addMapping("CMSPermGen", CMSPermGen.class);
  }
  public SharedHeap(Address addr) {
    super(addr);
  }
  public PermGen perm() {
    return (PermGen) ctor.instantiateWrapperFor(permGenField.getValue(addr));
  }
  public CollectedHeapName kind() {
    return CollectedHeapName.SHARED_HEAP;
  }
  public Generation permGen() {
    return perm().asGen();
  }
}
