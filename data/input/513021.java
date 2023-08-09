import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.ClassCollector;
import java.util.*;
public class ReferenceValue extends Category1Value
{
    private static final boolean DEBUG = false;
    protected final String  type;
    protected final Clazz   referencedClass;
    protected final boolean mayBeNull;
    public ReferenceValue(String  type,
                          Clazz   referencedClass,
                          boolean mayBeNull)
    {
        this.type            = type;
        this.referencedClass = referencedClass;
        this.mayBeNull       = mayBeNull;
    }
    public String getType()
    {
        return type;
    }
    public Clazz getReferencedClass()
    {
        return referencedClass;
    }
    public int isNull()
    {
        return type == null ? ALWAYS :
               mayBeNull    ? MAYBE  :
                              NEVER;
    }
    public int instanceOf(String otherType, Clazz otherReferencedClass)
    {
        String thisType = this.type;
        if (thisType == null)
        {
            return NEVER;
        }
        int thisDimensionCount   = ClassUtil.internalArrayTypeDimensionCount(thisType);
        int otherDimensionCount  = ClassUtil.internalArrayTypeDimensionCount(otherType);
        int commonDimensionCount = Math.min(thisDimensionCount, otherDimensionCount);
        thisType  = thisType.substring(commonDimensionCount);
        otherType = otherType.substring(commonDimensionCount);
        if (commonDimensionCount > 0 &&
            (ClassUtil.isInternalPrimitiveType(thisType.charAt(0)) ||
             ClassUtil.isInternalPrimitiveType(otherType.charAt(0))))
        {
            return !thisType.equals(otherType) ? NEVER :
                   mayBeNull                   ? MAYBE :
                                                 ALWAYS;
        }
        if (thisDimensionCount == commonDimensionCount)
        {
            thisType = ClassUtil.internalClassNameFromClassType(thisType);
        }
        if (otherDimensionCount == commonDimensionCount)
        {
            otherType = ClassUtil.internalClassNameFromClassType(otherType);
        }
        if (thisDimensionCount > otherDimensionCount &&
            !ClassUtil.isInternalArrayInterfaceName(otherType))
        {
            return NEVER;
        }
        if (thisDimensionCount < otherDimensionCount &&
            !ClassUtil.isInternalArrayInterfaceName(thisType))
        {
            return NEVER;
        }
        if (mayBeNull)
        {
            return MAYBE;
        }
        if (thisType.equals(otherType) ||
            ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT.equals(otherType))
        {
            return ALWAYS;
        }
        if (thisDimensionCount > otherDimensionCount)
        {
            return ALWAYS;
        }
        if (thisDimensionCount < otherDimensionCount)
        {
            return MAYBE;
        }
        return referencedClass      != null &&
               otherReferencedClass != null &&
               referencedClass.extendsOrImplements(otherReferencedClass) ?
                   ALWAYS :
                   MAYBE;
    }
    public IntegerValue arrayLength(ValueFactory valueFactory)
    {
        return valueFactory.createIntegerValue();
    }
    public Value arrayLoad(IntegerValue integerValue, ValueFactory valueFactory)
    {
        return
            type == null                         ? ValueFactory.REFERENCE_VALUE_NULL                        :
            !ClassUtil.isInternalArrayType(type) ? ValueFactory.REFERENCE_VALUE_JAVA_LANG_OBJECT_MAYBE_NULL :
                                                   valueFactory.createValue(type.substring(1),
                                                                            referencedClass,
                                                                            true);
    }
    public ReferenceValue generalize(ReferenceValue other)
    {
        if (this.equals(other))
        {
            return this;
        }
        String thisType  = this.type;
        String otherType = other.type;
        if (thisType == null && otherType == null)
        {
            return ValueFactory.REFERENCE_VALUE_NULL;
        }
        if (thisType == null)
        {
            return other.generalizeMayBeNull(true);
        }
        if (otherType == null)
        {
            return this.generalizeMayBeNull(true);
        }
        boolean mayBeNull = this.mayBeNull || other.mayBeNull;
        if (thisType.equals(otherType))
        {
            return this.generalizeMayBeNull(mayBeNull);
        }
        int thisDimensionCount   = ClassUtil.internalArrayTypeDimensionCount(thisType);
        int otherDimensionCount  = ClassUtil.internalArrayTypeDimensionCount(otherType);
        int commonDimensionCount = Math.min(thisDimensionCount, otherDimensionCount);
        if (thisDimensionCount == otherDimensionCount)
        {
            Clazz thisReferencedClass  = this.referencedClass;
            Clazz otherReferencedClass = other.referencedClass;
            if (thisReferencedClass  != null &&
                otherReferencedClass != null)
            {
                if (thisReferencedClass.extendsOrImplements(otherReferencedClass))
                {
                    return other.generalizeMayBeNull(mayBeNull);
                }
                if (otherReferencedClass.extendsOrImplements(thisReferencedClass))
                {
                    return this.generalizeMayBeNull(mayBeNull);
                }
                Set thisSuperClasses = new HashSet();
                thisReferencedClass.hierarchyAccept(false, true, true, false,
                                                    new ClassCollector(thisSuperClasses));
                Set otherSuperClasses = new HashSet();
                otherReferencedClass.hierarchyAccept(false, true, true, false,
                                                     new ClassCollector(otherSuperClasses));
                if (DEBUG)
                {
                    System.out.println("ReferenceValue.generalize this ["+thisReferencedClass.getName()+"] with other ["+otherReferencedClass.getName()+"]");
                    System.out.println("  This super classes:  "+thisSuperClasses);
                    System.out.println("  Other super classes: "+otherSuperClasses);
                }
                thisSuperClasses.retainAll(otherSuperClasses);
                if (DEBUG)
                {
                    System.out.println("  Common super classes: "+thisSuperClasses);
                }
                Clazz commonClazz = null;
                int maximumSuperClassCount = -1;
                Iterator commonSuperClasses = thisSuperClasses.iterator();
                while (commonSuperClasses.hasNext())
                {
                    Clazz commonSuperClass = (Clazz)commonSuperClasses.next();
                    int superClassCount = superClassCount(commonSuperClass, thisSuperClasses);
                    if (maximumSuperClassCount < superClassCount ||
                        (maximumSuperClassCount == superClassCount &&
                         commonClazz != null                       &&
                         commonClazz.getName().compareTo(commonSuperClass.getName()) > 0))
                    {
                        commonClazz            = commonSuperClass;
                        maximumSuperClassCount = superClassCount;
                    }
                }
                if (commonClazz == null)
                {
                    throw new IllegalArgumentException("Can't find common super class of ["+thisType+"] and ["+otherType+"]");
                }
                if (DEBUG)
                {
                    System.out.println("  Best common class: ["+commonClazz.getName()+"]");
                }
                return new ReferenceValue(commonDimensionCount == 0 ?
                                              commonClazz.getName() :
                                              ClassUtil.internalArrayTypeFromClassName(commonClazz.getName(),
                                                                                       commonDimensionCount),
                                          commonClazz,
                                          mayBeNull);
            }
        }
        else if (thisDimensionCount > otherDimensionCount)
        {
            if (ClassUtil.isInternalArrayInterfaceName(ClassUtil.internalClassNameFromClassType(otherType)))
            {
                return other.generalizeMayBeNull(mayBeNull);
            }
        }
        else if (thisDimensionCount < otherDimensionCount)
        {
            if (ClassUtil.isInternalArrayInterfaceName(ClassUtil.internalClassNameFromClassType(thisType)))
            {
                return this.generalizeMayBeNull(mayBeNull);
            }
        }
        if (commonDimensionCount > 0 &&
            (ClassUtil.isInternalPrimitiveType(otherType.charAt(commonDimensionCount))) ||
             ClassUtil.isInternalPrimitiveType(thisType.charAt(commonDimensionCount)))
        {
            commonDimensionCount--;
        }
        return commonDimensionCount == 0 ?
            mayBeNull ?
                ValueFactory.REFERENCE_VALUE_JAVA_LANG_OBJECT_MAYBE_NULL :
                ValueFactory.REFERENCE_VALUE_JAVA_LANG_OBJECT_NOT_NULL   :
            new ReferenceValue(ClassUtil.internalArrayTypeFromClassName(ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT,
                                                                        commonDimensionCount),
                               null,
                               mayBeNull);
    }
    private int superClassCount(Clazz subClass, Set classes)
    {
        int count = 0;
        Iterator iterator = classes.iterator();
        while (iterator.hasNext())
        {
            Clazz clazz = (Clazz)iterator.next();
            if (subClass.extendsOrImplements(clazz))
            {
                count++;
            }
        }
        return count;
    }
    public int equal(ReferenceValue other)
    {
        return this.type  == null && other.type == null ? ALWAYS : MAYBE;
    }
    public final int isNotNull()
    {
        return -isNull();
    }
    private ReferenceValue generalizeMayBeNull(boolean mayBeNull)
    {
        return this.mayBeNull || !mayBeNull ?
            this :
            new ReferenceValue(this.type, this.referencedClass, true);
    }
    public final int notEqual(ReferenceValue other)
    {
        return -equal(other);
    }
    public final ReferenceValue referenceValue()
    {
        return this;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.referenceValue());
    }
    public boolean isParticular()
    {
        return type == null;
    }
    public final int computationalType()
    {
        return TYPE_REFERENCE;
    }
    public final String internalType()
    {
        return
            type == null                        ? ClassConstants.INTERNAL_TYPE_JAVA_LANG_OBJECT :
            ClassUtil.isInternalArrayType(type) ? type                                          :
                                                  ClassConstants.INTERNAL_TYPE_CLASS_START +
                                                  type +
                                                  ClassConstants.INTERNAL_TYPE_CLASS_END;
    }
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        ReferenceValue other = (ReferenceValue)object;
        return this.type == null ? other.type == null :
                                   (this.mayBeNull == other.mayBeNull &&
                                    this.type.equals(other.type));
    }
    public int hashCode()
    {
        return this.getClass().hashCode() ^
               (type == null ? 0 : type.hashCode() ^ (mayBeNull ? 0 : 1));
    }
    public String toString()
    {
        return type == null ?
            "null" :
            type + (referencedClass == null ? "?" : "") + (mayBeNull ? "" : "!");
    }
}
