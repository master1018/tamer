class StringExpressionConstantData extends ConstantPoolData {
    StringExpression str;
    StringExpressionConstantData(ConstantPool tab, StringExpression str) {
        this.str = str;
        tab.put(str.getValue());
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_STRING);
        out.writeShort(tab.index(str.getValue()));
    }
    int order() {
        return 0;
    }
    public String toString() {
        return "StringExpressionConstantData[" + str.getValue() + "]=" + str.getValue().hashCode();
    }
}
