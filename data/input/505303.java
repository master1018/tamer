public class PackageByNameComparator implements Comparator<IPackageDelta> {
    public int compare(IPackageDelta a, IPackageDelta b) {
        assert a.getType() == b.getType();
        IPackage aPackage = null;
        IPackage bPackage = null;
        if (a.getFrom() != null) {
            aPackage = a.getFrom();
            bPackage = b.getFrom();
        } else {
            aPackage = a.getTo();
            bPackage = b.getTo();
        }
        return aPackage.getName().compareTo(bPackage.getName());
    }
}
