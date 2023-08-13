public class LoaderConstraintTable extends TwoOopHashtable {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("LoaderConstraintTable");
    nofBuckets = db.lookupIntConstant("LoaderConstraintTable::_nof_buckets").intValue();
  }
  private static int nofBuckets;
  public static int getNumOfBuckets() {
    return nofBuckets;
  }
  public LoaderConstraintTable(Address addr) {
    super(addr);
  }
  protected Class getHashtableEntryClass() {
    return LoaderConstraintEntry.class;
  }
}
