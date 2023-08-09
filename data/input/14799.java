public class SetVelTo {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setVelTo(10);
        if(performer.getVelTo() != 10)
            throw new RuntimeException("performer.getVelTo() didn't return 10!");
    }
}
