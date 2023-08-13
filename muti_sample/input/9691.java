abstract class CMap {
    static final short ShiftJISEncoding = 2;
    static final short GBKEncoding      = 3;
    static final short Big5Encoding     = 4;
    static final short WansungEncoding  = 5;
    static final short JohabEncoding    = 6;
    static final short MSUnicodeSurrogateEncoding = 10;
    static final char noSuchChar = (char)0xfffd;
    static final int SHORTMASK = 0x0000ffff;
    static final int INTMASK   = 0xffffffff;
    static final char[][] converterMaps = new char[7][];
    char[] xlat;
    static CMap initialize(TrueTypeFont font) {
        CMap cmap = null;
        int offset, platformID, encodingID=-1;
        int three0=0, three1=0, three2=0, three3=0, three4=0, three5=0,
            three6=0, three10=0;
        boolean threeStar = false;
        ByteBuffer cmapBuffer = font.getTableBuffer(TrueTypeFont.cmapTag);
        int cmapTableOffset = font.getTableSize(TrueTypeFont.cmapTag);
        short numberSubTables = cmapBuffer.getShort(2);
        for (int i=0; i<numberSubTables; i++) {
            cmapBuffer.position(i * 8 + 4);
            platformID = cmapBuffer.getShort();
            if (platformID == 3) {
                threeStar = true;
                encodingID = cmapBuffer.getShort();
                offset     = cmapBuffer.getInt();
                switch (encodingID) {
                case 0:  three0  = offset; break; 
                case 1:  three1  = offset; break; 
                case 2:  three2  = offset; break; 
                case 3:  three3  = offset; break; 
                case 4:  three4  = offset; break; 
                case 5:  three5  = offset; break; 
                case 6:  three6  = offset; break; 
                case 10: three10 = offset; break; 
                }
            }
        }
        if (threeStar) {
            if (three10 != 0) {
                cmap = createCMap(cmapBuffer, three10, null);
            }
            else if  (three0 != 0) {
                    cmap = createCMap(cmapBuffer, three0, null);
            }
            else if (three1 != 0) {
                cmap = createCMap(cmapBuffer, three1, null);
            }
            else if (three2 != 0) {
                cmap = createCMap(cmapBuffer, three2,
                                  getConverterMap(ShiftJISEncoding));
            }
            else if (three3 != 0) {
                cmap = createCMap(cmapBuffer, three3,
                                  getConverterMap(GBKEncoding));
            }
            else if (three4 != 0) {
                if (FontUtilities.isSolaris && font.platName != null &&
                    (font.platName.startsWith(
                     "/usr/openwin/lib/locale/zh_CN.EUC/X11/fonts/TrueType") ||
                     font.platName.startsWith(
                     "/usr/openwin/lib/locale/zh_CN/X11/fonts/TrueType") ||
                     font.platName.startsWith(
                     "/usr/openwin/lib/locale/zh/X11/fonts/TrueType"))) {
                    cmap = createCMap(cmapBuffer, three4,
                                       getConverterMap(GBKEncoding));
                }
                else {
                    cmap = createCMap(cmapBuffer, three4,
                                      getConverterMap(Big5Encoding));
                }
            }
            else if (three5 != 0) {
                cmap = createCMap(cmapBuffer, three5,
                                  getConverterMap(WansungEncoding));
            }
            else if (three6 != 0) {
                cmap = createCMap(cmapBuffer, three6,
                                  getConverterMap(JohabEncoding));
            }
        } else {
            cmap = createCMap(cmapBuffer, cmapBuffer.getInt(8), null);
        }
        return cmap;
    }
    static char[] getConverter(short encodingID) {
        int dBegin = 0x8000;
        int dEnd   = 0xffff;
        String encoding;
        switch (encodingID) {
        case ShiftJISEncoding:
            dBegin = 0x8140;
            dEnd   = 0xfcfc;
            encoding = "SJIS";
            break;
        case GBKEncoding:
            dBegin = 0x8140;
            dEnd   = 0xfea0;
            encoding = "GBK";
            break;
        case Big5Encoding:
            dBegin = 0xa140;
            dEnd   = 0xfefe;
            encoding = "Big5";
            break;
        case WansungEncoding:
            dBegin = 0xa1a1;
            dEnd   = 0xfede;
            encoding = "EUC_KR";
            break;
        case JohabEncoding:
            dBegin = 0x8141;
            dEnd   = 0xfdfe;
            encoding = "Johab";
            break;
        default:
            return null;
        }
        try {
            char[] convertedChars = new char[65536];
            for (int i=0; i<65536; i++) {
                convertedChars[i] = noSuchChar;
            }
            byte[] inputBytes = new byte[(dEnd-dBegin+1)*2];
            char[] outputChars = new char[(dEnd-dBegin+1)];
            int j = 0;
            int firstByte;
            if (encodingID == ShiftJISEncoding) {
                for (int i = dBegin; i <= dEnd; i++) {
                    firstByte = (i >> 8 & 0xff);
                    if (firstByte >= 0xa1 && firstByte <= 0xdf) {
                        inputBytes[j++] = (byte)0xff;
                        inputBytes[j++] = (byte)0xff;
                    } else {
                        inputBytes[j++] = (byte)firstByte;
                        inputBytes[j++] = (byte)(i & 0xff);
                    }
                }
            } else {
                for (int i = dBegin; i <= dEnd; i++) {
                    inputBytes[j++] = (byte)(i>>8 & 0xff);
                    inputBytes[j++] = (byte)(i & 0xff);
                }
            }
            Charset.forName(encoding).newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .replaceWith("\u0000")
            .decode(ByteBuffer.wrap(inputBytes, 0, inputBytes.length),
                    CharBuffer.wrap(outputChars, 0, outputChars.length),
                    true);
            for (int i = 0x20; i <= 0x7e; i++) {
                convertedChars[i] = (char)i;
            }
            if (encodingID == ShiftJISEncoding) {
                for (int i = 0xa1; i <= 0xdf; i++) {
                    convertedChars[i] = (char)(i - 0xa1 + 0xff61);
                }
            }
            System.arraycopy(outputChars, 0, convertedChars, dBegin,
                             outputChars.length);
            char [] invertedChars = new char[65536];
            for (int i=0;i<65536;i++) {
                if (convertedChars[i] != noSuchChar) {
                    invertedChars[convertedChars[i]] = (char)i;
                }
            }
            return invertedChars;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    static char[] getConverterMap(short encodingID) {
        if (converterMaps[encodingID] == null) {
           converterMaps[encodingID] = getConverter(encodingID);
        }
        return converterMaps[encodingID];
    }
    static CMap createCMap(ByteBuffer buffer, int offset, char[] xlat) {
        int subtableFormat = buffer.getChar(offset);
        long subtableLength;
        if (subtableFormat < 8) {
            subtableLength = buffer.getChar(offset+2);
        } else {
            subtableLength = buffer.getInt(offset+4) & INTMASK;
        }
        if (offset+subtableLength > buffer.capacity()) {
            if (FontUtilities.isLogging()) {
                FontUtilities.getLogger().warning("Cmap subtable overflows buffer.");
            }
        }
        switch (subtableFormat) {
        case 0:  return new CMapFormat0(buffer, offset);
        case 2:  return new CMapFormat2(buffer, offset, xlat);
        case 4:  return new CMapFormat4(buffer, offset, xlat);
        case 6:  return new CMapFormat6(buffer, offset, xlat);
        case 8:  return new CMapFormat8(buffer, offset, xlat);
        case 10: return new CMapFormat10(buffer, offset, xlat);
        case 12: return new CMapFormat12(buffer, offset, xlat);
        default: throw new RuntimeException("Cmap format unimplemented: " +
                                            (int)buffer.getChar(offset));
        }
    }
    abstract char getGlyph(int charCode);
    static class CMapFormat4 extends CMap {
        int segCount;
        int entrySelector;
        int rangeShift;
        char[] endCount;
        char[] startCount;
        short[] idDelta;
        char[] idRangeOffset;
        char[] glyphIds;
        CMapFormat4(ByteBuffer bbuffer, int offset, char[] xlat) {
            this.xlat = xlat;
            bbuffer.position(offset);
            CharBuffer buffer = bbuffer.asCharBuffer();
            buffer.get(); 
            int subtableLength = buffer.get();
            if (offset+subtableLength > bbuffer.capacity()) {
                subtableLength = bbuffer.capacity() - offset;
            }
            buffer.get(); 
            segCount = buffer.get()/2;
            int searchRange = buffer.get();
            entrySelector = buffer.get();
            rangeShift    = buffer.get()/2;
            startCount = new char[segCount];
            endCount = new char[segCount];
            idDelta = new short[segCount];
            idRangeOffset = new char[segCount];
            for (int i=0; i<segCount; i++) {
                endCount[i] = buffer.get();
            }
            buffer.get(); 
            for (int i=0; i<segCount; i++) {
                startCount[i] = buffer.get();
            }
            for (int i=0; i<segCount; i++) {
                idDelta[i] = (short)buffer.get();
            }
            for (int i=0; i<segCount; i++) {
                char ctmp = buffer.get();
                idRangeOffset[i] = (char)((ctmp>>1)&0xffff);
            }
            int pos = (segCount*8+16)/2;
            buffer.position(pos);
            int numGlyphIds = (subtableLength/2 - pos);
            glyphIds = new char[numGlyphIds];
            for (int i=0;i<numGlyphIds;i++) {
                glyphIds[i] = buffer.get();
            }
        }
        char getGlyph(int charCode) {
            int index = 0;
            char glyphCode = 0;
            int controlGlyph = getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            if (xlat != null) {
                charCode = xlat[charCode];
            }
            int left = 0, right = startCount.length;
            index = startCount.length >> 1;
            while (left < right) {
                if (endCount[index] < charCode) {
                    left = index + 1;
                } else {
                    right = index;
                }
                index = (left + right) >> 1;
            }
            if (charCode >= startCount[index] && charCode <= endCount[index]) {
                int rangeOffset = idRangeOffset[index];
                if (rangeOffset == 0) {
                    glyphCode = (char)(charCode + idDelta[index]);
                } else {
                    int glyphIDIndex = rangeOffset - segCount + index
                                         + (charCode - startCount[index]);
                    glyphCode = glyphIds[glyphIDIndex];
                    if (glyphCode != 0) {
                        glyphCode = (char)(glyphCode + idDelta[index]);
                    }
                }
            }
            if (glyphCode != 0) {
            }
            return glyphCode;
        }
    }
    static class CMapFormat0 extends CMap {
        byte [] cmap;
        CMapFormat0(ByteBuffer buffer, int offset) {
            int len = buffer.getChar(offset+2);
            cmap = new byte[len-6];
            buffer.position(offset+6);
            buffer.get(cmap);
        }
        char getGlyph(int charCode) {
            if (charCode < 256) {
                if (charCode < 0x0010) {
                    switch (charCode) {
                    case 0x0009:
                    case 0x000a:
                    case 0x000d: return CharToGlyphMapper.INVISIBLE_GLYPH_ID;
                    }
                }
                return (char)(0xff & cmap[charCode]);
            } else {
                return 0;
            }
        }
    }
    static class CMapFormat2 extends CMap {
        char[] subHeaderKey = new char[256];
        char[] firstCodeArray;
        char[] entryCountArray;
        short[] idDeltaArray;
        char[] idRangeOffSetArray;
        char[] glyphIndexArray;
        CMapFormat2(ByteBuffer buffer, int offset, char[] xlat) {
            this.xlat = xlat;
            int tableLen = buffer.getChar(offset+2);
            buffer.position(offset+6);
            CharBuffer cBuffer = buffer.asCharBuffer();
            char maxSubHeader = 0;
            for (int i=0;i<256;i++) {
                subHeaderKey[i] = cBuffer.get();
                if (subHeaderKey[i] > maxSubHeader) {
                    maxSubHeader = subHeaderKey[i];
                }
            }
            int numSubHeaders = (maxSubHeader >> 3) +1;
            firstCodeArray = new char[numSubHeaders];
            entryCountArray = new char[numSubHeaders];
            idDeltaArray  = new short[numSubHeaders];
            idRangeOffSetArray  = new char[numSubHeaders];
            for (int i=0; i<numSubHeaders; i++) {
                firstCodeArray[i] = cBuffer.get();
                entryCountArray[i] = cBuffer.get();
                idDeltaArray[i] = (short)cBuffer.get();
                idRangeOffSetArray[i] = cBuffer.get();
            }
            int glyphIndexArrSize = (tableLen-518-numSubHeaders*8)/2;
            glyphIndexArray = new char[glyphIndexArrSize];
            for (int i=0; i<glyphIndexArrSize;i++) {
                glyphIndexArray[i] = cBuffer.get();
            }
        }
        char getGlyph(int charCode) {
            int controlGlyph = getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            if (xlat != null) {
                charCode = xlat[charCode];
            }
            char highByte = (char)(charCode >> 8);
            char lowByte = (char)(charCode & 0xff);
            int key = subHeaderKey[highByte]>>3; 
            char mapMe;
            if (key != 0) {
                mapMe = lowByte;
            } else {
                mapMe = highByte;
                if (mapMe == 0) {
                    mapMe = lowByte;
                }
            }
            char firstCode = firstCodeArray[key];
            if (mapMe < firstCode) {
                return 0;
            } else {
                mapMe -= firstCode;
            }
            if (mapMe < entryCountArray[key]) {
                int glyphArrayOffset = ((idRangeOffSetArray.length-key)*8)-6;
                int glyphSubArrayStart =
                        (idRangeOffSetArray[key] - glyphArrayOffset)/2;
                char glyphCode = glyphIndexArray[glyphSubArrayStart+mapMe];
                if (glyphCode != 0) {
                    glyphCode += idDeltaArray[key]; 
                    return glyphCode;
                }
            }
            return 0;
        }
    }
    static class CMapFormat6 extends CMap {
        char firstCode;
        char entryCount;
        char[] glyphIdArray;
        CMapFormat6(ByteBuffer bbuffer, int offset, char[] xlat) {
             System.err.println("WARNING: CMapFormat8 is untested.");
             bbuffer.position(offset+6);
             CharBuffer buffer = bbuffer.asCharBuffer();
             firstCode = buffer.get();
             entryCount = buffer.get();
             glyphIdArray = new char[entryCount];
             for (int i=0; i< entryCount; i++) {
                 glyphIdArray[i] = buffer.get();
             }
         }
         char getGlyph(int charCode) {
            int controlGlyph = getControlCodeGlyph(charCode, true);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
             if (xlat != null) {
                 charCode = xlat[charCode];
             }
             charCode -= firstCode;
             if (charCode < 0 || charCode >= entryCount) {
                  return 0;
             } else {
                  return glyphIdArray[charCode];
             }
         }
    }
    static class CMapFormat8 extends CMap {
         byte[] is32 = new byte[8192];
         int nGroups;
         int[] startCharCode;
         int[] endCharCode;
         int[] startGlyphID;
         CMapFormat8(ByteBuffer bbuffer, int offset, char[] xlat) {
             System.err.println("WARNING: CMapFormat8 is untested.");
             bbuffer.position(12);
             bbuffer.get(is32);
             nGroups = bbuffer.getInt();
             startCharCode = new int[nGroups];
             endCharCode   = new int[nGroups];
             startGlyphID  = new int[nGroups];
         }
        char getGlyph(int charCode) {
            if (xlat != null) {
                throw new RuntimeException("xlat array for cmap fmt=8");
            }
            return 0;
        }
    }
    static class CMapFormat10 extends CMap {
         long firstCode;
         int entryCount;
         char[] glyphIdArray;
         CMapFormat10(ByteBuffer bbuffer, int offset, char[] xlat) {
             System.err.println("WARNING: CMapFormat10 is untested.");
             firstCode = bbuffer.getInt() & INTMASK;
             entryCount = bbuffer.getInt() & INTMASK;
             bbuffer.position(offset+20);
             CharBuffer buffer = bbuffer.asCharBuffer();
             glyphIdArray = new char[entryCount];
             for (int i=0; i< entryCount; i++) {
                 glyphIdArray[i] = buffer.get();
             }
         }
         char getGlyph(int charCode) {
             if (xlat != null) {
                 throw new RuntimeException("xlat array for cmap fmt=10");
             }
             int code = (int)(charCode - firstCode);
             if (code < 0 || code >= entryCount) {
                 return 0;
             } else {
                 return glyphIdArray[code];
             }
         }
    }
    static class CMapFormat12 extends CMap {
        int numGroups;
        int highBit =0;
        int power;
        int extra;
        long[] startCharCode;
        long[] endCharCode;
        int[] startGlyphID;
        CMapFormat12(ByteBuffer buffer, int offset, char[] xlat) {
            if (xlat != null) {
                throw new RuntimeException("xlat array for cmap fmt=12");
            }
            numGroups = buffer.getInt(offset+12);
            startCharCode = new long[numGroups];
            endCharCode = new long[numGroups];
            startGlyphID = new int[numGroups];
            buffer.position(offset+16);
            buffer = buffer.slice();
            IntBuffer ibuffer = buffer.asIntBuffer();
            for (int i=0; i<numGroups; i++) {
                startCharCode[i] = ibuffer.get() & INTMASK;
                endCharCode[i] = ibuffer.get() & INTMASK;
                startGlyphID[i] = ibuffer.get() & INTMASK;
            }
            int value = numGroups;
            if (value >= 1 << 16) {
                value >>= 16;
                highBit += 16;
            }
            if (value >= 1 << 8) {
                value >>= 8;
                highBit += 8;
            }
            if (value >= 1 << 4) {
                value >>= 4;
                highBit += 4;
            }
            if (value >= 1 << 2) {
                value >>= 2;
                highBit += 2;
            }
            if (value >= 1 << 1) {
                value >>= 1;
                highBit += 1;
            }
            power = 1 << highBit;
            extra = numGroups - power;
        }
        char getGlyph(int charCode) {
            int controlGlyph = getControlCodeGlyph(charCode, false);
            if (controlGlyph >= 0) {
                return (char)controlGlyph;
            }
            int probe = power;
            int range = 0;
            if (startCharCode[extra] <= charCode) {
                range = extra;
            }
            while (probe > 1) {
                probe >>= 1;
                if (startCharCode[range+probe] <= charCode) {
                    range += probe;
                }
            }
            if (startCharCode[range] <= charCode &&
                  endCharCode[range] >= charCode) {
                return (char)
                    (startGlyphID[range] + (charCode - startCharCode[range]));
            }
            return 0;
        }
    }
    static class NullCMapClass extends CMap {
        char getGlyph(int charCode) {
            return 0;
        }
    }
    public static final NullCMapClass theNullCmap = new NullCMapClass();
    final int getControlCodeGlyph(int charCode, boolean noSurrogates) {
        if (charCode < 0x0010) {
            switch (charCode) {
            case 0x0009:
            case 0x000a:
            case 0x000d: return CharToGlyphMapper.INVISIBLE_GLYPH_ID;
            }
        } else if (charCode >= 0x200c) {
            if ((charCode <= 0x200f) ||
                (charCode >= 0x2028 && charCode <= 0x202e) ||
                (charCode >= 0x206a && charCode <= 0x206f)) {
                return CharToGlyphMapper.INVISIBLE_GLYPH_ID;
            } else if (noSurrogates && charCode >= 0xFFFF) {
                return 0;
            }
        }
        return -1;
    }
}
