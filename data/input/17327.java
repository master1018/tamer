public class NullCreate {
    public static void main(String args[])
    {
        try{
            InputStreamReader osw = new InputStreamReader(null);
        } catch (NullPointerException e){
            return;
        }
        throw new RuntimeException("Create with null did not throw an error");
    }
}
