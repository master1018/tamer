public abstract class ProjectResourceItem extends ResourceItem {
    private final static Comparator<ResourceFile> sComparator = new Comparator<ResourceFile>() {
        public int compare(ResourceFile file1, ResourceFile file2) {
            FolderConfiguration fc1 = file1.getFolder().getConfiguration();
            FolderConfiguration fc2 = file2.getFolder().getConfiguration();
            return fc1.compareTo(fc2);
        }
    };
    protected final ArrayList<ResourceFile> mFiles = new ArrayList<ResourceFile>();
    public ProjectResourceItem(String name) {
        super(name);
    }
    public abstract boolean isEditableDirectly();
    protected void add(ResourceFile file) {
        mFiles.add(file);
    }
    protected void reset() {
        mFiles.clear();
    }
    public ResourceFile[] getSourceFileArray() {
        ArrayList<ResourceFile> list = new ArrayList<ResourceFile>();
        list.addAll(mFiles);
        Collections.sort(list, sComparator);
        return list.toArray(new ResourceFile[list.size()]);
    }
    public List<ResourceFile> getSourceFileList() {
        return Collections.unmodifiableList(mFiles);
    }
    protected void replaceWith(ProjectResourceItem item) {
        mFiles.clear();
        mFiles.addAll(item.mFiles);
    }
}
