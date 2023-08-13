public class Test4080522 {
    public static void main(String[] args) {
        OurSecurityManager sm = new OurSecurityManager();
        String[] path = {"a", "b"};
        test(path);
        System.setSecurityManager(sm);
        try {
            Beans.setDesignTime(true);
            throw new Error("Beans.setDesignTime should throw SecurityException");
        } catch (SecurityException exception) {
        }
        try {
            Beans.setGuiAvailable(true);
            throw new Error("Beans.setGuiAvailable should throw SecurityException");
        } catch (SecurityException exception) {
        }
        try {
            Introspector.setBeanInfoSearchPath(path);
            throw new Error("Introspector.setBeanInfoSearchPath should throw SecurityException");
        } catch (SecurityException exception) {
        }
        try {
            PropertyEditorManager.setEditorSearchPath(path);
            throw new Error("PropertyEditorManager.setEditorSearchPath should throw SecurityException");
        } catch (SecurityException exception) {
        }
        sm.friendly = true;
        test(path);
    }
    private static void test(String[] path) {
        try {
            Beans.setDesignTime(true);
            Beans.setGuiAvailable(true);
            Introspector.setBeanInfoSearchPath(path);
            PropertyEditorManager.setEditorSearchPath(path);
        } catch (SecurityException exception) {
            throw new Error("unexpected security exception", exception);
        }
    }
    private static class OurSecurityManager extends SecurityManager {
        boolean friendly;
        public void checkPropertiesAccess() {
            if (!friendly) {
                throw new SecurityException("No way");
            }
        }
    }
}
