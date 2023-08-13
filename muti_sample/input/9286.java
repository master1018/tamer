public class FooCharset
    extends Charset
{
    public FooCharset() {
        super("FOO",
              new String[] { "FOO-1", "FOO-2" });
    }
    public boolean contains(Charset cs) {
        return (cs instanceof FooCharset);
    }
    public CharsetDecoder newDecoder() { return null; }
    public CharsetEncoder newEncoder() { return null; }
}
