public class ClassPathEntry
{
    private File    file;
    private boolean output;
    private List    filter;
    private List    jarFilter;
    private List    warFilter;
    private List    earFilter;
    private List    zipFilter;
    public ClassPathEntry(File file, boolean isOutput)
    {
        this.file   = file;
        this.output = isOutput;
    }
    public String getName()
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (IOException ex)
        {
            return file.getPath();
        }
    }
    public File getFile()
    {
        return file;
    }
    public void setFile(File file)
    {
        this.file = file;
    }
    public boolean isOutput()
    {
        return output;
    }
    public void setOutput(boolean output)
    {
        this.output = output;
    }
    public List getFilter()
    {
        return filter;
    }
    public void setFilter(List filter)
    {
        this.filter = filter == null || filter.size() == 0 ? null : filter;
    }
    public List getJarFilter()
    {
        return jarFilter;
    }
    public void setJarFilter(List filter)
    {
        this.jarFilter = filter == null || filter.size() == 0 ? null : filter;
    }
    public List getWarFilter()
    {
        return warFilter;
    }
    public void setWarFilter(List filter)
    {
        this.warFilter = filter == null || filter.size() == 0 ? null : filter;
    }
    public List getEarFilter()
    {
        return earFilter;
    }
    public void setEarFilter(List filter)
    {
        this.earFilter = filter == null || filter.size() == 0 ? null : filter;
    }
    public List getZipFilter()
    {
        return zipFilter;
    }
    public void setZipFilter(List filter)
    {
        this.zipFilter = filter == null || filter.size() == 0 ? null : filter;
    }
}
