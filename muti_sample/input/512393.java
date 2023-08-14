import proguard.classfile.*;
import proguard.classfile.editor.ClassReferenceFixer;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.evaluation.value.Value;
import proguard.optimize.evaluation.StoringInvocationUnit;
public class MemberDescriptorSpecializer
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private static final boolean DEBUG = true;
    private final MemberVisitor extraParameterMemberVisitor;
    public MemberDescriptorSpecializer()
    {
        this(null);
    }
    public MemberDescriptorSpecializer(MemberVisitor extraParameterMemberVisitor)
    {
        this.extraParameterMemberVisitor = extraParameterMemberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        Value parameterValue = StoringInvocationUnit.getFieldValue(programField);
        if (parameterValue.computationalType() == Value.TYPE_REFERENCE)
        {
            Clazz referencedClass = parameterValue.referenceValue().getReferencedClass();
            if (programField.referencedClass != referencedClass)
            {
                if (DEBUG)
                {
                    System.out.println("MemberDescriptorSpecializer: "+programClass.getName()+"."+programField.getName(programClass)+" "+programField.getDescriptor(programClass));
                    System.out.println("  "+programField.referencedClass.getName()+" -> "+referencedClass.getName());
                }
                programField.referencedClass = referencedClass;
                if (extraParameterMemberVisitor != null)
                {
                    extraParameterMemberVisitor.visitProgramField(programClass, programField);
                }
            }
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        int firstParameterIndex =
            (programMethod.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;
        int parameterCount =
            ClassUtil.internalMethodParameterCount(programMethod.getDescriptor(programClass));
        int classIndex = 0;
        for (int parameterIndex = firstParameterIndex; parameterIndex < parameterCount; parameterIndex++)
        {
            Value parameterValue = StoringInvocationUnit.getMethodParameterValue(programMethod, parameterIndex);
             if (parameterValue.computationalType() == Value.TYPE_REFERENCE)
             {
                 Clazz referencedClass = parameterValue.referenceValue().getReferencedClass();
                 if (programMethod.referencedClasses[classIndex] != referencedClass)
                 {
                     if (DEBUG)
                     {
                         System.out.println("MemberDescriptorSpecializer: "+programClass.getName()+"."+programMethod.getName(programClass)+programMethod.getDescriptor(programClass));
                         System.out.println("  "+programMethod.referencedClasses[classIndex].getName()+" -> "+referencedClass.getName());
                     }
                     programMethod.referencedClasses[classIndex] = referencedClass;
                     if (extraParameterMemberVisitor != null)
                     {
                         extraParameterMemberVisitor.visitProgramMethod(programClass, programMethod);
                     }
                 }
                 classIndex++;
             }
        }
    }
}
