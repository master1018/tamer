public class SerializationTester {
    private static Object lastOutput = null;
    private SerializationTester() {
    }
    public static Object getDeserilizedObject(Object inputObject)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(inputObject);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object outputObject = ois.readObject();
        lastOutput = outputObject;
        ois.close();
        return outputObject;
    }
    public static boolean assertSame(Object inputObject) throws Exception {
        return inputObject == getDeserilizedObject(inputObject);
    }
    public static boolean assertEquals(Object inputObject) throws Exception {
        return inputObject.equals(getDeserilizedObject(inputObject));
    }
    public static boolean assertCompabilitySame(Object obj, String fileName)
            throws Exception {
        return obj == readObject(obj, fileName);
    }
    public static boolean assertCompabilityEquals(Object obj, String fileName)
            throws Exception {
        return obj.equals(readObject(obj, fileName));
    }
    public static Object readObject(Object obj, String fileName)
            throws Exception {
        InputStream input = null;
        ObjectInputStream oinput = null;
        URL url = SerializationTester.class.getResource(
                fileName);
        if (null == url) {
            writeObject(obj, new File(fileName).getName());
            throw new Error(
                    "Serialization file does not exist, created in the current dir.");
        }
        input = url.openStream();
        try {
            oinput = new ObjectInputStream(input);
            Object newObj = oinput.readObject();
            return newObj;
        } finally {
            try {
                if (null != oinput) {
                    oinput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
            }
        }
    }
    public static void writeObject(Object obj, String fileName)
            throws Exception {
        OutputStream output = null;
        ObjectOutputStream ooutput = null;
        try {
            output = new FileOutputStream(fileName);
            ooutput = new ObjectOutputStream(output);
            ooutput.writeObject(obj);
        } finally {
            try {
                if (null != ooutput) {
                    ooutput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != output) {
                    output.close();
                }
            } catch (Exception e) {
            }
        }
    }
    public static Object getLastOutput() {
        return lastOutput;
    }
    public static void main(String[] args) {
    }
}
