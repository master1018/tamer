public class Bug6989440 {
    public static void main(String[] args) {
        TestThread t1 = new TestThread(LocaleNameProvider.class);
        TestThread t2 = new TestThread(TimeZoneNameProvider.class);
        TestThread t3 = new TestThread(DateFormatProvider.class);
        t1.start();
        t2.start();
        t3.start();
    }
    static class TestThread extends Thread {
        private Class<? extends LocaleServiceProvider> cls;
        public TestThread(Class<? extends LocaleServiceProvider> providerClass) {
            cls = providerClass;
        }
        public void run() {
            LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(cls);
            pool.getAvailableLocales();
        }
    }
}
