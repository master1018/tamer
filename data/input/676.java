public class PhenoscapeDataLoader {
    public static final String DB_HOST = "db-host";
    public static final String DB_NAME = "db-name";
    public static final String DB_USER = "db-user";
    public static final String DB_PASSWORD = "db-password";
    public static final String ONTOLOGY_DIR = "ontology-dir";
    private Shard shard;
    private OBOSession session;
    public PhenoscapeDataLoader() throws SQLException, ClassNotFoundException {
        this.shard = this.initializeShard();
        this.session = this.loadOBOSession();
    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        PhenoscapeDataLoader pdl = new PhenoscapeDataLoader();
        pdl.processDataFolder(new File(args[0]));
    }
    private void processDataFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                this.processDataFolder(file);
            } else if (file.getName().endsWith(".xml")) {
                try {
                    this.processDataFile(file);
                } catch (XmlException e) {
                    log().error("Failed parsing " + file, e);
                } catch (IOException e) {
                    log().error("Failed reading " + file, e);
                }
            }
        }
    }
    private void processDataFile(File file) throws IOException, XmlException {
        log().info("Started work with " + file.getAbsolutePath());
        DataSet ds;
        try {
            NeXMLReader_1_0 reader = new NeXMLReader_1_0(file, this.session);
            ds = reader.getDataSet();
        } catch (XmlException xmle) {
            NeXMLReader reader = new NeXMLReader(file, this.session);
            ds = reader.getDataSet();
        }
        final OBDModelBridge bridge = new OBDModelBridge();
        log().info("Beginning graph construction");
        final Graph g = bridge.translate(ds);
        log().info("Putting graph into database");
        this.shard.putGraph(g);
        log().info(g.getStatements().size() + " records added");
    }
    private OBOSession loadOBOSession() {
        final OBOFileAdapter fileAdapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.setReadPaths(this.getOntologyPaths());
        config.setBasicSave(false);
        config.setAllowDangling(true);
        config.setFollowImports(false);
        try {
            return fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null);
        } catch (DataAdapterException e) {
            log().fatal("Failed to load ontologies", e);
            return null;
        }
    }
    private List<String> getOntologyPaths() {
        List<String> paths = new ArrayList<String>();
        File ontCache = new File(System.getProperty(ONTOLOGY_DIR));
        for (File f : ontCache.listFiles()) {
            paths.add(f.getAbsolutePath());
        }
        return paths;
    }
    private Shard initializeShard() throws SQLException, ClassNotFoundException {
        OBDSQLShard obdsql = new OBDSQLShard();
        obdsql.connect("jdbc:postgresql:
        return obdsql;
    }
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }
}
