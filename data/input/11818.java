public class SetVelFrom {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setVelFrom(10);
        if(performer.getVelFrom() != 10)
            throw new RuntimeException("performer.getVelFrom() didn't return 10!");
    }
}
