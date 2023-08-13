public final class CharsetICU extends Charset{
    private String icuCanonicalName;
    protected CharsetICU(String canonicalName, String icuCanonName, String[] aliases) {
         super(canonicalName,aliases);
         icuCanonicalName = icuCanonName;
    }
    public CharsetDecoder newDecoder(){
        long converterHandle = NativeConverter.openConverter(icuCanonicalName);
        return new CharsetDecoderICU(this,converterHandle);
    };
    private static final Map subByteMap = new HashMap();
    static{
        subByteMap.put("UTF-32",new byte[]{0x00, 0x00, (byte)0xfe, (byte)0xff});
        subByteMap.put("ibm-16684_P110-2003",new byte[]{0x40, 0x40}); 
        subByteMap.put("ibm-971_P100-1995",new byte[]{(byte)0xa1, (byte)0xa1}); 
    }
    public CharsetEncoder newEncoder(){
        long converterHandle = NativeConverter.openConverter(icuCanonicalName);
        byte[] replacement = NativeConverter.getSubstitutionBytes(converterHandle);
       try{
            return new CharsetEncoderICU(this,converterHandle, replacement);
        }catch(IllegalArgumentException ex){
            replacement = (byte[])subByteMap.get(icuCanonicalName);
            if(replacement==null){
                replacement = new byte[NativeConverter.getMinBytesPerChar(converterHandle)];
                for(int i=0; i<replacement.length; i++){
                    replacement[i]= 0x3f;
                }
            }
            NativeConverter.setSubstitutionBytes(converterHandle, replacement, replacement.length);
            return new CharsetEncoderICU(this,converterHandle, replacement);
        }
    } 
    public boolean contains(Charset cs){
        if (null == cs) {
        return false;
        } else if (this.equals(cs)) {
            return true;
        }
        long converterHandle1 = 0;
        long converterHandle2 = 0;
        try {
            converterHandle1 = NativeConverter.openConverter(this.name());
            if (converterHandle1 > 0) {
                converterHandle2 = NativeConverter.openConverter(cs.name());
                if (converterHandle2 > 0) {
                    return NativeConverter.contains(converterHandle1,
                            converterHandle2);
                }
            }
            return false;
        } finally {
            if (0 != converterHandle1) {
                NativeConverter.closeConverter(converterHandle1);
                if (0 != converterHandle2) {
                    NativeConverter.closeConverter(converterHandle2);
                }
            }
        }
    }
}
