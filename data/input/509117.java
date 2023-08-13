public class ClassPathElement extends Path
{
    private String filter;
    private String jarFilter;
    private String warFilter;
    private String earFilter;
    private String zipFilter;
    public ClassPathElement(Project project)
    {
        super(project);
    }
    public void appendClassPathEntriesTo(ClassPath classPath, boolean output)
    {
        File     baseDir = getProject().getBaseDir();
        String[] fileNames;
        if (isReference())
        {
            Object referencedObject = getCheckedRef(DataType.class,
                                                    DataType.class.getName());
            if (referencedObject instanceof Path)
            {
                Path path = (Path)referencedObject;
                fileNames = path.list();
            }
            else if (referencedObject instanceof AbstractFileSet)
            {
                AbstractFileSet fileSet = (AbstractFileSet)referencedObject;
                DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
                baseDir   = scanner.getBasedir();
                fileNames = scanner.getIncludedFiles();
            }
            else
            {
                throw new BuildException("The refid attribute doesn't point to a <path> element or a <fileset> element");
            }
        }
        else
        {
            fileNames = list();
        }
        if (output)
        {
            if (fileNames.length != 1)
            {
                throw new BuildException("The <outjar> element must specify exactly one file or directory ["+fileNames.length+"]");
            }
        }
        for (int index = 0; index < fileNames.length; index++)
        {
            String fileName = fileNames[index];
            File   file     = new File(fileName);
            ClassPathEntry entry =
                new ClassPathEntry(file.isAbsolute() ? file : new File(baseDir, fileName),
                                   output);
            entry.setFilter(ListUtil.commaSeparatedList(filter));
            entry.setJarFilter(ListUtil.commaSeparatedList(jarFilter));
            entry.setWarFilter(ListUtil.commaSeparatedList(warFilter));
            entry.setEarFilter(ListUtil.commaSeparatedList(earFilter));
            entry.setZipFilter(ListUtil.commaSeparatedList(zipFilter));
            classPath.add(entry);
        }
    }
    public void setFile(File file)
    {
        setLocation(file);
    }
    public void setDir(File file)
    {
        setLocation(file);
    }
    public void setName(File file)
    {
        setLocation(file);
    }
    public void setFilter(String filter)
    {
        this.filter = filter;
    }
    public void setJarfilter(String jarFilter)
    {
        this.jarFilter = jarFilter;
    }
    public void setWarfilter(String warFilter)
    {
        this.warFilter = warFilter;
    }
    public void setEarfilter(String earFilter)
    {
        this.earFilter = earFilter;
    }
    public void setZipfilter(String zipFilter)
    {
        this.zipFilter = zipFilter;
    }
}
