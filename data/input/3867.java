public class FooProvider
    extends CharsetProvider
{
    public FooProvider() {}
    public Iterator charsets() {
        return Collections.singleton(new FooCharset()).iterator();
    }
    public Charset charsetForName(String charsetName) {
        if (charsetName.equalsIgnoreCase("FOO"))
            return new FooCharset();
        return null;
    }
}
