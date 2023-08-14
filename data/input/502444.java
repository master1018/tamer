public class FolderDecorator implements ILightweightLabelDecorator {
    private ImageDescriptor mDescriptor;
    public FolderDecorator() {
        mDescriptor = AdtPlugin.getImageDescriptor("/icons/android_project.png"); 
    }
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IFolder) {
            IFolder folder = (IFolder)element;
            IProject project = folder.getProject();
            try {
                if (project.hasNature(AndroidConstants.NATURE)) {
                    if (folder.getParent().getType() == IResource.PROJECT) {
                        String name = folder.getName();
                        if (name.equals(SdkConstants.FD_ASSETS)) {
                            doDecoration(decoration, null);
                        } else if (name.equals(SdkConstants.FD_RESOURCES)) {
                            doDecoration(decoration, null);
                        } else if (name.equals(SdkConstants.FD_GEN_SOURCES)) {
                            doDecoration(decoration, " [Generated Java Files]");
                      } else if (name.equals(SdkConstants.FD_NATIVE_LIBS)) {
                          doDecoration(decoration, null);
                      } else if (folder.isLinked()) {
                          ProjectState state = Sdk.getProjectState(project);
                          LibraryState lib = state.getLibrary(folder.getName());
                          if (lib != null) {
                              doDecoration(decoration, " [Android Library]");
                          }
                        }
                    }
                }
            } catch (CoreException e) {
                AdtPlugin.log(e, "Unable to get nature of project '%s'.", project.getName());
            }
        }
    }
    public void doDecoration(IDecoration decoration, String suffix) {
        decoration.addOverlay(mDescriptor, IDecoration.TOP_LEFT);
        if (suffix != null) {
            decoration.addSuffix(suffix);
        }
    }
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
    public void addListener(ILabelProviderListener listener) {
    }
    public void removeListener(ILabelProviderListener listener) {
    }
    public void dispose() {
    }
}
