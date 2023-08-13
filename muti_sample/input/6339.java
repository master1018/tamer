public class ORBVersionFactory {
    private ORBVersionFactory() {} ;
    public static ORBVersion getFOREIGN()
    {
        return ORBVersionImpl.FOREIGN ;
    }
    public static ORBVersion getOLD()
    {
        return ORBVersionImpl.OLD ;
    }
    public static ORBVersion getNEW()
    {
        return ORBVersionImpl.NEW ;
    }
    public static ORBVersion getJDK1_3_1_01()
    {
        return ORBVersionImpl.JDK1_3_1_01 ;
    }
    public static ORBVersion getNEWER()
    {
        return ORBVersionImpl.NEWER ;
    }
    public static ORBVersion getPEORB()
    {
        return ORBVersionImpl.PEORB ;
    }
    public static ORBVersion getORBVersion()
    {
        return ORBVersionImpl.PEORB ;
    }
    public static ORBVersion create( InputStream is )
    {
        byte value = is.read_octet() ;
        return byteToVersion( value ) ;
    }
    private static ORBVersion byteToVersion( byte value )
    {
        switch (value) {
            case ORBVersion.FOREIGN : return ORBVersionImpl.FOREIGN ;
            case ORBVersion.OLD : return ORBVersionImpl.OLD ;
            case ORBVersion.NEW : return ORBVersionImpl.NEW ;
            case ORBVersion.JDK1_3_1_01: return ORBVersionImpl.JDK1_3_1_01 ;
            case ORBVersion.NEWER : return ORBVersionImpl.NEWER ;
            case ORBVersion.PEORB : return ORBVersionImpl.PEORB ;
            default : return new ORBVersionImpl(value);
        }
    }
}
