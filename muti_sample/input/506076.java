public class ApiOverviewPage implements IEmitter {
    private final IApiDelta apiDelta;
    private List<IPackageDelta> removedPackages;
    private List<IPackageDelta> addedPackages;
    private List<IPackageDelta> changedPackages;
    private Map<String, String> commonInfos;
    public ApiOverviewPage(IApiDelta apiDelta,
            Map<String, String> commonInfos) {
        this.apiDelta = apiDelta;
        this.commonInfos = commonInfos;
        prepareData();
    }
    private void prepareData() {
        if (apiDelta.getPackageDeltas().isEmpty()) {
            commonInfos.put("no_delta", "no_delta");
        }
        removedPackages = new ArrayList<IPackageDelta>(SigDelta
                .getRemoved(apiDelta.getPackageDeltas()));
        Collections.sort(removedPackages, new PackageByNameComparator());
        addedPackages = new ArrayList<IPackageDelta>(SigDelta.getAdded(apiDelta
                .getPackageDeltas()));
        Collections.sort(addedPackages, new PackageByNameComparator());
        changedPackages = new ArrayList<IPackageDelta>(SigDelta
                .getChanged(apiDelta.getPackageDeltas()));
        Collections.sort(changedPackages, new PackageByNameComparator());
    }
    public void writeTo(StringBuilder b) {
        StringTemplate template = TemplateStore
                .getStringTemplate("ApiOverviewPage");
        template.setArgumentContext(commonInfos);
        template.setAttribute("removed_packages", removedPackages);
        template.setAttribute("added_packages", addedPackages);
        template.setAttribute("changed_packages", changedPackages);
        b.append(template.toString());
    }
}
