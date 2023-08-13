public final class LineBreakMeasurer {
    private TextMeasurer tm = null;
    private int position = 0;
    int maxpos = 0;
    public LineBreakMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
    }
    public void deleteChar(AttributedCharacterIterator newText, int pos) {
        tm.deleteChar(newText, pos);
        position = newText.getBeginIndex();
        maxpos--;
    }
    public int getPosition() {
        return position;
    }
    public void insertChar(AttributedCharacterIterator newText, int pos) {
        tm.insertChar(newText, pos);
        position = newText.getBeginIndex();
        maxpos++;
    }
    public TextLayout nextLayout(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
        if (position == maxpos) {
            return null;
        }
        int nextPosition = nextOffset(wrappingWidth, offsetLimit, requireNextWord);
        if (nextPosition == position) {
            return null;
        }
        TextLayout layout = tm.getLayout(position, nextPosition);
        position = nextPosition;
        return layout;
    }
    public TextLayout nextLayout(float wrappingWidth) {
        return nextLayout(wrappingWidth, maxpos, false);
    }
    public int nextOffset(float wrappingWidth) {
        return nextOffset(wrappingWidth, maxpos, false);
    }
    public int nextOffset(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
        if (offsetLimit <= position) {
            throw new IllegalArgumentException(Messages.getString("awt.203")); 
        }
        if (position == maxpos) {
            return position;
        }
        int breakPos = tm.getLineBreakIndex(position, wrappingWidth);
        int correctedPos = breakPos;
        if (position >= correctedPos) {
            if (requireNextWord) {
                correctedPos = position;
            } else {
                correctedPos = Math.max(position + 1, breakPos);
            }
        }
        return Math.min(correctedPos, offsetLimit);
    }
    public void setPosition(int pos) {
        if (tm.aci.getBeginIndex() > pos || maxpos < pos) {
            throw new IllegalArgumentException(Messages.getString("awt.33")); 
        }
        position = pos;
    }
}
