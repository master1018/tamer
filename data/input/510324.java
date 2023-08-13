public class AndroidTestPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "com.android.ide.eclipse.adt.tests";
    private static AndroidTestPlugin sPlugin;
    public AndroidTestPlugin() {
    }
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        sPlugin = this;
    }
    @Override
    public void stop(BundleContext context) throws Exception {
        sPlugin = null;
        super.stop(context);
    }
    public static AndroidTestPlugin getDefault() {
        return sPlugin;
    }
}
