public class NullInfoArraysTest {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("write"))
            writeSerializedForms();
        else
            testSerializedForms();
    }
    private static void testSerializedForms() throws Exception {
        byte[][] serializedMBeanInfos =
            SerializedMBeanInfo.serializedMBeanInfos;
        for (int i = 0; i < serializedMBeanInfos.length; i++) {
            byte[] serializedMBeanInfo = serializedMBeanInfos[i];
            ByteArrayInputStream bis =
                new ByteArrayInputStream(serializedMBeanInfo);
            ObjectInputStream ois = new ObjectInputStream(bis);
            MBeanInfo mbi = (MBeanInfo) ois.readObject();
            System.out.println("Testing a " +
                               mbi.getClass().getName() + "...");
            if (mbi.getAttributes() == null ||
                mbi.getOperations() == null ||
                mbi.getConstructors() == null ||
                mbi.getNotifications() == null)
                throw new Exception("At least one getter returned null");
            System.out.println("OK");
        }
        System.out.println("Test passed");
    }
    private static void writeSerializedForms() throws Exception {
        OutputStream fos = new FileOutputStream("SerializedMBeanInfo.java");
        PrintWriter w = new PrintWriter(fos);
        w.println("
        w.println();
        w.println("public class SerializedMBeanInfo {");
        w.println("    public static final byte[][] serializedMBeanInfos = {");
        writeSerial(w, new MBeanInfo(null, null, null, null, null, null));
        writeSerial(w, new ModelMBeanInfoSupport(null, null, null, null, null,
                                                 null, null));
        writeSerial(w, new OpenMBeanInfoSupport(null, null, null, null, null,
                                                null));
        w.println("    };");
        w.println("}");
        w.close();
        fos.close();
        System.out.println("Wrote SerializedMBeanInfo.java");
    }
    private static void writeSerial(PrintWriter w, Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        byte[] bytes = bos.toByteArray();
        w.print("        {");
        for (int i = 0; i < bytes.length; i++) {
            w.print(bytes[i]);
            w.print(", ");
        }
        w.println("},");
    }
}
