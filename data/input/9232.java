public class Test4968709 {
    public static void main(String[] args) {
        String[] oldPath = PropertyEditorManager.getEditorSearchPath();
        String[] newPath = {"aaa.bbb", "aaa.ccc",};
        PropertyEditorManager.setEditorSearchPath(newPath);
        if (null != PropertyEditorManager.findEditor(Test4968709.class))
            throw new Error("found unexpected editor");
        PropertyEditorManager.setEditorSearchPath(oldPath);
        if (null == PropertyEditorManager.findEditor(Double.TYPE))
            throw new Error("expected editor is not found");
    }
}
