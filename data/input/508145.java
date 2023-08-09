package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
import java.util.*;
public class AttributeSorter
extends      SimplifiedVisitor
implements   ClassVisitor, MemberVisitor, AttributeVisitor, Comparator
{
    public void visitProgramClass(ProgramClass programClass)
    {
        Arrays.sort(programClass.attributes, 0, programClass.u2attributesCount, this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        Arrays.sort(programMember.attributes, 0, programMember.u2attributesCount, this);
        programMember.attributesAccept(programClass, this);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        Arrays.sort(codeAttribute.attributes, 0, codeAttribute.u2attributesCount, this);
    }
    public int compare(Object object1, Object object2)
    {
        Attribute attribute1 = (Attribute)object1;
        Attribute attribute2 = (Attribute)object2;
        return attribute1.u2attributeNameIndex < attribute2.u2attributeNameIndex ? -1 :
               attribute1.u2attributeNameIndex > attribute2.u2attributeNameIndex ?  1 :
                                                                                    0;
    }
}
