public class ParsePosition {
    private int currentPosition, errorIndex = -1;
    public ParsePosition(int index) {
        currentPosition = index;
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParsePosition)) {
            return false;
        }
        ParsePosition pos = (ParsePosition) object;
        return currentPosition == pos.currentPosition
                && errorIndex == pos.errorIndex;
    }
    public int getErrorIndex() {
        return errorIndex;
    }
    public int getIndex() {
        return currentPosition;
    }
    @Override
    public int hashCode() {
        return currentPosition + errorIndex;
    }
    public void setErrorIndex(int index) {
        errorIndex = index;
    }
    public void setIndex(int index) {
        currentPosition = index;
    }
    @Override
    public String toString() {
        return getClass().getName() + "[index=" + currentPosition 
                + ", errorIndex=" + errorIndex + "]"; 
    }
}
