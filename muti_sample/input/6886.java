public final class SurfaceType {
    private static int unusedUID = 1;
    private static HashMap surfaceUIDMap = new HashMap(100);
    public static final String
        DESC_ANY            = "Any Surface";
    public static final String
        DESC_INT_RGB        = "Integer RGB";
    public static final String
        DESC_INT_ARGB       = "Integer ARGB";
    public static final String
        DESC_INT_ARGB_PRE   = "Integer ARGB Premultiplied";
    public static final String
        DESC_INT_BGR        = "Integer BGR";
    public static final String
        DESC_3BYTE_BGR      = "3 Byte BGR";
    public static final String
        DESC_4BYTE_ABGR     = "4 Byte ABGR";
    public static final String
        DESC_4BYTE_ABGR_PRE = "4 Byte ABGR Premultiplied";
    public static final String
        DESC_USHORT_565_RGB = "Short 565 RGB";
    public static final String
        DESC_USHORT_555_RGB = "Short 555 RGB";
    public static final String
        DESC_USHORT_555_RGBx= "Short 555 RGBx";
    public static final String
        DESC_USHORT_4444_ARGB= "Short 4444 ARGB";
    public static final String
        DESC_BYTE_GRAY      = "8-bit Gray";
    public static final String
        DESC_USHORT_INDEXED = "16-bit Indexed";
    public static final String
        DESC_USHORT_GRAY    = "16-bit Gray";
    public static final String
        DESC_BYTE_BINARY    = "Packed Binary Bitmap";
    public static final String
        DESC_BYTE_INDEXED   = "8-bit Indexed";
    public static final String DESC_ANY_INT = "Any Discrete Integer";
    public static final String DESC_ANY_SHORT = "Any Discrete Short";
    public static final String DESC_ANY_BYTE = "Any Discrete Byte";
    public static final String DESC_ANY_3BYTE = "Any 3 Byte Component";
    public static final String DESC_ANY_4BYTE = "Any 4 Byte Component";
    public static final String DESC_ANY_INT_DCM = "Any Integer DCM";
    public static final String DESC_INT_RGBx = "Integer RGBx";
    public static final String DESC_INT_BGRx = "Integer BGRx";
    public static final String DESC_3BYTE_RGB = "3 Byte RGB";
    public static final String DESC_INT_ARGB_BM     = "Int ARGB (Bitmask)";
    public static final String DESC_BYTE_INDEXED_BM = "8-bit Indexed (Bitmask)";
    public static final String
        DESC_BYTE_INDEXED_OPAQUE = "8-bit Indexed (Opaque)";
    public static final String DESC_INDEX8_GRAY  = "8-bit Palettized Gray";
    public static final String DESC_INDEX12_GRAY = "12-bit Palettized Gray";
    public static final String
        DESC_BYTE_BINARY_1BIT = "Packed Binary 1-bit Bitmap";
    public static final String
        DESC_BYTE_BINARY_2BIT = "Packed Binary 2-bit Bitmap";
    public static final String
        DESC_BYTE_BINARY_4BIT = "Packed Binary 4-bit Bitmap";
    public static final String DESC_ANY_PAINT      = "Paint Object";
    public static final String DESC_ANY_COLOR      = "Single Color";
    public static final String DESC_OPAQUE_COLOR   = "Opaque Color";
    public static final String
        DESC_GRADIENT_PAINT        = "Gradient Paint";
    public static final String
        DESC_OPAQUE_GRADIENT_PAINT = "Opaque Gradient Paint";
    public static final String
        DESC_TEXTURE_PAINT         = "Texture Paint";
    public static final String
        DESC_OPAQUE_TEXTURE_PAINT  = "Opaque Texture Paint";
    public static final String
        DESC_LINEAR_GRADIENT_PAINT        = "Linear Gradient Paint";
    public static final String
        DESC_OPAQUE_LINEAR_GRADIENT_PAINT = "Opaque Linear Gradient Paint";
    public static final String
        DESC_RADIAL_GRADIENT_PAINT        = "Radial Gradient Paint";
    public static final String
        DESC_OPAQUE_RADIAL_GRADIENT_PAINT = "Opaque Radial Gradient Paint";
    public static final SurfaceType Any =
        new SurfaceType(null, DESC_ANY, PixelConverter.instance);
    public static final SurfaceType
        AnyInt            = Any.deriveSubType(DESC_ANY_INT);
    public static final SurfaceType
        AnyShort          = Any.deriveSubType(DESC_ANY_SHORT);
    public static final SurfaceType
        AnyByte           = Any.deriveSubType(DESC_ANY_BYTE);
    public static final SurfaceType
        AnyByteBinary     = Any.deriveSubType(DESC_BYTE_BINARY);
    public static final SurfaceType
        Any3Byte          = Any.deriveSubType(DESC_ANY_3BYTE);
    public static final SurfaceType
        Any4Byte          = Any.deriveSubType(DESC_ANY_4BYTE);
    public static final SurfaceType
        AnyDcm            = AnyInt.deriveSubType(DESC_ANY_INT_DCM);
    public static final SurfaceType
        Custom            = Any;
    public static final SurfaceType IntRgb =
        AnyDcm.deriveSubType(DESC_INT_RGB, PixelConverter.Xrgb.instance);
    public static final SurfaceType IntArgb =
        AnyDcm.deriveSubType(DESC_INT_ARGB, PixelConverter.Argb.instance);
    public static final SurfaceType IntArgbPre =
        AnyDcm.deriveSubType(DESC_INT_ARGB_PRE,
                             PixelConverter.ArgbPre.instance);
    public static final SurfaceType IntBgr =
        AnyDcm.deriveSubType(DESC_INT_BGR, PixelConverter.Xbgr.instance);
    public static final SurfaceType ThreeByteBgr =
        Any3Byte.deriveSubType(DESC_3BYTE_BGR, PixelConverter.Xrgb.instance);
    public static final SurfaceType FourByteAbgr =
        Any4Byte.deriveSubType(DESC_4BYTE_ABGR, PixelConverter.Rgba.instance);
    public static final SurfaceType FourByteAbgrPre =
        Any4Byte.deriveSubType(DESC_4BYTE_ABGR_PRE,
                               PixelConverter.RgbaPre.instance);
    public static final SurfaceType Ushort565Rgb =
        AnyShort.deriveSubType(DESC_USHORT_565_RGB,
                               PixelConverter.Ushort565Rgb.instance);
    public static final SurfaceType Ushort555Rgb =
        AnyShort.deriveSubType(DESC_USHORT_555_RGB,
                               PixelConverter.Ushort555Rgb.instance);
    public static final SurfaceType Ushort555Rgbx =
        AnyShort.deriveSubType(DESC_USHORT_555_RGBx,
                               PixelConverter.Ushort555Rgbx.instance);
    public static final SurfaceType Ushort4444Argb =
        AnyShort.deriveSubType(DESC_USHORT_4444_ARGB,
                               PixelConverter.Ushort4444Argb.instance);
    public static final SurfaceType UshortIndexed =
        AnyShort.deriveSubType(DESC_USHORT_INDEXED);
    public static final SurfaceType ByteGray =
        AnyByte.deriveSubType(DESC_BYTE_GRAY,
                              PixelConverter.ByteGray.instance);
    public static final SurfaceType UshortGray =
        AnyShort.deriveSubType(DESC_USHORT_GRAY,
                               PixelConverter.UshortGray.instance);
    public static final SurfaceType ByteBinary1Bit =
        AnyByteBinary.deriveSubType(DESC_BYTE_BINARY_1BIT);
    public static final SurfaceType ByteBinary2Bit =
        AnyByteBinary.deriveSubType(DESC_BYTE_BINARY_2BIT);
    public static final SurfaceType ByteBinary4Bit =
        AnyByteBinary.deriveSubType(DESC_BYTE_BINARY_4BIT);
    public static final SurfaceType ByteIndexed =
        AnyByte.deriveSubType(DESC_BYTE_INDEXED);
    public static final SurfaceType IntRgbx =
        AnyDcm.deriveSubType(DESC_INT_RGBx, PixelConverter.Rgbx.instance);
    public static final SurfaceType IntBgrx =
        AnyDcm.deriveSubType(DESC_INT_BGRx, PixelConverter.Bgrx.instance);
    public static final SurfaceType ThreeByteRgb =
        Any3Byte.deriveSubType(DESC_3BYTE_RGB, PixelConverter.Xbgr.instance);
    public static final SurfaceType IntArgbBm =
        AnyDcm.deriveSubType(DESC_INT_ARGB_BM, PixelConverter.ArgbBm.instance);
    public static final SurfaceType ByteIndexedBm =
        ByteIndexed.deriveSubType(DESC_BYTE_INDEXED_BM);
    public static final SurfaceType ByteIndexedOpaque =
        ByteIndexedBm.deriveSubType(DESC_BYTE_INDEXED_OPAQUE);
    public static final SurfaceType Index8Gray =
        ByteIndexedOpaque.deriveSubType(DESC_INDEX8_GRAY);
    public static final SurfaceType Index12Gray =
        Any.deriveSubType(DESC_INDEX12_GRAY);
    public static final SurfaceType AnyPaint =
        Any.deriveSubType(DESC_ANY_PAINT);
    public static final SurfaceType AnyColor =
        AnyPaint.deriveSubType(DESC_ANY_COLOR);
    public static final SurfaceType OpaqueColor =
        AnyColor.deriveSubType(DESC_OPAQUE_COLOR);
    public static final SurfaceType GradientPaint =
        AnyPaint.deriveSubType(DESC_GRADIENT_PAINT);
    public static final SurfaceType OpaqueGradientPaint =
        GradientPaint.deriveSubType(DESC_OPAQUE_GRADIENT_PAINT);
    public static final SurfaceType LinearGradientPaint =
        AnyPaint.deriveSubType(DESC_LINEAR_GRADIENT_PAINT);
    public static final SurfaceType OpaqueLinearGradientPaint =
        LinearGradientPaint.deriveSubType(DESC_OPAQUE_LINEAR_GRADIENT_PAINT);
    public static final SurfaceType RadialGradientPaint =
        AnyPaint.deriveSubType(DESC_RADIAL_GRADIENT_PAINT);
    public static final SurfaceType OpaqueRadialGradientPaint =
        RadialGradientPaint.deriveSubType(DESC_OPAQUE_RADIAL_GRADIENT_PAINT);
    public static final SurfaceType TexturePaint =
        AnyPaint.deriveSubType(DESC_TEXTURE_PAINT);
    public static final SurfaceType OpaqueTexturePaint =
        TexturePaint.deriveSubType(DESC_OPAQUE_TEXTURE_PAINT);
    public SurfaceType deriveSubType(String desc) {
        return new SurfaceType(this, desc);
    }
    public SurfaceType deriveSubType(String desc,
                                     PixelConverter pixelConverter) {
        return new SurfaceType(this, desc, pixelConverter);
    }
    private int uniqueID;
    private String desc;
    private SurfaceType next;
    protected PixelConverter pixelConverter;
    private SurfaceType(SurfaceType parent, String desc,
                        PixelConverter pixelConverter) {
        next = parent;
        this.desc = desc;
        this.uniqueID = makeUniqueID(desc);
        this.pixelConverter = pixelConverter;
    }
    private SurfaceType(SurfaceType parent, String desc) {
        next = parent;
        this.desc = desc;
        this.uniqueID = makeUniqueID(desc);
        this.pixelConverter = parent.pixelConverter;
    }
    public synchronized static final int makeUniqueID(String desc) {
        Integer i = (Integer) surfaceUIDMap.get((Object) desc);
        if (i == null) {
            if (unusedUID > 255) {
                throw new InternalError("surface type id overflow");
            }
            i = Integer.valueOf(unusedUID++);
            surfaceUIDMap.put(desc, i);
        }
        return i.intValue();
    }
    public int getUniqueID() {
        return uniqueID;
    }
    public String getDescriptor() {
        return desc;
    }
    public SurfaceType getSuperType() {
        return next;
    }
    public PixelConverter getPixelConverter() {
        return pixelConverter;
    }
    public int pixelFor(int rgb, ColorModel cm) {
        return pixelConverter.rgbToPixel(rgb, cm);
    }
    public int rgbFor(int pixel, ColorModel cm) {
        return pixelConverter.pixelToRgb(pixel, cm);
    }
    public int getAlphaMask() {
        return pixelConverter.getAlphaMask();
    }
    public int hashCode() {
        return desc.hashCode();
    }
    public boolean equals(Object o) {
        if (o instanceof SurfaceType) {
            return (((SurfaceType) o).uniqueID == this.uniqueID);
        }
        return false;
    }
    public String toString() {
        return desc;
    }
}
