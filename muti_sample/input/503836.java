public class VerisignCzagExtension
    extends DERIA5String
{
    public VerisignCzagExtension(
        DERIA5String str)
    {
        super(str.getString());
    }
    public String toString()
    {
        return "VerisignCzagExtension: " + this.getString();
    }
}
