public class TestMethodInformation {
    private boolean annotationExists = false;
    private List<TestTargetNew> targets = new ArrayList<TestTargetNew>();
    private String error = null;
    private Color color = Color.RED;
    public enum Level {
        TODO, PARTIAL, PARTIAL_COMPLETE, COMPLETE, ADDITIONAL, NOT_NECESSARY, NOT_FEASIBLE, SUFFICIENT
    }
    public enum Color {
        GREEN , YELLOW , RED
    }
    public TestMethodInformation(Originator originator,
            AnnotationDesc[] annots, ClassDoc targetClass) {
        if (targetClass == null) {
            addError("target class annotation missing!");
            return;
        }
        for (AnnotationDesc annot : annots) {
            if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.TestTargets")) {
                annotationExists = true;
                ElementValuePair[] pairs = annot.elementValues();
                if (pairs.length != 1
                        && !pairs[0].element().qualifiedName().equals(
                                "dalvik.annotation.TestTargets.value")) {
                    throw new RuntimeException("TestTargets has mismatched "
                            + "attributes");
                }
                AnnotationValue[] targets = (AnnotationValue[])pairs[0].value()
                        .value();
                for (AnnotationValue ttn : targets) {
                    AnnotationDesc ttnd = (AnnotationDesc)ttn.value();
                    handleTestTargetNew(originator, ttnd, targetClass);
                }
            } else if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.TestTargetNew")) {
                annotationExists = true;
                handleTestTargetNew(originator, annot, targetClass);
            } 
        }
        boolean targetsCorrect = true;
        for (TestTargetNew ttn : targets) {
            targetsCorrect &= (ttn.getTargetMethod() != null || ttn
                    .getTargetClass() != null);
        }
        if (annotationExists) {
            if (targetsCorrect) {
                color = Color.GREEN;
            } 
        } else {
            addError("no annotation!");
        }
    }
    private void handleTestTargetNew(Originator originator, AnnotationDesc ttn,
            ClassDoc targetClass) {
        TestTargetNew testTarget = new TestTargetNew(originator, ttn,
                targetClass);
        if (testTarget.isHavingProblems()) {
            addError(testTarget.getNotes());
        }
        targets.add(testTarget);
    }
    private void addError(String err) {
        if (error == null)
            error = "";
        error += err + " ; ";
    }
    public String getError() {
        return error;
    }
    public List<TestTargetNew> getTargets() {
        return targets;
    }
    public Color getColor() {
        return color;
    }
}
