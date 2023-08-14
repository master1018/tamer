public class DEREncodableVector
{
    private Vector  v = new Vector();
    public void add(
        DEREncodable   obj)
    {
        v.addElement(obj);
    }
    public DEREncodable get(
        int i)
    {
        return (DEREncodable)v.elementAt(i);
    }
    public int size()
    {
        return v.size();
    }
}
