public class OopMapSet extends VMObject {
  private static final boolean DEBUG = System.getProperty("sun.jvm.hotspot.compiler.OopMapSet.DEBUG") != null;
  private static CIntegerField omCountField;
  private static CIntegerField omSizeField;
  private static AddressField  omDataField;
  private static int REG_COUNT;
  private static int SAVED_ON_ENTRY_REG_COUNT;
  private static int C_SAVED_ON_ENTRY_REG_COUNT;
  private static class MyVisitor implements OopMapVisitor {
    private AddressVisitor addressVisitor;
    public MyVisitor(AddressVisitor oopVisitor) {
      setAddressVisitor(oopVisitor);
    }
    public void setAddressVisitor(AddressVisitor addressVisitor) {
      this.addressVisitor = addressVisitor;
    }
    public void visitOopLocation(Address oopAddr) {
      addressVisitor.visitAddress(oopAddr);
    }
    public void visitDerivedOopLocation(Address baseOopAddr, Address derivedOopAddr) {
      if (VM.getVM().isClientCompiler()) {
        Assert.that(false, "should not reach here");
      } else if (VM.getVM().isServerCompiler() &&
                 VM.getVM().useDerivedPointerTable()) {
        Assert.that(false, "FIXME: add derived pointer table");
      }
    }
    public void visitValueLocation(Address valueAddr) {
    }
    public void visitNarrowOopLocation(Address narrowOopAddr) {
      addressVisitor.visitCompOopAddress(narrowOopAddr);
    }
  }
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("OopMapSet");
    omCountField  = type.getCIntegerField("_om_count");
    omSizeField   = type.getCIntegerField("_om_size");
    omDataField   = type.getAddressField("_om_data");
    if (!VM.getVM().isCore()) {
      REG_COUNT = db.lookupIntConstant("REG_COUNT").intValue();
      if (VM.getVM().isServerCompiler()) {
        SAVED_ON_ENTRY_REG_COUNT = (int) db.lookupIntConstant("SAVED_ON_ENTRY_REG_COUNT").intValue();
        C_SAVED_ON_ENTRY_REG_COUNT = (int) db.lookupIntConstant("C_SAVED_ON_ENTRY_REG_COUNT").intValue();
      }
    }
  }
  public OopMapSet(Address addr) {
    super(addr);
  }
  public long getSize() {
    return omCountField.getValue(addr);
  }
  public OopMap getMapAt(int index) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that((index >= 0) && (index <= getSize()),"bad index");
    }
    Address omDataAddr = omDataField.getValue(addr);
    Address oopMapAddr = omDataAddr.getAddressAt(index * VM.getVM().getAddressSize());
    if (oopMapAddr == null) {
      return null;
    }
    return new OopMap(oopMapAddr);
  }
  public OopMap findMapAtOffset(long pcOffset, boolean debugging) {
    int i;
    int len = (int) getSize();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(len > 0, "must have pointer maps");
    }
    for (i = 0; i < len; i++) {
      if (getMapAt(i).getOffset() >= pcOffset) {
        break;
      }
    }
    if (!debugging) {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(i < len, "oopmap not found for pcOffset = " + pcOffset + "; len = " + len);
        Assert.that(getMapAt(i).getOffset() == pcOffset, "oopmap not found");
      }
    } else {
      if (i == len) {
        if (DEBUG) {
          System.out.println("can't find oopmap at " + pcOffset);
          System.out.print("Oopmap offsets are [ ");
          for (i = 0; i < len; i++) {
            System.out.print(getMapAt(i).getOffset());
          }
          System.out.println("]");
        }
        i = len - 1;
        return getMapAt(i);
      }
    }
    OopMap m = getMapAt(i);
    return m;
  }
  public static void oopsDo(Frame fr, CodeBlob cb, RegisterMap regMap, AddressVisitor oopVisitor, boolean debugging) {
    allDo(fr, cb, regMap, new MyVisitor(oopVisitor), debugging);
  }
  public static void allDo(Frame fr, CodeBlob cb, RegisterMap regMap, OopMapVisitor visitor, boolean debugging) {
    if (Assert.ASSERTS_ENABLED) {
      CodeBlob tmpCB = VM.getVM().getCodeCache().findBlob(fr.getPC());
      Assert.that(tmpCB != null && cb.equals(tmpCB), "wrong codeblob passed in");
    }
    OopMapSet maps = cb.getOopMaps();
    OopMap map = cb.getOopMapForReturnAddress(fr.getPC(), debugging);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "no ptr map found");
    }
    OopMapValue omv;
    {
      for (OopMapStream oms = new OopMapStream(map, OopMapValue.OopTypes.DERIVED_OOP_VALUE); !oms.isDone(); oms.next()) {
        if (VM.getVM().isClientCompiler()) {
          Assert.that(false, "should not reach here");
        }
        omv = oms.getCurrent();
        Address loc = fr.oopMapRegToLocation(omv.getReg(), regMap);
        if (loc != null) {
          Address baseLoc    = fr.oopMapRegToLocation(omv.getContentReg(), regMap);
          Address derivedLoc = loc;
          visitor.visitDerivedOopLocation(baseLoc, derivedLoc);
        }
      }
    }
    OopMapValue.OopTypes[] values = new OopMapValue.OopTypes[] {
      OopMapValue.OopTypes.OOP_VALUE, OopMapValue.OopTypes.VALUE_VALUE, OopMapValue.OopTypes.NARROWOOP_VALUE
    };
    {
      for (OopMapStream oms = new OopMapStream(map, values); !oms.isDone(); oms.next()) {
        omv = oms.getCurrent();
        Address loc = fr.oopMapRegToLocation(omv.getReg(), regMap);
        if (loc != null) {
          if (omv.getType() == OopMapValue.OopTypes.OOP_VALUE) {
            visitor.visitOopLocation(loc);
          } else if (omv.getType() == OopMapValue.OopTypes.VALUE_VALUE) {
            visitor.visitValueLocation(loc);
          } else if (omv.getType() == OopMapValue.OopTypes.NARROWOOP_VALUE) {
            visitor.visitNarrowOopLocation(loc);
          }
        }
      }
    }
  }
  public static void updateRegisterMap(Frame fr, CodeBlob cb, RegisterMap regMap, boolean debugging) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!VM.getVM().isCore(), "non-core builds only");
    }
    if (!VM.getVM().isDebugging()) {
      if (Assert.ASSERTS_ENABLED) {
        OopMapSet maps = cb.getOopMaps();
        Assert.that((maps != null) && (maps.getSize() > 0), "found null or empty OopMapSet for CodeBlob");
      }
    } else {
      OopMapSet maps = cb.getOopMaps();
      if ((maps == null) || (maps.getSize() == 0)) {
        return;
      }
    }
    regMap.setIncludeArgumentOops(cb.callerMustGCArguments(regMap.getThread()));
    int nofCallee = 0;
    Address[] locs = new Address[2 * REG_COUNT + 1];
    VMReg  [] regs = new VMReg  [2 * REG_COUNT + 1];
    OopMap map  = cb.getOopMapForReturnAddress(fr.getPC(), debugging);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "no ptr map found");
    }
    OopMapValue omv = null;
    for(OopMapStream oms = new OopMapStream(map, OopMapValue.OopTypes.CALLEE_SAVED_VALUE); !oms.isDone(); oms.next()) {
      omv = oms.getCurrent();
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(nofCallee < 2 * REG_COUNT, "overflow");
      }
      regs[nofCallee] = omv.getContentReg();
      locs[nofCallee] = fr.oopMapRegToLocation(omv.getReg(), regMap);
      nofCallee++;
    }
    if (Assert.ASSERTS_ENABLED) {
      if (VM.getVM().isServerCompiler()) {
        Assert.that(!cb.isRuntimeStub() ||
                    (nofCallee >= SAVED_ON_ENTRY_REG_COUNT || nofCallee >= C_SAVED_ON_ENTRY_REG_COUNT),
                    "must save all");
      }
    }
    for (int i = 0; i < nofCallee; i++) {
      regMap.setLocation(regs[i], locs[i]);
    }
  }
}
