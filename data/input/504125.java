public class DisabledService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }
}
