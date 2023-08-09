@TestTargetClass(ParserFactory.class)
public class ParserFactoryTest extends TestCase {
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "makeParser",
        args = { },
        notes = "Checks everything except META-INF case"
    )
    public void testMakeParser() throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        System.clearProperty("org.xml.sax.parser");
        try {
            ParserFactory.makeParser();
            fail("expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
        }
        System.setProperty("org.xml.sax.parser", "foo.bar.SAXParser");
        try {
            ParserFactory.makeParser();
            fail("expected ClassNotFoundException was not thrown");
        } catch (ClassNotFoundException e) {
        }
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoAccessParser");
        try {
            ParserFactory.makeParser();
            fail("expected IllegalAccessException was not thrown");
        } catch (IllegalAccessException e) {
        }
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoInstanceParser");
        try {
            ParserFactory.makeParser();
            fail("expected InstantiationException was not thrown");
        } catch (InstantiationException e) {
        }
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.NoSubclassParser");
        try {
            ParserFactory.makeParser();
            fail("expected ClassCastException was not thrown");
        } catch (ClassCastException e) {
        }
        System.setProperty("org.xml.sax.parser",
                "tests.api.org.xml.sax.support.DoNothingParser");
        ParserFactory.makeParser();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "makeParser",
        args = { String.class }
    )
    public void testMakeParserString() throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        try {
            ParserFactory.makeParser(null);
            fail("expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
        }
        try {
            ParserFactory.makeParser("foo.bar.SAXParser");
            fail("expected ClassNotFoundException was not thrown");
        } catch (ClassNotFoundException e) {
        }
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoAccessParser");
            fail("expected IllegalAccessException was not thrown");
        } catch (IllegalAccessException e) {
        }
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoInstanceParser");
            fail("expected InstantiationException was not thrown");
        } catch (InstantiationException e) {
        }
        try {
            ParserFactory.makeParser(
                    "tests.api.org.xml.sax.support.NoSubclassParser");
            fail("expected ClassCastException was not thrown");
        } catch (ClassCastException e) {
        }
        ParserFactory.makeParser(
                "tests.api.org.xml.sax.support.DoNothingParser");
    }
}
