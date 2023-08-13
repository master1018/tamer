public class T6622260 {
    public static void main(String[] args) throws Exception {
        new T6622260().run();
    }
    public void run() throws IOException {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        modifyClassFile(classFile);
        String output = javap(classFile);
        verify(output);
    }
    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("@Deprecated class Test { int f; void m() { } }");
        out.close();
        return f;
    }
    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { f.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
        String path = f.getPath();
        return new File(path.substring(0, path.length() - 5) + ".class");
    }
    void modifyClassFile(File f) throws IOException {
        String newAttributeName = "NonstandardAttribute";
        byte[] newAttributeData = { 0, 1, 2, 127, (byte)128, (byte)129, (byte)254, (byte)255 };
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        byte[] data = new byte[(int) f.length()];
        in.readFully(data);
        in.close();
        in = new DataInputStream(new ByteArrayInputStream(data));
        in.skipBytes(4); 
        in.skipBytes(2); 
        in.skipBytes(2); 
        int constantPoolPos = data.length - in.available();
        int constant_pool_count = skipConstantPool(in);
        int flagsPos = data.length - in.available();
        in.skipBytes(2); 
        in.skipBytes(2); 
        in.skipBytes(2); 
        int interfaces_count = in.readUnsignedShort();
        in.skipBytes(interfaces_count * 2);
        int field_count = in.readUnsignedShort();
        for (int i = 0; i < field_count; i++) {
            in.skipBytes(6); 
            skipAttributes(in);
        }
        int method_count = in.readUnsignedShort();
        for (int i = 0; i < method_count; i++) {
            in.skipBytes(6); 
            skipAttributes(in);
        }
        int classAttributesPos = data.length - in.available();
        int attributes_count = in.readUnsignedShort();
        f.renameTo(new File(f.getPath() + ".BAK"));
        DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
        out.write(data, 0, constantPoolPos);
        out.writeShort(constant_pool_count + 1);
        out.write(data, constantPoolPos + 2, flagsPos - constantPoolPos - 2);
        out.write(1); 
        out.writeUTF(newAttributeName);
        out.write(data, flagsPos, classAttributesPos - flagsPos);
        out.writeShort(attributes_count + 1);
        out.write(data, classAttributesPos + 2, data.length - classAttributesPos - 2);
        out.writeShort(constant_pool_count); 
        out.writeInt(newAttributeData.length);
        out.write(newAttributeData);
        out.close();
    }
    int skipConstantPool(DataInputStream in) throws IOException {
        int constant_pool_count = in.readUnsignedShort();
        for (int i = 1; i < constant_pool_count; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
            case  1: 
                int length = in.readUnsignedShort();
                in.skipBytes(length); 
                break;
            case  3: 
            case  4: 
                in.skipBytes(4); 
                break;
            case  5: 
            case  6: 
                in.skipBytes(8); 
                break;
            case  7: 
                in.skipBytes(2); 
                break;
            case  8: 
                in.skipBytes(2); 
                break;
            case  9: 
            case 10: 
            case 11: 
                in.skipBytes(4); 
                break;
            case 12: 
                in.skipBytes(4); 
                break;
            default:
                throw new Error("constant pool tag: " + tag);
            }
        }
        return constant_pool_count;
    }
    int skipAttributes(DataInputStream in) throws IOException {
        int attributes_count = in.readUnsignedShort();
        for (int i = 0; i < attributes_count; i++) {
            in.skipBytes(2); 
            int length = in.readInt();
            in.skipBytes(length); 
        }
        return attributes_count;
    }
    String javap(File f) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(new String[] { "-v", f.getPath() }, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        return sw.toString();
    }
    void verify(String output) {
        System.out.println(output);
        output = output.substring(output.indexOf("Test.java"));
        if (output.indexOf("-") >= 0)
            throw new Error("- found in output");
        if (output.indexOf("FFFFFF") >= 0)
            throw new Error("FFFFFF found in output");
    }
}
