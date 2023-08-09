@TestTargetClass(Pattern.class)
public class Pattern2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches(String regex, CharSequence input) method.",
        method = "matches",
        args = {java.lang.String.class, java.lang.CharSequence.class}
    )    
    public void testSimpleMatch() throws PatternSyntaxException {
        Pattern p = Pattern.compile("foo.*");
        Matcher m1 = p.matcher("foo123");
        assertTrue(m1.matches());
        assertTrue(m1.find(0));
        assertTrue(m1.lookingAt());
        Matcher m2 = p.matcher("fox");
        assertFalse(m2.matches());
        assertFalse(m2.find(0));
        assertFalse(m2.lookingAt());
        assertTrue(Pattern.matches("foo.*", "foo123"));
        assertFalse(Pattern.matches("foo.*", "fox"));
        assertFalse(Pattern.matches("bar", "foobar"));
        assertTrue(Pattern.matches("", ""));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testCursors() {
        Pattern p;
        Matcher m;
        try {
            p = Pattern.compile("foo");
            m = p.matcher("foobar");
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertFalse(m.find());
            m.reset();
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertFalse(m.find());
            m.reset("barfoobar");
            assertTrue(m.find());
            assertEquals(3, m.start());
            assertEquals(6, m.end());
            assertFalse(m.find());
            m.reset("barfoo");
            assertTrue(m.find());
            assertEquals(3, m.start());
            assertEquals(6, m.end());
            assertFalse(m.find());
            m.reset("foobarfoobarfoo");
            assertTrue(m.find());
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertTrue(m.find());
            assertEquals(6, m.start());
            assertEquals(9, m.end());
            assertTrue(m.find());
            assertEquals(12, m.start());
            assertEquals(15, m.end());
            assertFalse(m.find());
            assertTrue(m.find(0));
            assertEquals(0, m.start());
            assertEquals(3, m.end());
            assertTrue(m.find(4));
            assertEquals(6, m.start());
            assertEquals(9, m.end());
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("(p[0-9]*)#?(q[0-9]*)");
        m = p.matcher("p1#q3p2q42p5p71p63#q888");
        assertTrue(m.find());
        assertEquals(0, m.start());
        assertEquals(5, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(0, m.start(0));
        assertEquals(5, m.end(0));
        assertEquals(0, m.start(1));
        assertEquals(2, m.end(1));
        assertEquals(3, m.start(2));
        assertEquals(5, m.end(2));
        assertEquals("p1#q3", m.group());
        assertEquals("p1#q3", m.group(0));
        assertEquals("p1", m.group(1));
        assertEquals("q3", m.group(2));
        assertTrue(m.find());
        assertEquals(5, m.start());
        assertEquals(10, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(10, m.end(0));
        assertEquals(5, m.start(1));
        assertEquals(7, m.end(1));
        assertEquals(7, m.start(2));
        assertEquals(10, m.end(2));
        assertEquals("p2q42", m.group());
        assertEquals("p2q42", m.group(0));
        assertEquals("p2", m.group(1));
        assertEquals("q42", m.group(2));
        assertTrue(m.find());
        assertEquals(15, m.start());
        assertEquals(23, m.end());
        assertEquals(2, m.groupCount());
        assertEquals(15, m.start(0));
        assertEquals(23, m.end(0));
        assertEquals(15, m.start(1));
        assertEquals(18, m.end(1));
        assertEquals(19, m.start(2));
        assertEquals(23, m.end(2));
        assertEquals("p63#q888", m.group());
        assertEquals("p63#q888", m.group(0));
        assertEquals("p63", m.group(1));
        assertEquals("q888", m.group(2));
        assertFalse(m.find());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testReplace() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("a*b");
        m = p.matcher("aabfooaabfooabfoob");
        assertTrue(m.replaceAll("-").equals("-foo-foo-foo-"));
        assertTrue(m.replaceFirst("-").equals("-fooaabfooabfoob"));
        p = Pattern.compile("([bB])yte");
        m = p.matcher("Byte for byte");
        assertTrue(m.replaceFirst("$1ite").equals("Bite for byte"));
        assertTrue(m.replaceAll("$1ite").equals("Bite for bite"));
        p = Pattern.compile("\\d\\d\\d\\d([- ])");
        m = p.matcher("card #1234-5678-1234");
        assertTrue(m.replaceFirst("xxxx$1").equals("card #xxxx-5678-1234"));
        assertTrue(m.replaceAll("xxxx$1").equals("card #xxxx-xxxx-1234"));
        p = Pattern.compile("(up|left)( *)(right|down)");
        m = p.matcher("left right, up down");
        assertTrue(m.replaceFirst("$3$2$1").equals("right left, up down"));
        assertTrue(m.replaceAll("$3$2$1").equals("right left, down up"));
        p = Pattern.compile("([CcPp][hl]e[ea]se)");
        m = p.matcher("I want cheese. Please.");
        assertTrue(m.replaceFirst("<b> $1 </b>").equals(
                "I want <b> cheese </b>. Please."));
        assertTrue(m.replaceAll("<b> $1 </b>").equals(
                "I want <b> cheese </b>. <b> Please </b>."));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testEscapes() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("([a-z]+)\\\\([a-z]+);");
        m = p.matcher("fred\\ginger;abbott\\costello;jekell\\hyde;");
        assertTrue(m.find());
        assertEquals("fred", m.group(1));
        assertEquals("ginger", m.group(2));
        assertTrue(m.find());
        assertEquals("abbott", m.group(1));
        assertEquals("costello", m.group(2));
        assertTrue(m.find());
        assertEquals("jekell", m.group(1));
        assertEquals("hyde", m.group(2));
        assertFalse(m.find());
        p = Pattern.compile("([a-z]+)[\\n\\t\\r\\f\\e\\a]+([a-z]+)");
        m = p.matcher("aa\nbb;cc\u0009\rdd;ee\u000C\u001Bff;gg\n\u0007hh");
        assertTrue(m.find());
        assertEquals("aa", m.group(1));
        assertEquals("bb", m.group(2));
        assertTrue(m.find());
        assertEquals("cc", m.group(1));
        assertEquals("dd", m.group(2));
        assertTrue(m.find());
        assertEquals("ee", m.group(1));
        assertEquals("ff", m.group(2));
        assertTrue(m.find());
        assertEquals("gg", m.group(1));
        assertEquals("hh", m.group(2));
        assertFalse(m.find());
        p = Pattern.compile("([0-9]+)[\\07\\040\\0160];");
        m = p.matcher("11\u0007;22:;33 ;44p;");
        assertTrue(m.find());
        assertEquals("11", m.group(1));
        assertTrue(m.find());
        assertEquals("33", m.group(1));
        assertTrue(m.find());
        assertEquals("44", m.group(1));
        assertFalse(m.find());
        try {
            p = Pattern.compile("\\08");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\0");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\0;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        p = Pattern.compile("([0-9]+)[\\cA\\cB\\cC\\cD];");
        m = p.matcher("11\u0001;22:;33\u0002;44p;55\u0003;66\u0004;");
        assertTrue(m.find());
        assertEquals("11", m.group(1));
        assertTrue(m.find());
        assertEquals("33", m.group(1));
        assertTrue(m.find());
        assertEquals("55", m.group(1));
        assertTrue(m.find());
        assertEquals("66", m.group(1));
        assertFalse(m.find());
        int i, j;
        for (i = 0; i < 26; i++) {
            p = Pattern.compile("\\c" + Character.toString((char) ('A' + i)));
            int match_char = -1;
            for (j = 0; j < 255; j++) {
                m = p.matcher(Character.toString((char) j));
                if (m.matches()) {
                    assertEquals(-1, match_char);
                    match_char = j;
                }
            }
            assertTrue(match_char == i + 1);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies patterns with different ranges of characters.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies patterns with different ranges of characters.",
            method = "matcher",
            args = {CharSequence.class}
        )
    })
    public void testCharacterClasses() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("[p].*[l]");
        m = p.matcher("paul");
        assertTrue(m.matches());
        m = p.matcher("pool");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("pl");
        assertTrue(m.matches());
        p = Pattern.compile("[pm].*[lp]");
        m = p.matcher("prop");
        assertTrue(m.matches());
        m = p.matcher("mall");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("pill");
        assertTrue(m.matches());
        p = Pattern.compile("[<\\[].*[\\]>]");
        m = p.matcher("<foo>");
        assertTrue(m.matches());
        m = p.matcher("[bar]");
        assertTrue(m.matches());
        m = p.matcher("{foobar]");
        assertFalse(m.matches());
        m = p.matcher("<pill]");
        assertTrue(m.matches());
        p = Pattern.compile("[^bc][a-z]+[tr]");
        m = p.matcher("pat");
        assertTrue(m.matches());
        m = p.matcher("liar");
        assertTrue(m.matches());
        m = p.matcher("car");
        assertFalse(m.matches());
        m = p.matcher("gnat");
        assertTrue(m.matches());
        p = Pattern.compile("[a-z]_+[a-zA-Z]-+[0-9p-z]");
        m = p.matcher("d__F-8");
        assertTrue(m.matches());
        m = p.matcher("c_a-q");
        assertTrue(m.matches());
        m = p.matcher("a__R-a");
        assertFalse(m.matches());
        m = p.matcher("r_____d-----5");
        assertTrue(m.matches());
        p = Pattern.compile("[\\u1234-\\u2345]_+[a-z]-+[\u0001-\\x11]");
        m = p.matcher("\u2000_q-\u0007");
        assertTrue(m.matches());
        m = p.matcher("\u1234_z-\u0001");
        assertTrue(m.matches());
        m = p.matcher("r_p-q");
        assertFalse(m.matches());
        m = p.matcher("\u2345_____d-----\n");
        assertTrue(m.matches());
        p = Pattern.compile("[pm[t]][a-z]+[[r]lp]");
        m = p.matcher("prop");
        assertTrue(m.matches());
        m = p.matcher("tsar");
        assertTrue(m.matches());
        m = p.matcher("pong");
        assertFalse(m.matches());
        m = p.matcher("moor");
        assertTrue(m.matches());
        p = Pattern.compile("[[a-p]&&[g-z]]+-+[[a-z]&&q]-+[x&&[a-z]]-+");
        m = p.matcher("h--q--x--");
        assertTrue(m.matches());
        m = p.matcher("hog--q-x-");
        assertTrue(m.matches());
        m = p.matcher("ape--q-x-");
        assertFalse(m.matches());
        m = p.matcher("mop--q-x----");
        assertTrue(m.matches());
            p = Pattern.compile("[[xyz]&[axy]]");
            m = p.matcher("x");
            m = p.matcher("z");
            m = p.matcher("&");
            p = Pattern.compile("[abc[123]&&[345]def]");
            m = p.matcher("a");
            p = Pattern.compile("[[abc]&]");
        try {
            p = Pattern.compile("[[abc]&&");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        p = Pattern.compile("[[abc]\\&&[xyz]]");
        p = Pattern.compile("[[abc]&\\&[xyz]]");
        p = Pattern.compile("[[a-p]&&[g-z]&&[d-k]]");
        m = p.matcher("g");
        assertTrue(m.matches());
        m = p.matcher("m");
        assertFalse(m.matches());
        p = Pattern.compile("[[[a-p]&&[g-z]]&&[d-k]]");
        m = p.matcher("g");
        assertTrue(m.matches());
        m = p.matcher("m");
        assertFalse(m.matches());
        p = Pattern.compile("[[a-z]&&[^aeiou]][aeiou][[^xyz]&&[a-z]]");
        m = p.matcher("pop");
        assertTrue(m.matches());
        m = p.matcher("tag");
        assertTrue(m.matches());
        m = p.matcher("eat");
        assertFalse(m.matches());
        m = p.matcher("tax");
        assertFalse(m.matches());
        m = p.matcher("zip");
        assertTrue(m.matches());
        p = Pattern.compile(".+/x.z");
        m = p.matcher("!$/xyz");
        assertTrue(m.matches());
        m = p.matcher("%\n\r/x\nz");
        assertFalse(m.matches());
        p = Pattern.compile(".+/x.z", Pattern.DOTALL);
        m = p.matcher("%\n\r/x\nz");
        assertTrue(m.matches());
        p = Pattern.compile("\\d+[a-z][\\dx]");
        m = p.matcher("42a6");
        assertTrue(m.matches());
        m = p.matcher("21zx");
        assertTrue(m.matches());
        m = p.matcher("ab6");
        assertFalse(m.matches());
        m = p.matcher("56912f9");
        assertTrue(m.matches());
        p = Pattern.compile("\\D+[a-z]-[\\D3]");
        m = p.matcher("za-p");
        assertTrue(m.matches());
        m = p.matcher("%!e-3");
        assertTrue(m.matches());
        m = p.matcher("9a-x");
        assertFalse(m.matches());
        m = p.matcher("\u1234pp\ny-3");
        assertTrue(m.matches());
        p = Pattern.compile("<[a-zA-Z]+\\s+[0-9]+[\\sx][^\\s]>");
        m = p.matcher("<cat \t1\fx>");
        assertTrue(m.matches());
        m = p.matcher("<cat \t1\f >");
        assertFalse(m.matches());
        m = p
                .matcher("xyz <foo\n\r22 5> <pp \t\n\f\r \u000b41x\u1234><pp \nx7\rc> zzz");
        assertTrue(m.find());
        assertTrue(m.find());
        assertFalse(m.find());
        p = Pattern.compile("<[a-z] \\S[0-9][\\S\n]+[^\\S]221>");
        m = p.matcher("<f $0**\n** 221>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221>");
        assertTrue(m.matches());
        m = p.matcher("<z \t9\ng 221>");
        assertFalse(m.matches());
        m = p.matcher("<z 60\ngg\u1234\f221>");
        assertTrue(m.matches());
        p = Pattern.compile("<[a-z] \\S[0-9][\\S\n]+[^\\S]221[\\S&&[^abc]]>");
        m = p.matcher("<f $0**\n** 221x>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221z>");
        assertTrue(m.matches());
        m = p.matcher("<x 441\t221 >");
        assertFalse(m.matches());
        m = p.matcher("<x 441\t221c>");
        assertFalse(m.matches());
        m = p.matcher("<z \t9\ng 221x>");
        assertFalse(m.matches());
        m = p.matcher("<z 60\ngg\u1234\f221\u0001>");
        assertTrue(m.matches());
        p = Pattern.compile("<\\w+\\s[0-9]+;[^\\w]\\w+/[\\w$]+;");
        m = p.matcher("<f1 99;!foo5/a$7;");
        assertTrue(m.matches());
        m = p.matcher("<f$ 99;!foo5/a$7;");
        assertFalse(m.matches());
        m = p
                .matcher("<abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789 99;!foo5/a$7;");
        assertTrue(m.matches());
        p = Pattern.compile("<\\W\\w+\\s[0-9]+;[\\W_][^\\W]+\\s[0-9]+;");
        m = p.matcher("<$foo3\n99;_bar\t0;");
        assertTrue(m.matches());
        m = p.matcher("<hh 99;_g 0;");
        assertFalse(m.matches());
        m = p.matcher("<*xx\t00;^zz\f11;");
        assertTrue(m.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for patterns with POSIX characters.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for patterns with POSIX characters.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testPOSIXGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("\\p{Lower}+");
        m = p.matcher("abcdefghijklmnopqrstuvwxyz");
        assertTrue(m.matches());
        try {
            p = Pattern.compile("\\p");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{Lower");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{Lower;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        p = Pattern.compile("\\p{Upper}+");
        m = p.matcher("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertTrue(m.matches());
        try {
            p = Pattern.compile("\\p{Upper");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{Upper;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        int i;
        p = Pattern.compile("\\p{ASCII}");
        for (i = 0; i < 0x80; i++) {
            m = p.matcher(Character.toString((char) i));
            assertTrue(m.matches());
        }
        for (; i < 0xff; i++) {
            m = p.matcher(Character.toString((char) i));
            assertFalse(m.matches());
        }
        try {
            p = Pattern.compile("\\p{ASCII");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
        try {
            p = Pattern.compile("\\p{ASCII;");
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "!",
        args = {}
    )
    public void testUnicodeCategories() throws PatternSyntaxException {
        testCategory("Cf", "\u202B");
        testCategory("Co", "\uE000");
        testCategory("Cs", "\uD800");
        testCategory("Ll", "a", "b", "x", "y", "z", "-A", "-Z");
        testCategory("Lm", "\u02B9");
        testCategory("Lu", "B", "C", "-c");
        testCategory("Lo", "\u05E2");
        testCategory("Lt", "\u01C5");
        testCategory("Mc", "\u0903");
        testCategory("Me", "\u06DE");
        testCategory("Mn", "\u0300");
        testCategory("Nd", "\u0030");
        testCategory("Nl", "\u2164");
        testCategory("No", "\u0BF0");
        testCategory("Pd", "\u2015");
        testCategory("Pe", "\u207E");
        testCategory("Po", "\u00B7");
        testCategory("Ps", "\u0F3C");
        testCategory("Sc", "\u20A0");
        testCategory("Sk", "\u00B8");
        testCategory("Sm", "\u002B");
        testCategory("So", "\u0B70");
        testCategory("Zl", "\u2028");
        testCategory("Zp", "\u2029");
    }
    private void testCategory(String cat, String... matches) {
        String pa = "{"+cat+"}";
        String pat = "\\p"+pa;
        String npat = "\\P"+pa;
        Pattern p = Pattern.compile(pat);
        Pattern pn = Pattern.compile(npat);
        for (int j = 0; j < matches.length; j++) {
            String t = matches[j];
            boolean invert = t.startsWith("-");
            if (invert) { 
                t = t.substring(1);
                assertFalse("expected '"+t+"' to not be matched " +
                        "by pattern '"+pat, p.matcher(t).matches());
                assertTrue("expected '"+t+"' to  " +
                        "be matched by pattern '"+npat, pn.matcher(t).matches());                
            } else {
                assertTrue("expected '"+t+"' to be matched " +
                        "by pattern '"+pat, p.matcher(t).matches());
                assertFalse("expected '"+t+"' to  " +
                        "not be matched by pattern '"+npat, pn.matcher(t).matches());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matcher(CharSequence input) method for input specified by Unicode blocks.",
        method = "matcher",
        args = {java.lang.CharSequence.class}
    )
    public void testUnicodeBlocks() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        int i, j;
        for (i = 0; i < UBlocks.length; i++) {
            p = Pattern.compile("\\p{In" + UBlocks[i].name + "}");
            if (UBlocks[i].low > 0) {
                m = p.matcher(Character.toString((char) (UBlocks[i].low - 1)));
                assertFalse(UBlocks[i].name, m.matches());
            }
            for (j = UBlocks[i].low; j <= UBlocks[i].high; j++) {
                m = p.matcher(Character.toString((char) j));
                assertTrue(UBlocks[i].name, m.matches());
            }
            if (UBlocks[i].high < 0xFFFF) {
                m = p.matcher(Character.toString((char) (UBlocks[i].high + 1)));
                assertFalse(UBlocks[i].name, m.matches());
            }
            p = Pattern.compile("\\P{In" + UBlocks[i].name + "}");
            if (UBlocks[i].low > 0) {
                m = p.matcher(Character.toString((char) (UBlocks[i].low - 1)));
                assertTrue(UBlocks[i].name, m.matches());
            }
            for (j = UBlocks[i].low; j < UBlocks[i].high; j++) {
                m = p.matcher(Character.toString((char) j));
                assertFalse(UBlocks[i].name, m.matches());
            }
            if (UBlocks[i].high < 0xFFFF) {
                m = p.matcher(Character.toString((char) (UBlocks[i].high + 1)));
                assertTrue(UBlocks[i].name, m.matches());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "these tests are still partial, see TODO in the code",
        method = "!",
        args = {}
    )
    public void testCapturingGroups() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("(a+)b");
        m = p.matcher("aaaaaaaab");
        assertTrue(m.matches());
        assertEquals(1, m.groupCount());
        assertEquals("aaaaaaaa", m.group(1));
        p = Pattern.compile("((an)+)((as)+)");
        m = p.matcher("ananas");
        assertTrue(m.matches());
        assertEquals(4, m.groupCount());
        assertEquals("ananas", m.group(0));
        assertEquals("anan", m.group(1));
        assertEquals("an", m.group(2));
        assertEquals("as", m.group(3));
        assertEquals("as", m.group(4));
        p = Pattern.compile("(?:(?:an)+)(as)");
        m = p.matcher("ananas");
        assertTrue(m.matches());
        assertEquals(1, m.groupCount());
        assertEquals("as", m.group(1));
        try {
            m.group(2);
            fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ioobe) {
        }
        p = Pattern.compile("((an)+)as\\1");
        m = p.matcher("ananasanan");
        assertTrue(m.matches());
        try {
            p = Pattern.compile("((an)+)as\\4");
            fail("expected PatternSyntaxException");
        } catch (PatternSyntaxException pse) {
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "!",
        args = {}
    )
    public void testRepeats() {
        Pattern p;
        Matcher m;
        p = Pattern.compile("(abc)?c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertFalse(m.matches());
        p = Pattern.compile("(abc)*c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());
        p = Pattern.compile("(abc)+c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());
        p = Pattern.compile("(abc){0}c");
        m = p.matcher("abcc");
        assertFalse(m.matches());
        m = p.matcher("c");
        assertTrue(m.matches());
        p = Pattern.compile("(abc){1}c");
        m = p.matcher("abcc");
        assertTrue(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertFalse(m.matches());
        p = Pattern.compile("(abc){2}c");
        m = p.matcher("abcc");
        assertFalse(m.matches());
        m = p.matcher("c");
        assertFalse(m.matches());
        m = p.matcher("cc");
        assertFalse(m.matches());
        m = p.matcher("abcabcc");
        assertTrue(m.matches());
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "!",
        args = {}
    )
    public void testAnchors() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("^abc\\n^abc", Pattern.MULTILINE);
        m = p.matcher("abc\nabc");
        assertTrue(m.matches());
        p = Pattern.compile("^abc\\n^abc");
        m = p.matcher("abc\nabc");
        assertFalse(m.matches());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) method and matcher for created pattern.",
            method = "matcher",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testMisc() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("[a-z]+;\\Q[a-z]+;\\Q(foo.*);\\E[0-9]+");
        m = p.matcher("abc;[a-z]+;\\Q(foo.*);411");
        assertTrue(m.matches());
        m = p.matcher("abc;def;foo42;555");
        assertFalse(m.matches());
        m = p.matcher("abc;\\Qdef;\\Qfoo99;\\E123");
        assertFalse(m.matches());
        p = Pattern.compile("[a-z]+;(foo[0-9]-\\Q(...)\\E);[0-9]+");
        m = p.matcher("abc;foo5-(...);123");
        assertTrue(m.matches());
        assertEquals("foo5-(...)", m.group(1));
        m = p.matcher("abc;foo9-(xxx);789");
        assertFalse(m.matches());
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q$-\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def$-;123");
        assertTrue(m.matches());
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q-$\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def$-;123");
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\Q[0-9]\\E]+);[0-9]+");
        m = p.matcher("abc;bar0-def[99]-]0x[;123");
        p = Pattern.compile("[a-z]+;(bar[0-9]-[a-z\\[0\\-9\\]]+);[0-9]+");
        m = p.matcher("abc;bar0-def[99]-]0x[;123");
        assertTrue(m.matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies compile(String regex) method.",
        method = "compile",
        args = {java.lang.String.class}
    )
    public void testCompile1() throws PatternSyntaxException {
        Pattern pattern = Pattern
                .compile("[0-9A-Za-z][0-9A-Za-z\\x2e\\x3a\\x2d\\x5f]*");
        String name = "iso-8859-1";
        assertTrue(pattern.matcher(name).matches());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies compile(String regex, int flag) method.",
        method = "compile",
        args = {java.lang.String.class, int.class}
    )
    public void testCompile2() throws PatternSyntaxException {
        String findString = "\\Qimport\\E";
        Pattern pattern = Pattern.compile(findString, 0);
        Matcher matcher = pattern.matcher(new String(
                "import a.A;\n\n import b.B;\nclass C {}"));
        assertTrue(matcher.find(0));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) and compile(String regex, int flag) method for specific patterns.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) and compile(String regex, int flag) method for specific patterns.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testCompile3() throws PatternSyntaxException {
        Pattern p;
        Matcher m;
        p = Pattern.compile("a$");
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());
        p = Pattern.compile("(a$)");
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertEquals("a", m.group(1));
        assertFalse(m.find());
        p = Pattern.compile("^.*$", Pattern.MULTILINE);
        m = p.matcher("a\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());
        m = p.matcher("a\nb\n");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertTrue(m.find());
        assertEquals("b", m.group());
        assertFalse(m.find());
        m = p.matcher("a\nb");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertTrue(m.find());
        assertEquals("b", m.group());
        assertFalse(m.find());
        m = p.matcher("\naa\r\nbb\rcc\n\n");
        assertTrue(m.find());
        assertTrue(m.group().equals(""));
        assertTrue(m.find());
        assertEquals("aa", m.group());
        assertTrue(m.find());
        assertEquals("bb", m.group());
        assertTrue(m.find());
        assertEquals("cc", m.group());
        assertTrue(m.find());
        assertTrue(m.group().equals(""));
        assertFalse(m.find());
        m = p.matcher("a");
        assertTrue(m.find());
        assertEquals("a", m.group());
        assertFalse(m.find());
        p = Pattern.compile("^.*$");
        m = p.matcher("");
        assertTrue(m.find());
        assertTrue(m.group().equals(""));
        assertFalse(m.find());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies compile(String regex, int flag) method for specific string.",
        method = "compile",
        args = {java.lang.String.class, int.class}
    )
    public void testCompile4() throws PatternSyntaxException {
        String findString = "\\Qpublic\\E";
        StringBuffer text = new StringBuffer("    public class Class {\n"
                + "    public class Class {");
        Pattern pattern = Pattern.compile(findString, 0);
        Matcher matcher = pattern.matcher(text);
        boolean found = matcher.find();
        assertTrue(found);
        assertEquals(4, matcher.start());
        if (found) {
            text.delete(0, text.length());
            text.append("Text have been changed.");
            matcher.reset(text);
        }
        found = matcher.find();
        assertFalse(found);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies compile(String regex) methodfor specific string.",
        method = "compile",
        args = {java.lang.String.class}
    )
    public void testCompile5() throws PatternSyntaxException {
        Pattern p = Pattern.compile("^[0-9]");
        String s[] = p.split("12", -1);
        assertEquals("", s[0]);
        assertEquals("2", s[1]);
        assertEquals(2, s.length);
    }
    private static class UBInfo {
        public UBInfo(int low, int high, String name) {
            this.name = name;
            this.low = low;
            this.high = high;
        }
        public String name;
        public int low, high;
    }
    private static UBInfo[] UBlocks = {
    new UBInfo(0x0000, 0x007F, "BasicLatin"), 
            new UBInfo(0x0080, 0x00FF, "Latin-1Supplement"), 
            new UBInfo(0x0100, 0x017F, "LatinExtended-A"), 
            new UBInfo(0x0250, 0x02AF, "IPAExtensions"), 
            new UBInfo(0x02B0, 0x02FF, "SpacingModifierLetters"), 
            new UBInfo(0x0300, 0x036F, "CombiningDiacriticalMarks"), 
            new UBInfo(0x0370, 0x03FF, "Greek"), 
            new UBInfo(0x0400, 0x04FF, "Cyrillic"), 
            new UBInfo(0x0530, 0x058F, "Armenian"), 
            new UBInfo(0x0590, 0x05FF, "Hebrew"), 
            new UBInfo(0x0600, 0x06FF, "Arabic"), 
            new UBInfo(0x0700, 0x074F, "Syriac"), 
            new UBInfo(0x0780, 0x07BF, "Thaana"), 
            new UBInfo(0x0900, 0x097F, "Devanagari"), 
            new UBInfo(0x0980, 0x09FF, "Bengali"), 
            new UBInfo(0x0A00, 0x0A7F, "Gurmukhi"), 
            new UBInfo(0x0A80, 0x0AFF, "Gujarati"), 
            new UBInfo(0x0B00, 0x0B7F, "Oriya"), 
            new UBInfo(0x0B80, 0x0BFF, "Tamil"), 
            new UBInfo(0x0C00, 0x0C7F, "Telugu"), 
            new UBInfo(0x0C80, 0x0CFF, "Kannada"), 
            new UBInfo(0x0D00, 0x0D7F, "Malayalam"), 
            new UBInfo(0x0D80, 0x0DFF, "Sinhala"), 
            new UBInfo(0x0E00, 0x0E7F, "Thai"), 
            new UBInfo(0x0E80, 0x0EFF, "Lao"), 
            new UBInfo(0x0F00, 0x0FFF, "Tibetan"), 
            new UBInfo(0x1000, 0x109F, "Myanmar"), 
            new UBInfo(0x10A0, 0x10FF, "Georgian"), 
            new UBInfo(0x1100, 0x11FF, "HangulJamo"), 
            new UBInfo(0x1200, 0x137F, "Ethiopic"), 
            new UBInfo(0x13A0, 0x13FF, "Cherokee"), 
            new UBInfo(0x1400, 0x167F, "UnifiedCanadianAboriginalSyllabics"), 
            new UBInfo(0x1680, 0x169F, "Ogham"), 
            new UBInfo(0x16A0, 0x16FF, "Runic"), 
            new UBInfo(0x1780, 0x17FF, "Khmer"), 
            new UBInfo(0x1800, 0x18AF, "Mongolian"), 
            new UBInfo(0x1E00, 0x1EFF, "LatinExtendedAdditional"), 
            new UBInfo(0x1F00, 0x1FFF, "GreekExtended"), 
            new UBInfo(0x2000, 0x206F, "GeneralPunctuation"), 
            new UBInfo(0x2070, 0x209F, "SuperscriptsandSubscripts"), 
            new UBInfo(0x20A0, 0x20CF, "CurrencySymbols"), 
            new UBInfo(0x20D0, 0x20FF, "CombiningMarksforSymbols"), 
            new UBInfo(0x2100, 0x214F, "LetterlikeSymbols"), 
            new UBInfo(0x2150, 0x218F, "NumberForms"), 
            new UBInfo(0x2190, 0x21FF, "Arrows"), 
            new UBInfo(0x2200, 0x22FF, "MathematicalOperators"), 
            new UBInfo(0x2300, 0x23FF, "MiscellaneousTechnical"), 
            new UBInfo(0x2400, 0x243F, "ControlPictures"), 
            new UBInfo(0x2440, 0x245F, "OpticalCharacterRecognition"), 
            new UBInfo(0x2460, 0x24FF, "EnclosedAlphanumerics"), 
            new UBInfo(0x2500, 0x257F, "BoxDrawing"), 
            new UBInfo(0x2580, 0x259F, "BlockElements"), 
            new UBInfo(0x25A0, 0x25FF, "GeometricShapes"), 
            new UBInfo(0x2600, 0x26FF, "MiscellaneousSymbols"), 
            new UBInfo(0x2700, 0x27BF, "Dingbats"), 
            new UBInfo(0x2800, 0x28FF, "BraillePatterns"), 
            new UBInfo(0x2E80, 0x2EFF, "CJKRadicalsSupplement"), 
            new UBInfo(0x2F00, 0x2FDF, "KangxiRadicals"), 
            new UBInfo(0x2FF0, 0x2FFF, "IdeographicDescriptionCharacters"), 
            new UBInfo(0x3000, 0x303F, "CJKSymbolsandPunctuation"), 
            new UBInfo(0x3040, 0x309F, "Hiragana"), 
            new UBInfo(0x30A0, 0x30FF, "Katakana"), 
            new UBInfo(0x3100, 0x312F, "Bopomofo"), 
            new UBInfo(0x3130, 0x318F, "HangulCompatibilityJamo"), 
            new UBInfo(0x3190, 0x319F, "Kanbun"), 
            new UBInfo(0x31A0, 0x31BF, "BopomofoExtended"), 
            new UBInfo(0x3200, 0x32FF, "EnclosedCJKLettersandMonths"), 
            new UBInfo(0x3300, 0x33FF, "CJKCompatibility"), 
            new UBInfo(0x3400, 0x4DBF, "CJKUnifiedIdeographsExtensionA"), 
            new UBInfo(0x4E00, 0x9FFF, "CJKUnifiedIdeographs"), 
            new UBInfo(0xA000, 0xA48F, "YiSyllables"), 
            new UBInfo(0xA490, 0xA4CF, "YiRadicals"), 
            new UBInfo(0xAC00, 0xD7AF, "HangulSyllables"), 
            new UBInfo(0xF900, 0xFAFF, "CJKCompatibilityIdeographs"), 
            new UBInfo(0xFB00, 0xFB4F, "AlphabeticPresentationForms"), 
            new UBInfo(0xFB50, 0xFDFF, "ArabicPresentationForms-A"), 
            new UBInfo(0xFE20, 0xFE2F, "CombiningHalfMarks"), 
            new UBInfo(0xFE30, 0xFE4F, "CJKCompatibilityForms"), 
            new UBInfo(0xFE50, 0xFE6F, "SmallFormVariants"), 
            new UBInfo(0xFF00, 0xFFEF, "HalfwidthandFullwidthForms"), 
            new UBInfo(0xFFF0, 0xFFFF, "Specials") 
    };    
}