public class SerializationTests {
    static void checkSerialForm(BigDecimal bd) throws Exception  {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(bd);
        oos.flush();
        oos.close();
        ObjectInputStream ois = new
            ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BigDecimal tmp = (BigDecimal)ois.readObject();
        if (!bd.equals(tmp) ||
            bd.hashCode() != tmp.hashCode()) {
            System.err.print("  original : " + bd);
            System.err.println(" (hash: 0x" + Integer.toHexString(bd.hashCode()) + ")");
            System.err.print("serialized : " + tmp);
            System.err.println(" (hash: 0x" + Integer.toHexString(tmp.hashCode()) + ")");
            throw new RuntimeException("Bad serial roundtrip");
        }
    }
    public static void main(String[] args) throws Exception {
        BigDecimal values[] = {
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.TEN,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(10),
            new BigDecimal(Integer.MAX_VALUE),
            new BigDecimal(Long.MAX_VALUE-1),
            new BigDecimal(BigInteger.valueOf(1), 1),
            new BigDecimal(BigInteger.valueOf(100), 50),
        };
        for(BigDecimal value : values) {
            checkSerialForm(value);
            checkSerialForm(value.negate());
        }
    }
}
