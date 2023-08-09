public class UrlBase64
{
    private static final Encoder encoder = new UrlBase64Encoder();
    public static byte[] encode(
        byte[]    data)
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        try
        {
            encoder.encode(data, 0, data.length, bOut);
        }
        catch (IOException e)
        {
            throw new RuntimeException("exception encoding URL safe base64 string: " + e);
        }
        return bOut.toByteArray();
    }
    public static int encode(
        byte[]                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.encode(data, 0, data.length, out);
    }
    public static byte[] decode(
        byte[]    data)
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        try
        {
            encoder.decode(data, 0, data.length, bOut);
        }
        catch (IOException e)
        {
            throw new RuntimeException("exception decoding URL safe base64 string: " + e);
        }
        return bOut.toByteArray();
    }
    public static int decode(
        byte[]                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.decode(data, 0, data.length, out);
    }
    public static byte[] decode(
        String    data)
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        try
        {
            encoder.decode(data, bOut);
        }
        catch (IOException e)
        {
            throw new RuntimeException("exception decoding URL safe base64 string: " + e);
        }
        return bOut.toByteArray();
    }
    public static int decode(
        String                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.decode(data, out);
    }
}
