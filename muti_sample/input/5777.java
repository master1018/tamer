public class Test6397609 {
    public static void main(String[] args) throws Exception {
        MemoryClassLoader loader = new MemoryClassLoader();
        PropertyEditorManager.registerEditor(
                Object.class,
                loader.compile("Editor",
                               "public class Editor extends java.beans.PropertyEditorSupport {}"));
        if (!isEditorExist(Object.class)) {
            throw new Error("the editor is lost");
        }
        loader = null; 
        if (isEditorExist(Object.class)) {
            throw new Error("unexpected editor is found");
        }
    }
    private static boolean isEditorExist(Class type) {
        for (int i = 0; i < 10; i++) {
            System.gc(); 
            if (null == PropertyEditorManager.findEditor(type)) {
                return false;
            }
        }
        return true;
    }
}
