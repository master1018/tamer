class CompareClassPdiffs implements Comparator {
    public int compare(Object obj1, Object obj2){
        ClassDiff c1 = (ClassDiff)obj1;
        ClassDiff c2 = (ClassDiff)obj2;
        if (c1.pdiff < c2.pdiff)
            return 1;
        if (c1.pdiff > c2.pdiff)
            return -1;
        return c1.name_.compareTo(c2.name_);
    }
}
