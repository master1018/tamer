public class SkipTest {
    public static void main(String[] args) throws Exception {
        AudioFloatFormatConverter converter = new AudioFloatFormatConverter();
        byte[] data = { 10, 20, 30, 40, 30, 20, 10 };
        AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
        AudioFormat format2 = new AudioFormat(16000, 8, 1, true, false);
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(
                data), format, data.length);
        AudioInputStream ais2 = converter.getAudioInputStream(format2, ais);
        byte[] data2 = new byte[30];
        int ret = ais2.read(data2, 0, data2.length);
        ais.reset();
        AudioInputStream ais3 = converter.getAudioInputStream(format2, ais);
        byte[] data3 = new byte[100];
        ais3.skip(7);
        int ret2 = ais3.read(data3, 7, data3.length);
        if (ret2 != ret - 7)
            throw new Exception("Skip doesn't work correctly (" + ret2 + " != "
                    + (ret - 7) + ")");
        for (int i = 7; i < ret2 + 7; i++) {
            if (data3[i] != data2[i])
                throw new Exception("Skip doesn't work correctly (" + data3[i]
                        + " != " + data2[i] + ")");
        }
    }
}
