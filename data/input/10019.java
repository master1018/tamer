public class ReadUTF {
    private static Random generator = new Random();
    private static final int TEST_ITERATIONS = 1000;
    private static final int A_NUMBER_NEAR_65535 = 60000;
    private static final int MAX_CORRUPTIONS_PER_CYCLE = 3;
    public static final void main(String[] args) throws Exception {
        for (int i=0; i<TEST_ITERATIONS; i++) {
            try {
                writeAndReadAString();
            } catch (UTFDataFormatException utfdfe) {
                if (utfdfe.getMessage() == null)
                    throw new RuntimeException("vague exception thrown");
            } catch (EOFException eofe) {
            }
        }
    }
    private static void writeAndReadAString() throws Exception {
        int length = generator.nextInt(A_NUMBER_NEAR_65535) + 1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer testBuffer = new StringBuffer();
        for (int i=0; i<length; i++)
            testBuffer.append((char)generator.nextInt());
        String testString = testBuffer.toString();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(testString);
        byte[] testBytes = baos.toByteArray();
        int dataLength = testBytes.length;
        int corruptions = generator.nextInt(MAX_CORRUPTIONS_PER_CYCLE);
        for (int i=0; i<corruptions; i++) {
            int index = generator.nextInt(dataLength);
            testBytes[index] = (byte)generator.nextInt();
        }
        testBytes[dataLength-1] = (byte)generator.nextInt();
        testBytes[dataLength-2] = (byte)generator.nextInt();
        ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
        DataInputStream dis = new DataInputStream(bais);
        dis.readUTF();
    }
}
