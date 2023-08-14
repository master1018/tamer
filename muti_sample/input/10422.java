public class ReplaceableUCharacterIterator extends UCharacterIterator {
    public ReplaceableUCharacterIterator(String str){
        if(str==null){
            throw new IllegalArgumentException();
        }
        this.replaceable  = new ReplaceableString(str);
        this.currentIndex = 0;
    }
    public ReplaceableUCharacterIterator(StringBuffer buf){
        if(buf==null){
            throw new IllegalArgumentException();
        }
        this.replaceable  = new ReplaceableString(buf);
        this.currentIndex = 0;
    }
    public Object clone(){
        try {
          return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; 
        }
    }
    public int current(){
        if (currentIndex < replaceable.length()) {
            return replaceable.charAt(currentIndex);
        }
        return DONE;
    }
    public int getLength(){
        return replaceable.length();
    }
    public int getIndex(){
        return currentIndex;
    }
    public int next(){
        if (currentIndex < replaceable.length()) {
            return replaceable.charAt(currentIndex++);
        }
        return DONE;
    }
    public int previous(){
        if (currentIndex > 0) {
            return replaceable.charAt(--currentIndex);
        }
        return DONE;
    }
    public void setIndex(int currentIndex) {
        if (currentIndex < 0 || currentIndex > replaceable.length()) {
            throw new IllegalArgumentException();
        }
        this.currentIndex = currentIndex;
    }
    public int getText(char[] fillIn, int offset){
        int length = replaceable.length();
        if(offset < 0 || offset + length > fillIn.length){
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        replaceable.getChars(0,length,fillIn,offset);
        return length;
    }
    private Replaceable replaceable;
    private int currentIndex;
}
