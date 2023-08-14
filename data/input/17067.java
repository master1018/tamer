public class GetDataElementsTest {
    public static int width = 100;
    public static int height = 100;
    public static int dataType = DataBuffer.TYPE_BYTE;
    public static int numBands = 4;
    public static void main(String[] args) {
        SampleModel sm = new ComponentSampleModel(dataType, width, height, 4, width * 4, new int[] { 0, 1, 2, 3 } );
        DataBuffer db = sm.createDataBuffer();
        Object o = null;
        boolean testPassed = false;
        try {
            o = sm.getDataElements(Integer.MAX_VALUE, 0, 1, 1, o, db);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            testPassed = true;
        }
        if (!testPassed) {
            throw new RuntimeException("Excpected excprion was not thrown.");
        }
    }
}
