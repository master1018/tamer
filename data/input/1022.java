public class MiscellaneousFeature extends Feature {
    private ArrayList<String> allele = new ArrayList<String>();
    private int citation;
    private ArrayList<DatabaseCrossReference> databaseCrossReferences = new ArrayList<DatabaseCrossReference>();
    private String experiment = "";
    private String function = "";
    private String gene = "";
    private ArrayList<String> geneSynonyms = new ArrayList<String>();
    private FeatureInference inference = new FeatureInference();
    private String label = "";
    private String locusTag = "";
    private String map = "";
    private String notes = "";
    private String number = "";
    private ArrayList<String> oldLocusTags = new ArrayList<String>();
    private String phenotype = "";
    private ArrayList<String> products = new ArrayList<String>();
    private boolean pseudo;
    private String standardName = "";
    public MiscellaneousFeature() {
        super();
    }
    public ArrayList<String> getAllele() {
        return this.allele;
    }
    public int getCitation() {
        return this.citation;
    }
    public ArrayList<DatabaseCrossReference> getDatabaseCrossReferences() {
        return this.databaseCrossReferences;
    }
    public String getExperiment() {
        return this.experiment;
    }
    public String getFunction() {
        return this.function;
    }
    public String getGene() {
        return this.gene;
    }
    public ArrayList<String> getGeneSynonyms() {
        return this.geneSynonyms;
    }
    public FeatureInference getInference() {
        return this.inference;
    }
    public String getLabel() {
        return this.label;
    }
    public String getLocusTag() {
        return this.locusTag;
    }
    public String getMap() {
        return this.map;
    }
    public String getNotes() {
        return this.notes;
    }
    public String getNumber() {
        return this.number;
    }
    public ArrayList<String> getOldLocusTags() {
        return this.oldLocusTags;
    }
    public String getPhenotype() {
        return this.phenotype;
    }
    public ArrayList<String> getProducts() {
        return this.products;
    }
    public boolean getPseudo() {
        return this.pseudo;
    }
    public String getStandardName() {
        return this.standardName;
    }
    public void setCitation(int citation) {
        this.citation = citation;
    }
    public void setExperiment(String experiment) {
        this.experiment = experiment == null ? "" : experiment.trim();
    }
    public void setFunction(String function) {
        this.function = function == null ? "" : function.trim();
    }
    public void setGene(String gene) {
        this.gene = gene == null ? "" : gene.trim();
    }
    public void setInference(FeatureInference inference) {
        this.inference = inference == null ? new FeatureInference() : inference;
    }
    public void setLabel(String label) {
        this.label = label == null ? "" : label.trim();
    }
    public void setLocusTag(String locusTag) {
        this.locusTag = locusTag == null ? "" : locusTag.trim();
    }
    public void setMap(String map) {
        this.map = map == null ? "" : map.trim();
    }
    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }
    public void setNumber(String number) {
        this.number = number == null ? "" : number.trim();
    }
    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype == null ? "" : phenotype.trim();
    }
    public void setPseudo(boolean pseudo) {
        this.pseudo = pseudo;
    }
    public void setStandardName(String standardName) {
        this.standardName = standardName == null ? "" : standardName.trim();
    }
}
