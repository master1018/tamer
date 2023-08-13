import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class AttributeShrinker
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             AttributeVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.u2attributesCount =
            shrinkArray(programClass.attributes,
                        programClass.u2attributesCount);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.u2attributesCount =
            shrinkArray(programMember.attributes,
                        programMember.u2attributesCount);
        programMember.attributesAccept(programClass, this);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.u2attributesCount =
            shrinkArray(codeAttribute.attributes,
                        codeAttribute.u2attributesCount);
    }
    private static int shrinkArray(VisitorAccepter[] array, int length)
    {
        int counter = 0;
        for (int index = 0; index < length; index++)
        {
            if (AttributeUsageMarker.isUsed(array[index]))
            {
                array[counter++] = array[index];
            }
        }
        for (int index = counter; index < length; index++)
        {
            array[index] = null;
        }
        return counter;
    }
}
