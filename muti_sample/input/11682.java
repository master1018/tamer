public class SetExclusiveClass {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setExclusiveClass(10);
        if(performer.getExclusiveClass() != 10)
            throw new RuntimeException("performer.getExclusiveClass() didn't return 10!");
    }
}
