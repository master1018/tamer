public final class CharArrayBuffer {
    public CharArrayBuffer(int size) {
        data = new char[size];
    }
    public CharArrayBuffer(char[] buf) {
        data = buf;
    }
    public char[] data; 
    public int sizeCopied; 
}
