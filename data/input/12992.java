public class SetName {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setName("hello");
        if(!performer.getName().equals("hello"))
            throw new RuntimeException("performer.getName() didn't return \"hello\"!");
    }
}
