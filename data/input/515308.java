public class PackageIconLoaderTest extends IconLoaderTest {
    @Override
    protected IconLoader create() throws Exception {
        return new PackageIconLoader(mContext, mContext.getPackageName());
    }
}
