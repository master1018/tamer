public class MatchingStrategy implements IEditorMatchingStrategy {
    public boolean matches(IEditorReference editorRef, IEditorInput input) {
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput)input;
            IFile iFile = fileInput.getFile();
            ResourceFolder resFolder = ResourceManager.getInstance().getResourceFolder(iFile);
            if (resFolder != null && resFolder.getType() == ResourceFolderType.LAYOUT) {
                try {
                    IEditorInput editorInput = editorRef.getEditorInput();
                    if (editorInput instanceof FileEditorInput) {
                        FileEditorInput editorFileInput = (FileEditorInput)editorInput;
                        IFile editorIFile = editorFileInput.getFile();
                        return editorIFile.getProject().equals(iFile.getProject())
                            && editorIFile.getName().equals(iFile.getName());
                    }
                } catch (PartInitException e) {
                }
            }
        }
        return false;
    }
}
