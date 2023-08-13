public class BootstrapServer
{
    private ORB orb;
    public static final void main(String[] args)
    {
        String propertiesFilename = null;
        int initialPort = ORBConstants.DEFAULT_INITIAL_PORT;
        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-InitialServicesFile") && i < args.length -1) {
                propertiesFilename = args[i+1];
            }
            if (args[i].equals("-ORBInitialPort") && i < args.length-1) {
                initialPort = java.lang.Integer.parseInt(args[i+1]);
            }
        }
        if (propertiesFilename == null) {
            System.out.println( CorbaResourceUtil.getText("bootstrap.usage",
                "BootstrapServer"));
            return;
        }
        File file = new File(propertiesFilename);
        if (file.exists() == true && file.canRead() == false) {
            System.err.println(CorbaResourceUtil.getText(
                "bootstrap.filenotreadable", file.getAbsolutePath()));
            return;
        }
        System.out.println(CorbaResourceUtil.getText(
            "bootstrap.success", Integer.toString(initialPort),
            file.getAbsolutePath()));
        Properties props = new Properties() ;
        props.put( ORBConstants.SERVER_PORT_PROPERTY,
            Integer.toString( initialPort ) ) ;
        ORB orb = (ORB) org.omg.CORBA.ORB.init(args,props);
        LocalResolver lres = orb.getLocalResolver() ;
        Resolver fres = ResolverDefault.makeFileResolver( orb, file ) ;
        Resolver cres = ResolverDefault.makeCompositeResolver( fres, lres ) ;
        LocalResolver sres = ResolverDefault.makeSplitLocalResolver( cres, lres ) ;
        orb.setLocalResolver( sres ) ;
        try {
            orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            RuntimeException rte = new RuntimeException("This should not happen");
            rte.initCause(e);
            throw rte;
        }
        orb.run() ;
    }
}
