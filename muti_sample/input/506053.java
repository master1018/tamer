class ComparePkgPdiffs implements Comparator {
    public int compare(Object obj1, Object obj2){
        PackageDiff p1 = (PackageDiff)obj1;
        PackageDiff p2 = (PackageDiff)obj2;
        if (p1.pdiff < p2.pdiff)
            return 1;
        if (p1.pdiff > p2.pdiff)
            return -1;
        return p1.name_.compareTo(p2.name_);
    }
}
