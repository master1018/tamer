public class SplitNone implements Split {
    public List<Dataset> split(final Dataset dataset) throws AlephException {
        return Collections.singletonList(dataset);
    }
    public SplitObject getObject() {
        return new SplitObjectIdentity();
    }
    public SplitMeasure getMeasure() {
        return new SplitMeasureUnit();
    }
}
