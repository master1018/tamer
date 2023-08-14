class BinaryConstantPool implements Constants {
    private byte types[];
    private Object cpool[];
    BinaryConstantPool(DataInputStream in) throws IOException {
        types = new byte[in.readUnsignedShort()];
        cpool = new Object[types.length];
        for (int i = 1 ; i < cpool.length ; i++) {
            int j = i;
            switch(types[i] = in.readByte()) {
              case CONSTANT_UTF8:
                cpool[i] = in.readUTF();
                break;
              case CONSTANT_INTEGER:
                cpool[i] = new Integer(in.readInt());
                break;
              case CONSTANT_FLOAT:
                cpool[i] = new Float(in.readFloat());
                break;
              case CONSTANT_LONG:
                cpool[i++] = new Long(in.readLong());
                break;
              case CONSTANT_DOUBLE:
                cpool[i++] = new Double(in.readDouble());
                break;
              case CONSTANT_CLASS:
              case CONSTANT_STRING:
                cpool[i] = new Integer(in.readUnsignedShort());
                break;
              case CONSTANT_FIELD:
              case CONSTANT_METHOD:
              case CONSTANT_INTERFACEMETHOD:
              case CONSTANT_NAMEANDTYPE:
                cpool[i] = new Integer((in.readUnsignedShort() << 16) | in.readUnsignedShort());
                break;
              case 0:
              default:
                throw new ClassFormatError("invalid constant type: " + (int)types[i]);
            }
        }
    }
    public int getInteger(int n) {
        return (n == 0) ? 0 : ((Number)cpool[n]).intValue();
    }
    public Object getValue(int n) {
        return (n == 0) ? null : cpool[n];
    }
    public String getString(int n) {
        return (n == 0) ? null : (String)cpool[n];
    }
    public Identifier getIdentifier(int n) {
        return (n == 0) ? null : Identifier.lookup(getString(n));
    }
    public ClassDeclaration getDeclarationFromName(Environment env, int n) {
        return (n == 0) ? null : env.getClassDeclaration(Identifier.lookup(getString(n).replace('/','.')));
    }
    public ClassDeclaration getDeclaration(Environment env, int n) {
        return (n == 0) ? null : getDeclarationFromName(env, getInteger(n));
    }
    public Type getType(int n) {
        return Type.tType(getString(n));
    }
    public int getConstantType(int n) {
        return types[n];
    }
    public Object getConstant(int n, Environment env) {
        int constant_type = getConstantType(n);
        switch (constant_type) {
            case CONSTANT_INTEGER:
            case CONSTANT_FLOAT:
            case CONSTANT_LONG:
            case CONSTANT_DOUBLE:
                return getValue(n);
            case CONSTANT_CLASS:
                return getDeclaration(env, n);
            case CONSTANT_STRING:
                return getString(getInteger(n));
            case CONSTANT_FIELD:
            case CONSTANT_METHOD:
            case CONSTANT_INTERFACEMETHOD:
                try {
                    int key = getInteger(n);
                    ClassDefinition clazz =
                        getDeclaration(env, key >> 16).getClassDefinition(env);
                    int name_and_type = getInteger(key & 0xFFFF);
                    Identifier id = getIdentifier(name_and_type >> 16);
                    Type type = getType(name_and_type & 0xFFFF);
                    for (MemberDefinition field = clazz.getFirstMatch(id);
                         field != null;
                         field = field.getNextMatch()) {
                        Type field_type = field.getType();
                        if ((constant_type == CONSTANT_FIELD)
                            ? (field_type == type)
                            : (field_type.equalArguments(type)))
                            return field;
                    }
                } catch (ClassNotFound e) {
                }
                return null;
            default:
                throw new ClassFormatError("invalid constant type: " +
                                              constant_type);
        }
    }
    public Vector getDependencies(Environment env) {
        Vector v = new Vector();
        for (int i = 1 ; i < cpool.length ; i++) {
            switch(types[i]) {
              case CONSTANT_CLASS:
                v.addElement(getDeclarationFromName(env, getInteger(i)));
                break;
            }
        }
        return v;
    }
    Hashtable indexHashObject, indexHashAscii;
    Vector MoreStuff;
    public int indexObject(Object obj, Environment env) {
        if (indexHashObject == null)
            createIndexHash(env);
        Integer result = (Integer)indexHashObject.get(obj);
        if (result == null)
            throw new IndexOutOfBoundsException("Cannot find object " + obj + " of type " +
                                obj.getClass() + " in constant pool");
        return result.intValue();
    }
    public int indexString(String string, Environment env) {
        if (indexHashObject == null)
            createIndexHash(env);
        Integer result = (Integer)indexHashAscii.get(string);
        if (result == null) {
            if (MoreStuff == null) MoreStuff = new Vector();
            result = new Integer(cpool.length + MoreStuff.size());
            MoreStuff.addElement(string);
            indexHashAscii.put(string, result);
        }
        return result.intValue();
    }
    public void createIndexHash(Environment env) {
        indexHashObject = new Hashtable();
        indexHashAscii = new Hashtable();
        for (int i = 1; i < cpool.length; i++) {
            if (types[i] == CONSTANT_UTF8) {
                indexHashAscii.put(cpool[i], new Integer(i));
            } else {
                try {
                    indexHashObject.put(getConstant(i, env), new Integer(i));
                } catch (ClassFormatError e) { }
            }
        }
    }
    public void write(DataOutputStream out, Environment env) throws IOException {
        int length = cpool.length;
        if (MoreStuff != null)
            length += MoreStuff.size();
        out.writeShort(length);
        for (int i = 1 ; i < cpool.length; i++) {
            int type = types[i];
            Object x = cpool[i];
            out.writeByte(type);
            switch (type) {
                case CONSTANT_UTF8:
                    out.writeUTF((String) x);
                    break;
                case CONSTANT_INTEGER:
                    out.writeInt(((Number)x).intValue());
                    break;
                case CONSTANT_FLOAT:
                    out.writeFloat(((Number)x).floatValue());
                    break;
                case CONSTANT_LONG:
                    out.writeLong(((Number)x).longValue());
                    i++;
                    break;
                case CONSTANT_DOUBLE:
                    out.writeDouble(((Number)x).doubleValue());
                    i++;
                    break;
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                    out.writeShort(((Number)x).intValue());
                    break;
                case CONSTANT_FIELD:
                case CONSTANT_METHOD:
                case CONSTANT_INTERFACEMETHOD:
                case CONSTANT_NAMEANDTYPE: {
                    int value = ((Number)x).intValue();
                    out.writeShort(value >> 16);
                    out.writeShort(value & 0xFFFF);
                    break;
                }
                default:
                     throw new ClassFormatError("invalid constant type: "
                                                   + (int)types[i]);
            }
        }
        for (int i = cpool.length; i < length; i++) {
            String string = (String)(MoreStuff.elementAt(i - cpool.length));
            out.writeByte(CONSTANT_UTF8);
            out.writeUTF(string);
        }
    }
}
