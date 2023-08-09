import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.visitor.*;
class      NameMarker
extends    SimplifiedVisitor
implements ClassVisitor,
           MemberVisitor,
           AttributeVisitor,
           InnerClassesInfoVisitor,
           ConstantVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        keepClassName(programClass);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        keepClassName(libraryClass);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        keepFieldName(programClass, programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        keepMethodName(programClass, programMethod);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        keepFieldName(libraryClass, libraryField);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        keepMethodName(libraryClass, libraryMethod);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        int innerClassIndex = innerClassesInfo.u2innerClassIndex;
        int outerClassIndex = innerClassesInfo.u2outerClassIndex;
        if (innerClassIndex != 0 &&
            outerClassIndex != 0 &&
            clazz.getClassName(innerClassIndex).equals(clazz.getName()))
        {
            clazz.constantPoolEntryAccept(outerClassIndex, this);
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClassAccept(this);
    }
    public void keepClassName(Clazz clazz)
    {
        ClassObfuscator.setNewClassName(clazz,
                                        clazz.getName());
    }
    private void keepFieldName(Clazz clazz, Field field)
    {
        MemberObfuscator.setFixedNewMemberName(field,
                                               field.getName(clazz));
    }
    private void keepMethodName(Clazz clazz, Method method)
    {
        String name = method.getName(clazz);
        if (!name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            MemberObfuscator.setFixedNewMemberName(method,
                                                   method.getName(clazz));
        }
    }
}
