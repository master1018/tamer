public class Supplementary {
    public static void main(String[] args) {
        test1();        
        test2();        
        test3();        
        test4();        
        test5();        
        test6();        
        test7();        
        test8();        
        test9();        
        test10();       
    }
    static final String[] input = {
        "abc\uD800\uDC00def\uD800\uD800ab\uD800\uDC00cdefa\uDC00bcdef",
        "\uD800defg\uD800hij\uD800\uDC00klm\uDC00nop\uDC00\uD800rt\uDC00",
        "\uDC00abcd\uDBFF\uDFFFefgh\uD800\uDC009ik\uDC00\uDC00lm\uDC00no\uD800",
        "\uD800\uDC00!#$\uD800%&\uD800\uDC00;+\uDC00<>;=^\uDC00\\@\uD800\uDC00",
        "\uDB40\uDE00abc\uDE01\uDB40de\uDB40\uDE02f\uDB40\uDE03ghi\uDB40\uDE02",
        "\uD800\uDC00\uD800\uDC01\uD800\uDC02\uD800\uDC03\uD800\uDC04\uD800\uDC05"+
        "\uD800\uDC06\uD800\uDC07\uD800\uDC08\uD800\uDC08\uD800\uDC09\uD800\uDC0A"+
        "\uD800\uDC0B\uD800\uDC0C\uD800\uDC0D\uD800\uDC0A\uD800\uDC0F\uD800\uDC10"
    };
    static final int[][] golden1 = {
        {'a',    0xD800,  0xDC00, 0x10000, 0xE0200, 0x10000},
        {0xD800, 0x10000, 'g',    0xDC00,  0xE0202, 0xDC04}, 
        {'f',    0xDC00,  0xD800, 0xDC00,  0xDE02,  0xDC10}, 
        {'f',    'p',     0xDC00, '^',     0xE0202, 0xDC08}, 
    };
    static void test1() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            testCodePoint(At, s, 0, golden1[0][i]);
            testCodePoint(At, s, 9, golden1[1][i]);
            testCodePoint(At, s, s.length()-1, golden1[2][i]);
            testCodePoint(At, s.substring(17), 0, golden1[3][i]);
            testCodePoint(At, s, -1);
            testCodePoint(At, s, s.length());
        }
    }
    static final int[][] golden2 = {
        {'a',    0xD800, 0xDC00,  0xD800,  0xDB40,  0xD800}, 
        {0xD800, 'l',    0x10000, 0xDC00,  0xDB40,  0xD800}, 
        {'f',    0xDC00, 0xD800,  0x10000, 0xE0202, 0x10010},
        {'b',    'd',    'a',     0xDC00,  0xDE00,  0xDC00}, 
    };
    static void test2() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            testCodePoint(Before, s, 1, golden2[0][i]);
            testCodePoint(Before, s, 13, golden2[1][i]);
            testCodePoint(Before, s, s.length(), golden2[2][i]);
            testCodePoint(Before, s.substring(1), 1, golden2[3][i]);
            testCodePoint(Before, s, 0);
            testCodePoint(Before, s, s.length()+1);
        }
    }
    static final int[][] golden3 = {
        {'b',     -1,  1, 11, 20,  -1},
        {0xD800,  -1,  0,  5,  9,  19, -1},
        {0xDC00,  -1,  0, 12, 16,  17, 20, -1},
        {0x10000, -1,  0,  8, 21,  -1},
        {0xE0202, -1,  9, 17, -1},
        {0x1000A, -1, 22, 30, -1}
    };
    static void test3() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            testIndexOf(s, golden3[i][0], golden3[i][2]);
            testIndexOf(s, 'Z', -1);
            testIndexOf(s, 0xDB98, -1);
            testIndexOf(s, 0xDE76, -1);
            testIndexOf(s, 0x12345, -1);
            testIndexOf(s, -1, -1);
            testIndexOf(s, 0x110000, -1);
        }
    }
    static void test4() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            int ch = golden3[i][0];
            int fromIndex = 0;
            for (int j = 2; j < golden3[i].length; j++) {
                fromIndex = testIndexOf(s, fromIndex, ch,
                                        golden3[i][j]) + 1;
            }
            testIndexOf(s, -1, ch, golden3[i][2]);
            testIndexOf(s, s.length(), ch,
                        golden3[i][golden3[i].length-1]);
            testIndexOf(s, 0, 'Z', -1);
            testIndexOf(s, 0, 0xDB98, -1);
            testIndexOf(s, 0, 0xDE76, -1);
            testIndexOf(s, 0, 0x12345, -1);
            testIndexOf(s, 0, -1, -1);
            testIndexOf(s, 0, 0x110000, -1);
        }
    }
    static void test5() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            testLastIndexOf(s, golden3[i][0],
                        golden3[i][golden3[i].length-2]);
            testLastIndexOf(s, 'Z', -1);
            testLastIndexOf(s, 0xDB98, -1);
            testLastIndexOf(s, 0xDE76, -1);
            testLastIndexOf(s, 0x12345, -1);
            testLastIndexOf(s, -1, -1);
            testLastIndexOf(s, 0x110000, -1);
        }
    }
    static void test6() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            int ch = golden3[i][0];
            int len = s.length();
            int fromIndex = len - 1;
            for (int j = golden3[i].length - 2; j > 0; j--) {
                fromIndex = testLastIndexOf(s, fromIndex, ch,
                                        golden3[i][j]) - 1;
            }
            testLastIndexOf(s, -1, ch, golden3[i][1]);
            testLastIndexOf(s, len, ch, golden3[i][golden3[i].length-2]);
            testLastIndexOf(s, len, 'Z', -1);
            testLastIndexOf(s, len, 0xDB98, -1);
            testLastIndexOf(s, len, 0xDE76, -1);
            testLastIndexOf(s, len, 0x12345, -1);
            testLastIndexOf(s, len, -1, -1);
            testLastIndexOf(s, len, 0x110000, -1);
        }
    }
    static void test7() {
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            int nCodePoints = 0;
            int c;
            for (int j = 0; j < s.length(); j += Character.charCount(c)) {
                c = s.codePointAt(j);
                nCodePoints++;
            }
            int[] codePoints = new int[nCodePoints];
            int count = 0, mid = 0, offset = 0;
            for (int j = 0; j < s.length(); j += Character.charCount(c)) {
                if (mid == 0 && j >= s.length()/2) {
                    mid = j;
                    offset = count;
                }
                c = s.codePointAt(j);
                codePoints[count++] = c;
            }
            String cps = new String(codePoints, 0, count);
            check(!s.equals(cps), "new String(int[]...) with input[" + i + "]");
            cps = new String(codePoints, 0, offset);
            check(!s.substring(0, mid).equals(cps),
                  "first half: new String(int[]...) with input[" + i + "]");
            cps = new String(codePoints, offset, count - offset);
            check(!s.substring(mid).equals(cps),
                  "second half: new String(int[]...) with input[" + i + "]");
            testNewString(null, 0, count, NullPointerException.class);
            testNewString(codePoints, -1, count, IndexOutOfBoundsException.class);
            testNewString(codePoints, 0, count+1, IndexOutOfBoundsException.class);
            testNewString(codePoints, offset, count, IndexOutOfBoundsException.class);
            testNewString(codePoints, offset, -1, IndexOutOfBoundsException.class);
            testNewString(codePoints, count, 1, IndexOutOfBoundsException.class);
            codePoints[offset] = -1;
            testNewString(codePoints, 0, count, IllegalArgumentException.class);
            codePoints[offset] = Character.MAX_CODE_POINT+1;
            testNewString(codePoints, 0, count, IllegalArgumentException.class);
        }
        {
            int[] x = new int[Character.MAX_CODE_POINT];
            for (int i = 0; i < x.length; i++)
                if (i != 0xdbff) 
                    x[i] = i;
            final String s = new String(x, 0, x.length);
            check(s.codePointCount(0, s.length()) != x.length,
                  "s.codePointCount(0, s.length()) != x.length");
            check(s.length() <= x.length,
                  "s.length() <= x.length");
            for (int i = 0, j = 0; i < x.length; i++) {
                int c = s.codePointAt(j);
                check(c != x[i], "c != x[i]");
                j += Character.charCount(c);
            }
        }
    }
    static void test8() {
        for (int i = 0; i < input.length; i++) {
            String str = input[i];
            int length = str.length();
            for (int j = 0; j <= length; j++) {
                int result = str.codePointCount(j, length);
                int expected = Character.codePointCount(str, j, length);
                check(result != expected, "codePointCount(input["+i+"], "+j+", "+length+")",
                      result, expected);
                String substr = str.substring(j, length);
                result = substr.codePointCount(0, substr.length());
                check(result != expected, "substring:codePointCount(input["+i+"], "+j+", "+length+")",
                      result, expected);
            }
            for (int j = length; j >= 0; j--) {
                int result = str.codePointCount(0, j);
                int expected = Character.codePointCount(str, 0, j);
                check(result != expected, "codePointCount(input["+i+"], 0, "+j+")",
                      result, expected);
                String substr = str.substring(0, j);
                result = substr.codePointCount(0, substr.length());
                check(result != expected, "substring:codePointCount(input["+i+"], 0, "+j+")",
                      result, expected);
            }
            testCodePointCount(null, 0, 0, NullPointerException.class);
            testCodePointCount(str, -1, length, IndexOutOfBoundsException.class);
            testCodePointCount(str, 0, length+1, IndexOutOfBoundsException.class);
            testCodePointCount(str, length, length-1, IndexOutOfBoundsException.class);
        }
    }
    static void test9() {
        for (int i = 0; i < input.length; i++) {
            String str = input[i];
            int length = str.length();
            for (int j = 0; j <= length; j++) {
                int nCodePoints = Character.codePointCount(str, j, length);
                int result = str.offsetByCodePoints(j, nCodePoints);
                check(result != length,
                      "offsetByCodePoints(input["+i+"], "+j+", "+nCodePoints+")",
                      result, length);
                result = str.offsetByCodePoints(length, -nCodePoints);
                int expected = j;
                if (j > 0 && j < length) {
                    int cp = str.codePointBefore(j+1);
                    if (Character.isSupplementaryCodePoint(cp)) {
                        expected--;
                    }
                }
                check(result != expected,
                      "offsetByCodePoints(input["+i+"], "+j+", "+(-nCodePoints)+")",
                      result, expected);
            }
            for (int j = length; j >= 0; j--) {
                int nCodePoints = Character.codePointCount(str, 0, j);
                int result = str.offsetByCodePoints(0, nCodePoints);
                int expected = j;
                if (j > 0 && j < length) {
                    int cp = str.codePointAt(j-1);
                     if (Character.isSupplementaryCodePoint(cp)) {
                        expected++;
                    }
                }
                check(result != expected,
                      "offsetByCodePoints(input["+i+"], 0, "+nCodePoints+")",
                      result, expected);
                result = str.offsetByCodePoints(j, -nCodePoints);
                check(result != 0,
                      "offsetByCodePoints(input["+i+"], "+j+", "+(-nCodePoints)+")",
                      result, 0);
            }
            testOffsetByCodePoints(null, 0, 0, NullPointerException.class);
            testOffsetByCodePoints(str, -1, length, IndexOutOfBoundsException.class);
            testOffsetByCodePoints(str, 0, length+1, IndexOutOfBoundsException.class);
            testOffsetByCodePoints(str, 1, -2, IndexOutOfBoundsException.class);
            testOffsetByCodePoints(str, length, length-1, IndexOutOfBoundsException.class);
            testOffsetByCodePoints(str, length, -(length+1), IndexOutOfBoundsException.class);
        }
    }
    static void test10() {
        String header = "H\uD800e\uDFFFa\uDBFF\uDC00der<";
        for (int i = 0; i < input.length; i++) {
            String wholeString = header + input[i];
            String str = wholeString.substring(header.length());
            int length = str.length();
            for (int j = 0; j <= length; j++) {
                int nCodePoints = Character.codePointCount(str, j, length);
                int result = str.offsetByCodePoints(j, nCodePoints);
                check(result != length,
                      "offsetByCodePoints(input["+i+"], "+j+", "+nCodePoints+")",
                      result, length);
                result = str.offsetByCodePoints(length, -nCodePoints);
                int expected = j;
                if (j > 0 && j < length) {
                    int cp = str.codePointBefore(j+1);
                    if (Character.isSupplementaryCodePoint(cp)) {
                        expected--;
                    }
                }
                check(result != expected,
                      "offsetByCodePoints(input["+i+"], "+j+", "+(-nCodePoints)+")",
                      result, expected);
            }
            for (int j = length; j >= 0; j--) {
                int nCodePoints = Character.codePointCount(str, 0, j);
                int result = str.offsetByCodePoints(0, nCodePoints);
                int expected = j;
                if (j > 0 && j < length) {
                    int cp = str.codePointAt(j-1);
                     if (Character.isSupplementaryCodePoint(cp)) {
                        expected++;
                    }
                }
                check(result != expected,
                      "offsetByCodePoints(input["+i+"], 0, "+nCodePoints+")",
                      result, expected);
                result = str.offsetByCodePoints(j, -nCodePoints);
                check(result != 0,
                      "offsetByCodePoints(input["+i+"], "+j+", "+(-nCodePoints)+")",
                      result, 0);
            }
        }
    }
    static final boolean At = true, Before = false;
    static final boolean FIRST = true, LAST = false;
    static void testCodePoint(boolean isAt, String s, int index, int expected) {
        int c = isAt ? s.codePointAt(index) : s.codePointBefore(index);
        check(c != expected,
              "codePoint" + (isAt ? "At" : "Before") + "(" + index + ") for <"
              + s + ">", c, expected);
    }
    static void testCodePoint(boolean isAt, String s, int index) {
        boolean exceptionOccurred = false;
        try {
            int c = isAt ? s.codePointAt(index) : s.codePointBefore(index);
        }
        catch (StringIndexOutOfBoundsException e) {
            exceptionOccurred = true;
        }
        check(!exceptionOccurred,
              "codePoint" + (isAt ? "At" : "Before") + "(" + index + ") for <"
              + s + "> should throw StringIndexOutOfBoundsPointerException.");
    }
    static void testIndexOf(String s, int c, int expected) {
        testIndexOf2(s, c, expected);
        if (s.indexOf(c) != -1) {
            testIndexOf2(s + (char) c, c, expected);
            if (Character.isSupplementaryCodePoint(c)) {
                char[] surrogates = Character.toChars(c);
                testIndexOf2(s + new String(surrogates), c, expected);
                testIndexOf2(s + surrogates[0], c, expected);
                testIndexOf2(s + surrogates[1], c, expected);
                testIndexOf2(new String(surrogates) + s, c, 0);
                testIndexOf2(surrogates[0] + s, c, expected + 1);
                testIndexOf2(surrogates[1] + s, c, expected + 1);
            }
        }
    }
    static void testIndexOf2(String s, int c, int expected) {
        int index = s.indexOf(c);
        check(index != expected,
              "indexOf(" + toHexString(c) + ") for <" + s + ">",
              index, expected);
    }
    static void testLastIndexOf(String s, int c, int expected) {
        testLastIndexOf2(s, c, expected);
        if (s.lastIndexOf(c) != -1) {
            testLastIndexOf2((char) c + s, c, expected + 1);
            if (Character.isSupplementaryCodePoint(c)) {
                char[] surrogates = Character.toChars(c);
                testLastIndexOf2(s + new String(surrogates), c, s.length());
                testLastIndexOf2(s + surrogates[0], c, expected);
                testLastIndexOf2(s + surrogates[1], c, expected);
                testLastIndexOf2(new String(surrogates) + s, c, expected + 2);
                testLastIndexOf2(surrogates[0] + s, c, expected + 1);
                testLastIndexOf2(surrogates[1] + s, c, expected + 1);
            }
        }
    }
    static void testLastIndexOf2(String s, int c, int expected) {
        int index = s.lastIndexOf(c);
        check(index != expected,
              "lastIndexOf(" + toHexString(c) + ") for <" + s + ">",
              index, expected);
    }
    static int testIndexOf(String s, int fromIndex, int c, int expected) {
        int index = s.indexOf(c, fromIndex);
        check(index != expected,
              "indexOf(" + toHexString(c) + ", "
              + fromIndex + ") for <" + s + ">",
              index, expected);
        return index;
    }
    static int testLastIndexOf(String s, int fromIndex, int c, int expected) {
        int index = s.lastIndexOf(c, fromIndex);
        check(index != expected,
              "lastIndexOf(" + toHexString(c) + ", "
              + fromIndex + ") for <" + s + ">",
              index, expected);
        return index;
    }
    static void testNewString(int[] codePoints, int offset, int count, Class expectedException) {
        try {
            String s = new String(codePoints, offset, count);
        } catch (Exception e) {
            if (expectedException.isInstance(e)) {
                return;
            }
            throw new RuntimeException("Error: Unexpected exception", e);
        }
        check(true, "new String(int[]...) didn't throw " + expectedException.getName());
    }
    static void testCodePointCount(String str, int beginIndex, int endIndex,
                                   Class expectedException) {
        try {
            int n = str.codePointCount(beginIndex, endIndex);
        } catch (Exception e) {
            if (expectedException.isInstance(e)) {
                return;
            }
            throw new RuntimeException("Error: Unexpected exception", e);
        }
        check(true, "codePointCount() didn't throw " + expectedException.getName());
    }
    static void testOffsetByCodePoints(String str, int index, int offset,
                                       Class expectedException) {
        try {
            int n = str.offsetByCodePoints(index, offset);
        } catch (Exception e) {
            if (expectedException.isInstance(e)) {
                return;
            }
            throw new RuntimeException("Error: Unexpected exception", e);
        }
        check(true, "offsetByCodePoints() didn't throw " + expectedException.getName());
    }
    static void check(boolean err, String msg) {
        if (err) {
            throw new RuntimeException("Error: " + msg);
        }
    }
    static void check(boolean err, String s, int got, int expected) {
        if (err) {
            throw new RuntimeException("Error: " + s
                                       + " returned an unexpected value. got "
                                       + toHexString(got)
                                       + ", expected "
                                       + toHexString(expected));
        }
    }
    private static String toHexString(int c) {
        return "0x" + Integer.toHexString(c);
    }
}
