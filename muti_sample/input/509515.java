public class MacOutputStream
    extends FilterOutputStream
{
    protected Mac mac;
    public MacOutputStream(
        OutputStream stream,
        Mac          mac)
    {
        super(stream);
        this.mac = mac;
    }
    public void write(int b)
        throws IOException
    {
        mac.update((byte)b);
        out.write(b);
    }
    public void write(
        byte[] b,
        int off,
        int len)
        throws IOException
    {
        mac.update(b, off, len);
        out.write(b, off, len);
    }
    public Mac getMac()
    {
        return mac;
    }
}
