package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class AllAttributeVisitor
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             AttributeVisitor
{
    private final boolean          deep;
    private final AttributeVisitor attributeVisitor;
    public AllAttributeVisitor(AttributeVisitor attributeVisitor)
    {
        this(false, attributeVisitor);
    }
    public AllAttributeVisitor(boolean          deep,
                               AttributeVisitor attributeVisitor)
    {
        this.deep             = deep;
        this.attributeVisitor = attributeVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.attributesAccept(attributeVisitor);
        if (deep)
        {
            programClass.fieldsAccept(this);
            programClass.methodsAccept(this);
            programClass.attributesAccept(this);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass) {}
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.attributesAccept(programClass, attributeVisitor);
        if (deep)
        {
            programMember.attributesAccept(programClass, this);
        }
    }
    public void visitLibraryMember(LibraryClass programClass, LibraryMember programMember) {}
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.attributesAccept(clazz, method, attributeVisitor);
    }
}
