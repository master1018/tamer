public class CharacterIteratorWrapper extends UCharacterIterator {
    private CharacterIterator iterator;
    public CharacterIteratorWrapper(CharacterIterator iter){
        if(iter==null){
            throw new IllegalArgumentException();
        }
        iterator     = iter;
    }
    public int current() {
        int c = iterator.current();
        if(c==CharacterIterator.DONE){
          return DONE;
        }
        return c;
    }
    public int getLength() {
        return (iterator.getEndIndex() - iterator.getBeginIndex());
    }
    public int getIndex() {
        return iterator.getIndex();
    }
    public int next() {
        int i = iterator.current();
        iterator.next();
        if(i==CharacterIterator.DONE){
          return DONE;
        }
        return i;
    }
    public int previous() {
        int i = iterator.previous();
        if(i==CharacterIterator.DONE){
            return DONE;
        }
        return i;
    }
    public void setIndex(int index) {
        iterator.setIndex(index);
    }
    public int getText(char[] fillIn, int offset){
        int length =iterator.getEndIndex() - iterator.getBeginIndex();
        int currentIndex = iterator.getIndex();
        if(offset < 0 || offset + length > fillIn.length){
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        for (char ch = iterator.first(); ch != CharacterIterator.DONE; ch = iterator.next()) {
            fillIn[offset++] = ch;
        }
        iterator.setIndex(currentIndex);
        return length;
    }
    public Object clone(){
        try {
            CharacterIteratorWrapper result = (CharacterIteratorWrapper) super.clone();
            result.iterator = (CharacterIterator)this.iterator.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null; 
        }
    }
}
