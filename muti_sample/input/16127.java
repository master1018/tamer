public class NewModelSourceModelIdentifierBoolean {
    public static void main(String[] args) throws Exception {
        ModelSource src = new ModelSource(ModelSource.SOURCE_NOTEON_KEYNUMBER,ModelStandardTransform.DIRECTION_MAX2MIN);
        if(src.getIdentifier() != ModelSource.SOURCE_NOTEON_KEYNUMBER)
            throw new RuntimeException("src.getIdentifier() doesn't return ModelSource.SOURCE_NOTEON_KEYNUMBER!");
        if(!(src.getTransform() instanceof ModelStandardTransform))
            throw new RuntimeException("src.getTransform() doesn't return object which is instance of ModelStandardTransform!");
        ModelStandardTransform trans = (ModelStandardTransform)src.getTransform();
        if(trans.getDirection() != ModelStandardTransform.DIRECTION_MAX2MIN)
            throw new RuntimeException("trans.getDirection() doesn't return ModelStandardTransform.DIRECTION_MAX2MIN!");
    }
}
