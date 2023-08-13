public class MockService extends Service {
    public static boolean result = false;
    private final IBinder mBinder = new MockBinder();
    public class MockBinder extends Binder {
        MockService getService() {
            return MockService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        result = true;
        return mBinder;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        result = true;
    }
}
