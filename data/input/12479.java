public abstract class UCharacterIterator
                      implements Cloneable {
    protected UCharacterIterator(){
    }
    public static final int DONE = -1;
    public static final UCharacterIterator getInstance(String source){
        return new ReplaceableUCharacterIterator(source);
    }
    public static final UCharacterIterator getInstance(StringBuffer source){
        return new ReplaceableUCharacterIterator(source);
    }
    public static final UCharacterIterator getInstance(CharacterIterator source){
        return new CharacterIteratorWrapper(source);
    }
    public abstract int current();
    public abstract int getLength();
    public abstract int getIndex();
    public abstract int next();
    public int nextCodePoint(){
        int ch1 = next();
        if(UTF16.isLeadSurrogate((char)ch1)){
            int ch2 = next();
            if(UTF16.isTrailSurrogate((char)ch2)){
                return UCharacterProperty.getRawSupplementary((char)ch1,
                                                              (char)ch2);
            }else if (ch2 != DONE) {
                previous();
            }
        }
        return ch1;
    }
    public abstract int previous();
    public abstract void setIndex(int index);
    public abstract int getText(char[] fillIn, int offset);
    public final int getText(char[] fillIn) {
        return getText(fillIn, 0);
    }
    public String getText() {
        char[] text = new char[getLength()];
        getText(text);
        return new String(text);
    }
    public int moveIndex(int delta) {
        int x = Math.max(0, Math.min(getIndex() + delta, getLength()));
        setIndex(x);
        return x;
    }
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
