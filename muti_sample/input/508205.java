public class DERDump
    extends ASN1Dump
{
    public static String dumpAsString(
        DERObject   obj)
    {
        return _dumpAsString("", obj);
    }
    public static String dumpAsString(
        DEREncodable   obj)
    {
        return _dumpAsString("", obj.getDERObject());
    }
}
