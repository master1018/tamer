public class SetIdentifier {
    public static void main(String[] args) throws Exception {
        ModelDestination dest = new ModelDestination();
        dest.setIdentifier(ModelDestination.DESTINATION_EG1_ATTACK);
        if(dest.getIdentifier() != ModelDestination.DESTINATION_EG1_ATTACK)
            throw new RuntimeException("dest.getIdentifier() is not equals ModelDestination.DESTINATION_EG1_ATTACK!");
    }
}
