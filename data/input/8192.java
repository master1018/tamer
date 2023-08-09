public class EncapsulationUtility
{
    private EncapsulationUtility()
    {
    }
    public static void readIdentifiableSequence( List container,
        IdentifiableFactoryFinder finder, InputStream istr)
    {
        int count = istr.read_long() ;
        for (int ctr = 0; ctr<count; ctr++) {
            int id = istr.read_long() ;
            Identifiable obj = finder.create( id, istr ) ;
            container.add( obj ) ;
        }
    }
    public static  void writeIdentifiableSequence( List container, OutputStream os)
    {
        os.write_long( container.size() ) ;
        Iterator iter = container.iterator() ;
        while (iter.hasNext()) {
            Identifiable obj = (Identifiable)( iter.next() ) ;
            os.write_long( obj.getId() ) ;
            obj.write( os ) ;
        }
    }
    static public void writeOutputStream( OutputStream dataStream,
        OutputStream os )
    {
        byte[] data = ((CDROutputStream)dataStream).toByteArray() ;
        os.write_long( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }
    static public InputStream getEncapsulationStream( InputStream is )
    {
        byte[] data = readOctets( is ) ;
        EncapsInputStream result = new EncapsInputStream( is.orb(), data,
            data.length ) ;
        result.consumeEndian() ;
        return result ;
    }
    static public byte[] readOctets( InputStream is )
    {
        int len = is.read_ulong() ;
        byte[] data = new byte[len] ;
        is.read_octet_array( data, 0, len ) ;
        return data ;
    }
    static public void writeEncapsulation( WriteContents obj,
        OutputStream os )
    {
        EncapsOutputStream out = new EncapsOutputStream( (ORB)os.orb() ) ;
        out.putEndian() ;
        obj.writeContents( out ) ;
        writeOutputStream( out, os ) ;
    }
}
