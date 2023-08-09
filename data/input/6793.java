public class SetKeyFrom {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setKeyFrom(10);
        if(performer.getKeyFrom() != 10)
            throw new RuntimeException("performer.getKeyFrom() didn't return 10!");
    }
}
