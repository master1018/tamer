public class NewModelSourceModelIdentifier {
    public static void main(String[] args) throws Exception {
        ModelSource src = new ModelSource(ModelSource.SOURCE_NOTEON_KEYNUMBER);
        if(src.getIdentifier() != ModelSource.SOURCE_NOTEON_KEYNUMBER)
            throw new RuntimeException("src.getIdentifier() doesn't return ModelSource.SOURCE_NOTEON_KEYNUMBER!");
        if(!(src.getTransform() instanceof ModelStandardTransform))
            throw new RuntimeException("src.getTransform() doesn't return object which is instance of ModelStandardTransform!");
    }
}
