public class GetFrameLength {
    static float[] test_float_array;
    static byte[] test_byte_array;
    static AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
    static AudioFloatInputStream getStream1()
    {
        return AudioFloatInputStream.getInputStream(format, test_byte_array, 0, test_byte_array.length);
    }
    static AudioFloatInputStream getStream2()
    {
        AudioInputStream strm = new AudioInputStream(new ByteArrayInputStream(test_byte_array), format, 1024);
        return AudioFloatInputStream.getInputStream(strm);
    }
    static void setUp() throws Exception {
        test_float_array = new float[1024];
        test_byte_array = new byte[1024*format.getFrameSize()];
        for (int i = 0; i < 1024; i++) {
            double ii = i / 1024.0;
            ii = ii * ii;
            test_float_array[i] = (float)Math.sin(10*ii*2*Math.PI);
            test_float_array[i] += (float)Math.sin(1.731 + 2*ii*2*Math.PI);
            test_float_array[i] += (float)Math.sin(0.231 + 6.3*ii*2*Math.PI);
            test_float_array[i] *= 0.3;
        }
        AudioFloatConverter.getConverter(format).toByteArray(test_float_array, test_byte_array);
    }
    public static void main(String[] args) throws Exception {
        setUp();
        if(getStream1().getFrameLength() != 1024L)
            throw new RuntimeException("Incorrect frame length returned.");
        if(getStream2().getFrameLength() != 1024L)
            throw new RuntimeException("Incorrect frame length returned.");
    }
}
