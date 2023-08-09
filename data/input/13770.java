public class ConstantPoolVisitor {
  public void visitUTF8(int index, byte tag, String utf8) {
  }
  public void visitConstantValue(int index, byte tag, Object value) {
  }
  public void visitConstantString(int index, byte tag,
                                  String name, int nameIndex) {
  }
  public void visitDescriptor(int index, byte tag,
                              String memberName, String signature,
                              int memberNameIndex, int signatureIndex) {
  }
  public void visitMemberRef(int index, byte tag,
                             String className, String memberName, String signature,
                             int classNameIndex, int descriptorIndex) {
  }
    public static final byte
      CONSTANT_None = 0,
      CONSTANT_Utf8 = 1,
      CONSTANT_Integer = 3,
      CONSTANT_Float = 4,
      CONSTANT_Long = 5,
      CONSTANT_Double = 6,
      CONSTANT_Class = 7,
      CONSTANT_String = 8,
      CONSTANT_Fieldref = 9,
      CONSTANT_Methodref = 10,
      CONSTANT_InterfaceMethodref = 11,
      CONSTANT_NameAndType = 12;
    private static String[] TAG_NAMES = {
        "Empty",
        "Utf8",
        null, 
        "Integer",
        "Float",
        "Long",
        "Double",
        "Class",
        "String",
        "Fieldref",
        "Methodref",
        "InterfaceMethodref",
        "NameAndType"
    };
    public static String tagName(byte tag) {
        String name = null;
        if ((tag & 0xFF) < TAG_NAMES.length)
            name = TAG_NAMES[tag];
        if (name == null)
            name = "Unknown#"+(tag&0xFF);
        return name;
    }
}
