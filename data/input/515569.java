public class ProGuardObfuscator implements Obfuscator
{
    private static final String DEFAULT_CONFIGURATION = "default.pro";
    public void createScriptFile(File jadFile,
                                 File projectDir)
    {
    }
    public void run(File   obfuscatedJarFile,
                    String wtkBinDir,
                    String wtkLibDir,
                    String jarFileName,
                    String projectDirName,
                    String classPath,
                    String emptyAPI)
    throws IOException
    {
        Configuration configuration = new Configuration();
        ConfigurationParser parser = new ConfigurationParser(this.getClass().getResource(DEFAULT_CONFIGURATION));
        try
        {
            parser.parse(configuration);
            configuration.libraryJars = classPath(classPath);
            configuration.programJars = new ClassPath();
            configuration.programJars.add(new ClassPathEntry(new File(jarFileName), false));
            configuration.programJars.add(new ClassPathEntry(obfuscatedJarFile, true));
            configuration.useMixedCaseClassNames =
                !System.getProperty("os.name").regionMatches(true, 0, "windows", 0, 7);
            ProGuard proGuard = new ProGuard(configuration);
            proGuard.execute();
        }
        catch (ParseException ex)
        {
            throw new IOException(ex.getMessage());
        }
        finally
        {
            parser.close();
        }
    }
    private ClassPath classPath(String classPathString)
    {
        ClassPath classPath = new ClassPath();
        String separator = System.getProperty("path.separator");
        int index = 0;
        while (index < classPathString.length())
        {
            int next_index = classPathString.indexOf(separator, index);
            if (next_index < 0)
            {
                next_index = classPathString.length();
            }
            ClassPathEntry classPathEntry =
                new ClassPathEntry(new File(classPathString.substring(index, next_index)),
                                   false);
            classPath.add(classPathEntry);
            index = next_index + 1;
        }
        return classPath;
    }
}
