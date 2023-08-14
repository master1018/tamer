public class StandardMetadataFormat extends IIOMetadataFormatImpl {
    private void addSingleAttributeElement(String elementName,
                                           String parentName,
                                           int dataType) {
        addElement(elementName, parentName, CHILD_POLICY_EMPTY);
        addAttribute(elementName, "value", dataType, true, null);
    }
    public StandardMetadataFormat() {
        super(standardMetadataFormatName, CHILD_POLICY_SOME);
        List values;
        addElement("Chroma", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addElement("ColorSpaceType", "Chroma",
                   CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("XYZ");
        values.add("Lab");
        values.add("Luv");
        values.add("YCbCr");
        values.add("Yxy");
        values.add("YCCK");
        values.add("PhotoYCC");
        values.add("RGB");
        values.add("GRAY");
        values.add("HSV");
        values.add("HLS");
        values.add("CMYK");
        values.add("CMY");
        values.add("2CLR");
        values.add("3CLR");
        values.add("4CLR");
        values.add("5CLR");
        values.add("6CLR");
        values.add("7CLR");
        values.add("8CLR");
        values.add("9CLR");
        values.add("ACLR");
        values.add("BCLR");
        values.add("CCLR");
        values.add("DCLR");
        values.add("ECLR");
        values.add("FCLR");
        addAttribute("ColorSpaceType",
                     "name",
                     DATATYPE_STRING,
                     true,
                     null,
                     values);
        addElement("NumChannels", "Chroma",
                   CHILD_POLICY_EMPTY);
        addAttribute("NumChannels", "value",
                     DATATYPE_INTEGER,
                     true,
                     0, Integer.MAX_VALUE);
        addElement("Gamma", "Chroma", CHILD_POLICY_EMPTY);
        addAttribute("Gamma", "value",
                     DATATYPE_FLOAT, true, null);
        addElement("BlackIsZero", "Chroma", CHILD_POLICY_EMPTY);
        addBooleanAttribute("BlackIsZero", "value", true, true);
        addElement("Palette", "Chroma", 0, Integer.MAX_VALUE);
        addElement("PaletteEntry", "Palette", CHILD_POLICY_EMPTY);
        addAttribute("PaletteEntry", "index", DATATYPE_INTEGER,
                     true, null);
        addAttribute("PaletteEntry", "red", DATATYPE_INTEGER,
                     true, null);
        addAttribute("PaletteEntry", "green", DATATYPE_INTEGER,
                     true, null);
        addAttribute("PaletteEntry", "blue", DATATYPE_INTEGER,
                     true, null);
        addAttribute("PaletteEntry", "alpha", DATATYPE_INTEGER,
                     false, "255");
        addElement("BackgroundIndex", "Chroma", CHILD_POLICY_EMPTY);
        addAttribute("BackgroundIndex", "value", DATATYPE_INTEGER,
                     true, null);
        addElement("BackgroundColor", "Chroma", CHILD_POLICY_EMPTY);
        addAttribute("BackgroundColor", "red", DATATYPE_INTEGER,
                     true, null);
        addAttribute("BackgroundColor", "green", DATATYPE_INTEGER,
                     true, null);
        addAttribute("BackgroundColor", "blue", DATATYPE_INTEGER,
                     true, null);
        addElement("Compression", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addSingleAttributeElement("CompressionTypeName",
                                  "Compression",
                                  DATATYPE_STRING);
        addElement("Lossless", "Compression", CHILD_POLICY_EMPTY);
        addBooleanAttribute("Lossless", "value", true, true);
        addSingleAttributeElement("NumProgressiveScans",
                                  "Compression",
                                  DATATYPE_INTEGER);
        addSingleAttributeElement("BitRate",
                                  "Compression",
                                  DATATYPE_FLOAT);
        addElement("Data", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addElement("PlanarConfiguration", "Data", CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("PixelInterleaved");
        values.add("PlaneInterleaved");
        values.add("LineInterleaved");
        values.add("TileInterleaved");
        addAttribute("PlanarConfiguration", "value",
                     DATATYPE_STRING,
                     true,
                     null,
                     values);
        addElement("SampleFormat", "Data", CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("SignedIntegral");
        values.add("UnsignedIntegral");
        values.add("Real");
        values.add("Index");
        addAttribute("SampleFormat", "value",
                     DATATYPE_STRING,
                     true,
                     null,
                     values);
        addElement("BitsPerSample", "Data",
                   CHILD_POLICY_EMPTY);
        addAttribute("BitsPerSample", "value",
                     DATATYPE_INTEGER,
                     true,
                     1, Integer.MAX_VALUE);
        addElement("SignificantBitsPerSample", "Data", CHILD_POLICY_EMPTY);
        addAttribute("SignificantBitsPerSample", "value",
                     DATATYPE_INTEGER,
                     true,
                     1, Integer.MAX_VALUE);
        addElement("SampleMSB", "Data",
                   CHILD_POLICY_EMPTY);
        addAttribute("SampleMSB", "value",
                     DATATYPE_INTEGER,
                     true,
                     1, Integer.MAX_VALUE);
        addElement("Dimension", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addSingleAttributeElement("PixelAspectRatio",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addElement("ImageOrientation", "Dimension",
                   CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("Normal");
        values.add("Rotate90");
        values.add("Rotate180");
        values.add("Rotate270");
        values.add("FlipH");
        values.add("FlipV");
        values.add("FlipHRotate90");
        values.add("FlipVRotate90");
        addAttribute("ImageOrientation", "value",
                     DATATYPE_STRING,
                     true,
                     null,
                     values);
        addSingleAttributeElement("HorizontalPixelSize",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("VerticalPixelSize",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("HorizontalPhysicalPixelSpacing",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("VerticalPhysicalPixelSpacing",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("HorizontalPosition",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("VerticalPosition",
                                  "Dimension",
                                  DATATYPE_FLOAT);
        addSingleAttributeElement("HorizontalPixelOffset",
                                  "Dimension",
                                  DATATYPE_INTEGER);
        addSingleAttributeElement("VerticalPixelOffset",
                                  "Dimension",
                                  DATATYPE_INTEGER);
        addSingleAttributeElement("HorizontalScreenSize",
                                  "Dimension",
                                  DATATYPE_INTEGER);
        addSingleAttributeElement("VerticalScreenSize",
                                  "Dimension",
                                  DATATYPE_INTEGER);
        addElement("Document", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addElement("FormatVersion", "Document",
                   CHILD_POLICY_EMPTY);
        addAttribute("FormatVersion", "value",
                     DATATYPE_STRING,
                     true,
                     null);
        addElement("SubimageInterpretation", "Document",
                   CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("Standalone");
        values.add("SinglePage");
        values.add("FullResolution");
        values.add("ReducedResolution");
        values.add("PyramidLayer");
        values.add("Preview");
        values.add("VolumeSlice");
        values.add("ObjectView");
        values.add("Panorama");
        values.add("AnimationFrame");
        values.add("TransparencyMask");
        values.add("CompositingLayer");
        values.add("SpectralSlice");
        values.add("Unknown");
        addAttribute("SubimageInterpretation", "value",
                     DATATYPE_STRING,
                     true,
                     null,
                     values);
        addElement("ImageCreationTime", "Document",
                   CHILD_POLICY_EMPTY);
        addAttribute("ImageCreationTime", "year",
                     DATATYPE_INTEGER,
                     true,
                     null);
        addAttribute("ImageCreationTime", "month",
                     DATATYPE_INTEGER,
                     true,
                     null,
                     "1", "12", true, true);
        addAttribute("ImageCreationTime", "day",
                     DATATYPE_INTEGER,
                     true,
                     null,
                     "1", "31", true, true);
        addAttribute("ImageCreationTime", "hour",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "23", true, true);
        addAttribute("ImageCreationTime", "minute",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "59", true, true);
        addAttribute("ImageCreationTime", "second",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "60", true, true);
        addElement("ImageModificationTime", "Document",
                   CHILD_POLICY_EMPTY);
        addAttribute("ImageModificationTime", "year",
                     DATATYPE_INTEGER,
                     true,
                     null);
        addAttribute("ImageModificationTime", "month",
                     DATATYPE_INTEGER,
                     true,
                     null,
                     "1", "12", true, true);
        addAttribute("ImageModificationTime", "day",
                     DATATYPE_INTEGER,
                     true,
                     null,
                     "1", "31", true, true);
        addAttribute("ImageModificationTime", "hour",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "23", true, true);
        addAttribute("ImageModificationTime", "minute",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "59", true, true);
        addAttribute("ImageModificationTime", "second",
                     DATATYPE_INTEGER,
                     false,
                     "0",
                     "0", "60", true, true);
        addElement("Text", standardMetadataFormatName,
                   0, Integer.MAX_VALUE);
        addElement("TextEntry", "Text", CHILD_POLICY_EMPTY);
        addAttribute("TextEntry", "keyword",
                     DATATYPE_STRING,
                     false,
                     null);
        addAttribute("TextEntry", "value",
                     DATATYPE_STRING,
                     true,
                     null);
        addAttribute("TextEntry", "language",
                     DATATYPE_STRING,
                     false,
                     null);
        addAttribute("TextEntry", "encoding",
                     DATATYPE_STRING,
                     false,
                     null);
        values = new ArrayList();
        values.add("none");
        values.add("lzw");
        values.add("zip");
        values.add("bzip");
        values.add("other");
        addAttribute("TextEntry", "compression",
                     DATATYPE_STRING,
                     false,
                     "none",
                     values);
        addElement("Transparency", standardMetadataFormatName,
                   CHILD_POLICY_SOME);
        addElement("Alpha", "Transparency", CHILD_POLICY_EMPTY);
        values = new ArrayList();
        values.add("none");
        values.add("premultiplied");
        values.add("nonpremultiplied");
        addAttribute("Alpha", "value",
                     DATATYPE_STRING,
                     false,
                     "none",
                     values);
        addSingleAttributeElement("TransparentIndex", "Transparency",
                                  DATATYPE_INTEGER);
        addElement("TransparentColor", "Transparency",
                   CHILD_POLICY_EMPTY);
        addAttribute("TransparentColor", "value",
                     DATATYPE_INTEGER,
                     true,
                     0, Integer.MAX_VALUE);
        addElement("TileTransparencies", "Transparency",
                   0, Integer.MAX_VALUE);
        addElement("TransparentTile", "TileTransparencies",
                   CHILD_POLICY_EMPTY);
        addAttribute("TransparentTile", "x",
                     DATATYPE_INTEGER,
                     true,
                     null);
        addAttribute("TransparentTile", "y",
                     DATATYPE_INTEGER,
                     true,
                     null);
        addElement("TileOpacities", "Transparency",
                   0, Integer.MAX_VALUE);
        addElement("OpaqueTile", "TileOpacities",
                   CHILD_POLICY_EMPTY);
        addAttribute("OpaqueTile", "x",
                     DATATYPE_INTEGER,
                     true,
                     null);
        addAttribute("OpaqueTile", "y",
                     DATATYPE_INTEGER,
                     true,
                     null);
    }
    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
            return true;
    }
}
