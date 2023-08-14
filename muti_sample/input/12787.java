public abstract class MethodFinder {
    abstract boolean isCorrectMethod(MethodDoc method);
    public MethodDoc search(ClassDoc cd, MethodDoc method) {
        MethodDoc meth = searchInterfaces(cd, method);
        if (meth != null) {
            return meth;
        }
        ClassDoc icd = cd.superclass();
        if (icd != null) {
            meth = Util.findMethod(icd, method);
            if (meth != null) {
            if (isCorrectMethod(meth)) {
                    return meth;
                }
            }
            return search(icd, method);
        }
        return null;
    }
    public MethodDoc searchInterfaces(ClassDoc cd, MethodDoc method) {
        MethodDoc[] implementedMethods = (new ImplementedMethods(method, null)).build();
        for (int i = 0; i < implementedMethods.length; i++) {
            if (isCorrectMethod(implementedMethods[i])) {
                return implementedMethods[i];
            }
        }
        return null;
    }
}
