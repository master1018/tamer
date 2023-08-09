public class Locator2Impl extends LocatorImpl implements Locator2
{
    private String    encoding;
    private String    version;
    public Locator2Impl () { }
    public Locator2Impl (Locator locator)
    {
    super (locator);
    if (locator instanceof Locator2) {
        Locator2    l2 = (Locator2) locator;
        version = l2.getXMLVersion ();
        encoding = l2.getEncoding ();
    }
    }
    public String getXMLVersion ()
    { return version; }
    public String getEncoding ()
    { return encoding; }
    public void setXMLVersion (String version)
    { this.version = version; }
    public void setEncoding (String encoding)
    { this.encoding = encoding; }
}
