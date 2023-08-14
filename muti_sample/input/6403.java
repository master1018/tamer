public class SystemDictionary {
  private static AddressField dictionaryField;
  private static AddressField sharedDictionaryField;
  private static AddressField placeholdersField;
  private static AddressField loaderConstraintTableField;
  private static sun.jvm.hotspot.types.OopField javaSystemLoaderField;
  private static int nofBuckets;
  private static sun.jvm.hotspot.types.OopField objectKlassField;
  private static sun.jvm.hotspot.types.OopField classLoaderKlassField;
  private static sun.jvm.hotspot.types.OopField stringKlassField;
  private static sun.jvm.hotspot.types.OopField systemKlassField;
  private static sun.jvm.hotspot.types.OopField threadKlassField;
  private static sun.jvm.hotspot.types.OopField threadGroupKlassField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("SystemDictionary");
    dictionaryField = type.getAddressField("_dictionary");
    sharedDictionaryField = type.getAddressField("_shared_dictionary");
    placeholdersField = type.getAddressField("_placeholders");
    loaderConstraintTableField = type.getAddressField("_loader_constraints");
    javaSystemLoaderField = type.getOopField("_java_system_loader");
    nofBuckets = db.lookupIntConstant("SystemDictionary::_nof_buckets").intValue();
    objectKlassField = type.getOopField(WK_KLASS("Object_klass"));
    classLoaderKlassField = type.getOopField(WK_KLASS("ClassLoader_klass"));
    stringKlassField = type.getOopField(WK_KLASS("String_klass"));
    systemKlassField = type.getOopField(WK_KLASS("System_klass"));
    threadKlassField = type.getOopField(WK_KLASS("Thread_klass"));
    threadGroupKlassField = type.getOopField(WK_KLASS("ThreadGroup_klass"));
  }
  private static String WK_KLASS(String name) {
      return ("_well_known_klasses[SystemDictionary::"+WK_KLASS_ENUM_NAME(name)+"]");
  }
  private static String WK_KLASS_ENUM_NAME(String kname) {
      return (kname+"_knum");
  }
  public Dictionary dictionary() {
    Address tmp = dictionaryField.getValue();
    return (Dictionary) VMObjectFactory.newObject(Dictionary.class, tmp);
  }
  public Dictionary sharedDictionary() {
    Address tmp = sharedDictionaryField.getValue();
    return (Dictionary) VMObjectFactory.newObject(Dictionary.class, tmp);
  }
  public PlaceholderTable placeholders() {
    Address tmp = placeholdersField.getValue();
    return (PlaceholderTable) VMObjectFactory.newObject(PlaceholderTable.class, tmp);
  }
  public LoaderConstraintTable constraints() {
    Address tmp = placeholdersField.getValue();
    return (LoaderConstraintTable) VMObjectFactory.newObject(LoaderConstraintTable.class, tmp);
  }
  public static InstanceKlass getThreadKlass() {
    return (InstanceKlass) newOop(threadKlassField.getValue());
  }
  public static InstanceKlass getThreadGroupKlass() {
    return (InstanceKlass) newOop(threadGroupKlassField.getValue());
  }
  public static InstanceKlass getObjectKlass() {
    return (InstanceKlass) newOop(objectKlassField.getValue());
  }
  public static InstanceKlass getStringKlass() {
    return (InstanceKlass) newOop(stringKlassField.getValue());
  }
  public static InstanceKlass getClassLoaderKlass() {
    return (InstanceKlass) newOop(classLoaderKlassField.getValue());
  }
  public static InstanceKlass getSystemKlass() {
    return (InstanceKlass) newOop(systemKlassField.getValue());
  }
  public InstanceKlass getAbstractOwnableSynchronizerKlass() {
    return (InstanceKlass) find("java/util/concurrent/locks/AbstractOwnableSynchronizer",
                                null, null);
  }
  public static Oop javaSystemLoader() {
    return newOop(javaSystemLoaderField.getValue());
  }
  public static int getNumOfBuckets() {
    return nofBuckets;
  }
  private static Oop newOop(OopHandle handle) {
    return VM.getVM().getObjectHeap().newOop(handle);
  }
  public Klass find(String className, Oop classLoader, Oop protectionDomain) {
    Symbol sym = VM.getVM().getSymbolTable().probe(className);
    if (sym == null) return null;
    return find(sym, classLoader, protectionDomain);
  }
  public Klass find(Symbol className, Oop classLoader, Oop protectionDomain) {
    Dictionary dict = dictionary();
    long hash = dict.computeHash(className, classLoader);
    int index = dict.hashToIndex(hash);
    return dict.find(index, hash, className, classLoader, protectionDomain);
  }
  public static interface ClassVisitor {
    public void visit(Klass k);
  }
  public static interface ClassAndLoaderVisitor {
    public void visit(Klass k, Oop loader);
  }
  public void allClassesDo(final ClassVisitor v) {
    ClassVisitor visitor = new ClassVisitor() {
      public void visit(Klass k) {
        for (Klass l = k; l != null; l = l.arrayKlassOrNull()) {
          v.visit(l);
        }
      }
    };
    classesDo(visitor);
    VM.getVM().getUniverse().basicTypeClassesDo(visitor);
  }
  public void classesDo(ClassVisitor v) {
    dictionary().classesDo(v);
  }
  public void classesDo(ClassAndLoaderVisitor v) {
    dictionary().classesDo(v);
  }
  public void primArrayClassesDo(ClassAndLoaderVisitor v) {
    placeholders().primArrayClassesDo(v);
  }
}
