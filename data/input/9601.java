public class ByteToCharISO2022KR extends ByteToCharISO2022
{
    public ByteToCharISO2022KR()
    {
        SODesignator = new String[1];
        SODesignator[0] = "$)C";
        SOConverter = new ByteToCharConverter[1];
        try {
            SOConverter[0] = ByteToCharConverter.getConverter("KSC5601");
        } catch (Exception e) {};
    }
    public String getCharacterEncoding()
    {
        return "ISO2022KR";
    }
}
