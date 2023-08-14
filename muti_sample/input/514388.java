class PackageAPI implements Comparable {
    public String name_;
    public List classes_;  
    public String doc_ = null;
    public PackageAPI(String name) {
        name_ = name;
        classes_ = new ArrayList(); 
    }
    public int compareTo(Object o) {
        PackageAPI oPackageAPI = (PackageAPI)o;
        if (APIComparator.docChanged(doc_, oPackageAPI.doc_))
            return -1;
        return name_.compareTo(oPackageAPI.name_);
    }
    public boolean equals(Object o) {
        if (name_.compareTo(((PackageAPI)o).name_) == 0)
            return true;
        return false;
    }
}
