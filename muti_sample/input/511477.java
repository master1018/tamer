public class MockSyncAdapterService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return MockSyncAdapter.getMockSyncAdapter();
    }
}
