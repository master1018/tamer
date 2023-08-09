class TagStack implements DTDConstants {
    TagElement tag;
    Element elem;
    ContentModelState state;
    TagStack next;
    BitSet inclusions;
    BitSet exclusions;
    boolean net;
    boolean pre;
    TagStack(TagElement tag, TagStack next) {
        this.tag = tag;
        this.elem = tag.getElement();
        this.next = next;
        Element elem = tag.getElement();
        if (elem.getContent() != null) {
            this.state = new ContentModelState(elem.getContent());
        }
        if (next != null) {
            inclusions = next.inclusions;
            exclusions = next.exclusions;
            pre = next.pre;
        }
        if (tag.isPreformatted()) {
            pre = true;
        }
        if (elem.inclusions != null) {
            if (inclusions != null) {
                inclusions = (BitSet)inclusions.clone();
                inclusions.or(elem.inclusions);
            } else {
                inclusions = elem.inclusions;
            }
        }
        if (elem.exclusions != null) {
            if (exclusions != null) {
                exclusions = (BitSet)exclusions.clone();
                exclusions.or(elem.exclusions);
            } else {
                exclusions = elem.exclusions;
            }
        }
    }
    public Element first() {
        return (state != null) ? state.first() : null;
    }
    public ContentModel contentModel() {
        if (state == null) {
            return null;
        } else {
            return state.getModel();
        }
    }
    boolean excluded(int elemIndex) {
        return (exclusions != null) && exclusions.get(elem.getIndex());
    }
    boolean advance(Element elem) {
        if ((exclusions != null) && exclusions.get(elem.getIndex())) {
            return false;
        }
        if (state != null) {
            ContentModelState newState = state.advance(elem);
            if (newState != null) {
                state = newState;
                return true;
            }
        } else if (this.elem.getType() == ANY) {
            return true;
        }
        return (inclusions != null) && inclusions.get(elem.getIndex());
    }
    boolean terminate() {
        return (state == null) || state.terminate();
    }
    public String toString() {
        return (next == null) ?
            "<" + tag.getElement().getName() + ">" :
            next + " <" + tag.getElement().getName() + ">";
    }
}
class NPrintWriter extends PrintWriter {
    private int numLines = 5;
    private int numPrinted = 0;
    public NPrintWriter (int numberOfLines) {
        super(System.out);
        numLines = numberOfLines;
    }
    public void println(char[] array) {
        if (numPrinted >= numLines) {
            return;
        }
        char[] partialArray = null;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '\n') {
                numPrinted++;
            }
            if (numPrinted == numLines) {
                System.arraycopy(array, 0, partialArray, 0, i);
            }
        }
        if (partialArray != null) {
            super.print(partialArray);
        }
        if (numPrinted == numLines) {
            return;
        }
        super.println(array);
        numPrinted++;
    }
}
