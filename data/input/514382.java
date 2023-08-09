public class CachingIconLoaderTest extends IconLoaderTest {
    @Override
    protected IconLoader create() {
        return new CachingIconLoader(new MockIconLoader(mContext));
    }
}
