public class JTestCasePlugin extends AbstractUIPlugin {
    private static final String ICONS_DIRECTORY = "icons";
    public static JTestCasePlugin plugin;
    public ResourceBundle resourceBundle;
    public IResource currentResource = null;
    private IJavaSearchScope mSearchScope;
    public static ImageDescriptor getImageDescriptor(String filename) {
        ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.jtestcase.plugin", ICONS_DIRECTORY + File.separator + filename);
        return descriptor;
    }
    public JTestCasePlugin() {
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle("org.jtestcase.plugin.resources.JTestCasePluginResources");
        } catch (MissingResourceException _ex) {
            resourceBundle = null;
        }
    }
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }
    public static JTestCasePlugin getDefault() {
        return plugin;
    }
    public static String getResourceString(String key) {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try {
            return bundle == null ? key : bundle.getString(key);
        } catch (MissingResourceException _ex) {
            return key;
        }
    }
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    public InputStream getFileAsStream(String resource) {
        try {
            return getDefault().openStream(new Path(resource));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
