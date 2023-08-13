public class NewModelDestination {
    public static void main(String[] args) throws Exception {
        ModelDestination dest = new ModelDestination();
        if(dest.getIdentifier() != ModelDestination.DESTINATION_NONE)
            throw new RuntimeException("dest.getIdentifier() is not equals ModelDestination.DESTINATION_NONE!");
        if(!(dest.getTransform() instanceof ModelStandardTransform))
            throw new RuntimeException("dest.getTransform() is not instancoef ModelStandardTransform!");
    }
}
