import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.*;
import proguard.optimize.peephole.VariableShrinker;
public class MethodDescriptorShrinker
extends      SimplifiedVisitor
implements   MemberVisitor,
             AttributeVisitor
{
    private static final boolean DEBUG = false;
    private final MemberVisitor extraMemberVisitor;
    public MethodDescriptorShrinker()
    {
        this(null);
    }
    public MethodDescriptorShrinker(MemberVisitor extraMemberVisitor)
    {
        this.extraMemberVisitor = extraMemberVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String descriptor    = programMethod.getDescriptor(programClass);
        String newDescriptor = shrinkDescriptor(programMethod, descriptor);
        if (!descriptor.equals(newDescriptor))
        {
            programMethod.attributesAccept(programClass, this);
            String name    = programMethod.getName(programClass);
            String newName = name;
            if (!name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
            {
                newName += ClassConstants.SPECIAL_MEMBER_SEPARATOR + Long.toHexString(Math.abs((descriptor).hashCode()));
            }
            if (DEBUG)
            {
                System.out.println("MethodDescriptorShrinker:");
                System.out.println("  Class file        = "+programClass.getName());
                System.out.println("  Method name       = "+name);
                System.out.println("                   -> "+newName);
                System.out.println("  Method descriptor = "+descriptor);
                System.out.println("                   -> "+newDescriptor);
            }
            ConstantPoolEditor constantPoolEditor =
                new ConstantPoolEditor(programClass);
            if (!newName.equals(name))
            {
                programMethod.u2nameIndex =
                    constantPoolEditor.addUtf8Constant(newName);
            }
            programMethod.referencedClasses =
                shrinkReferencedClasses(programMethod,
                                        descriptor,
                                        programMethod.referencedClasses);
            programMethod.u2descriptorIndex =
                constantPoolEditor.addUtf8Constant(newDescriptor);
            if (extraMemberVisitor != null)
            {
                extraMemberVisitor.visitProgramMethod(programClass, programMethod);
            }
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitSignatureAttribute(Clazz clazz, Method method, SignatureAttribute signatureAttribute)
    {
        String signature    = clazz.getString(signatureAttribute.u2signatureIndex);
        String newSignature = shrinkDescriptor(method, signature);
        signatureAttribute.u2signatureIndex =
            new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSignature);
        signatureAttribute.referencedClasses =
            shrinkReferencedClasses(method,
                                    signature,
                                    signatureAttribute.referencedClasses);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        int[]          annotationsCounts = parameterAnnotationsAttribute.u2parameterAnnotationsCount;
        Annotation[][] annotations       = parameterAnnotationsAttribute.parameterAnnotations;
        int parameterIndex =
            (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;
        int annotationIndex    = 0;
        int newAnnotationIndex = 0;
        String descriptor = method.getDescriptor(clazz);
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(descriptor);
        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();
            if (ParameterUsageMarker.isParameterUsed(method, parameterIndex))
            {
                annotationsCounts[newAnnotationIndex] = annotationsCounts[annotationIndex];
                annotations[newAnnotationIndex++]     = annotations[annotationIndex];
            }
            annotationIndex++;
            parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
        }
        parameterAnnotationsAttribute.u2parametersCount = newAnnotationIndex;
        while (newAnnotationIndex < annotationIndex)
        {
            annotationsCounts[newAnnotationIndex] = 0;
            annotations[newAnnotationIndex++]     = null;
        }
    }
    private String shrinkDescriptor(Method method, String descriptor)
    {
        int parameterIndex =
            (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(descriptor);
        StringBuffer newDescriptorBuffer = new StringBuffer();
        newDescriptorBuffer.append(internalTypeEnumeration.formalTypeParameters());
        newDescriptorBuffer.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);
        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();
            if (ParameterUsageMarker.isParameterUsed(method, parameterIndex))
            {
                newDescriptorBuffer.append(type);
            }
            else if (DEBUG)
            {
                System.out.println("  Deleting parameter #"+parameterIndex+" ["+type+"]");
            }
            parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
        }
        newDescriptorBuffer.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        newDescriptorBuffer.append(internalTypeEnumeration.returnType());
        return newDescriptorBuffer.toString();
    }
    private Clazz[] shrinkReferencedClasses(Method  method,
                                            String  descriptor,
                                            Clazz[] referencedClasses)
    {
        if (referencedClasses != null)
        {
            int parameterIndex =
                (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                    0 : 1;
            int referencedClassIndex    = 0;
            int newReferencedClassIndex = 0;
            InternalTypeEnumeration internalTypeEnumeration =
                new InternalTypeEnumeration(descriptor);
            String type  = internalTypeEnumeration.formalTypeParameters();
            int    count = new DescriptorClassEnumeration(type).classCount();
            for (int counter = 0; counter < count; counter++)
            {
                referencedClasses[newReferencedClassIndex++] =
                    referencedClasses[referencedClassIndex++];
            }
            while (internalTypeEnumeration.hasMoreTypes())
            {
                type  = internalTypeEnumeration.nextType();
                count = new DescriptorClassEnumeration(type).classCount();
                if (ParameterUsageMarker.isParameterUsed(method, parameterIndex))
                {
                    for (int counter = 0; counter < count; counter++)
                    {
                        referencedClasses[newReferencedClassIndex++] =
                            referencedClasses[referencedClassIndex++];
                    }
                }
                else
                {
                    referencedClassIndex += count;
                }
                parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
            }
            type  = internalTypeEnumeration.returnType();
            count = new DescriptorClassEnumeration(type).classCount();
            for (int counter = 0; counter < count; counter++)
            {
                referencedClasses[newReferencedClassIndex++] =
                    referencedClasses[referencedClassIndex++];
            }
            while (newReferencedClassIndex < referencedClassIndex)
            {
                referencedClasses[newReferencedClassIndex++] = null;
            }
        }
        return referencedClasses;
    }
}
