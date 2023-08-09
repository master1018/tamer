public class CodeSetConversion
{
    public abstract static class CTBConverter
    {
        public abstract void convert(char chToConvert);
        public abstract void convert(String strToConvert);
        public abstract int getNumBytes();
        public abstract float getMaxBytesPerChar();
        public abstract boolean isFixedWidthEncoding();
        public abstract int getAlignment();
        public abstract byte[] getBytes();
    }
    public abstract static class BTCConverter
    {
        public abstract boolean isFixedWidthEncoding();
        public abstract int getFixedCharWidth();
        public abstract int getNumChars();
        public abstract char[] getChars(byte[] bytes, int offset, int length);
    }
    private class JavaCTBConverter extends CTBConverter
    {
        private ORBUtilSystemException wrapper = ORBUtilSystemException.get(
            CORBALogDomains.RPC_ENCODING ) ;
        private OMGSystemException omgWrapper = OMGSystemException.get(
            CORBALogDomains.RPC_ENCODING ) ;
        private CharsetEncoder ctb;
        private int alignment;
        private char[] chars = null;
        private int numBytes = 0;
        private int numChars = 0;
        private ByteBuffer buffer;
        private OSFCodeSetRegistry.Entry codeset;
        public JavaCTBConverter(OSFCodeSetRegistry.Entry codeset,
                                int alignmentForEncoding) {
            try {
                ctb = cache.getCharToByteConverter(codeset.getName());
                if (ctb == null) {
                    Charset tmpCharset = Charset.forName(codeset.getName());
                    ctb = tmpCharset.newEncoder();
                    cache.setConverter(codeset.getName(), ctb);
                }
            } catch(IllegalCharsetNameException icne) {
                throw wrapper.invalidCtbConverterName(icne,codeset.getName());
            } catch(UnsupportedCharsetException ucne) {
                throw wrapper.invalidCtbConverterName(ucne,codeset.getName());
            }
            this.codeset = codeset;
            alignment = alignmentForEncoding;
        }
        public final float getMaxBytesPerChar() {
            return ctb.maxBytesPerChar();
        }
        public void convert(char chToConvert) {
            if (chars == null)
                chars = new char[1];
            chars[0] = chToConvert;
            numChars = 1;
            convertCharArray();
        }
        public void convert(String strToConvert) {
            if (chars == null || chars.length < strToConvert.length())
                chars = new char[strToConvert.length()];
            numChars = strToConvert.length();
            strToConvert.getChars(0, numChars, chars, 0);
            convertCharArray();
        }
        public final int getNumBytes() {
            return numBytes;
        }
        public final int getAlignment() {
            return alignment;
        }
        public final boolean isFixedWidthEncoding() {
            return codeset.isFixedWidth();
        }
        public byte[] getBytes() {
            return buffer.array();
        }
        private void convertCharArray() {
            try {
                buffer = ctb.encode(CharBuffer.wrap(chars,0,numChars));
                numBytes = buffer.limit();
            } catch (IllegalStateException ise) {
                throw wrapper.ctbConverterFailure( ise ) ;
            } catch (MalformedInputException mie) {
                throw wrapper.badUnicodePair( mie ) ;
            } catch (UnmappableCharacterException uce) {
                throw omgWrapper.charNotInCodeset( uce ) ;
            } catch (CharacterCodingException cce) {
                throw wrapper.ctbConverterFailure( cce ) ;
            }
        }
    }
    private class UTF16CTBConverter extends JavaCTBConverter
    {
        public UTF16CTBConverter() {
            super(OSFCodeSetRegistry.UTF_16, 2);
        }
        public UTF16CTBConverter(boolean littleEndian) {
            super(littleEndian ?
                  OSFCodeSetRegistry.UTF_16LE :
                  OSFCodeSetRegistry.UTF_16BE,
                  2);
        }
    }
    private class JavaBTCConverter extends BTCConverter
    {
        private ORBUtilSystemException wrapper = ORBUtilSystemException.get(
            CORBALogDomains.RPC_ENCODING ) ;
        private OMGSystemException omgWrapper = OMGSystemException.get(
            CORBALogDomains.RPC_ENCODING ) ;
        protected CharsetDecoder btc;
        private char[] buffer;
        private int resultingNumChars;
        private OSFCodeSetRegistry.Entry codeset;
        public JavaBTCConverter(OSFCodeSetRegistry.Entry codeset) {
            btc = this.getConverter(codeset.getName());
            this.codeset = codeset;
        }
        public final boolean isFixedWidthEncoding() {
            return codeset.isFixedWidth();
        }
        public final int getFixedCharWidth() {
            return codeset.getMaxBytesPerChar();
        }
        public final int getNumChars() {
            return resultingNumChars;
        }
        public char[] getChars(byte[] bytes, int offset, int numBytes) {
            try {
                ByteBuffer byteBuf = ByteBuffer.wrap(bytes, offset, numBytes);
                CharBuffer charBuf = btc.decode(byteBuf);
                resultingNumChars = charBuf.limit();
                if (charBuf.limit() == charBuf.capacity()) {
                    buffer = charBuf.array();
                } else {
                    buffer = new char[charBuf.limit()];
                    charBuf.get(buffer, 0, charBuf.limit()).position(0);
                }
                return buffer;
            } catch (IllegalStateException ile) {
                throw wrapper.btcConverterFailure( ile ) ;
            } catch (MalformedInputException mie) {
                throw wrapper.badUnicodePair( mie ) ;
            } catch (UnmappableCharacterException uce) {
                throw omgWrapper.charNotInCodeset( uce ) ;
            } catch (CharacterCodingException cce) {
                throw wrapper.btcConverterFailure( cce ) ;
            }
        }
        protected CharsetDecoder getConverter(String javaCodeSetName) {
            CharsetDecoder result = null;
            try {
                result = cache.getByteToCharConverter(javaCodeSetName);
                if (result == null) {
                    Charset tmpCharset = Charset.forName(javaCodeSetName);
                    result = tmpCharset.newDecoder();
                    cache.setConverter(javaCodeSetName, result);
                }
            } catch(IllegalCharsetNameException icne) {
                throw wrapper.invalidBtcConverterName( icne, javaCodeSetName ) ;
            }
            return result;
        }
    }
    private class UTF16BTCConverter extends JavaBTCConverter
    {
        private boolean defaultToLittleEndian;
        private boolean converterUsesBOM = true;
        private static final char UTF16_BE_MARKER = (char) 0xfeff;
        private static final char UTF16_LE_MARKER = (char) 0xfffe;
        public UTF16BTCConverter(boolean defaultToLittleEndian) {
            super(OSFCodeSetRegistry.UTF_16);
            this.defaultToLittleEndian = defaultToLittleEndian;
        }
        public char[] getChars(byte[] bytes, int offset, int numBytes) {
            if (hasUTF16ByteOrderMarker(bytes, offset, numBytes)) {
                if (!converterUsesBOM)
                    switchToConverter(OSFCodeSetRegistry.UTF_16);
                converterUsesBOM = true;
                return super.getChars(bytes, offset, numBytes);
            } else {
                if (converterUsesBOM) {
                    if (defaultToLittleEndian)
                        switchToConverter(OSFCodeSetRegistry.UTF_16LE);
                    else
                        switchToConverter(OSFCodeSetRegistry.UTF_16BE);
                    converterUsesBOM = false;
                }
                return super.getChars(bytes, offset, numBytes);
            }
        }
        private boolean hasUTF16ByteOrderMarker(byte[] array, int offset, int length) {
            if (length >= 4) {
                int b1 = array[offset] & 0x00FF;
                int b2 = array[offset + 1] & 0x00FF;
                char marker = (char)((b1 << 8) | (b2 << 0));
                return (marker == UTF16_BE_MARKER || marker == UTF16_LE_MARKER);
            } else
                return false;
        }
        private void switchToConverter(OSFCodeSetRegistry.Entry newCodeSet) {
            btc = super.getConverter(newCodeSet.getName());
        }
    }
    public CTBConverter getCTBConverter(OSFCodeSetRegistry.Entry codeset) {
        int alignment = (!codeset.isFixedWidth() ?
                         1 :
                         codeset.getMaxBytesPerChar());
        return new JavaCTBConverter(codeset, alignment);
    }
    public CTBConverter getCTBConverter(OSFCodeSetRegistry.Entry codeset,
                                        boolean littleEndian,
                                        boolean useByteOrderMarkers) {
        if (codeset == OSFCodeSetRegistry.UCS_2)
            return new UTF16CTBConverter(littleEndian);
        if (codeset == OSFCodeSetRegistry.UTF_16) {
            if (useByteOrderMarkers)
                return new UTF16CTBConverter();
            else
                return new UTF16CTBConverter(littleEndian);
        }
        int alignment = (!codeset.isFixedWidth() ?
                         1 :
                         codeset.getMaxBytesPerChar());
        return new JavaCTBConverter(codeset, alignment);
    }
    public BTCConverter getBTCConverter(OSFCodeSetRegistry.Entry codeset) {
        return new JavaBTCConverter(codeset);
    }
    public BTCConverter getBTCConverter(OSFCodeSetRegistry.Entry codeset,
                                        boolean defaultToLittleEndian) {
        if (codeset == OSFCodeSetRegistry.UTF_16 ||
            codeset == OSFCodeSetRegistry.UCS_2) {
            return new UTF16BTCConverter(defaultToLittleEndian);
        } else {
            return new JavaBTCConverter(codeset);
        }
    }
    private int selectEncoding(CodeSetComponentInfo.CodeSetComponent client,
                               CodeSetComponentInfo.CodeSetComponent server) {
        int serverNative = server.nativeCodeSet;
        if (serverNative == 0) {
            if (server.conversionCodeSets.length > 0)
                serverNative = server.conversionCodeSets[0];
            else
                return CodeSetConversion.FALLBACK_CODESET;
        }
        if (client.nativeCodeSet == serverNative) {
            return serverNative;
        }
        for (int i = 0; i < client.conversionCodeSets.length; i++) {
            if (serverNative == client.conversionCodeSets[i]) {
                return serverNative;
            }
        }
        for (int i = 0; i < server.conversionCodeSets.length; i++) {
            if (client.nativeCodeSet == server.conversionCodeSets[i]) {
                return client.nativeCodeSet;
            }
        }
        for (int i = 0; i < server.conversionCodeSets.length; i++) {
            for (int y = 0; y < client.conversionCodeSets.length; y++) {
                if (server.conversionCodeSets[i] == client.conversionCodeSets[y]) {
                    return server.conversionCodeSets[i];
                }
            }
        }
        return CodeSetConversion.FALLBACK_CODESET;
    }
    public CodeSetComponentInfo.CodeSetContext negotiate(CodeSetComponentInfo client,
                                                         CodeSetComponentInfo server) {
        int charData
            = selectEncoding(client.getCharComponent(),
                             server.getCharComponent());
        if (charData == CodeSetConversion.FALLBACK_CODESET) {
            charData = OSFCodeSetRegistry.UTF_8.getNumber();
        }
        int wcharData
            = selectEncoding(client.getWCharComponent(),
                             server.getWCharComponent());
        if (wcharData == CodeSetConversion.FALLBACK_CODESET) {
            wcharData = OSFCodeSetRegistry.UTF_16.getNumber();
        }
        return new CodeSetComponentInfo.CodeSetContext(charData,
                                                       wcharData);
    }
    private CodeSetConversion() {}
    private static class CodeSetConversionHolder {
        static final CodeSetConversion csc = new CodeSetConversion() ;
    }
    public final static CodeSetConversion impl() {
        return CodeSetConversionHolder.csc ;
    }
    private static CodeSetConversion implementation;
    private static final int FALLBACK_CODESET = 0;
    private CodeSetCache cache = new CodeSetCache();
}
