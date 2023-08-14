public class ExampleFileView extends FileView {
    private final Map<String, Icon> icons = new HashMap<String, Icon>();
    private final Map<File, String> fileDescriptions =
            new HashMap<File, String>();
    private final Map<String, String> typeDescriptions =
            new HashMap<String, String>();
    @Override
    public String getName(File f) {
        return null;
    }
    public void putDescription(File f, String fileDescription) {
        fileDescriptions.put(f, fileDescription);
    }
    @Override
    public String getDescription(File f) {
        return fileDescriptions.get(f);
    }
    public void putTypeDescription(String extension, String typeDescription) {
        typeDescriptions.put(extension, typeDescription);
    }
    public void putTypeDescription(File f, String typeDescription) {
        putTypeDescription(getExtension(f), typeDescription);
    }
    @Override
    public String getTypeDescription(File f) {
        return typeDescriptions.get(getExtension(f));
    }
    private String getExtension(File f) {
        String name = f.getName();
        if (name != null) {
            int extensionIndex = name.lastIndexOf('.');
            if (extensionIndex < 0) {
                return null;
            }
            return name.substring(extensionIndex + 1).toLowerCase();
        }
        return null;
    }
    public void putIcon(String extension, Icon icon) {
        icons.put(extension, icon);
    }
    @Override
    public Icon getIcon(File f) {
        Icon icon = null;
        String extension = getExtension(f);
        if (extension != null) {
            icon = icons.get(extension);
        }
        return icon;
    }
    @Override
    public Boolean isTraversable(File f) {
        return null;    
    }
}
