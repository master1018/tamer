public class LocalizedMessage {
    static class LocalizedException extends Throwable {
        boolean localizedMessageCalled = false;
        LocalizedException() {
            localizedMessageCalled = false;
        }
        public String getLocalizedMessage() {
            localizedMessageCalled = true;
            return new String("FOOBAR");
        }
    }
    public static void main(String[] args) throws Exception {
        LocalizedException e = new LocalizedException();
        e.toString();
        if (!e.localizedMessageCalled) {
            throw new Exception("Throwable.toString() must call " +
                                "getLocalizedMessage()");
        }
    }
}
