public abstract class ColorModel implements Transparency {
    protected int pixel_bits; 
    protected int transferType;
    ColorSpace cs;
    boolean hasAlpha;
    boolean isAlphaPremultiplied;
    int transparency;
    int numColorComponents;
    int numComponents;
    int[] bits; 
    int[] maxValues = null; 
    int maxBitLength; 
    private static ColorModel RGBdefault;
    protected ColorModel(int pixel_bits, int[] bits, ColorSpace cspace, boolean hasAlpha,
            boolean isAlphaPremultiplied, int transparency, int transferType) {
        if (pixel_bits < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.26B")); 
        }
        if (bits == null) {
            throw new NullPointerException(Messages.getString("awt.26C")); 
        }
        int sum = 0;
        for (int element : bits) {
            if (element < 0) {
                throw new IllegalArgumentException(Messages.getString("awt.26D")); 
            }
            sum += element;
        }
        if (sum < 1) {
            throw new NullPointerException(Messages.getString("awt.26E")); 
        }
        if (cspace == null) {
            throw new IllegalArgumentException(Messages.getString("awt.26F")); 
        }
        if (transparency < Transparency.OPAQUE || transparency > Transparency.TRANSLUCENT) {
            throw new IllegalArgumentException(Messages.getString("awt.270")); 
        }
        this.pixel_bits = pixel_bits;
        this.bits = bits.clone();
        maxValues = new int[bits.length];
        maxBitLength = 0;
        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = (1 << bits[i]) - 1;
            if (bits[i] > maxBitLength) {
                maxBitLength = bits[i];
            }
        }
        cs = cspace;
        this.hasAlpha = hasAlpha;
        this.isAlphaPremultiplied = isAlphaPremultiplied;
        numColorComponents = cs.getNumComponents();
        if (hasAlpha) {
            numComponents = numColorComponents + 1;
        } else {
            numComponents = numColorComponents;
        }
        this.transparency = transparency;
        this.transferType = transferType;
    }
    public ColorModel(int bits) {
        if (bits < 1) {
            throw new IllegalArgumentException(Messages.getString("awt.271")); 
        }
        pixel_bits = bits;
        transferType = getTransferType(bits);
        cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        hasAlpha = true;
        isAlphaPremultiplied = false;
        transparency = Transparency.TRANSLUCENT;
        numColorComponents = 3;
        numComponents = 4;
        this.bits = null;
    }
    public Object getDataElements(int[] components, int offset, Object obj) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public Object getDataElements(float[] normComponents, int normOffset, Object obj) {
        int unnormComponents[] = getUnnormalizedComponents(normComponents, normOffset, null, 0);
        return getDataElements(unnormComponents, 0, obj);
    }
    public Object getDataElements(int rgb, Object pixel) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        return null;
    }
    public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    @Override
    public String toString() {
        return "ColorModel: Color Space = " + cs.toString() + "; has alpha = " 
                + hasAlpha + "; is alpha premultipied = " 
                + isAlphaPremultiplied + "; transparency = " + transparency 
                + "; number color components = " + numColorComponents 
                + "; pixel bits = " + pixel_bits + "; transfer type = " 
                + transferType;
    }
    public int[] getComponents(Object pixel, int[] components, int offset) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
        if (pixel == null) {
            throw new NullPointerException(Messages.getString("awt.294")); 
        }
        int unnormComponents[] = getComponents(pixel, null, 0);
        return getNormalizedComponents(unnormComponents, 0, normComponents, normOffset);
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ColorModel)) {
            return false;
        }
        ColorModel cm = (ColorModel)obj;
        return (pixel_bits == cm.getPixelSize() && transferType == cm.getTransferType()
                && cs.getType() == cm.getColorSpace().getType() && hasAlpha == cm.hasAlpha()
                && isAlphaPremultiplied == cm.isAlphaPremultiplied()
                && transparency == cm.getTransparency()
                && numColorComponents == cm.getNumColorComponents()
                && numComponents == cm.getNumComponents() && Arrays.equals(bits, cm
                .getComponentSize()));
    }
    public int getRed(Object inData) {
        return getRed(constructPixel(inData));
    }
    public int getRGB(Object inData) {
        return (getAlpha(inData) << 24 | getRed(inData) << 16 | getGreen(inData) << 8 | getBlue(inData));
    }
    public int getGreen(Object inData) {
        return getGreen(constructPixel(inData));
    }
    public int getBlue(Object inData) {
        return getBlue(constructPixel(inData));
    }
    public int getAlpha(Object inData) {
        return getAlpha(constructPixel(inData));
    }
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public boolean isCompatibleSampleModel(SampleModel sm) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public SampleModel createCompatibleSampleModel(int w, int h) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public boolean isCompatibleRaster(Raster raster) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public final ColorSpace getColorSpace() {
        return cs;
    }
    public float[] getNormalizedComponents(int[] components, int offset, float normComponents[],
            int normOffset) {
        if (bits == null) {
            throw new UnsupportedOperationException(Messages.getString("awt.26C")); 
        }
        if (normComponents == null) {
            normComponents = new float[numComponents + normOffset];
        }
        if (hasAlpha && isAlphaPremultiplied) {
            float normAlpha = (float)components[offset + numColorComponents]
                    / maxValues[numColorComponents];
            if (normAlpha != 0.0f) {
                for (int i = 0; i < numColorComponents; i++) {
                    normComponents[normOffset + i] = components[offset + i]
                            / (normAlpha * maxValues[i]);
                }
                normComponents[normOffset + numColorComponents] = normAlpha;
            } else {
                for (int i = 0; i < numComponents; i++) {
                    normComponents[normOffset + i] = 0.0f;
                }
            }
        } else {
            for (int i = 0; i < numComponents; i++) {
                normComponents[normOffset + i] = (float)components[offset + i] / maxValues[i];
            }
        }
        return normComponents;
    }
    public int getDataElement(int[] components, int offset) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public int[] getUnnormalizedComponents(float normComponents[], int normOffset,
            int components[], int offset) {
        if (bits == null) {
            throw new UnsupportedOperationException(Messages.getString("awt.26C")); 
        }
        if (normComponents.length - normOffset < numComponents) {
            throw new IllegalArgumentException(Messages.getString("awt.273")); 
        }
        if (components == null) {
            components = new int[numComponents + offset];
        } else {
            if (components.length - offset < numComponents) {
                throw new IllegalArgumentException(Messages.getString("awt.272")); 
            }
        }
        if (hasAlpha && isAlphaPremultiplied) {
            float alpha = normComponents[normOffset + numColorComponents];
            for (int i = 0; i < numColorComponents; i++) {
                components[offset + i] = (int)(normComponents[normOffset + i] * maxValues[i]
                        * alpha + 0.5f);
            }
            components[offset + numColorComponents] = (int)(normComponents[normOffset
                    + numColorComponents]
                    * maxValues[numColorComponents] + 0.5f);
        } else {
            for (int i = 0; i < numComponents; i++) {
                components[offset + i] = (int)(normComponents[normOffset + i] * maxValues[i] + 0.5f);
            }
        }
        return components;
    }
    public int getDataElement(float normComponents[], int normOffset) {
        int unnormComponents[] = getUnnormalizedComponents(normComponents, normOffset, null, 0);
        return getDataElement(unnormComponents, 0);
    }
    public int[] getComponents(int pixel, int components[], int offset) {
        throw new UnsupportedOperationException("This method is not " + 
                "supported by this ColorModel"); 
    }
    public abstract int getRed(int pixel);
    public int getRGB(int pixel) {
        return (getAlpha(pixel) << 24 | getRed(pixel) << 16 | getGreen(pixel) << 8 | getBlue(pixel));
    }
    public abstract int getGreen(int pixel);
    public int getComponentSize(int componentIdx) {
        if (bits == null) {
            throw new NullPointerException(Messages.getString("awt.26C")); 
        }
        if (componentIdx < 0 || componentIdx >= bits.length) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("awt.274")); 
        }
        return bits[componentIdx];
    }
    public abstract int getBlue(int pixel);
    public abstract int getAlpha(int pixel);
    public int[] getComponentSize() {
        if (bits != null) {
            return bits.clone();
        }
        return null;
    }
    public final boolean isAlphaPremultiplied() {
        return isAlphaPremultiplied;
    }
    public final boolean hasAlpha() {
        return hasAlpha;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        int tmp;
        if (hasAlpha) {
            hash ^= 1;
            hash <<= 8;
        }
        if (isAlphaPremultiplied) {
            hash ^= 1;
            hash <<= 8;
        }
        tmp = hash >>> 24;
        hash ^= numColorComponents;
        hash <<= 8;
        hash |= tmp;
        tmp = hash >>> 24;
        hash ^= transparency;
        hash <<= 8;
        hash |= tmp;
        tmp = hash >>> 24;
        hash ^= cs.getType();
        hash <<= 8;
        hash |= tmp;
        tmp = hash >>> 24;
        hash ^= pixel_bits;
        hash <<= 8;
        hash |= tmp;
        tmp = hash >>> 24;
        hash ^= transferType;
        hash <<= 8;
        hash |= tmp;
        if (bits != null) {
            for (int element : bits) {
                tmp = hash >>> 24;
                hash ^= element;
                hash <<= 8;
                hash |= tmp;
            }
        }
        return hash;
    }
    public int getTransparency() {
        return transparency;
    }
    public final int getTransferType() {
        return transferType;
    }
    public int getPixelSize() {
        return pixel_bits;
    }
    public int getNumComponents() {
        return numComponents;
    }
    public int getNumColorComponents() {
        return numColorComponents;
    }
    public static ColorModel getRGBdefault() {
        if (RGBdefault == null) {
            RGBdefault = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);
        }
        return RGBdefault;
    }
    private int constructPixel(Object obj) {
        int pixel = 0;
        switch (getTransferType()) {
            case DataBuffer.TYPE_BYTE:
                byte[] bPixel = (byte[])obj;
                if (bPixel.length > 1) {
                    throw new UnsupportedOperationException(Messages.getString("awt.275")); 
                }
                pixel = bPixel[0] & 0xff;
                break;
            case DataBuffer.TYPE_USHORT:
                short[] sPixel = (short[])obj;
                if (sPixel.length > 1) {
                    throw new UnsupportedOperationException(Messages.getString("awt.275")); 
                }
                pixel = sPixel[0] & 0xffff;
                break;
            case DataBuffer.TYPE_INT:
                int[] iPixel = (int[])obj;
                if (iPixel.length > 1) {
                    throw new UnsupportedOperationException(Messages.getString("awt.275")); 
                }
                pixel = iPixel[0];
                break;
            default:
                throw new UnsupportedOperationException(Messages.getString("awt.22D", 
                        transferType));
        }
        return pixel;
    }
    static int getTransferType(int bits) {
        if (bits <= 8) {
            return DataBuffer.TYPE_BYTE;
        } else if (bits <= 16) {
            return DataBuffer.TYPE_USHORT;
        } else if (bits <= 32) {
            return DataBuffer.TYPE_INT;
        } else {
            return DataBuffer.TYPE_UNDEFINED;
        }
    }
    @Override
    public void finalize() {
    }
}
