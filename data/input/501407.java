public final class Bidi {
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    public Bidi(AttributedCharacterIterator paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException(Messages.getString("text.14")); 
        }
        int begin = paragraph.getBeginIndex();
        int end = paragraph.getEndIndex();
        int length = end - begin;
        char text[] = new char[length + 1]; 
        if (length != 0) {
            text[0] = paragraph.first();
        } else {
            paragraph.first();
        }
        int flags = DIRECTION_DEFAULT_LEFT_TO_RIGHT;
        Object direction = paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        if (direction != null && direction instanceof Boolean) {
            if (direction.equals(TextAttribute.RUN_DIRECTION_LTR)) {
                flags = DIRECTION_LEFT_TO_RIGHT;
            } else {
                flags = DIRECTION_RIGHT_TO_LEFT;
            }
        }
        byte embeddings[] = null;
        for (int textLimit = 1, i = 1; i < length; textLimit = paragraph
                .getRunLimit(TextAttribute.BIDI_EMBEDDING)
                - begin + 1) {
            Object embedding = paragraph
                    .getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (embedding != null && embedding instanceof Integer) {
                int embLevel = ((Integer) embedding).intValue();
                if (embeddings == null) {
                    embeddings = new byte[length];
                }
                for (; i < textLimit; i++) {
                    text[i] = paragraph.next();
                    embeddings[i - 1] = (byte) embLevel;
                }
            } else {
                for (; i < textLimit; i++) {
                    text[i] = paragraph.next();
                }
            }
        }
        Object numericShaper = paragraph
                .getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (numericShaper != null && numericShaper instanceof NumericShaper) {
            ((NumericShaper) numericShaper).shape(text, 0, length);
        }
        long pBidi = createUBiDi(text, 0, embeddings, 0, length, flags);
        readBidiInfo(pBidi);
        BidiWrapper.ubidi_close(pBidi);
    }
    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart,
            int paragraphLength, int flags) {
        if (text == null || text.length - textStart < paragraphLength) {
            throw new IllegalArgumentException();
        }
        if (embeddings != null) {
            if (embeddings.length - embStart < paragraphLength) {
                throw new IllegalArgumentException();
            }
        }
        if (textStart < 0) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.0D", textStart)); 
        }
        if (embStart < 0) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.10", embStart)); 
        }
        if (paragraphLength < 0) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.11", paragraphLength)); 
        }
        long pBidi = createUBiDi(text, textStart, embeddings, embStart,
                paragraphLength, flags);
        readBidiInfo(pBidi);
        BidiWrapper.ubidi_close(pBidi);
    }
    public Bidi(String paragraph, int flags) {
        this((paragraph == null ? null : paragraph.toCharArray()), 0, null, 0,
                (paragraph == null ? 0 : paragraph.length()), flags);
    }
    private static long createUBiDi(char[] text, int textStart,
            byte[] embeddings, int embStart, int paragraphLength, int flags) {
        char[] realText = null;
        byte[] realEmbeddings = null;
        if (text == null || text.length - textStart < paragraphLength) {
            throw new IllegalArgumentException();
        }
        realText = new char[paragraphLength];
        System.arraycopy(text, textStart, realText, 0, paragraphLength);
        if (embeddings != null) {
            if (embeddings.length - embStart < paragraphLength) {
                throw new IllegalArgumentException();
            }
            if (paragraphLength > 0) {
                Bidi temp = new Bidi(text, textStart, null, 0, paragraphLength,
                        flags);
                realEmbeddings = new byte[paragraphLength];
                System.arraycopy(temp.offsetLevel, 0, realEmbeddings, 0,
                        paragraphLength);
                for (int i = 0; i < paragraphLength; i++) {
                    byte e = embeddings[i];
                    if (e < 0) {
                        realEmbeddings[i] = (byte) (BidiWrapper.UBIDI_LEVEL_OVERRIDE - e);
                    } else if (e > 0) {
                        realEmbeddings[i] = e;
                    } else {
                        realEmbeddings[i] |= (byte) BidiWrapper.UBIDI_LEVEL_OVERRIDE;
                    }
                }
            }
        }
        if (flags > 1 || flags < -2) {
            flags = 0;
        }
        long bidi = BidiWrapper.ubidi_open();
        BidiWrapper.ubidi_setPara(bidi, realText, paragraphLength,
                (byte) flags, realEmbeddings);
        return bidi;
    }
    private Bidi(long pBidi) {
        readBidiInfo(pBidi);
    }
    private void readBidiInfo(long pBidi) {
        length = BidiWrapper.ubidi_getLength(pBidi);
        offsetLevel = (length == 0) ? null : BidiWrapper.ubidi_getLevels(pBidi);
        baseLevel = BidiWrapper.ubidi_getParaLevel(pBidi);
        int runCount = BidiWrapper.ubidi_countRuns(pBidi);
        if (runCount == 0) {
            unidirectional = true;
            runs = null;
        } else if (runCount < 0) {
            runs = null;
        } else {
            runs = BidiWrapper.ubidi_getRuns(pBidi);
            if (runCount == 1 && runs[0].getLevel() == baseLevel) {
                unidirectional = true;
                runs = null;
            }
        }
        direction = BidiWrapper.ubidi_getDirection(pBidi);
    }
    private int baseLevel;
    private int length;
    private byte[] offsetLevel;
    private BidiRun[] runs;
    private int direction;
    private boolean unidirectional;
    public boolean baseIsLeftToRight() {
        return baseLevel % 2 == 0 ? true : false;
    }
    public Bidi createLineBidi(int lineStart, int lineLimit) {
        if (lineStart < 0 || lineLimit < 0 || lineLimit > length
                || lineStart > lineLimit) {
            throw new IllegalArgumentException(Messages.getString(
                    "text.12", new Object[] { lineStart, lineLimit, length })); 
        }
        char[] text = new char[this.length];
        Arrays.fill(text, 'a');
        byte[] embeddings = new byte[this.length];
        for (int i = 0; i < embeddings.length; i++) {
            embeddings[i] = (byte) -this.offsetLevel[i];
        }
        int dir = this.baseIsLeftToRight() ? Bidi.DIRECTION_LEFT_TO_RIGHT
                : Bidi.DIRECTION_RIGHT_TO_LEFT;
        long parent = createUBiDi(text, 0, embeddings, 0, this.length, dir);
        long line = BidiWrapper.ubidi_setLine(parent, lineStart, lineLimit);
        Bidi result = new Bidi(line);
        BidiWrapper.ubidi_close(line);
        BidiWrapper.ubidi_close(parent);
        return result;
    }
    public int getBaseLevel() {
        return baseLevel;
    }
    public int getLength() {
        return length;
    }
    public int getLevelAt(int offset) {
        try {
            return offsetLevel[offset] & ~BidiWrapper.UBIDI_LEVEL_OVERRIDE;
        } catch (RuntimeException e) {
            return baseLevel;
        }
    }
    public int getRunCount() {
        return unidirectional ? 1 : runs.length;
    }
    public int getRunLevel(int run) {
        return unidirectional ? baseLevel : runs[run].getLevel();
    }
    public int getRunLimit(int run) {
        return unidirectional ? length : runs[run].getLimit();
    }
    public int getRunStart(int run) {
        return unidirectional ? 0 : runs[run].getStart();
    }
    public boolean isLeftToRight() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_LTR;
    }
    public boolean isMixed() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_MIXED;
    }
    public boolean isRightToLeft() {
        return direction == BidiWrapper.UBiDiDirection_UBIDI_RTL;
    }
    public static void reorderVisually(byte[] levels, int levelStart,
            Object[] objects, int objectStart, int count) {
        if (count < 0 || levelStart < 0 || objectStart < 0
                || count > levels.length - levelStart
                || count > objects.length - objectStart) {
            throw new IllegalArgumentException(Messages.getString("text.13", 
                    new Object[] { levels.length, levelStart, objects.length,
                            objectStart, count }));
        }
        byte[] realLevels = new byte[count];
        System.arraycopy(levels, levelStart, realLevels, 0, count);
        int[] indices = BidiWrapper.ubidi_reorderVisual(realLevels, count);
        LinkedList<Object> result = new LinkedList<Object>();
        for (int i = 0; i < count; i++) {
            result.addLast(objects[objectStart + indices[i]]);
        }
        System.arraycopy(result.toArray(), 0, objects, objectStart, count);
    }
    public static boolean requiresBidi(char[] text, int start, int limit) {
        if (limit < 0 || start < 0 || start > limit || limit > text.length) {
            throw new IllegalArgumentException();
        }
        Bidi bidi = new Bidi(text, start, null, 0, limit - start, 0);
        return !bidi.isLeftToRight();
    }
    @Override
    public String toString() {
        return super.toString()
                + "[direction: " + direction + " baselevel: " + baseLevel 
                + " length: " + length + " runs: " + (unidirectional ? "null" : runs.toString()) + "]"; 
    }
}
