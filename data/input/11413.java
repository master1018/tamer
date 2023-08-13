public final class CDREncapsCodec
    extends org.omg.CORBA.LocalObject
    implements Codec
{
    private ORB orb;
    ORBUtilSystemException wrapper;
    private GIOPVersion giopVersion;
    public CDREncapsCodec( ORB orb, int major, int minor ) {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get(
            (com.sun.corba.se.spi.orb.ORB)orb, CORBALogDomains.RPC_PROTOCOL ) ;
        giopVersion = GIOPVersion.getInstance( (byte)major, (byte)minor );
    }
    public byte[] encode( Any data )
        throws InvalidTypeForEncoding
    {
        if ( data == null )
            throw wrapper.nullParam() ;
        return encodeImpl( data, true );
    }
    public Any decode ( byte[] data )
        throws FormatMismatch
    {
        if( data == null )
            throw wrapper.nullParam() ;
        return decodeImpl( data, null );
    }
    public byte[] encode_value( Any data )
        throws InvalidTypeForEncoding
    {
        if( data == null )
            throw wrapper.nullParam() ;
        return encodeImpl( data, false );
    }
    public Any decode_value( byte[] data, TypeCode tc )
        throws FormatMismatch, TypeMismatch
    {
        if( data == null )
            throw wrapper.nullParam() ;
        if( tc == null )
            throw  wrapper.nullParam() ;
        return decodeImpl( data, tc );
    }
    private byte[] encodeImpl( Any data, boolean sendTypeCode )
        throws InvalidTypeForEncoding
    {
        if( data == null )
            throw wrapper.nullParam() ;
        EncapsOutputStream cdrOut = new EncapsOutputStream(
            (com.sun.corba.se.spi.orb.ORB)orb, giopVersion );
        cdrOut.putEndian();
        if( sendTypeCode ) {
            cdrOut.write_TypeCode( data.type() );
        }
        data.write_value( cdrOut );
        return cdrOut.toByteArray();
    }
    private Any decodeImpl( byte[] data, TypeCode tc )
        throws FormatMismatch
    {
        if( data == null )
            throw wrapper.nullParam() ;
        AnyImpl any = null;  
        try {
            EncapsInputStream cdrIn = new EncapsInputStream( orb, data,
                data.length, giopVersion );
            cdrIn.consumeEndian();
            if( tc == null ) {
                tc = cdrIn.read_TypeCode();
            }
            any = new AnyImpl( (com.sun.corba.se.spi.orb.ORB)orb );
            any.read_value( cdrIn, tc );
        }
        catch( RuntimeException e ) {
            throw new FormatMismatch();
        }
        return any;
    }
}
