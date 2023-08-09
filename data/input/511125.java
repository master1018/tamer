public class RegexTest extends TestCase {
    @SmallTest
    public void testMatches() throws Exception {
        Pattern p = Pattern.compile("bcd");
        Matcher m = p.matcher("bcd");
        assertTrue("Should match.", m.matches());
        p = Pattern.compile("bcd");
        m = p.matcher("abcdefg");
        assertFalse("Should not match.", m.matches());
        m = p.matcher("bcdefg");
        assertFalse("Should not match.", m.matches());
        m = p.matcher("abcd");
        assertFalse("Should not match.", m.matches());
        p = Pattern.compile(".*");
        m = p.matcher("abc");
        assertTrue(m.matches());
        assertTrue(m.find());
        assertTrue(m.matches());
        p = Pattern.compile(".");
        m = p.matcher("abc");
        assertFalse(m.matches());
        assertTrue(m.find());
        assertFalse(m.matches());
        m.reset("z");
        assertTrue(m.matches());
        m.reset("xyz");
        assertFalse(m.matches());
        assertFalse("Erroneously matched partial string.  " +
                "See http:
        assertFalse("Erroneously matched partial string.  " +
                "See http:
        assertTrue("Generic regex should match.",
                Pattern.matches(".*", "bcd"));
        assertTrue("Grouped regex should match.",
                Pattern.matches("(b(c(d)))", "bcd"));
        assertTrue("Grouped regex should match.",
                Pattern.matches("(b)(c)(d)", "bcd"));
    }
    @SmallTest
    public void testGroupCount() throws Exception {
        Pattern p = Pattern.compile(
                "\\b(?:\\+?1)?"
                        + "(?:[ -\\.])?"
                        + "\\(?(\\d{3})?\\)?"
                        + "(?:[ -\\.\\/])?"
                        + "(\\d{3})"
                        + "(?:[ -\\.])?"
                        + "(\\d{4})\\b"
        );
        Matcher m = p.matcher("1 (919) 555-1212");
        assertEquals("groupCount is incorrect, see http:
                3, m.groupCount());
    }
    @SmallTest
    public void testGroups() throws Exception {
        Pattern p = Pattern.compile("(b)([c|d])(z*)");
        Matcher m = p.matcher("abcdefg");
        assertTrue(m.find());
        assertEquals(3, m.groupCount());
        assertEquals("bc", m.group(0));
        assertEquals("b", m.group(1));
        assertEquals("c", m.group(2));
        assertEquals("", m.group(3));
    }
    @SmallTest
    public void testFind() throws Exception {
        Pattern p = Pattern.compile(".");
        Matcher m = p.matcher("abc");
        assertTrue(m.find());
        assertEquals("a", m.group(0));
        assertTrue(m.find());
        assertEquals("b", m.group(0));
        assertTrue(m.find());
        assertEquals("c", m.group(0));
        assertFalse(m.find());
    }
    @SmallTest
    public void testReplaceAll() throws Exception {
        Pattern p = Pattern.compile("a*b");
        Matcher m = p.matcher("fooaabfooaabfooabfoob");
        String r = m.replaceAll("-");
        assertEquals("foo-foo-foo-foo-", r);
        p = Pattern.compile("a*b");
        m = p.matcher("aabfooaabfooabfoobfoo");
        r = m.replaceAll("-");
        assertEquals("-foo-foo-foo-foo", r);
    }
    @SmallTest
    public void testReplaceFirst() throws Exception {
        Pattern p = Pattern.compile("a*b");
        Matcher m = p.matcher("fooaabfooaabfooabfoob");
        String r = m.replaceFirst("-");
        assertEquals("foo-fooaabfooabfoob", r);
        p = Pattern.compile("a*b");
        m = p.matcher("aabfooaabfooabfoobfoo");
        r = m.replaceFirst("-");
        assertEquals("-fooaabfooabfoobfoo", r);
    }
    @SmallTest
    public void testSplit() throws Exception {
        Pattern p = Pattern.compile(":");
        String[] strings;
        strings = p.split("boo:and:foo");
        assertEquals(3, strings.length);
        assertEquals("boo", strings[0]);
        assertEquals("and", strings[1]);
        assertEquals("foo", strings[2]);
        strings = p.split("boo:and:foo", 2);
        assertEquals(2, strings.length);
        assertEquals("boo", strings[0]);
        assertEquals("and:foo", strings[1]);
        strings = p.split("boo:and:foo", 5);
        assertEquals(3, strings.length);
        assertEquals("boo", strings[0]);
        assertEquals("and", strings[1]);
        assertEquals("foo", strings[2]);
        strings = p.split("boo:and:foo", -2);
        assertEquals(3, strings.length);
        assertEquals("boo", strings[0]);
        assertEquals("and", strings[1]);
        assertEquals("foo", strings[2]);
        p = Pattern.compile("o");
        strings = p.split("boo:and:foo");
        assertEquals(3, strings.length);
        assertEquals("b", strings[0]);
        assertEquals("", strings[1]);
        assertEquals(":and:f", strings[2]);
        strings = p.split("boo:and:foo", 5);
        assertEquals(5, strings.length);
        assertEquals("b", strings[0]);
        assertEquals("", strings[1]);
        assertEquals(":and:f", strings[2]);
        assertEquals("", strings[3]);
        assertEquals("", strings[4]);
        strings = p.split("boo:and:foo", -2);
        assertEquals(5, strings.length);
        assertEquals("b", strings[0]);
        assertEquals("", strings[1]);
        assertEquals(":and:f", strings[2]);
        assertEquals("", strings[3]);
        assertEquals("", strings[4]);
        strings = p.split("boo:and:foo", 0);
        assertEquals(3, strings.length);
        assertEquals("b", strings[0]);
        assertEquals("", strings[1]);
        assertEquals(":and:f", strings[2]);
    }
    public static final Pattern TOP_LEVEL_DOMAIN_PATTERN = Pattern.compile(
            "((aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
            + "|(biz|b[abdefghijmnorstvwyz])"
            + "|(cat|com|coop|c[acdfghiklmnoruvxyz])"
            + "|d[ejkmoz]"
            + "|(edu|e[cegrstu])"
            + "|f[ijkmor]"
            + "|(gov|g[abdefghilmnpqrstuwy])"
            + "|h[kmnrtu]"
            + "|(info|int|i[delmnoqrst])"
            + "|(jobs|j[emop])"
            + "|k[eghimnrwyz]"
            + "|l[abcikrstuvy]"
            + "|(mil|mobi|museum|m[acdghklmnopqrstuvwxyz])"
            + "|(name|net|n[acefgilopruz])"
            + "|(org|om)"
            + "|(pro|p[aefghklmnrstwy])"
            + "|qa"
            + "|r[eouw]"
            + "|s[abcdeghijklmnortuvyz]"
            + "|(tel|travel|t[cdfghjklmnoprtvwz])"
            + "|u[agkmsyz]"
            + "|v[aceginu]"
            + "|w[fs]"
            + "|y[etu]"
            + "|z[amw])");
    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[\\+a-zA-Z0-9\\.\\_\\%\\-]+\\@" 
            + "(("
            + "[a-zA-Z0-9]\\.|"
            + "([a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9]\\.)+)"
            + TOP_LEVEL_DOMAIN_PATTERN
            + ")");
    @SmallTest
    public void testMonsterRegexCorrectness() {
        assertTrue(EMAIL_ADDRESS_PATTERN.matcher("a+b@gmail.com").matches());        
    }
    @SmallTest
    public void testMonsterRegexPerformance() {
        android.util.Log.e("RegexTest", "RegEx performance test started.");
        long t0 = System.currentTimeMillis();
        Matcher m = EMAIL_ADDRESS_PATTERN.matcher("donot repeate@RC8jjjjjjjjjjjjjjj");
        assertFalse(m.find());
        long t1 = System.currentTimeMillis();
        android.util.Log.e("RegexTest", "RegEx performance test finished, " +
                "took " + (t1 - t0) + " ms.");
    }
}
