        value = Pattern.class,
        untestedMethods= {
            @TestTargetNew(
                level = TestLevel.NOT_FEASIBLE,
                notes = "finalize is hard to test since the implementation only calls a native function",
                method = "finalize",
                args = {}
            )
        }        
)
public class PatternTest extends TestCase {
    String[] testPatterns = {
            "(a|b)*abb",
            "(1*2*3*4*)*567",
            "(a|b|c|d)*aab",
            "(1|2|3|4|5|6|7|8|9|0)(1|2|3|4|5|6|7|8|9|0)*",
            "(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ)*",
            "(a|b)*(a|b)*A(a|b)*lice.*",
            "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|"
                    + "i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*(1|2|3|4|5|6|7|8|9|0)*|while|for|struct|if|do",
    };
    String[] testPatternsAlt = {
            "[ab]\\b\\\\o5\\xF9\\u1E7B\\t\\n\\f\\r\\a\\e[yz]",
            "^\\p{Lower}*\\p{Upper}*\\p{ASCII}?\\p{Alpha}?\\p{Digit}*\\p{Alnum}\\p{Punct}\\p{Graph}\\p{Print}\\p{Blank}\\p{Cntrl}\\p{XDigit}\\p{Space}",
            "$\\p{javaLowerCase}\\p{javaUpperCase}\\p{javaWhitespace}\\p{javaMirrored}",
            "\\p{InGreek}\\p{Lu}\\p{Sc}\\P{InGreek}[\\p{L}&&[^\\p{Lu}]]" };
    String[] wrongTestPatterns = { "\\o9A", "\\p{Lawer}", "\\xG0" };
    final static int[] flagsSet = { Pattern.CASE_INSENSITIVE,
            Pattern.MULTILINE, Pattern.DOTALL, Pattern.UNICODE_CASE
             };
    final static int[] wrongFlagsSet = { 256, 512, 1024 };
    final static int DEFAULT_FLAGS = 0;
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "!",
        args = {}
    )
    public void testMatcher() {
        Pattern p = Pattern.compile("a");
        assertNotNull(p.matcher("bcde"));
        assertNotSame(p.matcher("a"), p.matcher("a"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of splitsplit(java.lang.String, int) method.",
        method = "split",
        args = {CharSequence.class, int.class}
    )          
    public void testSplitCharSequenceint() {
        assertEquals(",,".split(",", 3).length, 3);
        assertEquals(",,".split(",", 4).length, 3);
        assertEquals(Pattern.compile("o").split("boo:and:foo", 5).length, 5);
        assertEquals(Pattern.compile("b").split("ab", -1).length, 2);
        String s[];
        Pattern pat = Pattern.compile("x");
        s = pat.split("zxx:zzz:zxx", 10);
        assertEquals(s.length, 5);
        s = pat.split("zxx:zzz:zxx", 3);
        assertEquals(s.length, 3);
        s = pat.split("zxx:zzz:zxx", -1);
        assertEquals(s.length, 5);
        s = pat.split("zxx:zzz:zxx", 0);
        assertEquals(s.length, 3);
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", -1);
        assertEquals(s.length, 5);
        s = pat.split("", -1);
        assertEquals(s.length, 1);
        pat = Pattern.compile("");
        s = pat.split("", -1);
        assertEquals(s.length, 1);
        s = pat.split("abccbadfe", -1);
        assertEquals(s.length, 11);
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 0);
        assertEquals(s.length, 3);
        s = pat.split("", 0);
        assertEquals(s.length, 1);
        pat = Pattern.compile("");
        s = pat.split("", 0);
        assertEquals(s.length, 1);
        s = pat.split("abccbadfe", 0);
        assertEquals(s.length, 10);
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 12);
        assertEquals(s.length, 5);
        s = pat.split("", 6);
        assertEquals(s.length, 1);
        pat = Pattern.compile("");
        s = pat.split("", 11);
        assertEquals(s.length, 1);
        s = pat.split("abccbadfe", 15);
        assertEquals(s.length, 11);
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 5);
        assertEquals(s.length, 5);
        s = pat.split("", 1);
        assertEquals(s.length, 1);
        pat = Pattern.compile("");
        s = pat.split("", 1);
        assertEquals(s.length, 1);
        s = pat.split("abccbadfe", 11);
        assertEquals(s.length, 11);
        pat = Pattern.compile("b");
        s = pat.split("abccbadfebb", 3);
        assertEquals(s.length, 3);
        pat = Pattern.compile("");
        s = pat.split("abccbadfe", 5);
        assertEquals(s.length, 5);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of splitsplit(java.lang.String) method.",
        method = "split",
        args = {CharSequence.class}
    )          
    public void testSplitCharSequence() {
        String s[];
        Pattern pat = Pattern.compile("b");
        s = pat.split("abccbadfebb");
        assertEquals(s.length, 3);
        s = pat.split("");
        assertEquals(s.length, 1);
        pat = Pattern.compile("");
        s = pat.split("");
        assertEquals(s.length, 1);
        s = pat.split("abccbadfe");
        assertEquals(s.length, 10);
        String s1 = "";
        String[] arr = s1.split(":");
        assertEquals(arr.length, 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the functionality of pattern() method.",
        method = "pattern",
        args = {}
    )          
    public void testPattern() {
        for (String aPattern : testPatterns) {
            Pattern p = Pattern.compile(aPattern);
            try {
                assertTrue(p.pattern().equals(aPattern));
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testCompile() {
        for (String aPattern : testPatterns) {
            try {
                Pattern p = Pattern.compile(aPattern);
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
        for (String aPattern : testPatternsAlt) {
            try {
                Pattern p = Pattern.compile(aPattern);
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
        for (String aPattern : wrongTestPatterns) {
            try {
                Pattern p = Pattern.compile(aPattern);
                fail("PatternSyntaxException is expected");
            } catch (PatternSyntaxException pse) {
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method for different flags.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFlags() {
        String baseString;
        String testString;
        Pattern pat;
        Matcher mat;
        baseString = "((?i)|b)a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        baseString = "(?i)a|b";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)a|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "c|(?i)a|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)a|(?s)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)a|(?-i)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        baseString = "(?i)a|(?-i)c|b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        baseString = "(?i)a|(?-i)c|(?i)b";
        testString = "B";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)a|(?-i)b";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "((?i))a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        baseString = "|(?i)|a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)((?s)a.)";
        testString = "A\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)((?-i)a)";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        baseString = "(?i)(?s:a.)";
        testString = "A\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)fgh(?s:aa)";
        testString = "fghAA";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?i)((?-i))a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "abc(?i)d";
        testString = "ABCD";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        testString = "abcD";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "a(?i)a(?-i)a(?i)a(?-i)a";
        testString = "aAaAa";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "aAAAa";
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of flags() method for default flags.",
        method = "flags",
        args = {}
    )          
    public void testFlagsCompileDefault() {
        for (String pat : testPatternsAlt) {
            try {
                Pattern p = Pattern.compile(pat);
                assertEquals(p.flags(), DEFAULT_FLAGS);
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & flags() methods. Checks that compilation was corect.",
            method = "flags",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & flags() methods. Checks that compilation was corect.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        )
    })          
    public void testFlagsCompileValid() {
        for (String pat : testPatternsAlt) {
            for (int flags : flagsSet) {
                try {
                    Pattern p = Pattern.compile(pat, flags);
                    assertEquals(p.flags(), flags);
                } catch (Exception e) {
                    fail("Unexpected exception: " + e);
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.Checks that correct exceptions were thrown.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testCompileStringint() {
        String pattern = "b)a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
        }
        pattern = "bcde)a";
        try {
            Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
        }
        pattern = "bbg())a";
        try {
            Pattern pat = Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
        }
        pattern = "cdb(?i))a";
        try {
            Pattern pat = Pattern.compile(pattern);
            fail("Expected a PatternSyntaxException when compiling pattern: "
                    + pattern);
        } catch (PatternSyntaxException e) {
        }
        pattern = "(b\\1)a";
        Pattern.compile(pattern);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.Checks that correct exceptions were thrown.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testQuantCompileNeg() {
        String[] patterns = { "5{,2}", "{5asd", "{hgdhg", "{5,hjkh", "{,5hdsh",
                "{5,3shdfkjh}" };
        for (String element : patterns) {
            try {
                Pattern.compile(element);
                fail("PatternSyntaxException was expected, but compilation succeeds");
            } catch (PatternSyntaxException pse) {
                continue;
            }
        }
        String pattern = "(?![^\\<C\\f\\0146\\0270\\}&&[|\\02-\\x3E\\}|X-\\|]]{7,}+)[|\\\\\\x98\\<\\?\\u4FCFr\\,\\0025\\}\\004|\\0025-\\052\061]|(?<![|\\01-\\u829E])|(?<!\\p{Alpha})|^|(?-s:[^\\x15\\\\\\x24F\\a\\,\\a\\u97D8[\\x38\\a[\\0224-\\0306[^\\0020-\\u6A57]]]]??)(?uxix:[^|\\{\\[\\0367\\t\\e\\x8C\\{\\[\\074c\\]V[|b\\fu\\r\\0175\\<\\07f\\066s[^D-\\x5D]]])(?xx:^{5,}+)(?uuu)(?=^\\D)|(?!\\G)(?>\\.*?)(?![^|\\]\\070\\ne\\{\\t\\[\\053\\?\\\\\\x51\\a\\075\\0023-\\[&&[|\\022-\\xEA\\00-\\u41C2&&[^|a-\\xCC&&[^\\037\\uECB3\\u3D9A\\x31\\|\\<b\\0206\\uF2EC\\01m\\,\\ak\\a\\03&&\\p{Punct}]]]])(?-dxs:[|\\06-\\07|\\e-\\x63&&[|Tp\\u18A3\\00\\|\\xE4\\05\\061\\015\\0116C|\\r\\{\\}\\006\\xEA\\0367\\xC4\\01\\0042\\0267\\xBB\\01T\\}\\0100\\?[|\\[-\\u459B|\\x23\\x91\\rF\\0376[|\\?-\\x94\\0113-\\\\\\s]]]]{6}?)(?<=[^\\t-\\x42H\\04\\f\\03\\0172\\?i\\u97B6\\e\\f\\uDAC2])(?=\\.*+)(?>[^\\016\\r\\{\\,\\uA29D\\034\\02[\\02-\\[|\\t\\056\\uF599\\x62\\e\\<\\032\\uF0AC\\0026\\0205Q\\|\\\\\\06\\0164[|\\057-\\u7A98&&[\\061-g|\\|\\0276\\n\\042\\011\\e\\xE8\\x64B\\04\\u6D0EDW^\\p{Lower}]]]]?)(?<=[^\\n\\\\\\t\\u8E13\\,\\0114\\u656E\\xA5\\]&&[\\03-\\026|\\uF39D\\01\\{i\\u3BC2\\u14FE]])(?<=[^|\\uAE62\\054H\\|\\}&&^\\p{Space}])(?sxx)(?<=[\\f\\006\\a\\r\\xB4]{1,5})|(?x-xd:^{5}+)()";
        assertNotNull(Pattern.compile(pattern));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testQuantCompilePos() {
        String[] patterns = {"abc{2,}", "abc{5}" };
        for (String element : patterns) {
            Pattern.compile(element);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile() method. Also tested methods from matcher: matches(), start(int), group(int)",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testQuantComposition() {
        String pattern = "(a{1,3})aab";
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher mat = pat.matcher("aaab");
        mat.matches();
        mat.start(1);
        mat.group(1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testMatches() {
        String[][] posSeq = {
                { "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
                { "213567", "12324567", "1234567", "213213567",
                        "21312312312567", "444444567" },
                { "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
                { "213234567", "3458", "0987654", "7689546432", "0398576",
                        "98432", "5" },
                {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
                { "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
                        "abbbAbbbliceaaa", "Alice" },
                { "a123", "bnxnvgds156", "for", "while", "if", "struct" },
                { "xy" }, { "xy" }, { "xcy" }
        };
        for (int i = 0; i < testPatterns.length; i++) {
            for (int j = 0; j < posSeq[i].length; j++) {
                assertTrue("Incorrect match: " + testPatterns[i] + " vs "
                        + posSeq[i][j], Pattern.matches(testPatterns[i],
                        posSeq[i][j]));
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exception",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testMatchesException() {
        for (String aPattern : wrongTestPatterns) {
            try {
                Pattern.matches(aPattern, "Foo");
                fail("PatternSyntaxException is expected");
            } catch (PatternSyntaxException pse) {
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The test verifies the functionality of matches(java.lang.String,java.lang.CharSequence) method.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testTimeZoneIssue() {
        Pattern p = Pattern.compile("GMT(\\+|\\-)(\\d+)(:(\\d+))?");
        Matcher m = p.matcher("GMT-9:45");
        assertTrue(m.matches());
        assertEquals("-", m.group(1));
        assertEquals("9", m.group(2));
        assertEquals(":45", m.group(3));
        assertEquals("45", m.group(4));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of matches(java.lang.String,java.lang.CharSequence) method.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testCompileRanges() {
        String[] correctTestPatterns = { "[^]*abb]*", 
                "[a-d\\d]*abb", "[abc]*abb", "[a-e&&[de]]*abb", "[^abc]*abb",
                "[a-e&&[^de]]*abb", "[a-z&&[^m-p]]*abb", "[a-d[m-p]]*abb",
                "[a-zA-Z]*abb", "[+*?]*abb", "[^+*?]*abb" };
        String[] inputSecuence = { "kkkk",  "abcabcd124654abb",
                "abcabccbacababb", "dededededededeedabb", "gfdhfghgdfghabb",
                "accabacbcbaabb", "acbvfgtyabb", "adbcacdbmopabcoabb",
                "jhfkjhaSDFGHJkdfhHNJMjkhfabb", "+*??+*abb", "sdfghjkabb" };
        Pattern pat;
        for (int i = 0; i < correctTestPatterns.length; i++) {
            assertTrue("pattern: " + correctTestPatterns[i] + " input: "
                    + inputSecuence[i], Pattern.matches(correctTestPatterns[i],
                    inputSecuence[i]));
        }
        String[] wrongInputSecuence = { "]",   "abcabcd124k654abb",
                "abwcabccbacababb", "abababdeababdeabb", "abcabcacbacbabb",
                "acdcbecbaabb", "acbotyabb", "adbcaecdbmopabcoabb",
                "jhfkjhaSDFGHJk;dfhHNJMjkhfabb", "+*?a?+*abb", "sdf+ghjkabb" };
        for (int i = 0; i < correctTestPatterns.length; i++) {
            assertFalse("pattern: " + correctTestPatterns[i] + " input: "
                    + wrongInputSecuence[i], Pattern.matches(
                    correctTestPatterns[i], wrongInputSecuence[i]));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of matches(java.lang.String,java.lang.CharSequence) method for ranged patterns.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testRangesSpecialCases() {
        String neg_patterns[] = { "[a-&&[b-c]]", "[a-\\w]", "[b-a]", "[]" };
        for (String element : neg_patterns) {
            try {
                Pattern.compile(element);
                fail("PatternSyntaxException was expected: " + element);
            } catch (PatternSyntaxException pse) {
            }
        }
        String pos_patterns[] = { "[-]+", "----", "[a-]+", "a-a-a-a-aa--",
                "[\\w-a]+", "123-2312--aaa-213", "[a-]]+", "-]]]]]]]]]]]]]]]" };
        for (int i = 0; i < pos_patterns.length; i++) {
            String pat = pos_patterns[i++];
            String inp = pos_patterns[i];
            assertTrue("pattern: " + pat + " input: " + inp, Pattern.matches(
                    pat, inp));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The test verifies the functionality of matches(java.lang.String,java.lang.CharSequence) method.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
public void testZeroSymbols() {
        assertTrue(Pattern.matches("[\0]*abb", "\0\0\0\0\0\0abb"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of matcher(java.lang.String) method.",
        method = "matcher",
        args = {CharSequence.class}
    )          
    public void testEscapes() {
        Pattern pat = Pattern.compile("\\Q{]()*?");
        Matcher mat = pat.matcher("{]()*?");
        assertTrue(mat.matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testBug181() {
        Pattern.compile("[\\t-\\r]");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testOrphanQuantifiers() {
        try {
            Pattern.compile("+++++");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException pse) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testOrphanQuantifiers2() {
        try {
            Pattern pat = Pattern.compile("\\d+*");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException pse) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) method.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) method.",
            method = "compile",
            args = {java.lang.String.class}
        )
    })          
    public void testBug197() {
        Object[] vals = { ":", new Integer(2),
                new String[] { "boo", "and:foo" }, ":", new Integer(5),
                new String[] { "boo", "and", "foo" }, ":", new Integer(-2),
                new String[] { "boo", "and", "foo" }, ":", new Integer(3),
                new String[] { "boo", "and", "foo" }, ":", new Integer(1),
                new String[] { "boo:and:foo" }, "o", new Integer(5),
                new String[] { "b", "", ":and:f", "", "" }, "o",
                new Integer(4), new String[] { "b", "", ":and:f", "o" }, "o",
                new Integer(-2), new String[] { "b", "", ":and:f", "", "" },
                "o", new Integer(0), new String[] { "b", "", ":and:f" } };
        for (int i = 0; i < vals.length / 3;) {
            String[] res = Pattern.compile(vals[i++].toString()).split(
                    "boo:and:foo", ((Integer) vals[i++]).intValue());
            String[] expectedRes = (String[]) vals[i++];
            assertEquals(expectedRes.length, res.length);
            for (int j = 0; j < expectedRes.length; j++) {
                assertEquals(expectedRes[j], res[j]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The test verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testURIPatterns() {
        String URI_REGEXP_STR = "^(([^:/?#]+):)?(
        String SCHEME_REGEXP_STR = "^[a-zA-Z]{1}[\\w+-.]+$";
        String REL_URI_REGEXP_STR = "^(
        String IPV6_REGEXP_STR = "^[0-9a-fA-F\\:\\.]+(\\%\\w+)?$";
        String IPV6_REGEXP_STR2 = "^\\[[0-9a-fA-F\\:\\.]+(\\%\\w+)?\\]$";
        String IPV4_REGEXP_STR = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
        String HOSTNAME_REGEXP_STR = "\\w+[\\w\\-\\.]*";
        Pattern URI_REGEXP = Pattern.compile(URI_REGEXP_STR);
        Pattern REL_URI_REGEXP = Pattern.compile(REL_URI_REGEXP_STR);
        Pattern SCHEME_REGEXP = Pattern.compile(SCHEME_REGEXP_STR);
        Pattern IPV4_REGEXP = Pattern.compile(IPV4_REGEXP_STR);
        Pattern IPV6_REGEXP = Pattern.compile(IPV6_REGEXP_STR);
        Pattern IPV6_REGEXP2 = Pattern.compile(IPV6_REGEXP_STR2);
        Pattern HOSTNAME_REGEXP = Pattern.compile(HOSTNAME_REGEXP_STR);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases1() {
        Pattern pat = Pattern.compile(".*\n");
        Matcher mat = pat.matcher("a\n");
        mat.find();
        assertEquals("a\n", mat.group());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases2() {
        Pattern pat = Pattern.compile(".*A");
        Matcher mat = pat.matcher("aAa");
        mat.find();
        assertEquals("aA", mat.group());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases3() {
        Pattern pat = Pattern.compile(".*A");
        Matcher mat = pat.matcher("a\naA\n");
        mat.find();
        assertEquals("aA", mat.group());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases4() {
        Pattern pat = Pattern.compile("A.*");
        Matcher mat = pat.matcher("A\n");
        mat.find();
        assertEquals("A", mat.group());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases5() {
        Pattern pat = Pattern.compile(".*A.*");
        Matcher mat = pat.matcher("\nA\naaa\nA\naaAaa\naaaA\n");
        String[] res = { "A", "A", "aaAaa", "aaaA" };
        int k = 0;
        for (; mat.find(); k++) {
            assertEquals(res[k], mat.group());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testFindBoundaryCases6() {
        String[] res = { "", "a", "", "" };
        Pattern pat = Pattern.compile(".*");
        Matcher mat = pat.matcher("\na\n");
        int k = 0;
        for (; mat.find(); k++) {
            assertEquals(res[k], mat.group());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testBackReferences() {
        Pattern pat = Pattern.compile("(\\((\\w*):(.*):(\\2)\\))");
        Matcher mat = pat
                .matcher("(start1: word :start1)(start2: word :start2)");
        int k = 1;
        for (; mat.find(); k++) {
            assertEquals("start" + k, mat.group(2));
            assertEquals(" word ", mat.group(3));
            assertEquals("start" + k, mat.group(4));
        }
        assertEquals(3, k);
        pat = Pattern.compile(".*(.)\\1");
        mat = pat.matcher("saa");
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testNewLine() {
        Pattern pat = Pattern.compile("(^$)*\n", Pattern.MULTILINE);
        Matcher mat = pat.matcher("\r\n\n");
        int counter = 0;
        while (mat.find()) {
            counter++;
        }
        assertEquals(2, counter);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testFindGreedy() {
        Pattern pat = Pattern.compile(".*aaa", Pattern.DOTALL);
        Matcher mat = pat.matcher("aaaa\naaa\naaaaaa");
        mat.matches();
        assertEquals(15, mat.end());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization.",
            method = "!SerializationGolden",
            args = {}
        )
    })
    public void testSerialization() throws Exception {
        Pattern pat = Pattern.compile("a*bc");
        SerializableAssert comparator = new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                assertEquals(((Pattern) initial).toString(),
                        ((Pattern) deserialized).toString());
            }
        };
        SerializationTest.verifyGolden(this, pat, comparator);
        SerializationTest.verifySelf(pat, comparator);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testSOLQuant() {
        Pattern pat = Pattern.compile("$*", Pattern.MULTILINE);
        Matcher mat = pat.matcher("\n\n");
        int counter = 0;
        while (mat.find()) {
            counter++;
        }
        assertEquals(3, counter);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testIllegalEscape() {
        try {
            Pattern.compile("\\y");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException pse) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testEmptyFamily() {
        Pattern.compile("\\p{Lower}");
        String a = "*";
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testNonCaptConstr() {
        Pattern pat = Pattern.compile("(?i)b*(?-i)a*");
        assertTrue(pat.matcher("bBbBaaaa").matches());
        assertFalse(pat.matcher("bBbBAaAa").matches());
        pat = Pattern.compile("(?i:b*)a*");
        assertTrue(pat.matcher("bBbBaaaa").matches());
        assertFalse(pat.matcher("bBbBAaAa").matches());
        pat = Pattern
                .compile("(?:-|(-?\\d+\\d\\d\\d))?(?:-|-(\\d\\d))?(?:-|-(\\d\\d))?(T)?(?:(\\d\\d):(\\d\\d):(\\d\\d)(\\.\\d+)?)?(?:(?:((?:\\+|\\-)\\d\\d):(\\d\\d))|(Z))?");
        Matcher mat = pat.matcher("-1234-21-31T41:51:61.789+71:81");
        assertTrue(mat.matches());
        assertEquals("-1234", mat.group(1));
        assertEquals("21", mat.group(2));
        assertEquals("31", mat.group(3));
        assertEquals("T", mat.group(4));
        assertEquals("41", mat.group(5));
        assertEquals("51", mat.group(6));
        assertEquals("61", mat.group(7));
        assertEquals(".789", mat.group(8));
        assertEquals("+71", mat.group(9));
        assertEquals("81", mat.group(10));
        pat = Pattern.compile(".*\\.(?=log$).*$");
        assertTrue(pat.matcher("a.b.c.log").matches());
        assertFalse(pat.matcher("a.b.c.log.").matches());
        pat = Pattern.compile(".*\\.(?!log$).*$");
        assertFalse(pat.matcher("abc.log").matches());
        assertTrue(pat.matcher("abc.logg").matches());
        pat = Pattern.compile(".*(?<=abc)\\.log$");
        assertFalse(pat.matcher("cde.log").matches());
        assertTrue(pat.matcher("abc.log").matches());
        pat = Pattern.compile(".*(?<!abc)\\.log$");
        assertTrue(pat.matcher("cde.log").matches());
        assertFalse(pat.matcher("abc.log").matches());
        pat = Pattern.compile("(?>a*)abb");
        assertFalse(pat.matcher("aaabb").matches());
        pat = Pattern.compile("(?>a*)bb");
        assertTrue(pat.matcher("aaabb").matches());
        pat = Pattern.compile("(?>a|aa)aabb");
        assertTrue(pat.matcher("aaabb").matches());
        pat = Pattern.compile("(?>aa|a)aabb");
        assertFalse(pat.matcher("aaabb").matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testCorrectReplacementBackreferencedJointSet() {
        Pattern pat = Pattern.compile("ab(a)*\\1");
        pat = Pattern.compile("abc(cd)fg");
        pat = Pattern.compile("aba*cd");
        pat = Pattern.compile("ab(a)*+cd");
        pat = Pattern.compile("ab(a)*?cd");
        pat = Pattern.compile("ab(a)+cd");
        pat = Pattern.compile(".*(.)\\1");
        pat = Pattern.compile("ab((a)|c|d)e");
        pat = Pattern.compile("abc((a(b))cd)");
        pat = Pattern.compile("ab(a)++cd");
        pat = Pattern.compile("ab(a)?(c)d");
        pat = Pattern.compile("ab(a)?+cd");
        pat = Pattern.compile("ab(a)??cd");
        pat = Pattern.compile("ab(a)??cd");
        pat = Pattern.compile("ab(a){1,3}?(c)d");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testCompilePatternWithTerminatorMark() {
        Pattern pat = Pattern.compile("a\u0000\u0000cd");
        Matcher mat = pat.matcher("a\u0000\u0000cd");
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testAlternations() {
        String baseString = "|a|bc";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "a||bc";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "a|bc|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "a|b|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "a(|b|cd)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "a(b||cd)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "a(b|cd|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "a(b|c|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "a(|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "|";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "a(?:|)e";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("ae");
        assertTrue(mat.matches());
        baseString = "a||||bc";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("");
        assertTrue(mat.matches());
        baseString = "(?i-is)|a";
        pat = Pattern.compile(baseString);
        mat = pat.matcher("a");
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testMatchWithGroups() {
        String baseString = "jwkerhjwehrkwjehrkwjhrwkjehrjwkehrjkwhrkwehrkwhrkwrhwkhrwkjehr";
        String pattern = ".*(..).*\\1.*";
        assertTrue(Pattern.compile(pattern).matcher(baseString).matches());
        baseString = "saa";
        pattern = ".*(.)\\1";
        assertTrue(Pattern.compile(pattern).matcher(baseString).matches());
        assertTrue(Pattern.compile(pattern).matcher(baseString).find());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies split method for empty string.",
        method = "split",
        args = {java.lang.CharSequence.class}
    )
    public void testSplitEmptyCharSequence() {
        String s1 = "";
        String[] arr = s1.split(":");
        assertEquals(arr.length, 1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "The test verifies the functionality of compile(java.lang.String) & split(java.lang.CharSequence, int) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "The test verifies the functionality of compile(java.lang.String) & split(java.lang.CharSequence, int) methods.",
            method = "split",
            args = {java.lang.CharSequence.class, int.class}
        )
    })          
    public void testSplitEndsWithPattern() {
        assertEquals(",,".split(",", 3).length, 3);
        assertEquals(",,".split(",", 4).length, 3);
        assertEquals(Pattern.compile("o").split("boo:and:foo", 5).length, 5);
        assertEquals(Pattern.compile("b").split("ab", -1).length, 2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The test verifies the functionality of matches(java.lang.String), java.lang.CharSequence) method for case insensitive flags.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )          
    public void testCaseInsensitiveFlag() {
        assertTrue(Pattern.matches("(?i-:AbC)", "ABC"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testEmptyGroups() {
        Pattern pat = Pattern.compile("ab(?>)cda");
        Matcher mat = pat.matcher("abcda");
        assertTrue(mat.matches());
        pat = Pattern.compile("ab()");
        mat = pat.matcher("ab");
        assertTrue(mat.matches());
        pat = Pattern.compile("abc(?:)(..)");
        mat = pat.matcher("abcgf");
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & compile(java.lang.String, int) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & compile(java.lang.String, int) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        )
    })          
    public void testCompileNonCaptGroup() {
        boolean isCompiled = false;
        try {
            Pattern pat = Pattern.compile("(?:)");
            pat = Pattern.compile("(?:)", Pattern.DOTALL);
            pat = Pattern.compile("(?:)", Pattern.CASE_INSENSITIVE);
            pat = Pattern.compile("(?:)", Pattern.COMMENTS | Pattern.UNIX_LINES);
            isCompiled = true;
        } catch (PatternSyntaxException e) {
            System.out.println(e);
        }
        assertTrue(isCompiled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testEmbeddedFlags() {
        String baseString = "(?i)((?s)a)";
        String testString = "A";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?x)(?i)(?s)(?d)a";
        testString = "A";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "(?x)(?i)(?s)(?d)a.";
        testString = "a\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "abc(?x:(?i)(?s)(?d)a.)";
        testString = "abcA\n";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        baseString = "abc((?x)d)(?i)(?s)a";
        testString = "abcdA";
        pat = Pattern.compile(baseString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testAltWithFlags() {
        boolean isCompiled = false;
        try {
            Pattern pat = Pattern.compile("|(?i-xi)|()");
            isCompiled = true;
        } catch (PatternSyntaxException e) {
            System.out.println(e);
        }
        assertTrue(isCompiled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
public void testRestoreFlagsAfterGroup() {
        String baseString = "abc((?x)d)   a";
        String testString = "abcd   a";
        Pattern pat = Pattern.compile(baseString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of compile(java.lang.String) method.",
        method = "compile",
        args = {java.lang.String.class}
    )          
    public void testCompileCharacterClass() {
        Pattern pattern = Pattern.compile("\\p{javaLowerCase}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaUpperCase}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaWhitespace}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaMirrored}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaDefined}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaDigit}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaIdentifierIgnorable}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaISOControl}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaJavaIdentifierPart}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaJavaIdentifierStart}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaLetter}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaLetterOrDigit}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaSpaceChar}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaTitleCase}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaUnicodeIdentifierPart}");
        assertNotNull(pattern);
        pattern = Pattern.compile("\\p{javaUnicodeIdentifierStart}");
        assertNotNull(pattern);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testRangesWithSurrogatesSupplementary() {
        String patString = "[abc\uD8D2]";
        String testString = "\uD8D2";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "a";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "ef\uD8D2\uDD71gh";
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "ef\uD8D2gh";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[abc\uD8D3&&[c\uD8D3]]";
        testString = "c";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "a";
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        testString = "ef\uD8D3\uDD71gh";
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "ef\uD8D3gh";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[abc\uD8D3\uDBEE\uDF0C&&[c\uD8D3\uDBEE\uDF0C]]";
        testString = "c";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uDBEE\uDF0C";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "ef\uD8D3\uDD71gh";
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "ef\uD8D3gh";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[abc\uDBFC]\uDDC2cd";
        testString = "\uDBFC\uDDC2cd";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        testString = "a\uDDC2cd";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testSequencesWithSurrogatesSupplementary() {
        String patString = "abcd\uD8D3";
        String testString = "abcd\uD8D3\uDFFC";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        testString = "abcd\uD8D3abc";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "ab\uDBEFcd";
        testString = "ab\uDBEFcd";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        patString = "\uDFFCabcd";
        testString = "\uD8D3\uDFFCabcd";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "abc\uDFFCabcdecd";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "\uD8D3\uDFFCabcd";
        testString = "abc\uD8D3\uD8D3\uDFFCabcd";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.find());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testPredefinedClassesWithSurrogatesSupplementary() {
        String patString = "[123\\D]";
        String testString = "a";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "5";
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "3";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDFC4";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDADA";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDADA\uDFC4";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[123[^\\p{javaDigit}]]";
        testString = "a";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "5";
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "3";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDFC4";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDADA";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uDADA\uDFC4";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "\\p{Cs}";
        testString = "\uD916\uDE27";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        testString = "\uDE27\uD916";
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[\uD916\uDE271\uD91623&&[^\\p{Cs}]]";
        testString = "1";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        testString = "\uD916";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertFalse(mat.find());
        testString = "\uD916\uDE27";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.find());
        patString = "[a-\uD9A0\uDE8E]";
        testString = "\uD9A0\uDE81";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) &  compile(java.lang.String, int) matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) &  compile(java.lang.String, int) matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) &  compile(java.lang.String, int) matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testDotConstructionWithSurrogatesSupplementary() {
        String patString = ".";
        String testString = "\uD9A0\uDE81";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uDE81";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uD9A0";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\n";
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        patString = ".*\uDE81";
        testString = "\uD9A0\uDE81\uD9A0\uDE81\uD9A0\uDE81";
        pat = Pattern.compile(patString);
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
        testString = "\uD9A0\uDE81\uD9A0\uDE81\uDE81";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        patString = ".*";
        testString = "\uD9A0\uDE81\n\uD9A0\uDE81\uD9A0\n\uDE81";
        pat = Pattern.compile(patString, Pattern.DOTALL);
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "quote",
        args = {java.lang.String.class}
    )
    public void test_quoteLjava_lang_String() {
        for (String aPattern : testPatterns) {
            Pattern p = Pattern.compile(aPattern);
            try {
                assertEquals("quote was wrong for plain text", "\\Qtest\\E", p
                        .quote("test"));
                assertEquals("quote was wrong for text with quote sign",
                        "\\Q\\Qtest\\E", p.quote("\\Qtest"));
                assertEquals("quote was wrong for quotted text",
                        "\\Q\\Qtest\\E\\\\E\\Q\\E", p.quote("\\Qtest\\E"));
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void test_matcherLjava_lang_StringLjava_lang_CharSequence() {
        String[][] posSeq = {
                { "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
                { "213567", "12324567", "1234567", "213213567",
                        "21312312312567", "444444567" },
                { "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
                { "213234567", "3458", "0987654", "7689546432", "0398576",
                        "98432", "5" },
                {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
                { "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
                        "abbbAbbbliceaaa", "Alice" },
                { "a123", "bnxnvgds156", "for", "while", "if", "struct" },
                { "xy" }, { "xy" }, { "xcy" }
        };
        for (int i = 0; i < testPatterns.length; i++) {
            for (int j = 0; j < posSeq[i].length; j++) {
                assertTrue("Incorrect match: " + testPatterns[i] + " vs "
                        + posSeq[i][j], Pattern.compile(testPatterns[i])
                        .matcher(posSeq[i][j]).matches());
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testQuantifiersWithSurrogatesSupplementary() {
        String patString = "\uD9A0\uDE81*abc";
        String testString = "\uD9A0\uDE81\uD9A0\uDE81abc";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "abc";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testAlternationsWithSurrogatesSupplementary() {
        String patString = "\uDE81|\uD9A0\uDE81|\uD9A0";
        String testString = "\uD9A0";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uDE81";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uD9A0\uDE81";
        mat = pat.matcher(testString);
        assertTrue(mat.matches());
        testString = "\uDE81\uD9A0";
        mat = pat.matcher(testString);
        assertFalse(mat.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of compile(java.lang.String) & compile(java.lang.String, int) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testGroupsWithSurrogatesSupplementary() {
        String patString = "(\uD9A0)\uDE81";
        String testString = "\uD9A0\uDE81";
        Pattern pat = Pattern.compile(patString);
        Matcher mat = pat.matcher(testString);
        assertFalse(mat.matches());
        patString = "(\uD9A0)";
        testString = "\uD9A0\uDE81";
        pat = Pattern.compile(patString, Pattern.DOTALL);
        mat = pat.matcher(testString);
        assertFalse(mat.find());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "The test verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "The test verifies the functionality of compile(java.lang.String) & matcher(java.lang.CharSequence) methods.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })          
    public void testUnicodeCategoryWithSurrogatesSupplementary() {
        Pattern p = Pattern.compile("\\p{javaLowerCase}");
        Matcher matcher = p.matcher("\uD801\uDC28");
        assertTrue(matcher.find());
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PatternTest.class);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The test doesn't verify Matcher and should be moved to PatterTest.",
        method = "split",
        args = {java.lang.CharSequence.class, int.class}
    )
    public void testSplitEmpty() {
        Pattern pat = Pattern.compile("");
        String[] s = pat.split("", -1);
        assertEquals(1, s.length);
        assertEquals("", s[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        for (int i = 0; i < testPatterns.length; i++) {
            Pattern p = Pattern.compile(testPatterns[i]);
            assertEquals(testPatterns[i], p.toString());
        }
    }
}
