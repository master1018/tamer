@TestTargetClass(ContentHandler.class) 
public class ContentHandlerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getContent",
        args = {java.net.URLConnection.class, java.lang.Class[].class}
    )
    public void test_getContent() throws IOException {
        URLConnection conn = new URL("http:
        Class[] classes = { Foo.class, String.class, };
        ContentHandler handler = new ContentHandlerImpl();
        ((ContentHandlerImpl) handler).setContent(new Foo());
        Object content = handler.getContent(conn, classes);
        assertEquals("Foo", ((Foo) content).getFoo());
        ((ContentHandlerImpl) handler).setContent(new FooSub());
        content = handler.getContent(conn, classes);
        assertEquals("FooSub", ((Foo) content).getFoo());
        Class[] classes2 = { FooSub.class, String.class, };
        ((ContentHandlerImpl) handler).setContent(new Foo());
        content = handler.getContent(conn, classes2);
        assertNull(content);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getContent",
        args = {java.net.URLConnection.class}
    )
    public void test_getContentLURLConnection() throws IOException {
        URLConnection conn = new URL("http:
        Class[] classes = { Foo.class, String.class, };
        ContentHandler handler = new ContentHandlerImpl();
        ((ContentHandlerImpl) handler).setContent(new Foo());
        Object content = handler.getContent(conn);
        assertEquals("Foo", ((Foo) content).getFoo());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ContentHandler",
        args = {}
    )
    public void test_Constructor() {
        ContentHandlerImpl ch = new ContentHandlerImpl();
        ch.setContent(new Object());
    }
}
class ContentHandlerImpl extends ContentHandler {
    private Object content;
    @Override
    public Object getContent(URLConnection uConn) throws IOException {
        return content;
    }
    public void setContent(Object content) {
        this.content = content;
    }
}
class Foo {
    public String getFoo() {
        return "Foo";
    }
}
class FooSub extends Foo {
    public String getFoo() {
        return "FooSub";
    }
}
