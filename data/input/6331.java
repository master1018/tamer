public class HotSpotTypeDataBase extends BasicTypeDataBase {
  private Debugger symbolLookup;
  private String[] jvmLibNames;
  private static final int UNINITIALIZED_SIZE = -1;
  private static final int C_INT8_SIZE  = 1;
  private static final int C_INT32_SIZE = 4;
  private static final int C_INT64_SIZE = 8;
  private static final boolean DEBUG;
  static {
    DEBUG = System.getProperty("sun.jvm.hotspot.HotSpotTypeDataBase.DEBUG")
            != null;
  }
  public HotSpotTypeDataBase(MachineDescription machDesc,
                             VtblAccess vtblAccess,
                             Debugger symbolLookup,
                             String[] jvmLibNames) throws NoSuchSymbolException {
    super(machDesc, vtblAccess);
    this.symbolLookup = symbolLookup;
    this.jvmLibNames = jvmLibNames;
    readVMTypes();
    initializePrimitiveTypes();
    readVMStructs();
    readVMIntConstants();
    readVMLongConstants();
  }
  public Type lookupType(String cTypeName, boolean throwException) {
    Type fieldType = super.lookupType(cTypeName, false);
    if (fieldType == null && cTypeName.startsWith("const ")) {
      fieldType = (BasicType)lookupType(cTypeName.substring(6), false);
    }
    if (fieldType == null && cTypeName.endsWith(" const")) {
        fieldType = (BasicType)lookupType(cTypeName.substring(0, cTypeName.length() - 6), false);
    }
    if (fieldType == null) {
      if (cTypeName.startsWith("GrowableArray<") && cTypeName.endsWith(">*")) {
        String ttype = cTypeName.substring("GrowableArray<".length(),
                                            cTypeName.length() - 2);
        Type templateType = lookupType(ttype, false);
        if (templateType == null && typeNameIsPointerType(ttype)) {
          templateType = recursiveCreateBasicPointerType(ttype);
        }
        if (templateType == null) {
          lookupOrFail(ttype);
        }
        fieldType = recursiveCreateBasicPointerType(cTypeName);
      }
    }
    if (fieldType == null && typeNameIsPointerType(cTypeName)) {
      fieldType = recursiveCreateBasicPointerType(cTypeName);
    }
    if (fieldType == null && throwException) {
      super.lookupType(cTypeName, true);
    }
    return fieldType;
  }
  private void readVMTypes() {
    long typeEntryTypeNameOffset;
    long typeEntrySuperclassNameOffset;
    long typeEntryIsOopTypeOffset;
    long typeEntryIsIntegerTypeOffset;
    long typeEntryIsUnsignedOffset;
    long typeEntrySizeOffset;
    long typeEntryArrayStride;
    Address entryAddr = lookupInProcess("gHotSpotVMTypes");
    entryAddr = entryAddr.getAddressAt(0);
    if (entryAddr == null) {
      throw new RuntimeException("gHotSpotVMTypes was not initialized properly in the remote process; can not continue");
    }
    typeEntryTypeNameOffset       = getLongValueFromProcess("gHotSpotVMTypeEntryTypeNameOffset");
    typeEntrySuperclassNameOffset = getLongValueFromProcess("gHotSpotVMTypeEntrySuperclassNameOffset");
    typeEntryIsOopTypeOffset      = getLongValueFromProcess("gHotSpotVMTypeEntryIsOopTypeOffset");
    typeEntryIsIntegerTypeOffset  = getLongValueFromProcess("gHotSpotVMTypeEntryIsIntegerTypeOffset");
    typeEntryIsUnsignedOffset     = getLongValueFromProcess("gHotSpotVMTypeEntryIsUnsignedOffset");
    typeEntrySizeOffset           = getLongValueFromProcess("gHotSpotVMTypeEntrySizeOffset");
    typeEntryArrayStride          = getLongValueFromProcess("gHotSpotVMTypeEntryArrayStride");
    Address typeNameAddr = null;
    do {
      typeNameAddr = entryAddr.getAddressAt(typeEntryTypeNameOffset);
      if (typeNameAddr != null) {
        String typeName = CStringUtilities.getString(typeNameAddr);
        String superclassName = null;
        Address superclassNameAddr = entryAddr.getAddressAt(typeEntrySuperclassNameOffset);
        if (superclassNameAddr != null) {
          superclassName = CStringUtilities.getString(superclassNameAddr);
        }
        boolean isOopType     = (entryAddr.getCIntegerAt(typeEntryIsOopTypeOffset, C_INT32_SIZE, false) != 0);
        boolean isIntegerType = (entryAddr.getCIntegerAt(typeEntryIsIntegerTypeOffset, C_INT32_SIZE, false) != 0);
        boolean isUnsigned    = (entryAddr.getCIntegerAt(typeEntryIsUnsignedOffset, C_INT32_SIZE, false) != 0);
        long size             = entryAddr.getCIntegerAt(typeEntrySizeOffset, C_INT64_SIZE, true);
        createType(typeName, superclassName, isOopType, isIntegerType, isUnsigned, size);
      }
      entryAddr = entryAddr.addOffsetTo(typeEntryArrayStride);
    } while (typeNameAddr != null);
  }
  private void initializePrimitiveTypes() {
    setJBooleanType(lookupPrimitiveType("jboolean"));
    setJByteType   (lookupPrimitiveType("jbyte"));
    setJCharType   (lookupPrimitiveType("jchar"));
    setJDoubleType (lookupPrimitiveType("jdouble"));
    setJFloatType  (lookupPrimitiveType("jfloat"));
    setJIntType    (lookupPrimitiveType("jint"));
    setJLongType   (lookupPrimitiveType("jlong"));
    setJShortType  (lookupPrimitiveType("jshort"));
    ((BasicType) getJBooleanType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJByteType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJCharType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJDoubleType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJFloatType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJIntType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJLongType()).setIsJavaPrimitiveType(true);
    ((BasicType) getJShortType()).setIsJavaPrimitiveType(true);
  }
  private Type lookupPrimitiveType(String typeName) {
    Type type = lookupType(typeName, false);
    if (type == null) {
      throw new RuntimeException("Error initializing the HotSpotDataBase: could not find the primitive type \"" +
                                 typeName + "\" in the remote VM's VMStructs table. This type is required in " +
                                 "order to determine the size of Java primitive types. Can not continue.");
    }
    return type;
  }
  private void readVMStructs() {
    long structEntryTypeNameOffset;
    long structEntryFieldNameOffset;
    long structEntryTypeStringOffset;
    long structEntryIsStaticOffset;
    long structEntryOffsetOffset;
    long structEntryAddressOffset;
    long structEntryArrayStride;
    structEntryTypeNameOffset     = getLongValueFromProcess("gHotSpotVMStructEntryTypeNameOffset");
    structEntryFieldNameOffset    = getLongValueFromProcess("gHotSpotVMStructEntryFieldNameOffset");
    structEntryTypeStringOffset   = getLongValueFromProcess("gHotSpotVMStructEntryTypeStringOffset");
    structEntryIsStaticOffset     = getLongValueFromProcess("gHotSpotVMStructEntryIsStaticOffset");
    structEntryOffsetOffset       = getLongValueFromProcess("gHotSpotVMStructEntryOffsetOffset");
    structEntryAddressOffset      = getLongValueFromProcess("gHotSpotVMStructEntryAddressOffset");
    structEntryArrayStride        = getLongValueFromProcess("gHotSpotVMStructEntryArrayStride");
    Address entryAddr = lookupInProcess("gHotSpotVMStructs");
    entryAddr = entryAddr.getAddressAt(0);
    if (entryAddr == null) {
      throw new RuntimeException("gHotSpotVMStructs was not initialized properly in the remote process; can not continue");
    }
    Address fieldNameAddr = null;
    String typeName = null;
    String fieldName = null;
    String typeString = null;
    boolean isStatic = false;
    long offset = 0;
    Address staticFieldAddr = null;
    long size = 0;
    long index = 0;
    String opaqueName = "<opaque>";
    lookupOrCreateClass(opaqueName, false, false, false);
    do {
      fieldNameAddr = entryAddr.getAddressAt(structEntryFieldNameOffset);
      if (fieldNameAddr != null) {
        fieldName = CStringUtilities.getString(fieldNameAddr);
        Address addr = entryAddr.getAddressAt(structEntryTypeNameOffset);
        if (addr == null) {
          throw new RuntimeException("gHotSpotVMStructs unexpectedly had a NULL type name at index " + index);
        }
        typeName = CStringUtilities.getString(addr);
        addr = entryAddr.getAddressAt(structEntryTypeStringOffset);
        if (addr == null) {
          typeString = opaqueName;
        } else {
          typeString = CStringUtilities.getString(addr);
        }
        isStatic = !(entryAddr.getCIntegerAt(structEntryIsStaticOffset, C_INT32_SIZE, false) == 0);
        if (isStatic) {
          staticFieldAddr = entryAddr.getAddressAt(structEntryAddressOffset);
          offset = 0;
        } else {
          offset = entryAddr.getCIntegerAt(structEntryOffsetOffset, C_INT64_SIZE, true);
          staticFieldAddr = null;
        }
        BasicType containingType = lookupOrFail(typeName);
        BasicType fieldType = (BasicType)lookupType(typeString);
        createField(containingType, fieldName, fieldType,
                    isStatic, offset, staticFieldAddr);
      }
      ++index;
      entryAddr = entryAddr.addOffsetTo(structEntryArrayStride);
    } while (fieldNameAddr != null);
  }
  private void readVMIntConstants() {
    long intConstantEntryNameOffset;
    long intConstantEntryValueOffset;
    long intConstantEntryArrayStride;
    intConstantEntryNameOffset  = getLongValueFromProcess("gHotSpotVMIntConstantEntryNameOffset");
    intConstantEntryValueOffset = getLongValueFromProcess("gHotSpotVMIntConstantEntryValueOffset");
    intConstantEntryArrayStride = getLongValueFromProcess("gHotSpotVMIntConstantEntryArrayStride");
    Address entryAddr = lookupInProcess("gHotSpotVMIntConstants");
    entryAddr = entryAddr.getAddressAt(0);
    if (entryAddr == null) {
      throw new RuntimeException("gHotSpotVMIntConstants was not initialized properly in the remote process; can not continue");
    }
    Address nameAddr = null;
    do {
      nameAddr = entryAddr.getAddressAt(intConstantEntryNameOffset);
      if (nameAddr != null) {
        String name = CStringUtilities.getString(nameAddr);
        int value = (int) entryAddr.getCIntegerAt(intConstantEntryValueOffset, C_INT32_SIZE, false);
        Integer oldValue = lookupIntConstant(name, false);
        if (oldValue == null) {
          addIntConstant(name, value);
        } else {
          if (oldValue.intValue() != value) {
            throw new RuntimeException("Error: the integer constant \"" + name +
                                       "\" had its value redefined (old was " + oldValue +
                                       ", new is " + value + ". Aborting.");
          } else {
            System.err.println("Warning: the int constant \"" + name + "\" (declared in the remote VM in VMStructs::localHotSpotVMIntConstants) " +
                               "had its value declared as " + value + " twice. Continuing.");
          }
        }
      }
      entryAddr = entryAddr.addOffsetTo(intConstantEntryArrayStride);
    } while (nameAddr != null);
  }
  private void readVMLongConstants() {
    long longConstantEntryNameOffset;
    long longConstantEntryValueOffset;
    long longConstantEntryArrayStride;
    longConstantEntryNameOffset  = getLongValueFromProcess("gHotSpotVMLongConstantEntryNameOffset");
    longConstantEntryValueOffset = getLongValueFromProcess("gHotSpotVMLongConstantEntryValueOffset");
    longConstantEntryArrayStride = getLongValueFromProcess("gHotSpotVMLongConstantEntryArrayStride");
    Address entryAddr = lookupInProcess("gHotSpotVMLongConstants");
    entryAddr = entryAddr.getAddressAt(0);
    if (entryAddr == null) {
      throw new RuntimeException("gHotSpotVMLongConstants was not initialized properly in the remote process; can not continue");
    }
    Address nameAddr = null;
    do {
      nameAddr = entryAddr.getAddressAt(longConstantEntryNameOffset);
      if (nameAddr != null) {
        String name = CStringUtilities.getString(nameAddr);
        int value = (int) entryAddr.getCIntegerAt(longConstantEntryValueOffset, C_INT64_SIZE, true);
        Long oldValue = lookupLongConstant(name, false);
        if (oldValue == null) {
          addLongConstant(name, value);
        } else {
          if (oldValue.longValue() != value) {
            throw new RuntimeException("Error: the long constant \"" + name +
                                       "\" had its value redefined (old was " + oldValue +
                                       ", new is " + value + ". Aborting.");
          } else {
            System.err.println("Warning: the long constant \"" + name + "\" (declared in the remote VM in VMStructs::localHotSpotVMLongConstants) " +
                               "had its value declared as " + value + " twice. Continuing.");
          }
        }
      }
      entryAddr = entryAddr.addOffsetTo(longConstantEntryArrayStride);
    } while (nameAddr != null);
  }
  private BasicType lookupOrFail(String typeName) {
    BasicType type = (BasicType) lookupType(typeName, false);
    if (type == null) {
      throw new RuntimeException("Type \"" + typeName + "\", referenced in VMStructs::localHotSpotVMStructs in the remote VM, " +
                                 "was not present in the remote VMStructs::localHotSpotVMTypes table (should have been caught " +
                                 "in the debug build of that VM). Can not continue.");
    }
    return type;
  }
  private long getLongValueFromProcess(String symbol) {
    return lookupInProcess(symbol).getCIntegerAt(0, C_INT64_SIZE, true);
  }
  private Address lookupInProcess(String symbol) throws NoSuchSymbolException {
    for (int i = 0; i < jvmLibNames.length; i++) {
      Address addr = symbolLookup.lookup(jvmLibNames[i], symbol);
      if (addr != null) {
        return addr;
      }
    }
    String errStr = "(";
    for (int i = 0; i < jvmLibNames.length; i++) {
      errStr += jvmLibNames[i];
      if (i < jvmLibNames.length - 1) {
        errStr += ", ";
      }
    }
    errStr += ")";
    throw new NoSuchSymbolException(symbol,
                                    "Could not find symbol \"" + symbol +
                                    "\" in any of the known library names " +
                                    errStr);
  }
  private BasicType lookupOrCreateClass(String typeName, boolean isOopType,
                                        boolean isIntegerType, boolean isUnsigned) {
    BasicType type = (BasicType) lookupType(typeName, false);
    if (type == null) {
      type = createBasicType(typeName, isOopType, isIntegerType, isUnsigned);
    }
    return type;
  }
  private BasicType createBasicType(String typeName, boolean isOopType,
                                    boolean isIntegerType, boolean isUnsigned) {
    BasicType type = null;
    if (isIntegerType) {
      type = new BasicCIntegerType(this, typeName, isUnsigned);
    } else {
      if (typeNameIsPointerType(typeName)) {
        type = recursiveCreateBasicPointerType(typeName);
      } else {
        type = new BasicType(this, typeName);
      }
      if (isOopType) {
        if (typeName.equals("markOop")) {
          type = new BasicCIntegerType(this, typeName, true);
        } else {
          type.setIsOopType(true);
        }
      }
    }
    type.setSize(UNINITIALIZED_SIZE);
    addType(type);
    return type;
  }
  private BasicPointerType recursiveCreateBasicPointerType(String typeName) {
    BasicPointerType result = (BasicPointerType)super.lookupType(typeName, false);
    if (result != null) {
      return result;
    }
    String targetTypeName = typeName.substring(0, typeName.lastIndexOf('*')).trim();
    Type targetType = null;
    if (typeNameIsPointerType(targetTypeName)) {
      targetType = lookupType(targetTypeName, false);
      if (targetType == null) {
        targetType = recursiveCreateBasicPointerType(targetTypeName);
      }
    } else {
      targetType = lookupType(targetTypeName, false);
      if (targetType == null) {
        if (targetTypeName.equals("char") ||
            targetTypeName.equals("const char")) {
          BasicType basicTargetType = createBasicType(targetTypeName, false, true, false);
          basicTargetType.setSize(1);
          targetType = basicTargetType;
        } else if (targetTypeName.equals("u_char")) {
          BasicType basicTargetType = createBasicType(targetTypeName, false, true, true);
          basicTargetType.setSize(1);
          targetType = basicTargetType;
        } else if (targetTypeName.startsWith("GrowableArray<")) {
          BasicType basicTargetType = createBasicType(targetTypeName, false, false, false);
          BasicType generic = lookupOrFail("GenericGrowableArray");
          basicTargetType.setSize(generic.getSize());
          Iterator fields = generic.getFields();
          while (fields.hasNext()) {
              Field f = (Field)fields.next();
              basicTargetType.addField(internalCreateField(basicTargetType, f.getName(),
                                                           f.getType(), f.isStatic(),
                                                           f.getOffset(), null));
          }
          targetType = basicTargetType;
        } else {
          if (DEBUG) {
            System.err.println("WARNING: missing target type \"" + targetTypeName + "\" for pointer type \"" + typeName + "\"");
          }
          targetType = createBasicType(targetTypeName, false, false, false);
        }
      }
    }
    result = new BasicPointerType(this, typeName, targetType);
    result.setSize(UNINITIALIZED_SIZE);
    addType(result);
    return result;
  }
  private boolean typeNameIsPointerType(String typeName) {
    int i = typeName.length() - 1;
    while (i >= 0 && Character.isWhitespace(typeName.charAt(i))) {
      --i;
    }
    if (i >= 0 && typeName.charAt(i) == '*') {
      return true;
    }
    return false;
  }
    public void createType(String typeName, String superclassName,
                           boolean isOopType, boolean isIntegerType,
                           boolean isUnsigned, long size) {
        BasicType superclass = null;
        if (superclassName != null) {
            superclass = lookupOrCreateClass(superclassName, false, false, false);
        }
        BasicType curType = lookupOrCreateClass(typeName, isOopType, isIntegerType, isUnsigned);
        if (superclass != null) {
            if (curType.getSuperclass() == null) {
                curType.setSuperclass(superclass);
            }
            if (curType.getSuperclass() != superclass) {
                throw new RuntimeException("Error: the type \"" + typeName + "\" (declared in the remote VM in VMStructs::localHotSpotVMTypes) " +
                                           "had its superclass redefined (old was " + curType.getSuperclass().getName() + ", new is " +
                                           superclass.getName() + ").");
            }
        }
        if (curType.getSize() == UNINITIALIZED_SIZE) {
            curType.setSize(size);
        } else {
            if (curType.getSize() != size) {
                throw new RuntimeException("Error: the type \"" + typeName + "\" (declared in the remote VM in VMStructs::localHotSpotVMTypes) " +
                                           "had its size redefined (old was " + curType.getSize() + ", new is " + size + ").");
            }
            System.err.println("Warning: the type \"" + typeName + "\" (declared in the remote VM in VMStructs::localHotSpotVMTypes) " +
                               "had its size declared as " + size + " twice. Continuing.");
        }
    }
    public void createField(BasicType containingType,
                            String name, Type type, boolean isStatic,
                            long offset, Address staticFieldAddress) {
        containingType.addField(internalCreateField(containingType, name, type, isStatic, offset, staticFieldAddress));
    }
    Field internalCreateField(BasicType containingType,
                              String name, Type type, boolean isStatic,
                              long offset, Address staticFieldAddress) {
    if (type.isOopType()) {
      return new BasicOopField(this, containingType, name, type,
                               isStatic, offset, staticFieldAddress);
    }
    if (type instanceof CIntegerType) {
      return new BasicCIntegerField(this, containingType, name, type,
                                    isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJBooleanType())) {
      return new BasicJBooleanField(this, containingType, name, type,
                                    isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJByteType())) {
      return new BasicJByteField(this, containingType, name, type,
                                 isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJCharType())) {
      return new BasicJCharField(this, containingType, name, type,
                                 isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJDoubleType())) {
      return new BasicJDoubleField(this, containingType, name, type,
                                   isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJFloatType())) {
      return new BasicJFloatField(this, containingType, name, type,
                                  isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJIntType())) {
      return new BasicJIntField(this, containingType, name, type,
                                isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJLongType())) {
      return new BasicJLongField(this, containingType, name, type,
                                 isStatic, offset, staticFieldAddress);
    }
    if (type.equals(getJShortType())) {
      return new BasicJShortField(this, containingType, name, type,
                                  isStatic, offset, staticFieldAddress);
    }
    return new BasicField(this, containingType, name, type,
                          isStatic, offset, staticFieldAddress);
  }
  private void dumpMemory(Address addr, int len) {
    int i = 0;
    while (i < len) {
      System.err.print(addr.addOffsetTo(i) + ":");
      for (int j = 0; j < 8 && i < len; i++, j++) {
        String s = Long.toHexString(addr.getCIntegerAt(i, 1, true));
        System.err.print(" 0x");
        for (int k = 0; k < 2 - s.length(); k++) {
          System.err.print("0");
        }
        System.err.print(s);
      }
      System.err.println();
    }
  }
}
