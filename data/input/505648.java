import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import java.util.*;
public class ClassSpecificationElement extends DataType
{
    private static final String ANY_CLASS_KEYWORD  = "*";
    private String access;
    private String annotation;
    private String type;
    private String name;
    private String extendsAnnotation;
    private String extends_;
    private List   fieldSpecifications  = new ArrayList();
    private List   methodSpecifications = new ArrayList();
    public void appendTo(List classSpecifications)
    {
        ClassSpecificationElement classSpecificationElement = isReference() ?
            (ClassSpecificationElement)getCheckedRef(this.getClass(),
                                                     this.getClass().getName()) :
            this;
        ClassSpecification classSpecification =
            createClassSpecification(classSpecificationElement);
        classSpecifications.add(classSpecification);
    }
    protected ClassSpecification createClassSpecification(ClassSpecificationElement classSpecificationElement)
    {
        String access            = classSpecificationElement.access;
        String annotation        = classSpecificationElement.annotation;
        String type              = classSpecificationElement.type;
        String name              = classSpecificationElement.name;
        String extendsAnnotation = classSpecificationElement.extendsAnnotation;
        String extends_          = classSpecificationElement.extends_;
        if (name != null &&
            name.equals(ANY_CLASS_KEYWORD))
        {
            name = null;
        }
        ClassSpecification classSpecification =
            new ClassSpecification(null,
                                   requiredAccessFlags(true,  access, type),
                                   requiredAccessFlags(false, access, type),
                                   annotation        != null ? ClassUtil.internalType(annotation)        : null,
                                   name              != null ? ClassUtil.internalClassName(name)         : null,
                                   extendsAnnotation != null ? ClassUtil.internalType(extendsAnnotation) : null,
                                   extends_          != null ? ClassUtil.internalClassName(extends_)     : null);
        for (int index = 0; index < fieldSpecifications.size(); index++)
        {
            classSpecification.addField((MemberSpecification)fieldSpecifications.get(index));
        }
        for (int index = 0; index < methodSpecifications.size(); index++)
        {
            classSpecification.addMethod((MemberSpecification)methodSpecifications.get(index));
        }
        return classSpecification;
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
    public void setExtendsannotation(String extendsAnnotation)
    {
        this.extendsAnnotation = extendsAnnotation;
    }
    public void setExtends(String extends_)
    {
        this.extends_ = extends_;
    }
    public void setImplements(String implements_)
    {
        this.extends_ = implements_;
    }
    public void addConfiguredField(MemberSpecificationElement memberSpecificationElement)
    {
        if (fieldSpecifications == null)
        {
            fieldSpecifications = new ArrayList();
        }
        memberSpecificationElement.appendTo(fieldSpecifications,
                                            false,
                                            false);
    }
    public void addConfiguredMethod(MemberSpecificationElement memberSpecificationElement)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }
        memberSpecificationElement.appendTo(methodSpecifications,
                                            true,
                                            false);
    }
    public void addConfiguredConstructor(MemberSpecificationElement memberSpecificationElement)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }
        memberSpecificationElement.appendTo(methodSpecifications,
                                            true,
                                            true);
    }
    private int requiredAccessFlags(boolean set,
                                    String  access,
                                    String  type)
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
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_PUBLIC)     ? ClassConstants.INTERNAL_ACC_PUBLIC      :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_FINAL)      ? ClassConstants.INTERNAL_ACC_FINAL       :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_ABSTRACT)   ? ClassConstants.INTERNAL_ACC_ABSTRACT    :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_ANNOTATION) ? ClassConstants.INTERNAL_ACC_ANNOTATTION :
                        0;
                    if (accessFlag == 0)
                    {
                        throw new BuildException("Incorrect class access modifier ["+strippedToken+"]");
                    }
                    accessFlags |= accessFlag;
                }
            }
        }
        if (type != null && (type.startsWith("!") ^ set))
        {
            int accessFlag =
                type.equals("class")                                     ? 0                                     :
                type.equals(      ClassConstants.EXTERNAL_ACC_INTERFACE) ||
                type.equals("!" + ClassConstants.EXTERNAL_ACC_INTERFACE) ? ClassConstants.INTERNAL_ACC_INTERFACE :
                type.equals(      ClassConstants.EXTERNAL_ACC_ENUM)      ||
                type.equals("!" + ClassConstants.EXTERNAL_ACC_ENUM)      ? ClassConstants.INTERNAL_ACC_ENUM      :
                                                                           -1;
            if (accessFlag == -1)
            {
                throw new BuildException("Incorrect class type ["+type+"]");
            }
            accessFlags |= accessFlag;
        }
        return accessFlags;
    }
}
