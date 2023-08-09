public class NameServer
{
    private ORB orb;
    private File dbDir; 
    private final static String dbName = "names.db";
    public static void main(String args[])
    {
        NameServer ns = new NameServer(args);
        ns.run();
    }
    protected NameServer(String args[])
    {
        java.util.Properties props = System.getProperties();
        props.put( ORBConstants.SERVER_ID_PROPERTY, "1000" ) ;
        props.put("org.omg.CORBA.ORBClass",
                  "com.sun.corba.se.impl.orb.ORBImpl");
        orb = (ORB) org.omg.CORBA.ORB.init(args,props);
        String dbDirName = props.getProperty( ORBConstants.DB_DIR_PROPERTY ) +
            props.getProperty("file.separator") + dbName +
            props.getProperty("file.separator");
        dbDir = new File(dbDirName);
        if (!dbDir.exists()) dbDir.mkdir();
    }
    protected void run()
    {
        try {
            NameService ns = new NameService(orb, dbDir);
            NamingContext rootContext = ns.initialNamingContext();
            InitialNameService ins = InitialNameServiceHelper.narrow(
                                     orb.resolve_initial_references(
                                     ORBConstants.INITIAL_NAME_SERVICE_NAME ));
            ins.bind( "NameService", rootContext, true);
            System.out.println(CorbaResourceUtil.getText("pnameserv.success"));
            orb.run();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
