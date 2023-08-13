public class MetaKeywords {
    private final Configuration configuration;
    public MetaKeywords(Configuration configuration) {
        this.configuration = configuration;
    }
    public String[] getMetaKeywords(ClassDoc classdoc) {
        ArrayList<String> results = new ArrayList<String>();
        if( configuration.keywords ) {
            results.addAll(getClassKeyword(classdoc));
            results.addAll(getMemberKeywords(classdoc.fields()));
            results.addAll(getMemberKeywords(classdoc.methods()));
        }
        return results.toArray(new String[]{});
    }
    protected ArrayList<String> getClassKeyword(ClassDoc classdoc) {
        String cltypelower = classdoc.isInterface() ? "interface" : "class";
        ArrayList<String> metakeywords = new ArrayList<String>(1);
        metakeywords.add(classdoc.qualifiedName() + " " + cltypelower);
        return metakeywords;
    }
    public String[] getMetaKeywords(PackageDoc packageDoc) {
        if( configuration.keywords ) {
            String pkgName = Util.getPackageName(packageDoc);
            return new String[] { pkgName + " " + "package" };
        } else {
            return new String[] {};
        }
    }
    public String[] getOverviewMetaKeywords(String title, String docTitle) {
        if( configuration.keywords ) {
            String windowOverview = configuration.getText(title);
            String[] metakeywords = { windowOverview };
            if (docTitle.length() > 0 ) {
                metakeywords[0] += ", " + docTitle;
            }
            return metakeywords;
        } else {
            return new String[] {};
        }
    }
    protected ArrayList<String> getMemberKeywords(MemberDoc[] memberdocs) {
        ArrayList<String> results = new ArrayList<String>();
        String membername;
        for (int i=0; i < memberdocs.length; i++) {
            membername = memberdocs[i].name()
                             + (memberdocs[i].isMethod() ? "()" : "");
            if ( ! results.contains(membername) ) {
                results.add(membername);
            }
        }
        return results;
    }
}
