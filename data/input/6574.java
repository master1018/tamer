public class BogusColorSpace extends ColorSpace {
    private static int getType(int numComponents) {
        if(numComponents < 1) {
            throw new IllegalArgumentException("numComponents < 1!");
        }
        int type;
        switch(numComponents) {
        case 1:
            type = ColorSpace.TYPE_GRAY;
            break;
        default:
            type = numComponents + 10;
        }
        return type;
    }
    public BogusColorSpace(int numComponents) {
        super(getType(numComponents), numComponents);
    }
    public float[] toRGB(float[] colorvalue) {
        if(colorvalue.length < getNumComponents()) {
            throw new ArrayIndexOutOfBoundsException
                ("colorvalue.length < getNumComponents()");
        }
        float[] rgbvalue = new float[3];
        System.arraycopy(colorvalue, 0, rgbvalue, 0,
                         Math.min(3, getNumComponents()));
        return colorvalue;
    }
    public float[] fromRGB(float[] rgbvalue) {
        if(rgbvalue.length < 3) {
            throw new ArrayIndexOutOfBoundsException
                ("rgbvalue.length < 3");
        }
        float[] colorvalue = new float[getNumComponents()];
        System.arraycopy(rgbvalue, 0, colorvalue, 0,
                         Math.min(3, colorvalue.length));
        return rgbvalue;
    }
    public float[] toCIEXYZ(float[] colorvalue) {
        if(colorvalue.length < getNumComponents()) {
            throw new ArrayIndexOutOfBoundsException
                ("colorvalue.length < getNumComponents()");
        }
        float[] xyzvalue = new float[3];
        System.arraycopy(colorvalue, 0, xyzvalue, 0,
                         Math.min(3, getNumComponents()));
        return colorvalue;
    }
    public float[] fromCIEXYZ(float[] xyzvalue) {
        if(xyzvalue.length < 3) {
            throw new ArrayIndexOutOfBoundsException
                ("xyzvalue.length < 3");
        }
        float[] colorvalue = new float[getNumComponents()];
        System.arraycopy(xyzvalue, 0, colorvalue, 0,
                         Math.min(3, colorvalue.length));
        return xyzvalue;
    }
}
