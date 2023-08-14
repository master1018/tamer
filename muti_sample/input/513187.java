import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import proguard.util.ListUtil;
import java.util.*;
public class MemberSpecificationElement extends DataType
{
    private String access;
    private String annotation;
    private String type;
    private String name;
    private String parameters;
    public void appendTo(List    memberSpecifications,
                         boolean isMethod,
                         boolean isConstructor)
    {
        MemberSpecificationElement memberSpecificationElement = isReference() ?
            (MemberSpecificationElement)getCheckedRef(this.getClass(),
                                                      this.getClass().getName()) :
            this;
        String access     = memberSpecificationElement.access;
        String type       = memberSpecificationElement.type;
        String annotation = memberSpecificationElement.annotation;
        String name       = memberSpecificationElement.name;
        String parameters = memberSpecificationElement.parameters;
        if (annotation != null)
        {
            annotation = ClassUtil.internalType(annotation);
        }
        if (isMethod)
        {
            if (isConstructor)
            {
                if (type != null)
                {
                    throw new BuildException("Type attribute not allowed in constructor specification ["+type+"]");
                }
                if (parameters != null)
                {
                    type = ClassConstants.EXTERNAL_TYPE_VOID;
                }
                name = ClassConstants.INTERNAL_METHOD_NAME_INIT;
            }
            else if ((type != null) ^ (parameters != null))
            {
                throw new BuildException("Type and parameters attributes must always be present in combination in method specification");
            }
        }
        else
        {
            if (parameters != null)
            {
                throw new BuildException("Parameters attribute not allowed in field specification ["+parameters+"]");
            }
        }
        List parameterList = ListUtil.commaSeparatedList(parameters);
        String descriptor =
            parameters != null ? ClassUtil.internalMethodDescriptor(type, parameterList) :
            type       != null ? ClassUtil.internalType(type)                            :
                                 null;
        MemberSpecification memberSpecification =
            new MemberSpecification(requiredAccessFlags(true,  access),
                                    requiredAccessFlags(false, access),
                                    annotation,
                                    name,
                                    descriptor);
        memberSpecifications.add(memberSpecification);
    }
    public void setAccess(String access)
    {
        this.access = access;
    }
    public void setAnnotation(String annotation)
    {
        this.annotation = annotation;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setParameters(String parameters)
    {
        this.parameters = parameters;
    }
    public void setParam(String parameters)
    {
        this.parameters = parameters;
    }
    private int requiredAccessFlags(boolean set,
                                    String  access)
    throws BuildException
    {
        int accessFlags = 0;
        if (access != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(access, " ,");
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();
                if (token.startsWith("!") ^ set)
                {
                    String strippedToken = token.startsWith("!") ?
                        token.substring(1) :
                        token;
                    int accessFlag =
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_PUBLIC)       ? ClassConstants.INTERNAL_ACC_PUBLIC       :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_PRIVATE)      ? ClassConstants.INTERNAL_ACC_PRIVATE      :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_PROTECTED)    ? ClassConstants.INTERNAL_ACC_PROTECTED    :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_STATIC)       ? ClassConstants.INTERNAL_ACC_STATIC       :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_FINAL)        ? ClassConstants.INTERNAL_ACC_FINAL        :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_SYNCHRONIZED) ? ClassConstants.INTERNAL_ACC_SYNCHRONIZED :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_VOLATILE)     ? ClassConstants.INTERNAL_ACC_VOLATILE     :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_TRANSIENT)    ? ClassConstants.INTERNAL_ACC_TRANSIENT    :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_NATIVE)       ? ClassConstants.INTERNAL_ACC_NATIVE       :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_ABSTRACT)     ? ClassConstants.INTERNAL_ACC_ABSTRACT     :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_STRICT)       ? ClassConstants.INTERNAL_ACC_STRICT       :
                        0;
                    if (accessFlag == 0)
                    {
                        throw new BuildException("Incorrect class member access modifier ["+strippedToken+"]");
                    }
                    accessFlags |= accessFlag;
                }
            }
        }
        return accessFlags;
    }
}
