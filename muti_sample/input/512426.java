final class WbxmlSerializer {
    private OutputStream mOut;
    private int mNativeHandle;
    private static int PUBLIC_ID_IMPS_11 = 0x10;
    private static int PUBLIC_ID_IMPS_12 = 0x11;
    private static int PUBLIC_ID_IMPS_13 = 0x12;
    public WbxmlSerializer(ImpsVersion impsVersion) {
        if (impsVersion == ImpsVersion.IMPS_VERSION_11) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_11);
        } else if (impsVersion == ImpsVersion.IMPS_VERSION_12) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_12);
        } else if (impsVersion == ImpsVersion.IMPS_VERSION_13) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_13);
        } else {
            throw new IllegalArgumentException("Unsupported IMPS version");
        }
        if (mNativeHandle == 0) {
            throw new OutOfMemoryError();
        }
    }
    @Override
    protected void finalize() {
        if (mNativeHandle != 0) {
            nativeRelease(mNativeHandle);
        }
    }
    public void reset() {
        nativeReset(mNativeHandle);
        mOut = null;
    }
    public void setOutput(OutputStream out) {
        mOut = out;
    }
    public void startElement(String name, String[] atts) throws IOException,
            SerializerException {
        try {
            nativeStartElement(mNativeHandle, name, atts);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }
    public void characters(String chars) throws IOException, SerializerException {
        try {
            nativeCharacters(mNativeHandle, chars);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }
    public void endElement() throws IOException, SerializerException {
        try {
            nativeEndElement(mNativeHandle);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }
    void onWbxmlData(byte[] data, int len) throws IOException {
        if (mOut != null) {
            mOut.write(data, 0, len);
        }
    }
    native int nativeCreate(int publicId);
    native void nativeReset(int nativeHandle);
    native void nativeRelease(int nativeHandle);
    native void nativeStartElement(int nativeHandle, String name, String[] atts)
            throws IOException, IllegalArgumentException;
    native void nativeCharacters(int nativeHandle, String characters)
            throws IOException, IllegalArgumentException;
    native void nativeEndElement(int nativeHandle)
            throws IOException, IllegalArgumentException;
    static {
        try {
            System.loadLibrary("wbxml_jni");
        }catch(UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load library libwbxml_jni.so");
        }
    }
}
