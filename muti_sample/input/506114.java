public class GroovyRule implements Rule {
    private final String mName;
    private final Script mScript;
    private final Binding mBinding;
    private final Closure mClosure;
    private final List<Class> mCategories;
    public GroovyRule(String name, Script script) {
        mName = name;
        mScript = script;
        mBinding = new Binding();
        mScript.setBinding(mBinding);
        mClosure = new Closure(this) {
            @Override
            public Object call() {
                return mScript.run();
            }
        };
        mCategories = new ArrayList<Class>();
        Collections.addAll(mCategories, DOMCategory.class, LayoutAnalysisCategory.class);
    }
    public String getName() {
        return mName;
    }
    public void run(LayoutAnalysis analysis, Node node) {
        mBinding.setVariable("analysis", analysis);
        mBinding.setVariable("node", node);
        GroovyCategorySupport.use(mCategories, mClosure);
    }
}
