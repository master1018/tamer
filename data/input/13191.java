public class SetKeyTo {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setKeyTo(10);
        if(performer.getKeyTo() != 10)
            throw new RuntimeException("performer.getKeyTo() didn't return 10!");
    }
}
