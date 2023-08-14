public class BaseBuilderTest extends TestCase {
    public void testParseAaptOutput() {
        Pattern p = Pattern.compile( "^(.+):(\\d+):\\s(.+)$"); 
        String s = "C:\\java\\workspace-android\\AndroidApp\\res\\values\\strings.xml:11: WARNING: empty 'some warning text";
        Matcher m = p.matcher(s);
        assertEquals(true, m.matches());
        assertEquals("C:\\java\\workspace-android\\AndroidApp\\res\\values\\strings.xml", m.group(1));
        assertEquals("11", m.group(2));
        assertEquals("WARNING: empty 'some warning text", m.group(3));
    }
}
