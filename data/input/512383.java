class PostReceiverCreationAction implements IPostTypeCreationAction {
    private final static PostReceiverCreationAction sAction = new PostReceiverCreationAction();
    private PostReceiverCreationAction() {
    }
    public static IPostTypeCreationAction getAction() {
        return sAction;
    }
    public void processNewType(IType newType) {
        try {
            String methodContent = 
                "    @Override\n" +
                "    public void onReceive(Context context, Intent intent) {\n" +
                "        
                "    }";
            newType.createMethod(methodContent, null , false ,
                    new NullProgressMonitor());
            ICompilationUnit compilationUnit = null;
            IJavaElement element = newType;
            do {
                IJavaElement parentElement = element.getParent();
                if (parentElement !=  null) {
                    if (parentElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
                        compilationUnit = (ICompilationUnit)parentElement;
                    }
                    element = parentElement;
                } else {
                    break;
                }
            } while (compilationUnit == null);
            if (compilationUnit != null) {
                compilationUnit.createImport(AndroidConstants.CLASS_CONTEXT,
                        null , new NullProgressMonitor());
                compilationUnit.createImport(AndroidConstants.CLASS_INTENT,
                        null , new NullProgressMonitor());
            }
        } catch (JavaModelException e) {
        }
    }
}
