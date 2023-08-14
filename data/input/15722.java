public class AccessibleAttributeSequence {
    public int startIndex;
    public int endIndex;
    public AttributeSet attributes;
    public AccessibleAttributeSequence(int start, int end, AttributeSet attr) {
        startIndex = start;
        endIndex = end;
        attributes = attr;
    }
};
