abstract class AndroidTestReference implements ITestReference, ITestIdentifier {
    public ITestIdentifier getIdentifier() {
        return this;
    }
    public void run(TestExecution execution) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ITestIdentifier) {
            ITestIdentifier testid = (ITestIdentifier) obj;
            return getName().equals(testid.getName());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
