public class SocialApiGuiceModuleTest extends TestCase {
    private Injector injector;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        injector = Guice.createInjector(new SocialApiGuiceModule(), new PropertiesModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(OAuthDataStore.class).toInstance(EasyMock.createMock(OAuthDataStore.class));
            }
        });
    }
    public void testAuthHandler() {
        injector.getInstance(AuthenticationHandlerProvider.class).get();
        AuthenticationHandlerProvider provider = injector.getInstance(AuthenticationHandlerProvider.class);
        assertEquals(3, provider.get().size());
        List<AuthenticationHandler> handlers = injector.getInstance(Key.get(new TypeLiteral<List<AuthenticationHandler>>() {
        }));
        assertEquals(3, handlers.size());
    }
}
