public class JavaStreamObjectCopierImpl implements ObjectCopier {
    public JavaStreamObjectCopierImpl( ORB orb )
    {
        this.orb = orb ;
    }
    public Object copy(Object obj) {
        if (obj instanceof Remote) {
            return Utility.autoConnect(obj,orb,true);
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 10000 ) ;
            ObjectOutputStream oos = new ObjectOutputStream( os ) ;
            oos.writeObject( obj ) ;
            byte[] arr = os.toByteArray() ;
            InputStream is = new ByteArrayInputStream( arr ) ;
            ObjectInputStream ois = new ObjectInputStream( is ) ;
            return ois.readObject();
        } catch (Exception exc) {
            System.out.println( "Failed with exception:" + exc ) ;
            return null ;
        }
    }
    private ORB orb;
}
