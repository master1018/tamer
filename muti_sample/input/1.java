public class Klass extends Oop implements ClassConstants {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  public static int LH_INSTANCE_SLOW_PATH_BIT;
  public static int LH_LOG2_ELEMENT_SIZE_SHIFT;
  public static int LH_ELEMENT_TYPE_SHIFT;
  public static int LH_HEADER_SIZE_SHIFT;
  public static int LH_ARRAY_TAG_SHIFT;
  public static int LH_ARRAY_TAG_TYPE_VALUE;
  public static int LH_ARRAY_TAG_OBJ_VALUE;
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type    = db.lookupType("Klass");
    javaMirror   = new OopField(type.getOopField("_java_mirror"), Oop.getHeaderSize());
    superField   = new OopField(type.getOopField("_super"), Oop.getHeaderSize());
    layoutHelper = new IntField(type.getJIntField("_layout_helper"), Oop.getHeaderSize());
    name         = type.getAddressField("_name");
    accessFlags  = new CIntField(type.getCIntegerField("_access_flags"), Oop.getHeaderSize());
    subklass     = new OopField(type.getOopField("_subklass"), Oop.getHeaderSize());
    nextSibling  = new OopField(type.getOopField("_next_sibling"), Oop.getHeaderSize());
    allocCount   = new CIntField(type.getCIntegerField("_alloc_count"), Oop.getHeaderSize());
    LH_INSTANCE_SLOW_PATH_BIT  = db.lookupIntConstant("Klass::_lh_instance_slow_path_bit").intValue();
    LH_LOG2_ELEMENT_SIZE_SHIFT = db.lookupIntConstant("Klass::_lh_log2_element_size_shift").intValue();
    LH_ELEMENT_TYPE_SHIFT      = db.lookupIntConstant("Klass::_lh_element_type_shift").intValue();
    LH_HEADER_SIZE_SHIFT       = db.lookupIntConstant("Klass::_lh_header_size_shift").intValue();
    LH_ARRAY_TAG_SHIFT         = db.lookupIntConstant("Klass::_lh_array_tag_shift").intValue();
    LH_ARRAY_TAG_TYPE_VALUE    = db.lookupIntConstant("Klass::_lh_array_tag_type_value").intValue();
    LH_ARRAY_TAG_OBJ_VALUE     = db.lookupIntConstant("Klass::_lh_array_tag_obj_value").intValue();
  }
  Klass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  public int getClassStatus() {
    return 0; 
  }
  public boolean isKlass()             { return true; }
  private static OopField  javaMirror;
  private static OopField  superField;
  private static IntField layoutHelper;
  private static AddressField  name;
  private static CIntField accessFlags;
  private static OopField  subklass;
  private static OopField  nextSibling;
  private static CIntField allocCount;
  private Address getValue(AddressField field) {
    return getHandle().getAddressAt(field.getOffset() + Oop.getHeaderSize());
  }
  protected Symbol getSymbol(AddressField field) {
    return Symbol.create(getHandle().getAddressAt(field.getOffset() + Oop.getHeaderSize()));
  }
  public Instance getJavaMirror()       { return (Instance) javaMirror.getValue(this);   }
  public Klass    getSuper()            { return (Klass)    superField.getValue(this);   }
  public Klass    getJavaSuper()        { return null;  }
  public int      getLayoutHelper()     { return (int)           layoutHelper.getValue(this); }
  public Symbol   getName()             { return getSymbol(name); }
  public long     getAccessFlags()      { return            accessFlags.getValue(this);  }
  public AccessFlags getAccessFlagsObj(){ return new AccessFlags(getAccessFlags());      }
  public Klass    getSubklassKlass()    { return (Klass)    subklass.getValue(this);     }
  public Klass    getNextSiblingKlass() { return (Klass)    nextSibling.getValue(this);  }
  public long     getAllocCount()       { return            allocCount.getValue(this);   }
  public long computeModifierFlags() {
    return 0L; 
  }
  public final long getClassModifiers() {
    long flags = computeModifierFlags();
    if (isSuper()) {
       flags |= JVM_ACC_SUPER;
    }
    return flags;
  }
  public boolean isSubclassOf(Klass k) {
    if (k != null) {
      Klass t = this;
      while (t != null) {
        if (t.equals(k)) return true;
        t = t.getSuper();
      }
    }
    return false;
  }
  public boolean isSubtypeOf(Klass k) {
    return computeSubtypeOf(k);
  }
  boolean computeSubtypeOf(Klass k) {
    return isSubclassOf(k);
  }
  public Klass lca( Klass k2 ) {
    Klass k1 = this;
    while ( true ) {
      if ( k1.isSubtypeOf(k2) ) return k2;
      if ( k2.isSubtypeOf(k1) ) return k1;
      k1 = k1.getSuper();
      k2 = k2.getSuper();
    }
  }
  public void printValueOn(PrintStream tty) {
    tty.print("Klass");
  }
  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(javaMirror, true);
      visitor.doOop(superField, true);
      visitor.doInt(layoutHelper, true);
      visitor.doCInt(accessFlags, true);
      visitor.doOop(subklass, true);
      visitor.doOop(nextSibling, true);
      visitor.doCInt(allocCount, true);
    }
  }
  public long getObjectSize() {
    throw new RuntimeException("should not reach here");
  }
  public Klass arrayKlass(int rank)       { return arrayKlassImpl(false, rank); }
  public Klass arrayKlass()               { return arrayKlassImpl(false);       }
  public Klass arrayKlassOrNull(int rank) { return arrayKlassImpl(true, rank);  }
  public Klass arrayKlassOrNull()         { return arrayKlassImpl(true);        }
  public Klass arrayKlassImpl(boolean orNull, int rank) {
    throw new RuntimeException("array_klass should be dispatched to instanceKlass, objArrayKlass or typeArrayKlass");
  }
  public Klass arrayKlassImpl(boolean orNull) {
    throw new RuntimeException("array_klass should be dispatched to instanceKlass, objArrayKlass or typeArrayKlass");
  }
  public String signature() { return getName().asString(); }
  public boolean isPublic()                 { return getAccessFlagsObj().isPublic(); }
  public boolean isFinal()                  { return getAccessFlagsObj().isFinal(); }
  public boolean isInterface()              { return getAccessFlagsObj().isInterface(); }
  public boolean isAbstract()               { return getAccessFlagsObj().isAbstract(); }
  public boolean isSuper()                  { return getAccessFlagsObj().isSuper(); }
  public boolean isSynthetic()              { return getAccessFlagsObj().isSynthetic(); }
  public boolean hasFinalizer()             { return getAccessFlagsObj().hasFinalizer(); }
  public boolean isCloneable()              { return getAccessFlagsObj().isCloneable(); }
  public boolean hasVanillaConstructor()    { return getAccessFlagsObj().hasVanillaConstructor(); }
  public boolean hasMirandaMethods ()       { return getAccessFlagsObj().hasMirandaMethods(); }
}
