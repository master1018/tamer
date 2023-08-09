public final class TextMeasurer implements Cloneable {
    AttributedCharacterIterator aci;
    FontRenderContext frc;
    TextRunBreaker breaker = null;
    TextMetricsCalculator tmc = null;
    public TextMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
        this.aci = text;
        this.frc = frc;
        breaker = new TextRunBreaker(aci, this.frc);
        tmc = new TextMetricsCalculator(breaker);
    }
    public void insertChar(AttributedCharacterIterator newParagraph, int insertPos) {
        AttributedCharacterIterator oldAci = aci;
        aci = newParagraph;
        if ((oldAci.getEndIndex() - oldAci.getBeginIndex())
                - (aci.getEndIndex() - aci.getBeginIndex()) != -1) {
            breaker = new TextRunBreaker(aci, this.frc);
            tmc = new TextMetricsCalculator(breaker);
        } else {
            breaker.insertChar(newParagraph, insertPos);
        }
    }
    public void deleteChar(AttributedCharacterIterator newParagraph, int deletePos) {
        AttributedCharacterIterator oldAci = aci;
        aci = newParagraph;
        if ((oldAci.getEndIndex() - oldAci.getBeginIndex())
                - (aci.getEndIndex() - aci.getBeginIndex()) != 1) {
            breaker = new TextRunBreaker(aci, this.frc);
            tmc = new TextMetricsCalculator(breaker);
        } else {
            breaker.deleteChar(newParagraph, deletePos);
        }
    }
    @Override
    protected Object clone() {
        return new TextMeasurer((AttributedCharacterIterator)aci.clone(), frc);
    }
    public TextLayout getLayout(int start, int limit) {
        breaker.pushSegments(start - aci.getBeginIndex(), limit - aci.getBeginIndex());
        breaker.createAllSegments();
        TextLayout layout = new TextLayout((TextRunBreaker)breaker.clone());
        breaker.popSegments();
        return layout;
    }
    public float getAdvanceBetween(int start, int end) {
        breaker.pushSegments(start - aci.getBeginIndex(), end - aci.getBeginIndex());
        breaker.createAllSegments();
        float retval = tmc.createMetrics().getAdvance();
        breaker.popSegments();
        return retval;
    }
    public int getLineBreakIndex(int start, float maxAdvance) {
        breaker.createAllSegments();
        return breaker.getLineBreakIndex(start - aci.getBeginIndex(), maxAdvance)
                + aci.getBeginIndex();
    }
}
