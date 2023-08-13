public class CharToByteISO2022CN_GB extends CharToByteISO2022
{
    public CharToByteISO2022CN_GB()
    {
        SODesignator = "$)A";
        try {
            codeConverter = CharToByteConverter.getConverter("GB2312");
        } catch (Exception e) {};
    }
    public int getMaxBytesPerChar()
    {
        return maximumDesignatorLength+4;
    }
    public String getCharacterEncoding()
    {
        return "ISO2022CN_GB";
    }
}
