public abstract class DOMServiceProvider
{
    public DOMServiceProvider()
    {
    }
    public abstract boolean canHandle(Object obj);
    public abstract org.w3c.dom.Document getDocument(Object obj) throws DOMUnsupportedException;
    public abstract org.w3c.dom.DOMImplementation getDOMImplementation();
}
