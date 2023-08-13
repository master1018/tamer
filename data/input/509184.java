abstract class Descriptors
{
    public static final char JAVA_NAME_SEPARATOR = '.';
    public static final char VM_NAME_SEPARATOR = '/';
    public static String combine (final String packageName, final String name, final char separator)
    {
        if ((name == null) || (name.length () == 0))
            throw new IllegalArgumentException ("null or empty input: name");
        if ((packageName == null) || (packageName.length () == 0))
            return name;
        else
            return new StringBuffer (packageName).append (separator).append (name).toString ();
    }
    public static String combineJavaName (final String packageName, final String name)
    {
        return combine (packageName, name, JAVA_NAME_SEPARATOR);
    }
    public static String combineVMName (final String packageName, final String name)
    {
        return combine (packageName, name, VM_NAME_SEPARATOR);
    }
    public static String javaNameToVMName (final String javaName)
    {
        if (javaName == null) return null;
        return javaName.replace ('.', '/');
    }
    public static String vmNameToJavaName (final String vmName)
    {
        if (vmName == null) return null;
        return vmName.replace ('/', '.');
    }
    public static String methodVMNameToJavaName (final String className,
                                                 final String methodVMName,
                                                 final String descriptor,
                                                 final boolean renameInits,
                                                 final boolean shortTypeNames,
                                                 final boolean appendReturnType)
    {
        final StringBuffer out = new StringBuffer ();
        if (renameInits)
        {
            if (IClassDefConstants.CLINIT_NAME.equals (methodVMName))
                return "<static initializer>";
            else if (IClassDefConstants.INIT_NAME.equals (methodVMName))
                out.append (className);
            else
                out.append (methodVMName);
        }
        else
        {
            if (IClassDefConstants.CLINIT_NAME.equals (methodVMName))
                return IClassDefConstants.CLINIT_NAME;
            else
                out.append (methodVMName);
        }
        final char [] chars = descriptor.toCharArray ();
        int end;
        out.append (" (");
        {
            for (end = chars.length; chars [-- end] != ')'; );
            for (int start = 1; start < end; )
            {
                if (start > 1) out.append (", ");
                start = typeDescriptorToJavaName (chars, start, shortTypeNames, out);
            }
        }        
        if (appendReturnType)
        {
            out.append ("): ");
            typeDescriptorToJavaName (chars, end + 1, shortTypeNames, out);
        }
        else
        {
            out.append (')');
        }
        return out.toString ();
    }
    private static int typeDescriptorToJavaName (final char [] descriptor, int start,
                                                 final boolean shortTypeNames,
                                                 final StringBuffer out)
    {
        int dims;
        for (dims = 0; descriptor [start] == '['; ++ dims, ++ start);
        char c = descriptor [start ++]; 
        switch (c)
        {
            case 'L':
            {
                if (shortTypeNames)
                {
                    int lastSlash = -1;
                    for (int s = start; descriptor [s] != ';'; ++ s)
                    {
                        if (descriptor [s] == '/') lastSlash = s;
                    }
                    for (start = lastSlash > 0 ? lastSlash + 1 : start; descriptor [start] != ';'; ++ start)
                    {
                        c = descriptor [start];
                        if (RENAME_INNER_CLASSES)
                            out.append (c != '$' ? c : '.');
                        else
                            out.append (c);
                    }                        
                }
                else
                {
                    for (; descriptor [start] != ';'; ++ start)
                    {
                        c = descriptor [start];
                        out.append (c != '/' ? c : '.');
                    }
                }
                ++ start;
            }
            break;
            case 'B': out.append ("byte"); break;
            case 'C': out.append ("char"); break;
            case 'D': out.append ("double"); break;
            case 'F': out.append ("float"); break;
            case 'I': out.append ("int"); break;
            case 'J': out.append ("long"); break;
            case 'S': out.append ("short"); break;
            case 'Z': out.append ("boolean"); break;
            case 'V': out.append ("void"); break;
            default:          
                throw new IllegalStateException ("unknown type descriptor element: " + c);
        } 
        if (dims > 0)
        {
            out.append (' ');
            for (int d = 0; d < dims; ++ d) out.append ("[]");
        }
        return start;
    }
    private Descriptors () {} 
    private static final boolean RENAME_INNER_CLASSES = false;
} 
