class PostActivityCreationAction implements IPostTypeCreationAction {
    private final static PostActivityCreationAction sAction = new PostActivityCreationAction();
    private PostActivityCreationAction() {
    }
    public static IPostTypeCreationAction getAction() {
        return sAction;
    }
    public void processNewType(IType newType) {
        try {
            String methodContent = 
                "    \n" +
                "    @Override\n" +
                "    public void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "\n" +
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
                compilationUnit.createImport(AndroidConstants.CLASS_BUNDLE,
                        null , new NullProgressMonitor());
            }
        } catch (JavaModelException e) {
        }
    }
}
