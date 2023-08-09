public abstract class AbstractFileFilter implements IOFileFilter {
    public boolean accept(File file) {
        return accept(file.getParentFile(), file.getName());
    }
    public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }
    public String toString() {
        String name = getClass().getName();
        int period = name.lastIndexOf('.');
        return (period > 0 ? name.substring(period + 1) : name);
    }
}
