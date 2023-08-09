public class AnnotationPointer {
    final ExecutableMemberDoc targetMethod;
    private List<TestTargetNew> targets = new ArrayList<TestTargetNew>();
    AnnotationPointer(ExecutableMemberDoc targetMethod) {
        this.targetMethod = targetMethod;
    }
    public void addTestTargetNew(TestTargetNew testMethodInfo) {
        targets.add(testMethodInfo);
    }
    public List<TestTargetNew> getTargets() {
        return targets;
    }
    public void addProxiesFrom(AnnotationPointer ap) {
        List<TestTargetNew> t = ap.targets;
        for (TestTargetNew ttn : t) {
            TestTargetNew tnew = ttn.cloneMe("<b>(INDIRECTLY tested)</b>");
            targets.add(tnew);
        }
    }
}
