public class AndroidJUnitPropertyTester extends PropertyTester {
    private static final String PROPERTY_IS_TEST = "isTest";  
    private static final String PROPERTY_CAN_LAUNCH_AS_JUNIT_TEST = "canLaunchAsJUnit"; 
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof IAdaptable)) {
            final String elementName = (receiver == null ? "null" : 
                receiver.getClass().getName());
            throw new IllegalArgumentException(
                    String.format("Element must be of type IAdaptable, is %s", 
                            elementName));
        }
        IJavaElement element;
        if (receiver instanceof IJavaElement) {
            element = (IJavaElement) receiver;
        } else if (receiver instanceof IResource) {
            element = JavaCore.create((IResource) receiver);
            if (element == null) {
                return false;
            }
        } else { 
            element= (IJavaElement) ((IAdaptable) receiver).getAdapter(IJavaElement.class);
            if (element == null) {
                IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(
                        IResource.class);
                element = JavaCore.create(resource);
                if (element == null) {
                    return false;
                }
            }
        }
        if (PROPERTY_IS_TEST.equals(property)) { 
            return isJUnitTest(element);
        } else if (PROPERTY_CAN_LAUNCH_AS_JUNIT_TEST.equals(property)) {
            return canLaunchAsJUnitTest(element);
        }
        throw new IllegalArgumentException(
                String.format("Unknown test property '%s'", property)); 
    }
    private boolean canLaunchAsJUnitTest(IJavaElement element) {
        try {
            switch (element.getElementType()) {
                case IJavaElement.JAVA_PROJECT:
                    return true; 
                case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                    return false; 
                case IJavaElement.PACKAGE_FRAGMENT:
                    return ((IPackageFragment) element).hasChildren(); 
                case IJavaElement.COMPILATION_UNIT:
                case IJavaElement.CLASS_FILE:
                case IJavaElement.TYPE:
                case IJavaElement.METHOD:
                    return isJUnitTest(element);
                default:
                    return false;
            }
        } catch (JavaModelException e) {
            return false;
        }
    }
    private boolean isJUnitTest(IJavaElement element) {
        try {
            IType testType = null;
            if (element instanceof ICompilationUnit) {
                testType = (((ICompilationUnit) element)).findPrimaryType();
            } else if (element instanceof IClassFile) {
                testType = (((IClassFile) element)).getType();
            } else if (element instanceof IType) {
                testType = (IType) element;
            } else if (element instanceof IMember) {
                testType = ((IMember) element).getDeclaringType();
            }
            if (testType != null && testType.exists()) {
                return TestSearchEngine.isTestOrTestSuite(testType);
            }
        } catch (CoreException e) {
        }
        return false;
    }
}
