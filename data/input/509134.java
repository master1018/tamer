public class tmpCallbackHandler implements CallbackHandler {
    public void handle(Callback[] callback) throws IOException,
            UnsupportedCallbackException {
        if (callback == null) {
            throw new UnsupportedCallbackException(null, "callback is null");
        }
        if (callback.length == 0) {
            throw new UnsupportedCallbackException(null, "callback is empty");
        }
    }
}