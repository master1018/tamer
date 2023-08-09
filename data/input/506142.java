public class MockCorpusFactory implements CorpusFactory {
    public Collection<Corpus> createCorpora(Sources sources) {
        ArrayList<Corpus> corpora = new ArrayList<Corpus>();
        for (Source source : sources.getSources()) {
            corpora.add(new MockCorpus(source));
        }
        return corpora;
    }
}
