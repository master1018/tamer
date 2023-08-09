public class RefinedSoundex implements StringEncoder {
    public static final RefinedSoundex US_ENGLISH = new RefinedSoundex();
    public static final char[] US_ENGLISH_MAPPING = "01360240043788015936020505".toCharArray();
    private char[] soundexMapping;
    public RefinedSoundex() {
        this(US_ENGLISH_MAPPING);
    }
    public RefinedSoundex(char[] mapping) {
        this.soundexMapping = mapping;
    }
    public int difference(String s1, String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }
    public Object encode(Object pObject) throws EncoderException {
        if (!(pObject instanceof java.lang.String)) {
            throw new EncoderException("Parameter supplied to RefinedSoundex encode is not of type java.lang.String");
        }
        return soundex((String) pObject);
    }
    public String encode(String pString) {
        return soundex(pString);
    }
    char getMappingCode(char c) {
        if (!Character.isLetter(c)) {
            return 0;
        }
        return this.soundexMapping[Character.toUpperCase(c) - 'A'];
    }
    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.length() == 0) {
            return str;
        }
        StringBuffer sBuf = new StringBuffer();
        sBuf.append(str.charAt(0));
        char last, current;
        last = '*';
        for (int i = 0; i < str.length(); i++) {
            current = getMappingCode(str.charAt(i));
            if (current == last) {
                continue;
            } else if (current != 0) {
                sBuf.append(current);
            }
            last = current;
        }
        return sBuf.toString();
    }
}
