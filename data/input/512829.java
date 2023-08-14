package proguard.classfile.visitor;
import proguard.classfile.*;
import java.util.Set;
public class ClassVersionSetter implements ClassVisitor
{
    private final int classVersion;
    private final Set newerClassVersions;
    public ClassVersionSetter(int classVersion)
    {
        this(classVersion, null);
    }
    public ClassVersionSetter(int classVersion,
                              Set newerClassVersions)
    {
        this.classVersion       = classVersion;
        this.newerClassVersions = newerClassVersions;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (programClass.u4version > classVersion &&
            newerClassVersions != null)
        {
            newerClassVersions.add(new Integer(programClass.u4version));
        }
        programClass.u4version = classVersion;
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
}
