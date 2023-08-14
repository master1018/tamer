public class UpToDateChecker
{
    private final Configuration configuration;
    public UpToDateChecker(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public boolean check()
    {
        long inputLastModified  = configuration.lastModified;
        long outputLastModified = Long.MAX_VALUE;
        ClassPath programJars = configuration.programJars;
        ClassPath libraryJars = configuration.libraryJars;
        if (programJars != null)
        {
            for (int index = 0; index < programJars.size(); index++)
            {
                if (inputLastModified >= outputLastModified)
                {
                    break;
                }
                ClassPathEntry classPathEntry = programJars.get(index);
                if (classPathEntry.isOutput())
                {
                    long lastModified = lastModified(classPathEntry.getFile(), true);
                    if (outputLastModified > lastModified)
                    {
                        outputLastModified = lastModified;
                    }
                }
                else
                {
                    long lastModified = lastModified(classPathEntry.getFile(), false);
                    if (inputLastModified < lastModified)
                    {
                        inputLastModified = lastModified;
                    }
                }
            }
        }
        if (libraryJars != null)
        {
            for (int index = 0; index < libraryJars.size(); index++)
            {
                if (inputLastModified >= outputLastModified)
                {
                    break;
                }
                ClassPathEntry classPathEntry = libraryJars.get(index);
                long lastModified = lastModified(classPathEntry.getFile(), false);
                if (inputLastModified < lastModified)
                {
                    inputLastModified = lastModified;
                }
            }
        }
        boolean outputUpToDate = inputLastModified < outputLastModified;
        if (outputUpToDate)
        {
            System.out.println("The output is up to date");
        }
        return outputUpToDate;
    }
    private long lastModified(File file, boolean minimum)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            long lastModified = files.length != 0 && minimum ?
                Long.MAX_VALUE : 0L;
            for (int index = 0; index < files.length; index++)
            {
                long fileLastModified = lastModified(files[index], minimum);
                if ((lastModified < fileLastModified) ^ minimum)
                {
                    lastModified = fileLastModified;
                }
            }
            return lastModified;
        }
        else
        {
            return file.lastModified();
        }
    }
}
