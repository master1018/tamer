public class CharToByteISO2022KR extends CharToByteISO2022
{
    public CharToByteISO2022KR()
    {
        SODesignator = "$)C";
        try {
            codeConverter = CharToByteConverter.getConverter("KSC5601");
        } catch (Exception e) {};
    }
    public int getMaxBytesPerChar()
    {
        return maximumDesignatorLength+4;
    }
    public String getCharacterEncoding()
    {
        return "ISO2022KR";
    }
}
