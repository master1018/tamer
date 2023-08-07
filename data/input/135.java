public class VersionChoice implements Serializable {
    public static final String PROPERTY_PREFERRED_VERSION = "preferred-version";
    public static final String PROPERTY_AVAILABLE_VERSIONS = "available-versions";
    private List<Version> availableVersions = null;
    private Version selectedVersion = null;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    public VersionChoice() {
    }
    public void setSelectedVersion(Version version) {
        boolean equals = false;
        if (version == null) {
            if (this.getSelectedVersion() == null) {
                equals = true;
            }
        } else {
            equals = version.equals(this.getSelectedVersion());
        }
        if (!equals) {
            if (version != null) {
                if (!this.isAnAvailableVersion(version)) {
                    throw new IllegalArgumentException("the selected version has to be an available version");
                }
            }
            Version oldVersion = this.getSelectedVersion();
            this.selectedVersion = version;
            this.firePropertyChange(PROPERTY_PREFERRED_VERSION, oldVersion, version);
        }
    }
    public Version getSelectedVersion() {
        return this.selectedVersion;
    }
    private boolean isAnAvailableVersion(Version version) {
        boolean result = false;
        if (version != null) {
            if (this.availableVersions != null) {
                result = this.availableVersions.contains(version);
            }
        }
        return result;
    }
    public void removeAvailableVersions() {
        this.setSelectedVersion(null);
        if (this.availableVersions != null) {
            for (int i = 0; i < this.availableVersions.size(); i++) {
                Object oldValue = this.availableVersions.get(i);
                Object newValue = null;
                this.fireIndexedPropertyChange(PROPERTY_AVAILABLE_VERSIONS, i, oldValue, newValue);
            }
            this.availableVersions.clear();
        }
    }
    public void removeAvailableVersion(Version version) {
        if (version != null && this.availableVersions != null) {
            int index = this.availableVersions.indexOf(version);
            if (index >= 0) {
                boolean removed = this.availableVersions.remove(version);
                if (removed) {
                    for (int i = index; i < this.availableVersions.size(); i++) {
                        Object oldValue = null;
                        Object newValue = null;
                        if (i == index) {
                            oldValue = version;
                        } else {
                            oldValue = this.availableVersions.get(i - 1);
                        }
                        newValue = this.availableVersions.get(i);
                        this.fireIndexedPropertyChange(PROPERTY_AVAILABLE_VERSIONS, i, oldValue, newValue);
                    }
                }
                if (version.equals(this.getSelectedVersion())) {
                    if (this.availableVersions.size() == 0) {
                        this.setSelectedVersion(null);
                    } else {
                        this.setSelectedVersion(this.availableVersions.get(this.availableVersions.size() - 1));
                    }
                }
            }
        }
    }
    public boolean containsAvailableVersions() {
        boolean result = false;
        if (this.availableVersions != null) {
            result = this.availableVersions.size() > 0;
        }
        return result;
    }
    public boolean addAvailableVersion(Version version) {
        return this.addAvailableVersion(version, false);
    }
    public Version getMostRecentVersion() {
        Version version = null;
        if (this.availableVersions != null) {
            version = this.availableVersions.get(this.availableVersions.size() - 1);
        }
        return version;
    }
    public List<Version> getAvailableVersions() {
        List<Version> list = null;
        if (this.availableVersions != null && this.availableVersions.size() > 0) {
            synchronized (this) {
                list = new ArrayList<Version>(this.availableVersions);
            }
        }
        if (list != null) {
            Collections.sort(list);
        }
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }
    public boolean addAvailableVersion(Version version, boolean moreRecentVersionAsSelected) {
        boolean result = false;
        if (version != null) {
            if (this.availableVersions == null) {
                this.availableVersions = new SortedList<Version>();
            }
            if (!this.availableVersions.contains(version)) {
                if (this.availableVersions.add(version)) {
                    int index = this.availableVersions.indexOf(version);
                    for (int i = index; i < this.availableVersions.size(); i++) {
                        Object oldValue = null;
                        Object newValue = null;
                        if (i < this.availableVersions.size() - 1) {
                            oldValue = this.availableVersions.get(i + 1);
                        } else {
                            oldValue = null;
                        }
                        newValue = this.availableVersions.get(i);
                        this.fireIndexedPropertyChange(PROPERTY_AVAILABLE_VERSIONS, i, oldValue, newValue);
                    }
                    result = true;
                }
            }
        }
        if (moreRecentVersionAsSelected && result) {
            this.setSelectedVersion(this.availableVersions.get(this.availableVersions.size() - 1));
        }
        return result;
    }
    public VersionChoice copy() {
        VersionChoice result = new VersionChoice();
        result.availableVersions = this.availableVersions;
        result.selectedVersion = this.selectedVersion;
        return result;
    }
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof VersionChoice) {
            VersionChoice other = (VersionChoice) obj;
            Version version = this.getSelectedVersion();
            Version otherVersion = other.getSelectedVersion();
            if (version == null) {
                if (otherVersion == null) {
                    result = true;
                }
            } else {
                result = version.equals(otherVersion);
            }
            if (result) {
                List<Version> versions = this.availableVersions;
                List<Version> otherVersions = other.availableVersions;
                if (versions == null) {
                    if (otherVersions == null) {
                        result = true;
                    }
                } else {
                    if (otherVersions != null) {
                        if (versions.size() == otherVersions.size()) {
                            int i = 0;
                            for (; i < versions.size(); i++) {
                                Version currentVersion = versions.get(i);
                                if (!otherVersions.contains(currentVersion)) {
                                    break;
                                }
                            }
                            if (i == versions.size()) {
                                result = true;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(propertyName, listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(propertyName, listener);
    }
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.support.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        this.support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
}
