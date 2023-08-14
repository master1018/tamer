public class CharsetString {
    public char[] charsetChars;
    public int offset;
    public int length;
    public FontDescriptor fontDescriptor;
    public CharsetString(char charsetChars[], int offset, int length,
                         FontDescriptor fontDescriptor){
        this.charsetChars = charsetChars;
        this.offset = offset;
        this.length = length;
        this.fontDescriptor = fontDescriptor;
    }
}
