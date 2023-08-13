public class SetInstance {
    public static void main(String[] args) throws Exception {
        ModelIdentifier id = new ModelIdentifier("test","a",1);
        id.setInstance(2);
        if(id.getInstance() != 2)
            throw new RuntimeException("id.getInstance() doesn't return 2!");
    }
}
