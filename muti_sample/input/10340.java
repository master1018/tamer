public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence
{
    private final char value[];
    private final int offset;
    private final int count;
    private int hash; 
    private static final long serialVersionUID = -6849794470754667710L;
    private static final ObjectStreamField[] serialPersistentFields =
        new ObjectStreamField[0];
    public String() {
        this.offset = 0;
        this.count = 0;
        this.value = new char[0];
    }
    public String(String original) {
        int size = original.count;
        char[] originalValue = original.value;
        char[] v;
        if (originalValue.length > size) {
            int off = original.offset;
            v = Arrays.copyOfRange(originalValue, off, off+size);
        } else {
            v = originalValue;
        }
        this.offset = 0;
        this.count = size;
        this.value = v;
    }
    public String(char value[]) {
        int size = value.length;
        this.offset = 0;
        this.count = size;
        this.value = Arrays.copyOf(value, size);
    }
    public String(char value[], int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }
        this.offset = 0;
        this.count = count;
        this.value = Arrays.copyOfRange(value, offset, offset+count);
    }
    public String(int[] codePoints, int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset > codePoints.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }
        final int end = offset + count;
        int n = count;
        for (int i = offset; i < end; i++) {
            int c = codePoints[i];
            if (Character.isBmpCodePoint(c))
                continue;
            else if (Character.isValidCodePoint(c))
                n++;
            else throw new IllegalArgumentException(Integer.toString(c));
        }
        final char[] v = new char[n];
        for (int i = offset, j = 0; i < end; i++, j++) {
            int c = codePoints[i];
            if (Character.isBmpCodePoint(c))
                v[j] = (char) c;
            else
                Character.toSurrogates(c, v, j++);
        }
        this.value  = v;
        this.count  = n;
        this.offset = 0;
    }
    @Deprecated
    public String(byte ascii[], int hibyte, int offset, int count) {
        checkBounds(ascii, offset, count);
        char value[] = new char[count];
        if (hibyte == 0) {
            for (int i = count ; i-- > 0 ;) {
                value[i] = (char) (ascii[i + offset] & 0xff);
            }
        } else {
            hibyte <<= 8;
            for (int i = count ; i-- > 0 ;) {
                value[i] = (char) (hibyte | (ascii[i + offset] & 0xff));
            }
        }
        this.offset = 0;
        this.count = count;
        this.value = value;
    }
    @Deprecated
    public String(byte ascii[], int hibyte) {
        this(ascii, hibyte, 0, ascii.length);
    }
    private static void checkBounds(byte[] bytes, int offset, int length) {
        if (length < 0)
            throw new StringIndexOutOfBoundsException(length);
        if (offset < 0)
            throw new StringIndexOutOfBoundsException(offset);
        if (offset > bytes.length - length)
            throw new StringIndexOutOfBoundsException(offset + length);
    }
    public String(byte bytes[], int offset, int length, String charsetName)
        throws UnsupportedEncodingException
    {
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        checkBounds(bytes, offset, length);
        char[] v = StringCoding.decode(charsetName, bytes, offset, length);
        this.offset = 0;
        this.count = v.length;
        this.value = v;
    }
    public String(byte bytes[], int offset, int length, Charset charset) {
        if (charset == null)
            throw new NullPointerException("charset");
        checkBounds(bytes, offset, length);
        char[] v = StringCoding.decode(charset, bytes, offset, length);
        this.offset = 0;
        this.count = v.length;
        this.value = v;
    }
    public String(byte bytes[], String charsetName)
        throws UnsupportedEncodingException
    {
        this(bytes, 0, bytes.length, charsetName);
    }
    public String(byte bytes[], Charset charset) {
        this(bytes, 0, bytes.length, charset);
    }
    public String(byte bytes[], int offset, int length) {
        checkBounds(bytes, offset, length);
        char[] v  = StringCoding.decode(bytes, offset, length);
        this.offset = 0;
        this.count = v.length;
        this.value = v;
    }
    public String(byte bytes[]) {
        this(bytes, 0, bytes.length);
    }
    public String(StringBuffer buffer) {
        String result = buffer.toString();
        this.value = result.value;
        this.count = result.count;
        this.offset = result.offset;
    }
    public String(StringBuilder builder) {
        String result = builder.toString();
        this.value = result.value;
        this.count = result.count;
        this.offset = result.offset;
    }
    String(int offset, int count, char value[]) {
        this.value = value;
        this.offset = offset;
        this.count = count;
    }
    public int length() {
        return count;
    }
    public boolean isEmpty() {
        return count == 0;
    }
    public char charAt(int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index + offset];
    }
    public int codePointAt(int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAtImpl(value, offset + index, offset + count);
    }
    public int codePointBefore(int index) {
        int i = index - 1;
        if ((i < 0) || (i >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointBeforeImpl(value, offset + index, offset);
    }
    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        return Character.codePointCountImpl(value, offset+beginIndex, endIndex-beginIndex);
    }
    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException();
        }
        return Character.offsetByCodePointsImpl(value, offset, count,
                                                offset+index, codePointOffset) - offset;
    }
    void getChars(char dst[], int dstBegin) {
        System.arraycopy(value, offset, dst, dstBegin, count);
    }
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > count) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        System.arraycopy(value, offset + srcBegin, dst, dstBegin,
             srcEnd - srcBegin);
    }
    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > count) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        int j = dstBegin;
        int n = offset + srcEnd;
        int i = offset + srcBegin;
        char[] val = value;   
        while (i < n) {
            dst[j++] = (byte)val[i++];
        }
    }
    public byte[] getBytes(String charsetName)
        throws UnsupportedEncodingException
    {
        if (charsetName == null) throw new NullPointerException();
        return StringCoding.encode(charsetName, value, offset, count);
    }
    public byte[] getBytes(Charset charset) {
        if (charset == null) throw new NullPointerException();
        return StringCoding.encode(charset, value, offset, count);
    }
    public byte[] getBytes() {
        return StringCoding.encode(value, offset, count);
    }
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String)anObject;
            int n = count;
            if (n == anotherString.count) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = offset;
                int j = anotherString.offset;
                while (n-- != 0) {
                    if (v1[i++] != v2[j++])
                        return false;
                }
                return true;
            }
        }
        return false;
    }
    public boolean contentEquals(StringBuffer sb) {
        synchronized(sb) {
            return contentEquals((CharSequence)sb);
        }
    }
    public boolean contentEquals(CharSequence cs) {
        if (count != cs.length())
            return false;
        if (cs instanceof AbstractStringBuilder) {
            char v1[] = value;
            char v2[] = ((AbstractStringBuilder)cs).getValue();
            int i = offset;
            int j = 0;
            int n = count;
            while (n-- != 0) {
                if (v1[i++] != v2[j++])
                    return false;
            }
            return true;
        }
        if (cs.equals(this))
            return true;
        char v1[] = value;
        int i = offset;
        int j = 0;
        int n = count;
        while (n-- != 0) {
            if (v1[i++] != cs.charAt(j++))
                return false;
        }
        return true;
    }
    public boolean equalsIgnoreCase(String anotherString) {
        return (this == anotherString) ? true :
               (anotherString != null) && (anotherString.count == count) &&
               regionMatches(true, 0, anotherString, 0, count);
    }
    public int compareTo(String anotherString) {
        int len1 = count;
        int len2 = anotherString.count;
        int n = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;
        int i = offset;
        int j = anotherString.offset;
        if (i == j) {
            int k = i;
            int lim = n + i;
            while (k < lim) {
                char c1 = v1[k];
                char c2 = v2[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
        } else {
            while (n-- != 0) {
                char c1 = v1[i++];
                char c2 = v2[j++];
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }
        return len1 - len2;
    }
    public static final Comparator<String> CASE_INSENSITIVE_ORDER
                                         = new CaseInsensitiveComparator();
    private static class CaseInsensitiveComparator
                         implements Comparator<String>, java.io.Serializable {
        private static final long serialVersionUID = 8575799808933029326L;
        public int compare(String s1, String s2) {
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }
    }
    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }
    public boolean regionMatches(int toffset, String other, int ooffset,
                                 int len) {
        char ta[] = value;
        int to = offset + toffset;
        char pa[] = other.value;
        int po = other.offset + ooffset;
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long)count - len)
            || (ooffset > (long)other.count - len)) {
            return false;
        }
        while (len-- > 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }
    public boolean regionMatches(boolean ignoreCase, int toffset,
                           String other, int ooffset, int len) {
        char ta[] = value;
        int to = offset + toffset;
        char pa[] = other.value;
        int po = other.offset + ooffset;
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long)count - len) ||
                (ooffset > (long)other.count - len)) {
            return false;
        }
        while (len-- > 0) {
            char c1 = ta[to++];
            char c2 = pa[po++];
            if (c1 == c2) {
                continue;
            }
            if (ignoreCase) {
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2) {
                    continue;
                }
                if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }
    public boolean startsWith(String prefix, int toffset) {
        char ta[] = value;
        int to = offset + toffset;
        char pa[] = prefix.value;
        int po = prefix.offset;
        int pc = prefix.count;
        if ((toffset < 0) || (toffset > count - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }
    public boolean endsWith(String suffix) {
        return startsWith(suffix, count - suffix.count);
    }
    public int hashCode() {
        int h = hash;
        if (h == 0 && count > 0) {
            int off = offset;
            char val[] = value;
            int len = count;
            for (int i = 0; i < len; i++) {
                h = 31*h + val[off++];
            }
            hash = h;
        }
        return h;
    }
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }
    public int indexOf(int ch, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= count) {
            return -1;
        }
        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            final char[] value = this.value;
            final int offset = this.offset;
            final int max = offset + count;
            for (int i = offset + fromIndex; i < max ; i++) {
                if (value[i] == ch) {
                    return i - offset;
                }
            }
            return -1;
        } else {
            return indexOfSupplementary(ch, fromIndex);
        }
    }
    private int indexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            final int offset = this.offset;
            final char hi = Character.highSurrogate(ch);
            final char lo = Character.lowSurrogate(ch);
            final int max = offset + count - 1;
            for (int i = offset + fromIndex; i < max; i++) {
                if (value[i] == hi && value[i+1] == lo) {
                    return i - offset;
                }
            }
        }
        return -1;
    }
    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, count - 1);
    }
    public int lastIndexOf(int ch, int fromIndex) {
        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            final char[] value = this.value;
            final int offset = this.offset;
            int i = offset + Math.min(fromIndex, count - 1);
            for (; i >= offset ; i--) {
                if (value[i] == ch) {
                    return i - offset;
                }
            }
            return -1;
        } else {
            return lastIndexOfSupplementary(ch, fromIndex);
        }
    }
    private int lastIndexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            final int offset = this.offset;
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int i = offset + Math.min(fromIndex, count - 2);
            for (; i >= offset; i--) {
                if (value[i] == hi && value[i+1] == lo) {
                    return i - offset;
                }
            }
        }
        return -1;
    }
    public int indexOf(String str) {
        return indexOf(str, 0);
    }
    public int indexOf(String str, int fromIndex) {
        return indexOf(value, offset, count,
                       str.value, str.offset, str.count, fromIndex);
    }
    static int indexOf(char[] source, int sourceOffset, int sourceCount,
                       char[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        char first  = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);
        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] ==
                         target[k]; j++, k++);
                if (j == end) {
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }
    public int lastIndexOf(String str) {
        return lastIndexOf(str, count);
    }
    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(value, offset, count,
                           str.value, str.offset, str.count, fromIndex);
    }
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           char[] target, int targetOffset, int targetCount,
                           int fromIndex) {
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target[strLastIndex];
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;
    startSearchForLastChar:
        while (true) {
            while (i >= min && source[i] != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;
            while (j > start) {
                if (source[j--] != target[k--]) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }
    public String substring(int beginIndex) {
        return substring(beginIndex, count);
    }
    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > count) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        return ((beginIndex == 0) && (endIndex == count)) ? this :
            new String(offset + beginIndex, endIndex - beginIndex, value);
    }
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.substring(beginIndex, endIndex);
    }
    public String concat(String str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        char buf[] = new char[count + otherLen];
        getChars(0, count, buf, 0);
        str.getChars(0, otherLen, buf, count);
        return new String(0, count + otherLen, buf);
    }
    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            int len = count;
            int i = -1;
            char[] val = value; 
            int off = offset;   
            while (++i < len) {
                if (val[off + i] == oldChar) {
                    break;
                }
            }
            if (i < len) {
                char buf[] = new char[len];
                for (int j = 0 ; j < i ; j++) {
                    buf[j] = val[off+j];
                }
                while (i < len) {
                    char c = val[off + i];
                    buf[i] = (c == oldChar) ? newChar : c;
                    i++;
                }
                return new String(0, len, buf);
            }
        }
        return this;
    }
    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }
    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) > -1;
    }
    public String replaceFirst(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
    }
    public String replaceAll(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
    }
    public String replace(CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(
            this).replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }
    public String[] split(String regex, int limit) {
        char ch = 0;
        if (((regex.count == 1 &&
             ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) ||
             (regex.length() == 2 &&
              regex.charAt(0) == '\\' &&
              (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
              ((ch-'a')|('z'-ch)) < 0 &&
              ((ch-'A')|('Z'-ch)) < 0)) &&
            (ch < Character.MIN_HIGH_SURROGATE ||
             ch > Character.MAX_LOW_SURROGATE))
        {
            int off = 0;
            int next = 0;
            boolean limited = limit > 0;
            ArrayList<String> list = new ArrayList<>();
            while ((next = indexOf(ch, off)) != -1) {
                if (!limited || list.size() < limit - 1) {
                    list.add(substring(off, next));
                    off = next + 1;
                } else {    
                    list.add(substring(off, count));
                    off = count;
                    break;
                }
            }
            if (off == 0)
                return new String[] { this };
            if (!limited || list.size() < limit)
                list.add(substring(off, count));
            int resultSize = list.size();
            if (limit == 0)
                while (resultSize > 0 && list.get(resultSize-1).length() == 0)
                    resultSize--;
            String[] result = new String[resultSize];
            return list.subList(0, resultSize).toArray(result);
        }
        return Pattern.compile(regex).split(this, limit);
    }
    public String[] split(String regex) {
        return split(regex, 0);
    }
    public String toLowerCase(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        int     firstUpper;
        scan: {
            for (firstUpper = 0 ; firstUpper < count; ) {
                char c = value[offset+firstUpper];
                if ((c >= Character.MIN_HIGH_SURROGATE) &&
                    (c <= Character.MAX_HIGH_SURROGATE)) {
                    int supplChar = codePointAt(firstUpper);
                    if (supplChar != Character.toLowerCase(supplChar)) {
                        break scan;
                    }
                    firstUpper += Character.charCount(supplChar);
                } else {
                    if (c != Character.toLowerCase(c)) {
                        break scan;
                    }
                    firstUpper++;
                }
            }
            return this;
        }
        char[]  result = new char[count];
        int     resultOffset = 0;  
        System.arraycopy(value, offset, result, 0, firstUpper);
        String lang = locale.getLanguage();
        boolean localeDependent =
            (lang == "tr" || lang == "az" || lang == "lt");
        char[] lowerCharArray;
        int lowerChar;
        int srcChar;
        int srcCount;
        for (int i = firstUpper; i < count; i += srcCount) {
            srcChar = (int)value[offset+i];
            if ((char)srcChar >= Character.MIN_HIGH_SURROGATE &&
                (char)srcChar <= Character.MAX_HIGH_SURROGATE) {
                srcChar = codePointAt(i);
                srcCount = Character.charCount(srcChar);
            } else {
                srcCount = 1;
            }
            if (localeDependent || srcChar == '\u03A3') { 
                lowerChar = ConditionalSpecialCasing.toLowerCaseEx(this, i, locale);
            } else if (srcChar == '\u0130') { 
                lowerChar = Character.ERROR;
            } else {
                lowerChar = Character.toLowerCase(srcChar);
            }
            if ((lowerChar == Character.ERROR) ||
                (lowerChar >= Character.MIN_SUPPLEMENTARY_CODE_POINT)) {
                if (lowerChar == Character.ERROR) {
                     if (!localeDependent && srcChar == '\u0130') {
                         lowerCharArray =
                             ConditionalSpecialCasing.toLowerCaseCharArray(this, i, Locale.ENGLISH);
                     } else {
                        lowerCharArray =
                            ConditionalSpecialCasing.toLowerCaseCharArray(this, i, locale);
                     }
                } else if (srcCount == 2) {
                    resultOffset += Character.toChars(lowerChar, result, i + resultOffset) - srcCount;
                    continue;
                } else {
                    lowerCharArray = Character.toChars(lowerChar);
                }
                int mapLen = lowerCharArray.length;
                if (mapLen > srcCount) {
                    char[] result2 = new char[result.length + mapLen - srcCount];
                    System.arraycopy(result, 0, result2, 0,
                        i + resultOffset);
                    result = result2;
                }
                for (int x=0; x<mapLen; ++x) {
                    result[i+resultOffset+x] = lowerCharArray[x];
                }
                resultOffset += (mapLen - srcCount);
            } else {
                result[i+resultOffset] = (char)lowerChar;
            }
        }
        return new String(0, count+resultOffset, result);
    }
    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }
    public String toUpperCase(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        int     firstLower;
        scan: {
            for (firstLower = 0 ; firstLower < count; ) {
                int c = (int)value[offset+firstLower];
                int srcCount;
                if ((c >= Character.MIN_HIGH_SURROGATE) &&
                    (c <= Character.MAX_HIGH_SURROGATE)) {
                    c = codePointAt(firstLower);
                    srcCount = Character.charCount(c);
                } else {
                    srcCount = 1;
                }
                int upperCaseChar = Character.toUpperCaseEx(c);
                if ((upperCaseChar == Character.ERROR) ||
                    (c != upperCaseChar)) {
                    break scan;
                }
                firstLower += srcCount;
            }
            return this;
        }
        char[]  result       = new char[count]; 
        int     resultOffset = 0;  
        System.arraycopy(value, offset, result, 0, firstLower);
        String lang = locale.getLanguage();
        boolean localeDependent =
            (lang == "tr" || lang == "az" || lang == "lt");
        char[] upperCharArray;
        int upperChar;
        int srcChar;
        int srcCount;
        for (int i = firstLower; i < count; i += srcCount) {
            srcChar = (int)value[offset+i];
            if ((char)srcChar >= Character.MIN_HIGH_SURROGATE &&
                (char)srcChar <= Character.MAX_HIGH_SURROGATE) {
                srcChar = codePointAt(i);
                srcCount = Character.charCount(srcChar);
            } else {
                srcCount = 1;
            }
            if (localeDependent) {
                upperChar = ConditionalSpecialCasing.toUpperCaseEx(this, i, locale);
            } else {
                upperChar = Character.toUpperCaseEx(srcChar);
            }
            if ((upperChar == Character.ERROR) ||
                (upperChar >= Character.MIN_SUPPLEMENTARY_CODE_POINT)) {
                if (upperChar == Character.ERROR) {
                    if (localeDependent) {
                        upperCharArray =
                            ConditionalSpecialCasing.toUpperCaseCharArray(this, i, locale);
                    } else {
                        upperCharArray = Character.toUpperCaseCharArray(srcChar);
                    }
                } else if (srcCount == 2) {
                    resultOffset += Character.toChars(upperChar, result, i + resultOffset) - srcCount;
                    continue;
                } else {
                    upperCharArray = Character.toChars(upperChar);
                }
                int mapLen = upperCharArray.length;
                if (mapLen > srcCount) {
                    char[] result2 = new char[result.length + mapLen - srcCount];
                    System.arraycopy(result, 0, result2, 0,
                        i + resultOffset);
                    result = result2;
                }
                for (int x=0; x<mapLen; ++x) {
                    result[i+resultOffset+x] = upperCharArray[x];
                }
                resultOffset += (mapLen - srcCount);
            } else {
                result[i+resultOffset] = (char)upperChar;
            }
        }
        return new String(0, count+resultOffset, result);
    }
    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }
    public String trim() {
        int len = count;
        int st = 0;
        int off = offset;      
        char[] val = value;    
        while ((st < len) && (val[off + st] <= ' ')) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < count)) ? substring(st, len) : this;
    }
    public String toString() {
        return this;
    }
    public char[] toCharArray() {
        char result[] = new char[count];
        getChars(0, count, result, 0);
        return result;
    }
    public static String format(String format, Object ... args) {
        return new Formatter().format(format, args).toString();
    }
    public static String format(Locale l, String format, Object ... args) {
        return new Formatter(l).format(format, args).toString();
    }
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }
    public static String valueOf(char data[]) {
        return new String(data);
    }
    public static String valueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }
    public static String copyValueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }
    public static String copyValueOf(char data[]) {
        return copyValueOf(data, 0, data.length);
    }
    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }
    public static String valueOf(char c) {
        char data[] = {c};
        return new String(0, 1, data);
    }
    public static String valueOf(int i) {
        return Integer.toString(i);
    }
    public static String valueOf(long l) {
        return Long.toString(l);
    }
    public static String valueOf(float f) {
        return Float.toString(f);
    }
    public static String valueOf(double d) {
        return Double.toString(d);
    }
    public native String intern();
}
