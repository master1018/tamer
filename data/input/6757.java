class ClassConstantData extends ConstantPoolData {
    String name;
    ClassConstantData(ConstantPool tab, ClassDeclaration clazz) {
        String sig = clazz.getType().getTypeSignature();
        name = sig.substring(1, sig.length()-1);
        tab.put(name);
    }
    ClassConstantData(ConstantPool tab, Type t) {
        name = t.getTypeSignature();
        tab.put(name);
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_CLASS);
        out.writeShort(tab.index(name));
    }
    int order() {
        return 1;
    }
    public String toString() {
        return "ClassConstantData[" + name + "]";
    }
}
