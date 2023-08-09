public class EmptyService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
