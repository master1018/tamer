abstract class  ConstantPoolData implements RuntimeConstants {
    int index;
    abstract void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException;
    int order() {
        return 0;
    }
    int width() {
        return 1;
    }
}
