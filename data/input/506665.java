public class ConfigurableResourceItem extends ProjectResourceItem {
    public ConfigurableResourceItem(String name) {
        super(name);
    }
    public boolean hasAlternates() {
        for (ResourceFile file : mFiles) {
            if (file.getFolder().getConfiguration().isDefault() == false) {
                return true;
            }
        }
        return false;
    }
    public boolean hasDefault() {
        for (ResourceFile file : mFiles) {
            if (file.getFolder().getConfiguration().isDefault()) {
                return true;
            }
        }
        return (mFiles.size() == 0);
    }
    public int getAlternateCount() {
        int count = 0;
        for (ResourceFile file : mFiles) {
            if (file.getFolder().getConfiguration().isDefault() == false) {
                count++;
            }
        }
        return count;
    }
    @Override
    public boolean isEditableDirectly() {
        return hasAlternates() == false;
    }
}
