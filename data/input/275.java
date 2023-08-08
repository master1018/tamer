public class AttributesExampleReader extends AbstractExampleReader {
    private Iterator<Example> parent;
    private ExampleSet exampleSet;
    public AttributesExampleReader(Iterator<Example> parent, ExampleSet exampleSet) {
        this.parent = parent;
        this.exampleSet = exampleSet;
    }
    public boolean hasNext() {
        return this.parent.hasNext();
    }
    public Example next() {
        if (!hasNext()) return null;
        Example example = this.parent.next();
        if (example == null) return null;
        return new Example(example.getDataRow(), exampleSet);
    }
}
