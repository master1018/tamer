final class NativeConverter{
    public static final native int convertByteToChar( long converterHandle,
                                   byte[] input, int inEnd,
                                   char[] output, int outEnd,
                                   int[] data,
                                   boolean flush);
    public static final native int decode( long converterHandle,
                                   byte[] input, int inEnd,
                                   char[] output, int outEnd,
                                   int[] data,
                                   boolean flush);
    public static final native int convertCharToByte(long converterHandle,
                                   char[] input, int inEnd,
                                   byte[] output, int outEnd,
                                   int[] data,
                                   boolean flush); 
    public static final native int encode(long converterHandle,
                                   char[] input, int inEnd,
                                   byte[] output, int outEnd,
                                   int[] data,
                                   boolean flush);
    public static final native int flushCharToByte(long converterHandle,
                                   byte[] output, 
                                   int outEnd, 
                                   int[] data);
    public static final native int flushByteToChar(long converterHandle,
                                   char[] output,  
                                   int outEnd, 
                                   int[] data);
    public static final native long openConverter(String encoding);
    public static final native void resetByteToChar(long  converterHandle);
    public static final native void resetCharToByte(long  converterHandle);
    public static final native void closeConverter(long converterHandle);
    public static final native int setSubstitutionChars( long converterHandle,
                                   char[] subChars,int length); 
    public static final native int setSubstitutionBytes( long converterHandle,
                                   byte[] subChars,int length);
    public static final native int setSubstitutionModeCharToByte(long converterHandle, 
                                   boolean mode);
    public static final native int setSubstitutionModeByteToChar(long converterHandle, 
                                   boolean mode);
    public static final native int countInvalidBytes(long converterHandle, int[] length);
    public static final native int countInvalidChars(long converterHandle, int[] length);
    public static final native int getMaxBytesPerChar(long converterHandle);
    public static final native int getMinBytesPerChar(long converterHandle);
    public static final native float getAveBytesPerChar(long converterHandle);
    public static final native int getMaxCharsPerByte(long converterHandle);
    public static final native float getAveCharsPerByte(long converterHandle);
    public static final native boolean contains(long converterHandle1, long converterHandle2);
    public static final native byte[] getSubstitutionBytes(long converterHandle);
    public static final native boolean canEncode(long converterHandle,int codeUnit);
    public static final native boolean canDecode(long converterHandle,byte[] bytes);
    public static final native String[] getAvailable();
    public static final native int countAliases(String enc);
    public static final native String[] getAliases(String enc);
    public static final native String getCanonicalName(String enc);
    public static final native String getICUCanonicalName(String enc);
    public static final native String getJavaCanonicalName(String icuCanonicalName);
    public static final native int setCallbackDecode(long converterHandle, int onMalformedInput, int onUnmappableInput, char[] subChars, int length);
    public static final native int setCallbackEncode(long converterHandle, int onMalformedInput, int onUnmappableInput, byte[] subBytes, int length);
    public static final native long safeClone(long converterHandle);
    public static final int STOP_CALLBACK = 0;
    public static final int SKIP_CALLBACK = 1;
    public static final int SUBSTITUTE_CALLBACK = 2;
}
