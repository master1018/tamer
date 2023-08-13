public class Default {
   public static void main(String args[]) throws Exception {
        TextCallbackHandler h = new TextCallbackHandler();
        NameCallback nc = new NameCallback("Name: ", "charlie");
        ConfirmationCallback cc = new ConfirmationCallback
                        ("Correct?",
                        ConfirmationCallback.INFORMATION,
                        ConfirmationCallback.YES_NO_OPTION,
                        ConfirmationCallback.NO);
        Callback[] callbacks = { nc, cc };
        h.handle(callbacks);
        if (cc.getSelectedIndex() == ConfirmationCallback.YES) {
            System.out.println("yes");
        } else {
            System.out.println("no");
        }
   }
}
