class StringConstantData extends ConstantPoolData {
    String str;
    StringConstantData(ConstantPool tab, String str) {
        this.str = str;
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_UTF8);
        out.writeUTF(str);
    }
    int order() {
        return 4;
    }
    public String toString() {
        return "StringConstantData[" + str + "]=" + str.hashCode();
    }
}
