public class AndFileFilter
        extends AbstractFileFilter
        implements ConditionalFileFilter, Serializable {
    private List fileFilters;
    public AndFileFilter() {
        this.fileFilters = new ArrayList();
    }
    public AndFileFilter(final List fileFilters) {
        if (fileFilters == null) {
            this.fileFilters = new ArrayList();
        } else {
            this.fileFilters = new ArrayList(fileFilters);
        }
    }
    public AndFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
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
    public boolean removeFileFilter(final IOFileFilter ioFileFilter) {
        return this.fileFilters.remove(ioFileFilter);
    }
    public void setFileFilters(final List fileFilters) {
        this.fileFilters = new ArrayList(fileFilters);
    }
    public boolean accept(final File file) {
        if (this.fileFilters.size() == 0) {
            return false;
        }
        for (Iterator iter = this.fileFilters.iterator(); iter.hasNext();) {
            IOFileFilter fileFilter = (IOFileFilter) iter.next();
            if (!fileFilter.accept(file)) {
                return false;
            }
        }
        return true;
    }
    public boolean accept(final File file, final String name) {
        if (this.fileFilters.size() == 0) {
            return false;
        }
        for (Iterator iter = this.fileFilters.iterator(); iter.hasNext();) {
            IOFileFilter fileFilter = (IOFileFilter) iter.next();
            if (!fileFilter.accept(file, name)) {
                return false;
            }
        }
        return true;
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
