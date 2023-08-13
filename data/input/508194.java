public class FieldPosition {
    private int myField, beginIndex, endIndex;
    private Format.Field myAttribute;
    public FieldPosition(int field) {
        myField = field;
    }
    public FieldPosition(Format.Field attribute) {
        myAttribute = attribute;
        myField = -1;
    }
    public FieldPosition(Format.Field attribute, int field) {
        myAttribute = attribute;
        myField = field;
    }
    void clear() {
        beginIndex = endIndex = 0;
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FieldPosition)) {
            return false;
        }
        FieldPosition pos = (FieldPosition) object;
        return myField == pos.myField && myAttribute == pos.myAttribute
                && beginIndex == pos.beginIndex && endIndex == pos.endIndex;
    }
    public int getBeginIndex() {
        return beginIndex;
    }
    public int getEndIndex() {
        return endIndex;
    }
    public int getField() {
        return myField;
    }
    public Format.Field getFieldAttribute() {
        return myAttribute;
    }
    @Override
    public int hashCode() {
        int attributeHash = (myAttribute == null) ? 0 : myAttribute.hashCode();
        return attributeHash + myField * 10 + beginIndex * 100 + endIndex;
    }
    public void setBeginIndex(int index) {
        beginIndex = index;
    }
    public void setEndIndex(int index) {
        endIndex = index;
    }
    @Override
    public String toString() {
        return getClass().getName() + "[attribute=" + myAttribute + ", field=" 
                + myField + ", beginIndex=" + beginIndex + ", endIndex=" 
                + endIndex + "]"; 
    }
}
