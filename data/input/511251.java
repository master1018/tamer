final class ICC_ProfileStub extends ICC_Profile {
    private static final long serialVersionUID = 501389760875253507L;
    transient int colorspace;
    public ICC_ProfileStub(int csSpecifier) {
        switch (csSpecifier) {
            case ColorSpace.CS_sRGB:
            case ColorSpace.CS_CIEXYZ:
            case ColorSpace.CS_LINEAR_RGB:
            case ColorSpace.CS_PYCC:
            case ColorSpace.CS_GRAY:
                break;
            default:
                throw new IllegalArgumentException(Messages.getString("awt.15D")); 
        }
        colorspace = csSpecifier;
    }
    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    private Object writeReplace() throws ObjectStreamException {
        return loadProfile();
    }
    @Override
    public void write(OutputStream s) throws IOException {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public void setData(int tagSignature, byte[] tagData) {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public byte[] getData(int tagSignature) {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public byte[] getData() {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    protected void finalize() {
    }
    @Override
    public int getProfileClass() {
        return CLASS_COLORSPACECONVERSION;
    }
    @Override
    public int getPCSType() {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public int getNumComponents() {
        switch (colorspace) {
            case ColorSpace.CS_sRGB:
            case ColorSpace.CS_CIEXYZ:
            case ColorSpace.CS_LINEAR_RGB:
            case ColorSpace.CS_PYCC:
                return 3;
            case ColorSpace.CS_GRAY:
                return 1;
            default:
                throw new UnsupportedOperationException("Stub cannot perform this operation"); 
        }
    }
    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    @Override
    public int getColorSpaceType() {
        switch (colorspace) {
            case ColorSpace.CS_sRGB:
            case ColorSpace.CS_LINEAR_RGB:
                return ColorSpace.TYPE_RGB;
            case ColorSpace.CS_CIEXYZ:
                return ColorSpace.TYPE_XYZ;
            case ColorSpace.CS_PYCC:
                return ColorSpace.TYPE_3CLR;
            case ColorSpace.CS_GRAY:
                return ColorSpace.TYPE_GRAY;
            default:
                throw new UnsupportedOperationException("Stub cannot perform this operation"); 
        }
    }
    public static ICC_Profile getInstance(String fileName) throws IOException {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    public static ICC_Profile getInstance(InputStream s) throws IOException {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    public static ICC_Profile getInstance(byte[] data) {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    public static ICC_Profile getInstance(int cspace) {
        throw new UnsupportedOperationException("Stub cannot perform this operation"); 
    }
    public ICC_Profile loadProfile() {
        switch (colorspace) {
            case ColorSpace.CS_sRGB:
                return ICC_Profile.getInstance(ColorSpace.CS_sRGB);
            case ColorSpace.CS_GRAY:
                return ICC_Profile.getInstance(ColorSpace.CS_GRAY);
            case ColorSpace.CS_CIEXYZ:
                return ICC_Profile.getInstance(ColorSpace.CS_CIEXYZ);
            case ColorSpace.CS_LINEAR_RGB:
                return ICC_Profile.getInstance(ColorSpace.CS_LINEAR_RGB);
            case ColorSpace.CS_PYCC:
                return ICC_Profile.getInstance(ColorSpace.CS_PYCC);
            default:
                throw new UnsupportedOperationException("Stub cannot perform this operation"); 
        }
    }
}