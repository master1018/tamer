public class RobustOopDeterminator {
  private static OopField klassField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("oopDesc");
    if (VM.getVM().isCompressedOopsEnabled()) {
      klassField = type.getNarrowOopField("_metadata._compressed_klass");
    } else {
      klassField = type.getOopField("_metadata._klass");
    }
  }
  public static boolean oopLooksValid(OopHandle oop) {
    if (oop == null) {
      return false;
    }
    if (!VM.getVM().getUniverse().isIn(oop)) {
      return false;
    }
    try {
      for (int i = 0; i < 4; ++i) {
        OopHandle next = klassField.getValue(oop);
        if (next == null) {
          return false;
        }
        if (next.equals(oop)) {
          return true;
        }
        oop = next;
      }
      return false;
    }
    catch (AddressException e) {
      return false;
    }
  }
}
