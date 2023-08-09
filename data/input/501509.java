public class ByteArrayPartSource implements PartSource {
    private String fileName;
    private byte[] bytes;
    public ByteArrayPartSource(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }
    public long getLength() {
        return bytes.length;
    }
    public String getFileName() {
        return fileName;
    }
    public InputStream createInputStream() {
        return new ByteArrayInputStream(bytes);
    }
}
