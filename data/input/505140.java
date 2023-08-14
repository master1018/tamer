public class PackageOverviewPage implements IEmitter {
    private static final String PACGE = "PackageOverviewPage";
    private final IPackageDelta delta;
    private List<IClassDefinitionDelta> removedClasses;
    private List<IClassDefinitionDelta> addedClasses;
    private List<IClassDefinitionDelta> changedClasses;
    private final Map<String, String> commonInfos;
    public PackageOverviewPage(IPackageDelta delta,
            Map<String, String> commonInfos) {
        this.delta = delta;
        this.commonInfos = commonInfos;
        prepareData();
    }
    private void prepareData() {
        removedClasses = new ArrayList<IClassDefinitionDelta>(SigDelta
                .getRemoved(delta.getClassDeltas()));
        Collections.sort(removedClasses, new ClassByNameComparator());
        addedClasses = new ArrayList<IClassDefinitionDelta>(SigDelta
                .getAdded(delta.getClassDeltas()));
        Collections.sort(addedClasses, new ClassByNameComparator());
        changedClasses = new ArrayList<IClassDefinitionDelta>(SigDelta
                .getChanged(delta.getClassDeltas()));
        Collections.sort(changedClasses, new ClassByNameComparator());
    }
    public void writeTo(StringBuilder b) {
        StringTemplate template = TemplateStore.getStringTemplate(PACGE);
        template.setArgumentContext(commonInfos);
        template.setAttribute("package_delta", delta);
        template.setAttribute("removed_classes", removedClasses);
        template.setAttribute("added_classes", addedClasses);
        template.setAttribute("changed_classes", changedClasses);
        b.append(template.toString());
    }
}
