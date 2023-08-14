final class NativeBreakIterator {
    private NativeBreakIterator() {
    }
    static native String[] getAvailableLocalesImpl();
    static native int getCharacterInstanceImpl(String locale);
    static native int getWordInstanceImpl(String locale);
    static native int getLineInstanceImpl(String locale);
    static native int getSentenceInstanceImpl(String locale);
    static native void closeBreakIteratorImpl(int biaddress);
    static native void setTextImpl(int biaddress, String text);
    static native int cloneImpl(int biaddress);
    static native int precedingImpl(int biaddress, int offset);
    static native boolean isBoundaryImpl(int biaddress, int offset);
    static native int nextImpl(int biaddress, int n);
    static native int previousImpl(int biaddress);
    static native int currentImpl(int biaddress);
    static native int firstImpl(int biaddress);
    static native int followingImpl(int biaddress, int offset);
    static native int lastImpl(int biaddress);
}
