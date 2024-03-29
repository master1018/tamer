public class PNGMetadataFormat extends IIOMetadataFormatImpl {
    private static IIOMetadataFormat instance = null;
    private static String VALUE_0 = "0";
    private static String VALUE_1 = "1";
    private static String VALUE_12 = "12";
    private static String VALUE_23 = "23";
    private static String VALUE_31 = "31";
    private static String VALUE_59 = "59";
    private static String VALUE_60 = "60";
    private static String VALUE_255 = "255";
    private static String VALUE_MAX_16 = "65535"; 
    private static String VALUE_MAX_32 = "2147483647"; 
    private PNGMetadataFormat() {
        super(PNGMetadata.nativeMetadataFormatName,
              CHILD_POLICY_SOME);
        addElement("IHDR", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("IHDR", "width",
                     DATATYPE_INTEGER, true, null,
                     VALUE_1, VALUE_MAX_32, true, true);
        addAttribute("IHDR", "height",
                     DATATYPE_INTEGER, true, null,
                     VALUE_1, VALUE_MAX_32, true, true);
        addAttribute("IHDR", "bitDepth",
                     DATATYPE_INTEGER, true, null,
                     Arrays.asList(PNGMetadata.IHDR_bitDepths));
        String[] colorTypes = {
            "Grayscale", "RGB", "Palette", "GrayAlpha", "RGBAlpha"
        };
        addAttribute("IHDR", "colorType",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(colorTypes));
        addAttribute("IHDR", "compressionMethod",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.IHDR_compressionMethodNames));
        addAttribute("IHDR", "filterMethod",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.IHDR_filterMethodNames));
        addAttribute("IHDR", "interlaceMethod",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.IHDR_interlaceMethodNames));
        addElement("PLTE", PNGMetadata.nativeMetadataFormatName,
                   1, 256);
        addElement("PLTEEntry", "PLTE",
                   CHILD_POLICY_EMPTY);
        addAttribute("PLTEEntry", "index",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("PLTEEntry", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("PLTEEntry", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("PLTEEntry", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("bKGD", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_CHOICE);
        addElement("bKGD_Grayscale", "bKGD",
                   CHILD_POLICY_EMPTY);
        addAttribute("bKGD_Grayscale", "gray",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("bKGD_RGB", "bKGD",
                   CHILD_POLICY_EMPTY);
        addAttribute("bKGD_RGB", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("bKGD_RGB", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("bKGD_RGB", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("bKGD_Palette", "bKGD",
                   CHILD_POLICY_EMPTY);
        addAttribute("bKGD_Palette", "index",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("cHRM", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("cHRM", "whitePointX",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "whitePointY",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "redX",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "redY",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "greenX",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "greenY",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "blueX",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("cHRM", "blueY",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("gAMA", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("gAMA", "value",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_32, true, true);
        addElement("hIST", PNGMetadata.nativeMetadataFormatName,
                   1, 256);
        addElement("hISTEntry", "hIST",
                   CHILD_POLICY_EMPTY);
        addAttribute("hISTEntry", "index",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("hISTEntry", "value",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("iCCP", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("iCCP", "profileName",
                     DATATYPE_STRING, true, null);
        addAttribute("iCCP", "compressionMethod",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.iCCP_compressionMethodNames));
        addObjectValue("iCCP", byte.class, 0, Integer.MAX_VALUE);
        addElement("iTXt", PNGMetadata.nativeMetadataFormatName,
                   1, Integer.MAX_VALUE);
        addElement("iTXtEntry", "iTXt",
                   CHILD_POLICY_EMPTY);
        addAttribute("iTXtEntry", "keyword",
                     DATATYPE_STRING, true, null);
        addBooleanAttribute("iTXtEntry", "compressionFlag",
                            false, false);
        addAttribute("iTXtEntry", "compressionMethod",
                     DATATYPE_STRING, true, null);
        addAttribute("iTXtEntry", "languageTag",
                     DATATYPE_STRING, true, null);
        addAttribute("iTXtEntry", "translatedKeyword",
                     DATATYPE_STRING, true, null);
        addAttribute("iTXtEntry", "text",
                     DATATYPE_STRING, true, null);
        addElement("pHYS", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("pHYS", "pixelsPerUnitXAxis",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_32, true, true);
        addAttribute("pHYS", "pixelsPerUnitYAxis",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_32, true, true);
        addAttribute("pHYS", "unitSpecifier",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.unitSpecifierNames));
        addElement("sBIT", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_CHOICE);
        addElement("sBIT_Grayscale", "sBIT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sBIT_Grayscale", "gray",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sBIT_GrayAlpha", "sBIT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sBIT_GrayAlpha", "gray",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_GrayAlpha", "alpha",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sBIT_RGB", "sBIT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sBIT_RGB", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_RGB", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_RGB", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sBIT_RGBAlpha", "sBIT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sBIT_RGBAlpha", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_RGBAlpha", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_RGBAlpha", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_RGBAlpha", "alpha",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sBIT_Palette", "sBIT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sBIT_Palette", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_Palette", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sBIT_Palette", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sPLT", PNGMetadata.nativeMetadataFormatName,
                   1, 256);
        addElement("sPLTEntry", "sPLT",
                   CHILD_POLICY_EMPTY);
        addAttribute("sPLTEntry", "index",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sPLTEntry", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sPLTEntry", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sPLTEntry", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("sPLTEntry", "alpha",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("sRGB", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("sRGB", "renderingIntent",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.renderingIntentNames));
        addElement("tEXt", PNGMetadata.nativeMetadataFormatName,
                   1, Integer.MAX_VALUE);
        addElement("tEXtEntry", "tEXt",
                   CHILD_POLICY_EMPTY);
        addAttribute("tEXtEntry", "keyword",
                     DATATYPE_STRING, true, null);
        addAttribute("tEXtEntry", "value",
                     DATATYPE_STRING, true, null);
        addElement("tIME", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_EMPTY);
        addAttribute("tIME", "year",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("tIME", "month",
                     DATATYPE_INTEGER, true, null,
                     VALUE_1, VALUE_12, true, true);
        addAttribute("tIME", "day",
                     DATATYPE_INTEGER, true, null,
                     VALUE_1, VALUE_31, true, true);
        addAttribute("tIME", "hour",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_23, true, true);
        addAttribute("tIME", "minute",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_59, true, true);
        addAttribute("tIME", "second",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_60, true, true);
        addElement("tRNS", PNGMetadata.nativeMetadataFormatName,
                   CHILD_POLICY_CHOICE);
        addElement("tRNS_Grayscale", "tRNS",
                   CHILD_POLICY_EMPTY);
        addAttribute("tRNS_Grayscale", "gray",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("tRNS_RGB", "tRNS",
                   CHILD_POLICY_EMPTY);
        addAttribute("tRNS_RGB", "red",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("tRNS_RGB", "green",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addAttribute("tRNS_RGB", "blue",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_MAX_16, true, true);
        addElement("tRNS_Palette", "tRNS",
                   CHILD_POLICY_EMPTY);
        addAttribute("tRNS_Palette", "index",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addAttribute("tRNS_Palette", "alpha",
                     DATATYPE_INTEGER, true, null,
                     VALUE_0, VALUE_255, true, true);
        addElement("zTXt", PNGMetadata.nativeMetadataFormatName,
                   1, Integer.MAX_VALUE);
        addElement("zTXtEntry", "zTXt",
                   CHILD_POLICY_EMPTY);
        addAttribute("zTXtEntry", "keyword",
                     DATATYPE_STRING, true, null);
        addAttribute("zTXtEntry", "compressionMethod",
                     DATATYPE_STRING, true, null,
                     Arrays.asList(PNGMetadata.zTXt_compressionMethodNames));
        addAttribute("zTXtEntry", "text",
                     DATATYPE_STRING, true, null);
        addElement("UnknownChunks", PNGMetadata.nativeMetadataFormatName,
                   1, Integer.MAX_VALUE);
        addElement("UnknownChunk", "UnknownChunks",
                   CHILD_POLICY_EMPTY);
        addAttribute("UnknownChunk", "type",
                     DATATYPE_STRING, true, null);
        addObjectValue("UnknownChunk", byte.class, 0, Integer.MAX_VALUE);
    }
    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
        return true;
    }
    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new PNGMetadataFormat();
        }
        return instance;
    }
}
