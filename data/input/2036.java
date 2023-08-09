public class NewModelSourceModelIdentifierModelTransform {
    public static void main(String[] args) throws Exception {
        ModelStandardTransform trans = new ModelStandardTransform();
        ModelSource src = new ModelSource(ModelSource.SOURCE_NOTEON_KEYNUMBER, trans);
        if(src.getIdentifier() != ModelSource.SOURCE_NOTEON_KEYNUMBER)
            throw new RuntimeException("src.getIdentifier() doesn't return ModelSource.SOURCE_NOTEON_KEYNUMBER!");
        if(src.getTransform() != trans)
            throw new RuntimeException("src.getTransform() doesn't return trans!");
    }
}
