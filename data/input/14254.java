abstract class CharacterData {
    abstract int getProperties(int ch);
    abstract int getType(int ch);
    abstract boolean isWhitespace(int ch);
    abstract boolean isMirrored(int ch);
    abstract boolean isJavaIdentifierStart(int ch);
    abstract boolean isJavaIdentifierPart(int ch);
    abstract boolean isUnicodeIdentifierStart(int ch);
    abstract boolean isUnicodeIdentifierPart(int ch);
    abstract boolean isIdentifierIgnorable(int ch);
    abstract int toLowerCase(int ch);
    abstract int toUpperCase(int ch);
    abstract int toTitleCase(int ch);
    abstract int digit(int ch, int radix);
    abstract int getNumericValue(int ch);
    abstract byte getDirectionality(int ch);
    int toUpperCaseEx(int ch) {
        return toUpperCase(ch);
    }
    char[] toUpperCaseCharArray(int ch) {
        return null;
    }
    boolean isOtherLowercase(int ch) {
        return false;
    }
    boolean isOtherUppercase(int ch) {
        return false;
    }
    boolean isOtherAlphabetic(int ch) {
        return false;
    }
    boolean isIdeographic(int ch) {
        return false;
    }
    static final CharacterData of(int ch) {
        if (ch >>> 8 == 0) {     
            return CharacterDataLatin1.instance;
        } else {
            switch(ch >>> 16) {  
            case(0):
                return CharacterData00.instance;
            case(1):
                return CharacterData01.instance;
            case(2):
                return CharacterData02.instance;
            case(14):
                return CharacterData0E.instance;
            case(15):   
            case(16):   
                return CharacterDataPrivateUse.instance;
            default:
                return CharacterDataUndefined.instance;
            }
        }
    }
}
