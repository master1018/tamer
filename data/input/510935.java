@TestTargetClass(Matcher.class)
public class ReplaceTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the basic functionality of replaceFirst(java.lang.String) & replaceAll(java.lang.String) methods.",
            method = "replaceFirst",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the basic functionality of replaceFirst(java.lang.String) & replaceAll(java.lang.String) methods.",
            method = "replaceAll",
            args = {java.lang.String.class}
        )
    })          
    public void testSimpleReplace() throws PatternSyntaxException {
        String target, pattern, repl;
        target = "foobarfobarfoofo1barfort";
        pattern = "fo[^o]";
        repl = "xxx";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(target);
        assertEquals("foobarxxxarfoofo1barfort", m.replaceFirst(repl));
        assertEquals("foobarxxxarfooxxxbarxxxt", m.replaceAll(repl));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of replaceFirst(java.lang.String) & replaceAll(java.lang.String) methods.",
            method = "replaceFirst",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the functionality of replaceFirst(java.lang.String) & replaceAll(java.lang.String) methods.",
            method = "replaceAll",
            args = {java.lang.String.class}
        )
    })          
    public void testCaptureReplace() {
        String target, pattern, repl, s;
        Pattern p = null;
        Matcher m;
        target = "[31]foo;bar[42];[99]xyz";
        pattern = "\\[([0-9]+)\\]([a-z]+)";
        repl = "$2[$1]";
        p = Pattern.compile(pattern);
        m = p.matcher(target);
        s = m.replaceFirst(repl);
        assertEquals("foo[31];bar[42];[99]xyz", s);
        s = m.replaceAll(repl);
        assertEquals("foo[31];bar[42];xyz[99]", s);
        target = "[31]foo(42)bar{63}zoo;[12]abc(34)def{56}ghi;{99}xyz[88]xyz(77)xyz;";
        pattern = "\\[([0-9]+)\\]([a-z]+)\\(([0-9]+)\\)([a-z]+)\\{([0-9]+)\\}([a-z]+)";
        repl = "[$5]$6($3)$4{$1}$2";
        p = Pattern.compile(pattern);
        m = p.matcher(target);
        s = m.replaceFirst(repl);
        assertEquals("[63]zoo(42)bar{31}foo;[12]abc(34)def{56}ghi;{99}xyz[88]xyz(77)xyz;", s
                );
        s = m.replaceAll(repl);
        assertEquals("[63]zoo(42)bar{31}foo;[56]ghi(34)def{12}abc;{99}xyz[88]xyz(77)xyz;", s
                );
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the functionality of replaceAll(java.lang.String) method with backslash chars.",
        method = "replaceAll",
        args = {java.lang.String.class}
    )          
    public void testEscapeReplace() {
        String target, pattern, repl, s;
        target = "foo'bar''foo";
        pattern = "'";
        repl = "\\'";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo'bar''foo", s);
        repl = "\\\\'";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo\\'bar\\'\\'foo", s);
        repl = "\\$3";
        s = target.replaceAll(pattern, repl);
        assertEquals("foo$3bar$3$3foo", s);
    }
}
