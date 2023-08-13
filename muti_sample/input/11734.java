public class NewModelDestinationModelIdentifier {
    public static void main(String[] args) throws Exception {
        ModelDestination dest = new ModelDestination(ModelDestination.DESTINATION_EG1_ATTACK);
        if(dest.getIdentifier() != ModelDestination.DESTINATION_EG1_ATTACK)
            throw new RuntimeException("dest.getIdentifier() is not equals ModelDestination.DESTINATION_EG1_ATTACK!");
        if(!(dest.getTransform() instanceof ModelStandardTransform))
            throw new RuntimeException("dest.getTransform() is not instancoef ModelStandardTransform!");
    }
}
