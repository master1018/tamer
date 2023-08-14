public final class CodeSetComponentInfo {
    public static final class CodeSetComponent {
        int nativeCodeSet;
        int[] conversionCodeSets;
        public boolean equals( Object obj )
        {
            if (this == obj)
                return true ;
            if (!(obj instanceof CodeSetComponent))
                return false ;
            CodeSetComponent other = (CodeSetComponent)obj ;
            return (nativeCodeSet == other.nativeCodeSet) &&
                Arrays.equals( conversionCodeSets, other.conversionCodeSets ) ;
        }
        public int hashCode()
        {
            int result = nativeCodeSet ;
            for (int ctr=0; ctr<conversionCodeSets.length; ctr++)
                result = 37*result + conversionCodeSets[ctr] ;
            return result ;
        }
        public CodeSetComponent() {}
        public CodeSetComponent(int nativeCodeSet, int[] conversionCodeSets) {
            this.nativeCodeSet = nativeCodeSet;
            if (conversionCodeSets == null)
                this.conversionCodeSets = new int[0];
            else
                this.conversionCodeSets = conversionCodeSets;
        }
        public void read(MarshalInputStream in) {
            nativeCodeSet = in.read_ulong();
            int len = in.read_long();
            conversionCodeSets = new int[len];
            in.read_ulong_array(conversionCodeSets, 0, len);
        }
        public void write(MarshalOutputStream out) {
            out.write_ulong(nativeCodeSet);
            out.write_long(conversionCodeSets.length);
            out.write_ulong_array(conversionCodeSets, 0, conversionCodeSets.length);
        }
        public String toString() {
            StringBuffer sbuf = new StringBuffer("CodeSetComponent(");
            sbuf.append("native:");
            sbuf.append(Integer.toHexString(nativeCodeSet));
            sbuf.append(" conversion:");
            if (conversionCodeSets == null)
                sbuf.append("null");
            else {
                for (int i = 0; i < conversionCodeSets.length; i++) {
                    sbuf.append(Integer.toHexString(conversionCodeSets[i]));
                    sbuf.append(' ');
                }
            }
            sbuf.append( ")" ) ;
            return sbuf.toString();
        }
    }
    private CodeSetComponent forCharData;
    private CodeSetComponent forWCharData;
    public boolean equals( Object obj )
    {
        if (this == obj)
            return true ;
        if (!(obj instanceof CodeSetComponentInfo))
            return false ;
        CodeSetComponentInfo other = (CodeSetComponentInfo)obj ;
        return forCharData.equals( other.forCharData ) &&
            forWCharData.equals( other.forWCharData ) ;
    }
    public int hashCode()
    {
        return forCharData.hashCode() ^ forWCharData.hashCode() ;
    }
    public String toString() {
        StringBuffer sbuf = new StringBuffer("CodeSetComponentInfo(");
        sbuf.append("char_data:");
        sbuf.append(forCharData.toString());
        sbuf.append(" wchar_data:");
        sbuf.append(forWCharData.toString());
        sbuf.append(")");
        return sbuf.toString();
    }
    public CodeSetComponentInfo() {
        forCharData = CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.forCharData;
        forWCharData = CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.forWCharData;
    }
    public CodeSetComponentInfo(CodeSetComponent charData,
                                CodeSetComponent wcharData) {
        forCharData = charData;
        forWCharData = wcharData;
    }
    public void read(MarshalInputStream in) {
        forCharData = new CodeSetComponent();
        forCharData.read(in);
        forWCharData = new CodeSetComponent();
        forWCharData.read(in);
    }
    public void write(MarshalOutputStream out) {
        forCharData.write(out);
        forWCharData.write(out);
    }
    public CodeSetComponent getCharComponent() {
        return forCharData;
    }
    public CodeSetComponent getWCharComponent() {
        return forWCharData;
    }
    public static final class CodeSetContext {
        private int char_data;
        private int wchar_data;
        public CodeSetContext() {}
        public CodeSetContext(int charEncoding, int wcharEncoding) {
            char_data = charEncoding;
            wchar_data = wcharEncoding;
        }
        public void read(MarshalInputStream in) {
            char_data = in.read_ulong();
            wchar_data = in.read_ulong();
        }
        public void write(MarshalOutputStream out) {
            out.write_ulong(char_data);
            out.write_ulong(wchar_data);
        }
        public int getCharCodeSet() {
            return char_data;
        }
        public int getWCharCodeSet() {
            return wchar_data;
        }
        public String toString() {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append("CodeSetContext char set: ");
            sbuf.append(Integer.toHexString(char_data));
            sbuf.append(" wchar set: ");
            sbuf.append(Integer.toHexString(wchar_data));
            return sbuf.toString();
        }
    }
    public static final CodeSetComponentInfo JAVASOFT_DEFAULT_CODESETS;
    static {
        CodeSetComponent charData
            = new CodeSetComponent(OSFCodeSetRegistry.ISO_8859_1.getNumber(),
                                   new int[] {
                                       OSFCodeSetRegistry.UTF_8.getNumber(),
                                       OSFCodeSetRegistry.ISO_646.getNumber()
                                   });
        CodeSetComponent wcharData
            = new CodeSetComponent(OSFCodeSetRegistry.UTF_16.getNumber(),
                                   new int[]
                                   {
                                       OSFCodeSetRegistry.UCS_2.getNumber()
                                   });
        JAVASOFT_DEFAULT_CODESETS = new CodeSetComponentInfo(charData, wcharData);
    }
    public static CodeSetComponent createFromString(String str) {
        ORBUtilSystemException wrapper = ORBUtilSystemException.get(
            CORBALogDomains.RPC_ENCODING ) ;
        if (str == null || str.length() == 0)
            throw wrapper.badCodeSetString() ;
        StringTokenizer stok = new StringTokenizer(str, ", ", false);
        int nativeSet = 0;
        int conversionInts[] = null;
        try {
            nativeSet = Integer.decode(stok.nextToken()).intValue();
            if (OSFCodeSetRegistry.lookupEntry(nativeSet) == null)
                throw wrapper.unknownNativeCodeset( new Integer(nativeSet) ) ;
            List conversionList = new ArrayList(10);
            while (stok.hasMoreTokens()) {
                Integer value = Integer.decode(stok.nextToken());
                if (OSFCodeSetRegistry.lookupEntry(value.intValue()) == null)
                    throw wrapper.unknownConversionCodeSet( value ) ;
                conversionList.add(value);
            }
            conversionInts = new int[conversionList.size()];
            for (int i = 0; i < conversionInts.length; i++)
                conversionInts[i] = ((Integer)conversionList.get(i)).intValue();
        } catch (NumberFormatException nfe) {
            throw wrapper.invalidCodeSetNumber( nfe ) ;
        } catch (NoSuchElementException nsee) {
            throw wrapper.invalidCodeSetString( nsee, str ) ;
        }
        return new CodeSetComponent(nativeSet, conversionInts);
    }
    public static final CodeSetContext LOCAL_CODE_SETS
        = new CodeSetContext(OSFCodeSetRegistry.ISO_8859_1.getNumber(),
                             OSFCodeSetRegistry.UTF_16.getNumber());
}
