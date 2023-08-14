import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import java.io.PrintStream;
public class MappingPrinter
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor
{
    private final PrintStream ps;
    public MappingPrinter()
    {
        this(System.out);
    }
    public MappingPrinter(PrintStream printStream)
    {
        this.ps = printStream;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        String name    = programClass.getName();
        String newName = ClassObfuscator.newClassName(programClass);
        ps.println(ClassUtil.externalClassName(name) +
                   " -> " +
                   ClassUtil.externalClassName(newName) +
                   ":");
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        String newName = MemberObfuscator.newMemberName(programField);
        if (newName != null)
        {
            ps.println("    " +
                       ClassUtil.externalFullFieldDescription(
                           0,
                           programField.getName(programClass),
                           programField.getDescriptor(programClass)) +
                       " -> " +
                       newName);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String name = programMethod.getName(programClass);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }
        String newName = MemberObfuscator.newMemberName(programMethod);
        if (newName != null)
        {
            ps.println("    " +
                       lineNumberRange(programClass, programMethod) +
                       ClassUtil.externalFullMethodDescription(
                           programClass.getName(),
                           0,
                           programMethod.getName(programClass),
                           programMethod.getDescriptor(programClass)) +
                       " -> " +
                       newName);
        }
    }
    private static String lineNumberRange(ProgramClass programClass, ProgramMember programMember)
    {
        String range = programMember.getLineNumberRange(programClass);
        return range != null ?
            (range + ":") :
            "";
    }
}
