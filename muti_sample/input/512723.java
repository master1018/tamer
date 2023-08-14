public class OrFileFilter
        extends AbstractFileFilter
        implements ConditionalFileFilter, Serializable {
    private List fileFilters;
    public OrFileFilter() {
        this.fileFilters = new ArrayList();
    }
    public OrFileFilter(final List fileFilters) {
        if (fileFilters == null) {
            this.fileFilters = new ArrayList();
        } else {
            this.fileFilters = new ArrayList(fileFilters);
        }
    }
    public OrFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        if (filter1 == null || filter2 == null) {
            throw new IllegalArgumentException("The filters must not be null");
        }
        this.fileFilters = new ArrayList();
        addFileFilter(filter1);
        addFileFilter(filter2);
    }
    public void addFileFilter(final IOFileFilter ioFileFilter) {
        this.fileFilters.add(ioFileFilter);
    }
    public List getFileFilters() {
        return Collections.unmodifiableList(this.fileFilters);
    }
    public boolean removeFileFilter(IOFileFilter ioFileFilter) {
        return this.fileFilters.remove(ioFileFilter);
    }
    public void setFileFilters(final List fileFilters) {
        this.fileFilters = fileFilters;
    }
    public boolean accept(final File file) {
        for (Iterator iter = this.fileFilters.iterator(); iter.hasNext();) {
            IOFileFilter fileFilter = (IOFileFilter) iter.next();
            if (fileFilter.accept(file)) {
                return true;
            }
        }
        return false;
    }
    public boolean accept(final File file, final String name) {
        for (Iterator iter = this.fileFilters.iterator(); iter.hasNext();) {
            IOFileFilter fileFilter = (IOFileFilter) iter.next();
            if (fileFilter.accept(file, name)) {
                return true;
            }
        }
        return false;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.toString());
        buffer.append("(");
        if (fileFilters != null) {
            for (int i = 0; i < fileFilters.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                Object filter = fileFilters.get(i);
                buffer.append(filter == null ? "null" : filter.toString());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}
