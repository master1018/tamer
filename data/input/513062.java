package proguard.classfile.util;
import proguard.classfile.ClassConstants;
import java.util.Stack;
public class DescriptorClassEnumeration
{
    private String  descriptor;
    private int     index;
    private int     nestingLevel;
    private boolean isInnerClassName;
    private String  accumulatedClassName;
    private Stack   accumulatedClassNames;
    public DescriptorClassEnumeration(String descriptor)
    {
        this.descriptor = descriptor;
    }
    public int classCount()
    {
        int count = 0;
        nextFluff();
        while (hasMoreClassNames())
        {
            count++;
            nextClassName();
            nextFluff();
        }
        index = 0;
        return count;
    }
    public boolean hasMoreClassNames()
    {
        return index < descriptor.length();
    }
    public String nextFluff()
    {
        int fluffStartIndex = index;
        loop: while (index < descriptor.length())
        {
            switch (descriptor.charAt(index++))
            {
                case ClassConstants.INTERNAL_TYPE_GENERIC_START:
                {
                    nestingLevel++;
                    if (accumulatedClassNames == null)
                    {
                        accumulatedClassNames = new Stack();
                    }
                    accumulatedClassNames.push(accumulatedClassName);
                    break;
                }
                case ClassConstants.INTERNAL_TYPE_GENERIC_END:
                {
                    nestingLevel--;
                    accumulatedClassName = (String)accumulatedClassNames.pop();
                    continue loop;
                }
                case ClassConstants.INTERNAL_TYPE_GENERIC_BOUND:
                {
                    continue loop;
                }
                case ClassConstants.INTERNAL_TYPE_CLASS_START:
                {
                    nestingLevel += 2;
                    isInnerClassName = false;
                    break loop;
                }
                case ClassConstants.INTERNAL_TYPE_CLASS_END:
                {
                    nestingLevel -= 2;
                    break;
                }
                case ClassConstants.EXTERNAL_INNER_CLASS_SEPARATOR:
                {
                    isInnerClassName = true;
                    break loop;
                }
                case ClassConstants.INTERNAL_TYPE_GENERIC_VARIABLE_START:
                {
                    while (descriptor.charAt(index++) != ClassConstants.INTERNAL_TYPE_CLASS_END);
                    break;
                }
            }
            if (nestingLevel == 1 &&
                descriptor.charAt(index) != ClassConstants.INTERNAL_TYPE_GENERIC_END)
            {
                while (descriptor.charAt(index++) != ClassConstants.INTERNAL_TYPE_GENERIC_BOUND);
            }
        }
        return descriptor.substring(fluffStartIndex, index);
    }
    public String nextClassName()
    {
        int classNameStartIndex = index;
        loop: while (true)
        {
            switch (descriptor.charAt(index))
            {
                case ClassConstants.INTERNAL_TYPE_GENERIC_START:
                case ClassConstants.INTERNAL_TYPE_CLASS_END:
                case ClassConstants.EXTERNAL_INNER_CLASS_SEPARATOR:
                {
                    break loop;
                }
            }
            index++;
        }
        String className = descriptor.substring(classNameStartIndex, index);
        accumulatedClassName = isInnerClassName ?
            accumulatedClassName + ClassConstants.INTERNAL_INNER_CLASS_SEPARATOR + className :
            className;
        return accumulatedClassName;
    }
    public boolean isInnerClassName()
    {
        return isInnerClassName;
    }
    public static void main(String[] args)
    {
        try
        {
            for (int index = 0; index < args.length; index++)
            {
                String descriptor = args[index];
                System.out.println("Descriptor ["+descriptor+"]");
                DescriptorClassEnumeration enumeration = new DescriptorClassEnumeration(descriptor);
                System.out.println("  Fluff: ["+enumeration.nextFluff()+"]");
                while (enumeration.hasMoreClassNames())
                {
                    System.out.println("  Name:  ["+enumeration.nextClassName()+"]");
                    System.out.println("  Fluff: ["+enumeration.nextFluff()+"]");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
