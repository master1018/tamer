class NameAndTypeConstantData extends ConstantPoolData {
    String name;
    String type;
    NameAndTypeConstantData(ConstantPool tab, NameAndTypeData nt) {
        name = nt.field.getName().toString();
        type = nt.field.getType().getTypeSignature();
        tab.put(name);
        tab.put(type);
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_NAMEANDTYPE);
        out.writeShort(tab.index(name));
        out.writeShort(tab.index(type));
    }
    int order() {
        return 3;
    }
}
