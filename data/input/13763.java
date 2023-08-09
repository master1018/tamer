public class JDBFileFilter extends FileFilter {
    private static String TYPE_UNKNOWN = "Type Unknown";
    private static String HIDDEN_FILE = "Hidden File";
    private Hashtable<String, JDBFileFilter> filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;
    public JDBFileFilter() {
        this.filters = new Hashtable<String, JDBFileFilter>();
    }
    public JDBFileFilter(String extension) {
        this(extension,null);
    }
    public JDBFileFilter(String extension, String description) {
        this();
        if(extension!=null) {
         addExtension(extension);
      }
        if(description!=null) {
         setDescription(description);
      }
    }
    public JDBFileFilter(String[] filters) {
        this(filters, null);
    }
    public JDBFileFilter(String[] filters, String description) {
        this();
        for (String filter : filters) {
            addExtension(filter);
        }
        if(description!=null) {
         setDescription(description);
      }
    }
    @Override
    public boolean accept(File f) {
        if(f != null) {
            if(f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if(extension != null && filters.get(getExtension(f)) != null) {
                return true;
            };
        }
        return false;
    }
     public String getExtension(File f) {
        if(f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if(i>0 && i<filename.length()-1) {
                return filename.substring(i+1).toLowerCase();
            };
        }
        return null;
    }
    public void addExtension(String extension) {
        if(filters == null) {
            filters = new Hashtable<String, JDBFileFilter>(5);
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }
    @Override
    public String getDescription() {
        if(fullDescription == null) {
            if(description == null || isExtensionListInDescription()) {
                fullDescription = description==null ? "(" : description + " (";
                Enumeration<String> extensions = filters.keys();
                if(extensions != null) {
                    fullDescription += "." + extensions.nextElement();
                    while (extensions.hasMoreElements()) {
                        fullDescription += ", " + extensions.nextElement();
                    }
                }
                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }
        return fullDescription;
    }
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription = b;
        fullDescription = null;
    }
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }
}
