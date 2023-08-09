public class FileResolverImpl implements Resolver
{
    private ORB orb ;
    private File file ;
    private Properties savedProps ;
    private long fileModified = 0 ;
    public FileResolverImpl( ORB orb, File file )
    {
        this.orb = orb ;
        this.file = file ;
        savedProps = new Properties() ;
    }
    public org.omg.CORBA.Object resolve( String name )
    {
        check() ;
        String stringifiedObject = savedProps.getProperty( name ) ;
        if (stringifiedObject == null) {
            return null;
        }
        return orb.string_to_object( stringifiedObject ) ;
    }
    public java.util.Set list()
    {
        check() ;
        Set result = new HashSet() ;
        Enumeration theKeys = savedProps.propertyNames();
        while (theKeys.hasMoreElements()) {
            result.add( theKeys.nextElement() ) ;
        }
        return result ;
    }
    private void check()
    {
        if (file == null)
            return;
        long lastMod = file.lastModified();
        if (lastMod > fileModified) {
            try {
                FileInputStream fileIS = new FileInputStream(file);
                savedProps.clear();
                savedProps.load(fileIS);
                fileIS.close();
                fileModified = lastMod;
            } catch (java.io.FileNotFoundException e) {
                System.err.println( CorbaResourceUtil.getText(
                    "bootstrap.filenotfound", file.getAbsolutePath()));
            } catch (java.io.IOException e) {
                System.err.println( CorbaResourceUtil.getText(
                    "bootstrap.exception",
                    file.getAbsolutePath(), e.toString()));
            }
        }
    }
}
