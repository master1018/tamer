public final class TextHitInfo {
    private int charIdx; 
    private boolean isTrailing;
    private TextHitInfo(int idx, boolean isTrailing) {
        charIdx = idx;
        this.isTrailing = isTrailing;
    }
    @Override
    public String toString() {
        return new String("TextHitInfo[" + charIdx + ", " + 
                (isTrailing ? "Trailing" : "Leading") + "]" 
        );
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextHitInfo) {
            return equals((TextHitInfo)obj);
        }
        return false;
    }
    public boolean equals(TextHitInfo thi) {
        return thi != null && thi.charIdx == charIdx && thi.isTrailing == isTrailing;
    }
    public TextHitInfo getOffsetHit(int offset) {
        return new TextHitInfo(charIdx + offset, isTrailing);
    }
    public TextHitInfo getOtherHit() {
        return isTrailing ? new TextHitInfo(charIdx + 1, false)
                : new TextHitInfo(charIdx - 1, true);
    }
    public boolean isLeadingEdge() {
        return !isTrailing;
    }
    @Override
    public int hashCode() {
        return HashCode.combine(charIdx, isTrailing);
    }
    public int getInsertionIndex() {
        return isTrailing ? charIdx + 1 : charIdx;
    }
    public int getCharIndex() {
        return charIdx;
    }
    public static TextHitInfo trailing(int charIndex) {
        return new TextHitInfo(charIndex, true);
    }
    public static TextHitInfo leading(int charIndex) {
        return new TextHitInfo(charIndex, false);
    }
    public static TextHitInfo beforeOffset(int offset) {
        return new TextHitInfo(offset - 1, true);
    }
    public static TextHitInfo afterOffset(int offset) {
        return new TextHitInfo(offset, false);
    }
}
