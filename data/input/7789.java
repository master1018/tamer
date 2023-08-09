public class SingleClassLoader extends ClassLoader {
    SingleClassLoader(String singleClassName, Class superclass,
                      ClassLoader parent) {
        super(parent);
        Constructor superConstr;
        try {
            superConstr = superclass.getDeclaredConstructor(new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Superclass must have no-arg " +
                                               "constructor");
        }
        int superConstrMods = superConstr.getModifiers();
        if ((superConstrMods & (Modifier.PUBLIC|Modifier.PROTECTED)) == 0) {
            final String msg =
                "Superclass no-arg constructor must be public or protected";
            throw new IllegalArgumentException(msg);
        }
        this.singleClassName = singleClassName;
        final Class c;
        try {
            c = makeClass(superclass);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        this.singleClass = c;
    }
    private Class makeClass(Class superclass) throws IOException {
        final String superName = superclass.getName();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        dout = new DataOutputStream(bout);
        String thisNameInternal = singleClassName.replace('.', '/');
        String superNameInternal = superName.replace('.', '/');
        dout.writeInt(0xcafebabe); 
        dout.writeInt(0x0003002d); 
        dout.writeShort(10);       
        cpoolIndex = 1;
        int thisNameConst = writeUTFConst(thisNameInternal);
        int thisClassConst = writeClassConst(thisNameConst);
        int superNameConst = writeUTFConst(superNameInternal);
        int superClassConst = writeClassConst(superNameConst);
        int initNameConst = writeUTFConst("<init>");
        int voidNoArgSigConst = writeUTFConst("()V");
        int codeNameConst = writeUTFConst("Code");
        int superConstructorNameAndTypeConst =
            writeNameAndTypeConst(initNameConst, voidNoArgSigConst);
        int superConstructorMethodRefConst =
            writeMethodRefConst(superClassConst,
                                superConstructorNameAndTypeConst);
        dout.writeShort(Modifier.PUBLIC | 0x20 );
        dout.writeShort(thisClassConst);
        dout.writeShort(superClassConst);
        dout.writeInt(0);   
        dout.writeShort(1); 
        dout.writeShort(Modifier.PUBLIC);
        dout.writeShort(initNameConst);
        dout.writeShort(voidNoArgSigConst);
        dout.writeShort(1); 
        dout.writeShort(codeNameConst);
        dout.writeInt(17);   
        dout.writeShort(1);  
        dout.writeShort(1);  
        dout.writeInt(5);    
        dout.writeByte(42);  
        dout.writeByte(183); 
        dout.writeShort(superConstructorMethodRefConst);
        dout.writeByte(177); 
        dout.writeShort(0);  
        dout.writeShort(0);  
        dout.writeShort(0);  
        dout.close();
        byte[] classBytes = bout.toByteArray();
        dout = null;
        return
            defineClass(singleClassName, classBytes, 0, classBytes.length);
    }
    protected Class findClass(String name) throws ClassNotFoundException {
        if (name.equals(singleClassName))
            return singleClass;
        else
            throw new ClassNotFoundException(name);
    }
    private int writeUTFConst(String s) throws IOException {
        dout.writeByte(1);
        dout.writeUTF(s);
        return cpoolIndex++;
    }
    private int writeClassConst(int nameIndex) throws IOException {
        dout.writeByte(7);
        dout.writeShort((short) nameIndex);
        return cpoolIndex++;
    }
    private int writeNameAndTypeConst(int nameIndex, int typeIndex)
            throws IOException {
        dout.writeByte(12);
        dout.writeShort((short) nameIndex);
        dout.writeShort((short) typeIndex);
        return cpoolIndex++;
    }
    private int writeMethodRefConst(int classIndex, int nameAndTypeIndex)
            throws IOException {
        dout.writeByte(10);
        dout.writeShort((short) classIndex);
        dout.writeShort((short) nameAndTypeIndex);
        return cpoolIndex++;
    }
    private final String singleClassName;
    private final Class singleClass;
    private DataOutputStream dout;
    private int cpoolIndex;
}
