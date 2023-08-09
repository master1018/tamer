package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class StringSharer
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             AttributeVisitor
{
    private String name;
    private String type;
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        Clazz superClass = libraryClass.superClass;
        if (superClass != null)
        {
            libraryClass.superClassName = superClass.getName();
        }
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitAnyStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        Member referencedMember = stringConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = stringConstant.referencedClass;
            name = referencedMember.getName(referencedClass);
            clazz.constantPoolEntryAccept(stringConstant.u2stringIndex, this);
        }
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        Member referencedMember = refConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = refConstant.referencedClass;
            name = referencedMember.getName(referencedClass);
            type = referencedMember.getDescriptor(referencedClass);
            clazz.constantPoolEntryAccept(refConstant.u2nameAndTypeIndex, this);
        }
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        if (name != null)
        {
            clazz.constantPoolEntryAccept(nameAndTypeConstant.u2nameIndex, this);
            name = type;
            clazz.constantPoolEntryAccept(nameAndTypeConstant.u2descriptorIndex, this);
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        Clazz referencedClass = classConstant.referencedClass;
        if (referencedClass != null)
        {
            name = referencedClass.getName();
            clazz.constantPoolEntryAccept(classConstant.u2nameIndex, this);
        }
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        if (name != null)
        {
            if (name.equals(utf8Constant.getString()))
            {
                utf8Constant.setString(name);
            }
            name = null;
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute)
    {
        name = attribute.getAttributeName(clazz).intern();
        clazz.constantPoolEntryAccept(attribute.u2attributeNameIndex, this);
    }
}
