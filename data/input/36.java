public class Activator extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "com.berdaflex.filearranger";
    private static Activator plugin;
    private BundleContext context;
    private int explorerNum = 0;
    private String currentExplorerId = null;
    private Map<String, ExplorerConfig> explorers = null;
    public Activator() {
        explorers = new ConcurrentHashMap<String, ExplorerConfig>();
    }
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        this.context = context;
        FileArranger.getDefault().startup();
    }
    public void stop(BundleContext context) throws Exception {
        if (explorers != null) {
            explorers.clear();
            explorers = null;
        }
        FileArranger.getDefault().shutdown();
        super.stop(context);
        plugin = null;
        this.context = null;
    }
    public static Activator getDefault() {
        return plugin;
    }
    public BundleContext getContext() {
        return context;
    }
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    public String getNextNavigatorId() {
        if ((explorers == null) || ((explorers != null) && (explorers.isEmpty()))) {
            explorerNum = 0;
        }
        explorerNum++;
        return Integer.toString(explorerNum);
    }
    public ExplorerConfig getNavigatorConfig(String key) {
        if (explorers != null) {
            ExplorerConfig config = explorers.get(key);
            return config;
        }
        return null;
    }
    public void setExplorerConfig(String key, ExplorerConfig explorerConfig) {
        if (explorers != null) {
            explorers.put(key, explorerConfig);
        }
    }
    public void removeNavigatorConfig(String key) {
        if (explorers != null) {
            explorers.remove(key);
        } else {
            currentExplorerId = null;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString()).append(" explorers=");
        for (String key : explorers.keySet()) {
            sb.append(key).append(":").append(explorers.get(key).toString());
        }
        sb.append(", currentExplorerId=").append(currentExplorerId);
        return sb.toString();
    }
    public String getCurrentExplorerId() {
        if ((currentExplorerId == null) && ((explorers != null) && !explorers.isEmpty())) {
            currentExplorerId = "0";
        }
        return currentExplorerId;
    }
    public void setCurrentExplorerId(String currentExplorerId) {
        this.currentExplorerId = currentExplorerId;
    }
}
