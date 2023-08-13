class FieldConstantData extends ConstantPoolData {
    MemberDefinition field;
    NameAndTypeData nt;
    FieldConstantData(ConstantPool tab, MemberDefinition field) {
        this.field = field;
        nt = new NameAndTypeData(field);
        tab.put(field.getClassDeclaration());
        tab.put(nt);
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        if (field.isMethod()) {
            if (field.getClassDefinition().isInterface()) {
                out.writeByte(CONSTANT_INTERFACEMETHOD);
            } else {
                out.writeByte(CONSTANT_METHOD);
            }
        } else {
            out.writeByte(CONSTANT_FIELD);
        }
        out.writeShort(tab.index(field.getClassDeclaration()));
        out.writeShort(tab.index(nt));
    }
    int order() {
        return 2;
    }
}
