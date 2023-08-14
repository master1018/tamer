import proguard.classfile.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class ClassRenamer
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.thisClassConstantAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.thisClassName = ClassObfuscator.newClassName(libraryClass);
        libraryClass.fieldsAccept(this);
        libraryClass.methodsAccept(this);
    }
    public void visitProgramMember(ProgramClass  programClass,
                                     ProgramMember programMember)
    {
        String name    = programMember.getName(programClass);
        String newName = MemberObfuscator.newMemberName(programMember);
        if (newName != null &&
            !newName.equals(name))
        {
            programMember.u2nameIndex =
                new ConstantPoolEditor(programClass).addUtf8Constant(newName);
        }
    }
    public void visitLibraryMember(LibraryClass  libraryClass,
                                   LibraryMember libraryMember)
    {
        String newName = MemberObfuscator.newMemberName(libraryMember);
        if (newName != null)
        {
            libraryMember.name = newName;
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        String newName = ClassObfuscator.newClassName(clazz);
        if (newName != null)
        {
            classConstant.u2nameIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newName);
        }
    }
}
