final class XFileSet extends FileSet
{
    public XFileSet ()
    {
        super ();
    }
    public XFileSet (final FileSet fileset)
    {
        super (fileset);
    }
    public void setFile (final File file)
    {
        if (IANTVersion.ANT_1_5_PLUS)
        {
            super.setFile (file);
        }
        else
        {
            if (isReference ()) throw tooManyAttributes ();
            final File parent = file.getParentFile ();
            if (parent != null) setDir (parent);
            final PatternSet.NameEntry include = createInclude ();
            include.setName (file.getName ());
        }
    }
} 
