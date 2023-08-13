public class NewModelSource {
    public static void main(String[] args) throws Exception {
        ModelSource src = new ModelSource();
        if(src.getIdentifier() != ModelSource.SOURCE_NONE)
            throw new RuntimeException("src.getIdentifier() doesn't return ModelSource.SOURCE_NONE!");
        if(!(src.getTransform() instanceof ModelStandardTransform))
            throw new RuntimeException("src.getTransform() doesn't return object which is instance of ModelStandardTransform!");
    }
}
