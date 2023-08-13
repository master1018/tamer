public class MockParserStream extends InputStream {
    int[] array;
    int pos = 0;
    Object value;
    MockParserStream (int[] _array) {
        array = _array;
    }
    @Override
    public int read() throws IOException {
        try {
            return array[pos++];
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("End of stream");
        }
    }
    public void setResult(Object _value) {
        value = _value;
    }
    public Object getResult() {
        return value;
    }
}
