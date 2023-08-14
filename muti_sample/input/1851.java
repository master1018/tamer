public class Field {
  Field(FieldIdentifier id, long offset, boolean isVMField) {
    this.offset    = offset;
    this.id        = id;
    this.isVMField = isVMField;
  }
  Field(InstanceKlass holder, int fieldArrayIndex) {
    this.holder = holder;
    this.fieldArrayIndex = fieldArrayIndex;
    ConstantPool cp      = holder.getConstants();
    TypeArray fields     = holder.getFields();
    short access         = fields.getShortAt(fieldArrayIndex + InstanceKlass.ACCESS_FLAGS_OFFSET);
    short nameIndex      = fields.getShortAt(fieldArrayIndex + InstanceKlass.NAME_INDEX_OFFSET);
    short signatureIndex = fields.getShortAt(fieldArrayIndex + InstanceKlass.SIGNATURE_INDEX_OFFSET);
    offset               = VM.getVM().buildIntFromShorts(fields.getShortAt(fieldArrayIndex + InstanceKlass.LOW_OFFSET),
                                                         fields.getShortAt(fieldArrayIndex + InstanceKlass.HIGH_OFFSET));
    short genericSignatureIndex = fields.getShortAt(fieldArrayIndex + InstanceKlass.GENERIC_SIGNATURE_INDEX_OFFSET);
    Symbol name = cp.getSymbolAt(nameIndex);
    id          = new NamedFieldIdentifier(name.asString());
    signature   = cp.getSymbolAt(signatureIndex);
    if (genericSignatureIndex != 0)  {
       genericSignature = cp.getSymbolAt(genericSignatureIndex);
    } else {
       genericSignature = null;
    }
    fieldType   = new FieldType(signature);
    accessFlags = new AccessFlags(access);
  }
  private long            offset;
  private FieldIdentifier id;
  private boolean         isVMField;
  private InstanceKlass   holder;
  private FieldType       fieldType;
  private Symbol          signature;
  private Symbol          genericSignature;
  private AccessFlags     accessFlags;
  private int             fieldArrayIndex;
  public long getOffset() { return offset; }
  public FieldIdentifier getID() { return id; }
  public boolean isVMField() { return isVMField; }
  public boolean isNamedField() { return (id instanceof NamedFieldIdentifier); }
  public void printOn(PrintStream tty) {
    getID().printOn(tty);
    tty.print(" {" + getOffset() + "} :");
  }
  public InstanceKlass getFieldHolder() {
    return holder;
  }
  public int getFieldArrayIndex() {
    return fieldArrayIndex;
  }
  public long getAccessFlags() { return accessFlags.getValue(); }
  public AccessFlags getAccessFlagsObj() { return accessFlags; }
  public FieldType getFieldType() { return fieldType; }
  public Symbol getSignature() { return signature; }
  public Symbol getGenericSignature() { return genericSignature; }
  public boolean isPublic()                  { return accessFlags.isPublic(); }
  public boolean isPrivate()                 { return accessFlags.isPrivate(); }
  public boolean isProtected()               { return accessFlags.isProtected(); }
  public boolean isPackagePrivate()          { return !isPublic() && !isPrivate() && !isProtected(); }
  public boolean isStatic()                  { return accessFlags.isStatic(); }
  public boolean isFinal()                   { return accessFlags.isFinal(); }
  public boolean isVolatile()                { return accessFlags.isVolatile(); }
  public boolean isTransient()               { return accessFlags.isTransient(); }
  public boolean isSynthetic()               { return accessFlags.isSynthetic(); }
  public boolean isEnumConstant()            { return accessFlags.isEnum();      }
  public boolean equals(Object obj) {
     if (obj == null) {
        return false;
     }
     if (! (obj instanceof Field)) {
        return false;
     }
     Field other = (Field) obj;
     return this.getFieldHolder().equals(other.getFieldHolder()) &&
            this.getID().equals(other.getID());
  }
  public int hashCode() {
     return getFieldHolder().hashCode() ^ getID().hashCode();
  }
}
