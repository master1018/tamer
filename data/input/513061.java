public class PluginList {
    private ArrayList<Plugin> mPlugins;
    @Deprecated
    public PluginList() {
        mPlugins = new ArrayList<Plugin>();
    }
    @Deprecated
    public synchronized List getList() {
        return mPlugins;
    }
    @Deprecated
    public synchronized void addPlugin(Plugin plugin) {
        if (!mPlugins.contains(plugin)) {
            mPlugins.add(plugin);
        }
    }
    @Deprecated
    public synchronized void removePlugin(Plugin plugin) {
        int location = mPlugins.indexOf(plugin);
        if (location != -1) {
            mPlugins.remove(location);
        }
    }
    @Deprecated
    public synchronized void clear() {
        mPlugins.clear();
    }
    @Deprecated
    public synchronized void pluginClicked(Context context, int position) {
        try {
            Plugin plugin = mPlugins.get(position);
            plugin.dispatchClickEvent(context);
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
