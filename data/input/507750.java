public class ExchangeSender extends Sender {
    public static Sender newInstance(Context context, String uri) {
        return new ExchangeSender(context, uri);
    }
    private ExchangeSender(Context context, String _uri) {
    }
    @Override
    public void close() {
    }
    @Override
    public void open() {
    }
    @Override
    public void sendMessage(long messageId) {
    }
    @Override
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return null;
    }
}
