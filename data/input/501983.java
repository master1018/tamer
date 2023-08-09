package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.visitor.*;
import proguard.util.StringMatcher;
public class NamedAttributeDeleter implements ClassVisitor
{
    private final String attributeName;
    public NamedAttributeDeleter(String attributeName)
    {
        this.attributeName = attributeName;
    }
    public void visitLibraryClass(LibraryClass libraryClass) {}
    public void visitProgramClass(ProgramClass programClass)
    {
        new AttributesEditor(programClass, false).deleteAttribute(attributeName);
    }
}