public class CharToByteISO2022CN_CNS extends CharToByteISO2022
{
    public CharToByteISO2022CN_CNS()
    {
        SODesignator = "$)G";
        SS2Designator = "$*H";
        SS3Designator = "$+I";
        try {
            codeConverter = CharToByteConverter.getConverter("CNS11643");
        } catch (Exception e) {};
    }
    public int getMaxBytesPerChar()
    {
        return maximumDesignatorLength+4;
    }
    public String getCharacterEncoding()
    {
        return "ISO2022CN_CNS";
    }
}
