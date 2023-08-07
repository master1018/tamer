public class WsmoRepositoryTest extends TestCase {
    private WsmoConnection repository;
    protected void setUp() throws Exception {
        repository = com.ontotext.ordi.Factory.create(WsmoSource.class, null).getConnection();
        repository.unwrap(TConnection.class).removeStatement(null, null, null, null);
    }
    protected void tearDown() throws Exception {
        repository.unwrap(TConnection.class).removeStatement(null, null, null, null);
        repository.getDataSource().shutdown();
        repository = null;
    }
    public void testCrossEntityDeleting() throws Exception {
        WsmoFactory factory = Factory.createWsmoFactory(null);
        Ontology o1 = factory.createOntology(iri("o1"));
        Ontology o2 = factory.createOntology(iri("o2"));
        Ontology o3 = factory.createOntology(iri("o3"));
        Concept c1 = factory.createConcept(iri("c1"));
        Attribute a1 = c1.createAttribute(iri("a1"));
        o1.addConcept(c1);
        repository.saveOntology(o1);
        o2.addConcept(c1);
        repository.save(o2);
        o3.addConcept(c1);
        repository.save(c1);
        dumpRepositoryStatements(repository.unwrap(TConnection.class).search(null, null, null, null, null), System.out);
        repository.remove(a1.getIdentifier());
        System.out.println();
        dumpRepositoryStatements(repository.unwrap(TConnection.class).search(null, null, null, null, null), System.out);
        repository.remove(o1.getIdentifier());
        System.out.println();
        dumpRepositoryStatements(repository.unwrap(TConnection.class).search(null, null, null, null, null), System.out);
    }
    public void testToPesistAllWSML() throws Exception {
        for (File file : new File("src/test/resources/").listFiles()) {
            if (file.getName().endsWith(".wsml") == false) continue;
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file.getName());
            if (stream == null) continue;
            TopEntity[] tes = new org.deri.wsmo4j.io.parser.wsml.ParserImpl(new HashMap<Object, Object>()).parse(new InputStreamReader(stream));
            for (int i = 0; i < tes.length; i++) {
                TopEntity te = tes[i];
                if (te instanceof Ontology) {
                    repository.addOntology((Ontology) te);
                    repository.getOntology((IRI) te.getIdentifier());
                } else if (te instanceof Goal) {
                    repository.addGoal((Goal) te);
                    repository.getGoal((IRI) te.getIdentifier());
                } else if (te instanceof Mediator) {
                    repository.addMediator((Mediator) te);
                    repository.getMediator((IRI) te.getIdentifier());
                } else if (te instanceof WebService) {
                    repository.addWebService((WebService) te);
                    repository.getWebService((IRI) te.getIdentifier());
                }
            }
            for (int i = 0; i < tes.length; i++) {
                repository.remove(tes[i].getIdentifier());
            }
            dumpRepositoryStatements(repository.unwrap(TConnection.class).search(null, null, null, null, null), null);
        }
    }
    private int dumpRepositoryStatements(CloseableIterator<? extends TStatement> data, PrintStream out) {
        int count = 0;
        while (data.hasNext()) {
            TStatement st = data.next();
            String ts = "";
            Iterator<URI> iteratorTs = st.getTriplesetIterator();
            while (iteratorTs.hasNext()) {
                ts += iteratorTs.next().toString() + " ";
            }
            if (st.getSubject().stringValue().startsWith("htt:
                continue;
            }
            if (out != null) {
                out.println(String.format("subj=%s; pred=%s; obj=%s; ng=%s; {%s}", st.getSubject(), st.getPredicate(), st.getObject(), st.getContext(), ts));
            }
            count++;
        }
        return count;
    }
    private IRI iri(String localName) {
        return new IRIImpl("urn:WsmoRepository:Test:" + localName);
    }
}
