public class LexicographicalCorpusRanker extends AbstractCorpusRanker {
    public LexicographicalCorpusRanker(Corpora corpora) {
        super(corpora);
    }
    @Override
    public List<Corpus> rankCorpora(Corpora corpora) {
        ArrayList<Corpus> ordered = new ArrayList<Corpus>(corpora.getEnabledCorpora());
        Collections.sort(ordered, new Comparator<Corpus>() {
            public int compare(Corpus c1, Corpus c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        return ordered;
    }
}
