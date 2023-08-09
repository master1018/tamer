public class MockAccountService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return MockAccountAuthenticator.getMockAuthenticator(this).getIBinder();
    }
}
