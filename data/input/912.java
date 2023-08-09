public class BoundsCheck {
    public static void main(String args[]) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos);
        String data = "Data to be written";
        char cdata[] = {'a', 'b', 'c', 'd', 'a', 'b', 'c', 'd'};
        boolean caughtException = false;
        try {
            osw.write(data, -3, 5);
            throw new RuntimeException("Test failed for negative offset");
        } catch (IndexOutOfBoundsException  e){ }
        try {
            osw.write(data, 3, -5);
            throw new RuntimeException("Test failed for negative length");
        } catch (IndexOutOfBoundsException  e){ }
        try {
            osw.write(data, 3, 75);
            throw new RuntimeException("Test failed for len+off > str.length");
        } catch (IndexOutOfBoundsException  e){ }
        try {
            osw.write(cdata, -3, 5);
            throw new RuntimeException("Test failed for negative offset");
        } catch (IndexOutOfBoundsException  e){ }
        try {
            osw.write(cdata, 3, -5);
            throw new RuntimeException("Test failed for negative length");
        } catch (IndexOutOfBoundsException  e){ }
        try {
            osw.write(cdata, 3, 75);
            throw new RuntimeException("Test failed for len+off > str.length");
        } catch (IndexOutOfBoundsException  e){ }
    }
}
