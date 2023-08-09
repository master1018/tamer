class NumberConstantData extends ConstantPoolData {
    Number num;
    NumberConstantData(ConstantPool tab, Number num) {
        this.num = num;
    }
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        if (num instanceof Integer) {
            out.writeByte(CONSTANT_INTEGER);
            out.writeInt(num.intValue());
        } else if (num instanceof Long) {
            out.writeByte(CONSTANT_LONG);
            out.writeLong(num.longValue());
        } else if (num instanceof Float) {
            out.writeByte(CONSTANT_FLOAT);
            out.writeFloat(num.floatValue());
        } else if (num instanceof Double) {
            out.writeByte(CONSTANT_DOUBLE);
            out.writeDouble(num.doubleValue());
        }
    }
    int order() {
        return (width() == 1) ? 0 : 3;
    }
    int width() {
        return ((num instanceof Double) || (num instanceof Long)) ? 2 : 1;
    }
}
