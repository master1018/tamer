public class ExtractStringDescriptor extends RefactoringDescriptor {
    public static final String ID =
        "com.android.ide.eclipse.adt.refactoring.extract.string";  
    private final Map<String, String> mArguments;
    public ExtractStringDescriptor(String project, String description, String comment,
            Map<String, String> arguments) {
        super(ID, project, description, comment,
                RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE 
        );
        mArguments = arguments;
    }
    public Map<String, String> getArguments() {
        return mArguments;
    }
    @Override
    public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        try {
            ExtractStringRefactoring ref = new ExtractStringRefactoring(mArguments);
            return ref;
        } catch (NullPointerException e) {
            status.addFatalError("Failed to recreate ExtractStringRefactoring from descriptor");
            return null;
        }
    }
}
