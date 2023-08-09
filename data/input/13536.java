public abstract class GenericIdentifiable implements Identifiable
{
    private int id;
    private byte data[];
    public GenericIdentifiable(int id, InputStream is)
    {
        this.id = id ;
        data = EncapsulationUtility.readOctets( is ) ;
    }
    public int getId()
    {
        return id ;
    }
    public void write(OutputStream os)
    {
        os.write_ulong( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }
    public String toString()
    {
        return "GenericIdentifiable[id=" + getId() + "]" ;
    }
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false ;
        if (!(obj instanceof GenericIdentifiable))
            return false ;
        GenericIdentifiable encaps = (GenericIdentifiable)obj ;
        return (getId() == encaps.getId()) &&
            Arrays.equals( getData(), encaps.getData() ) ;
    }
    public int hashCode()
    {
        int result = 17 ;
        for (int ctr=0; ctr<data.length; ctr++ )
            result = 37*result + data[ctr] ;
        return result ;
    }
    public GenericIdentifiable(int id, byte[] data)
    {
        this.id = id ;
        this.data = (byte[])(data.clone()) ;
    }
    public byte[] getData()
    {
        return data ;
    }
}
