public class Confirm {
    public static void main(String[] args) throws Exception {
        System.setIn(new ByteArrayInputStream("1\n".getBytes()));
        new TextCallbackHandler().handle(new Callback[]{
            new ConfirmationCallback("Prince", ConfirmationCallback.INFORMATION,
                    new String[]{"To be", "Not to be"}, 0)});
    }
}
