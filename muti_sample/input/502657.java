import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
public class DuplicateInitializerFixer
extends      SimplifiedVisitor
implements   MemberVisitor,
             AttributeVisitor
{
    private static final boolean DEBUG = false;
    private static final char[] TYPES = new char[]
    {
        ClassConstants.INTERNAL_TYPE_BYTE,
        ClassConstants.INTERNAL_TYPE_CHAR,
        ClassConstants.INTERNAL_TYPE_SHORT,
        ClassConstants.INTERNAL_TYPE_INT,
        ClassConstants.INTERNAL_TYPE_BOOLEAN
    };
    private final MemberVisitor extraFixedInitializerVisitor;
    public DuplicateInitializerFixer()
    {
        this(null);
    }
    public DuplicateInitializerFixer(MemberVisitor extraFixedInitializerVisitor)
    {
        this.extraFixedInitializerVisitor = extraFixedInitializerVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String name = programMethod.getName(programClass);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            String descriptor    = programMethod.getDescriptor(programClass);
            Method similarMethod = programClass.findMethod(name, descriptor);
            if (!programMethod.equals(similarMethod))
            {
                if (!KeepMarker.isKept(programMethod))
                {
                    programMethod = (ProgramMethod)similarMethod;
                }
                int index = descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
                for (int typeIndex = 0; typeIndex < TYPES.length; typeIndex++)
                {
                    String newDescriptor =
                        descriptor.substring(0, index) +
                        TYPES[typeIndex] +
                        descriptor.substring(index);
                    if (programClass.findMethod(name, newDescriptor) == null)
                    {
                        if (DEBUG)
                        {
                            System.out.println("DuplicateInitializerFixer:");
                            System.out.println("  ["+programClass.getName()+"]: "+name+descriptor+" -> "+newDescriptor);
                        }
                        programMethod.u2descriptorIndex =
                            new ConstantPoolEditor(programClass).addUtf8Constant(newDescriptor);
                        programMethod.attributesAccept(programClass,
                                                       this);
                        if (extraFixedInitializerVisitor != null)
                        {
                            extraFixedInitializerVisitor.visitProgramMethod(programClass, programMethod);
                        }
                        return;
                    }
                }
                throw new IllegalStateException("Can't find unique constructor descriptor for ["+
                                                programClass.getName()+"."+
                                                programMethod.getName(programClass)+
                                                programMethod.getDescriptor(programClass)+"]");
            }
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        int maxLocals =
            ClassUtil.internalMethodParameterSize(method.getDescriptor(clazz),
                                                  method.getAccessFlags());
        if (codeAttribute.u2maxLocals < maxLocals)
        {
            codeAttribute.u2maxLocals = maxLocals;
        }
    }
    public void visitSignatureAttribute(Clazz clazz, Method method, SignatureAttribute signatureAttribute)
    {
        String descriptor      = method.getDescriptor(clazz);
        int    descriptorIndex = descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        String signature       = clazz.getString(signatureAttribute.u2signatureIndex);
        int    signatureIndex  = signature.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        String newSignature = signature.substring(0, signatureIndex) +
                              descriptor.charAt(descriptorIndex - 1) +
                              signature.substring(signatureIndex);
        signatureAttribute.u2signatureIndex =
            new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSignature);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        int oldParametersCount = parameterAnnotationsAttribute.u2parametersCount++;
        if (parameterAnnotationsAttribute.u2parameterAnnotationsCount == null ||
            parameterAnnotationsAttribute.u2parameterAnnotationsCount.length < parameterAnnotationsAttribute.u2parametersCount)
        {
            int[]          annotationsCounts = new int[parameterAnnotationsAttribute.u2parametersCount];
            Annotation[][] annotations       = new Annotation[parameterAnnotationsAttribute.u2parametersCount][];
            System.arraycopy(parameterAnnotationsAttribute.u2parameterAnnotationsCount,
                             0,
                             annotationsCounts,
                             0,
                             oldParametersCount);
            System.arraycopy(parameterAnnotationsAttribute.parameterAnnotations,
                             0,
                             annotations,
                             0,
                             oldParametersCount);
            parameterAnnotationsAttribute.u2parameterAnnotationsCount = annotationsCounts;
            parameterAnnotationsAttribute.parameterAnnotations        = annotations;
        }
    }
}