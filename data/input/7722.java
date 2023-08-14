public class Bug6653944 {
    private static int errorCount = 0;
    public static void main(String[] args) throws Exception {
        Calendar buddhist = Calendar.getInstance(new Locale("th", "TH"));
        int expectedYear = buddhist.get(Calendar.YEAR);
        Calendar deserialized = (Calendar) deserialize(serialize(buddhist));
        compare(deserialized, buddhist);
        int deserializedYear = deserialized.get(Calendar.YEAR);
        compare(deserializedYear, expectedYear);
        buddhist.add(Calendar.YEAR, 12);
        expectedYear = buddhist.get(Calendar.YEAR);
        deserialized.add(Calendar.YEAR, 12);
        deserializedYear = deserialized.get(Calendar.YEAR);
        compare(deserialized, buddhist);
        compare(deserializedYear, expectedYear);
        if (errorCount > 0) {
            throw new RuntimeException("Bug6653944: failed");
        }
    }
    private static void compare(int got, int expected) {
        if (got != expected) {
            System.err.println("got " + got + ", expected " + expected);
            errorCount++;
        }
    }
    private static void compare(Calendar got, Calendar expected) {
        if (!got.equals(expected)) {
            System.err.println("got " + got + ", expected " + expected);
            errorCount++;
        }
    }
    private static byte[] serialize(Serializable obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }
    private static Object deserialize(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}
