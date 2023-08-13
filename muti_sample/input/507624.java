package proguard.classfile.util;
import proguard.classfile.ClassConstants;
public class InternalTypeEnumeration
{
    private String descriptor;
    private int    firstIndex;
    private int    lastIndex;
    private int    index;
    public InternalTypeEnumeration(String descriptor)
    {
        this.descriptor = descriptor;
        this.firstIndex = descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);
        this.lastIndex  = descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        this.index      = firstIndex + 1;
        if (lastIndex < 0)
        {
            lastIndex = descriptor.length();
        }
    }
    public String formalTypeParameters()
    {
        return descriptor.substring(0, firstIndex);
    }
    public boolean hasMoreTypes()
    {
        return index < lastIndex;
    }
    public String nextType()
    {
        int startIndex = index;
        skipArray();
        char c = descriptor.charAt(index++);
        switch (c)
        {
            case ClassConstants.INTERNAL_TYPE_CLASS_START:
            case ClassConstants.INTERNAL_TYPE_GENERIC_VARIABLE_START:
            {
                skipClass();
                break;
            }
            case ClassConstants.INTERNAL_TYPE_GENERIC_START:
            {
                skipGeneric();
                break;
            }
        }
        return descriptor.substring(startIndex, index);
    }
    public String returnType()
    {
        return descriptor.substring(lastIndex + 1);
    }
    private void skipArray()
    {
        while (descriptor.charAt(index) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            index++;
        }
    }
    private void skipClass()
    {
        while (true)
        {
            char c = descriptor.charAt(index++);
            switch (c)
            {
                case ClassConstants.INTERNAL_TYPE_GENERIC_START:
                    skipGeneric();
                    break;
                case ClassConstants.INTERNAL_TYPE_CLASS_END:
                    return;
            }
        }
    }
    private void skipGeneric()
    {
        int nestingLevel = 1;
        do
        {
            char c = descriptor.charAt(index++);
            switch (c)
            {
                case ClassConstants.INTERNAL_TYPE_GENERIC_START:
                    nestingLevel++;
                    break;
                case ClassConstants.INTERNAL_TYPE_GENERIC_END:
                    nestingLevel--;
                    break;
            }
        }
        while (nestingLevel > 0);
    }
    public static void main(String[] args)
    {
        try
        {
            for (int index = 0; index < args.length; index++)
            {
                String descriptor = args[index];
                System.out.println("Descriptor ["+descriptor+"]");
                InternalTypeEnumeration enumeration = new InternalTypeEnumeration(descriptor);
                if (enumeration.firstIndex >= 0)
                {
                    System.out.println("  Formal type parameters ["+enumeration.formalTypeParameters()+"]");
                }
                while (enumeration.hasMoreTypes())
                {
                    System.out.println("  Type ["+enumeration.nextType()+"]");
                }
                if (enumeration.lastIndex < descriptor.length())
                {
                    System.out.println("  Return type ["+enumeration.returnType()+"]");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
