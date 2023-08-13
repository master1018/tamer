public class StubIORImpl
{
    private int hashCode;
    private byte[] typeData;
    private int[] profileTags;
    private byte[][] profileData;
    public StubIORImpl()
    {
        hashCode = 0 ;
        typeData = null ;
        profileTags = null ;
        profileData = null ;
    }
    public String getRepositoryId()
    {
        if (typeData == null)
            return null ;
        return new String( typeData ) ;
    }
    public StubIORImpl( org.omg.CORBA.Object obj )
    {
        OutputStream ostr = StubAdapter.getORB( obj ).create_output_stream();
        ostr.write_Object(obj);
        InputStream istr = ostr.create_input_stream();
        int typeLength = istr.read_long();
        typeData = new byte[typeLength];
        istr.read_octet_array(typeData, 0, typeLength);
        int numProfiles = istr.read_long();
        profileTags = new int[numProfiles];
        profileData = new byte[numProfiles][];
        for (int i = 0; i < numProfiles; i++) {
            profileTags[i] = istr.read_long();
            profileData[i] = new byte[istr.read_long()];
            istr.read_octet_array(profileData[i], 0, profileData[i].length);
        }
    }
    public Delegate getDelegate( ORB orb )
    {
        OutputStream ostr = orb.create_output_stream();
        ostr.write_long(typeData.length);
        ostr.write_octet_array(typeData, 0, typeData.length);
        ostr.write_long(profileTags.length);
        for (int i = 0; i < profileTags.length; i++) {
            ostr.write_long(profileTags[i]);
            ostr.write_long(profileData[i].length);
            ostr.write_octet_array(profileData[i], 0, profileData[i].length);
        }
        InputStream istr = ostr.create_input_stream() ;
        org.omg.CORBA.Object obj = (org.omg.CORBA.Object)istr.read_Object();
        return StubAdapter.getDelegate( obj ) ;
    }
    public  void doRead( java.io.ObjectInputStream stream )
        throws IOException, ClassNotFoundException
    {
        int typeLength = stream.readInt();
        typeData = new byte[typeLength];
        stream.readFully(typeData);
        int numProfiles = stream.readInt();
        profileTags = new int[numProfiles];
        profileData = new byte[numProfiles][];
        for (int i = 0; i < numProfiles; i++) {
            profileTags[i] = stream.readInt();
            profileData[i] = new byte[stream.readInt()];
            stream.readFully(profileData[i]);
        }
    }
    public  void doWrite( ObjectOutputStream stream )
        throws IOException
    {
        stream.writeInt(typeData.length);
        stream.write(typeData);
        stream.writeInt(profileTags.length);
        for (int i = 0; i < profileTags.length; i++) {
            stream.writeInt(profileTags[i]);
            stream.writeInt(profileData[i].length);
            stream.write(profileData[i]);
        }
    }
    public synchronized int hashCode()
    {
        if (hashCode == 0) {
            for (int i = 0; i < typeData.length; i++) {
                hashCode = hashCode * 37 + typeData[i];
            }
            for (int i = 0; i < profileTags.length; i++) {
                hashCode = hashCode * 37 + profileTags[i];
                for (int j = 0; j < profileData[i].length; j++) {
                    hashCode = hashCode * 37 + profileData[i][j];
                }
            }
        }
        return hashCode;
    }
    private boolean equalArrays( int[] data1, int[] data2 )
    {
        if (data1.length != data2.length)
            return false ;
        for (int ctr=0; ctr<data1.length; ctr++) {
            if (data1[ctr] != data2[ctr])
                return false ;
        }
        return true ;
    }
    private boolean equalArrays( byte[] data1, byte[] data2 )
    {
        if (data1.length != data2.length)
            return false ;
        for (int ctr=0; ctr<data1.length; ctr++) {
            if (data1[ctr] != data2[ctr])
                return false ;
        }
        return true ;
    }
    private boolean equalArrays( byte[][] data1, byte[][] data2 )
    {
        if (data1.length != data2.length)
            return false ;
        for (int ctr=0; ctr<data1.length; ctr++) {
            if (!equalArrays( data1[ctr], data2[ctr] ))
                return false ;
        }
        return true ;
    }
    public boolean equals(java.lang.Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StubIORImpl)) {
            return false;
        }
        StubIORImpl other = (StubIORImpl) obj;
        if (other.hashCode() != this.hashCode()) {
            return false;
        }
        return equalArrays( typeData, other.typeData ) &&
            equalArrays( profileTags, other.profileTags ) &&
            equalArrays( profileData, other.profileData ) ;
    }
    private void appendByteArray( StringBuffer result, byte[] data )
    {
        for ( int ctr=0; ctr<data.length; ctr++ ) {
            result.append( Integer.toHexString( data[ctr] ) ) ;
        }
    }
    public String toString()
    {
        StringBuffer result = new StringBuffer() ;
        result.append( "SimpleIORImpl[" ) ;
        String repositoryId = new String( typeData ) ;
        result.append( repositoryId ) ;
        for (int ctr=0; ctr<profileTags.length; ctr++) {
            result.append( ",(" ) ;
            result.append( profileTags[ctr] ) ;
            result.append( ")" ) ;
            appendByteArray( result,  profileData[ctr] ) ;
        }
        result.append( "]" ) ;
        return result.toString() ;
    }
}
