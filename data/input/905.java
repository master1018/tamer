public class IntActSearchEngine extends FastSearchEngine<IntActBinaryInteraction> {
    private static final String[] DEFAULT_FIELDS = { "identifiers", DefaultColumnSet.PUB_ID.getShortName(), DefaultColumnSet.PUB_1ST_AUTHORS.getShortName(), "species", DefaultColumnSet.INTERACTION_TYPES.getShortName(), DefaultColumnSet.INTER_DETECTION_METHODS.getShortName(), DefaultColumnSet.INTERACTION_ID.getShortName(), "properties", IntActColumnSet.HOSTORGANISM.getShortName(), IntActColumnSet.EXPANSION_METHOD.getShortName(), IntActColumnSet.DATASET.getShortName() };
    public IntActSearchEngine(Directory indexDirectory) throws IOException {
        super(indexDirectory);
    }
    public IntActSearchEngine(File indexDirectory) throws IOException {
        super(indexDirectory);
    }
    public IntActSearchEngine(String indexDirectory) throws IOException {
        super(indexDirectory);
    }
    @Override
    protected DocumentBuilder createDocumentBuilder() {
        return new IntActDocumentBuilder();
    }
    protected String[] getSearchFields() {
        return DEFAULT_FIELDS;
    }
}
