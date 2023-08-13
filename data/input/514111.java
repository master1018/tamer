final class ExtensionFileFilter extends FileFilter
{
    private final String   description;
    private final String[] extensions;
    public ExtensionFileFilter(String description, String[] extensions)
    {
        this.description = description;
        this.extensions  = extensions;
    }
    public String getDescription()
    {
        return description;
    }
    public boolean accept(File file)
    {
        if (file.isDirectory())
        {
            return true;
        }
        String fileName = file.getName().toLowerCase();
        for (int index = 0; index < extensions.length; index++)
        {
            if (fileName.endsWith(extensions[index]))
            {
                return true;
            }
        }
        return false;
    }
}
