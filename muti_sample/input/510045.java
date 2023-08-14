public class GPL
{
    public static void check()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Exception().printStackTrace(new PrintStream(out));
        LineNumberReader reader = new LineNumberReader(
                                  new InputStreamReader(
                                  new ByteArrayInputStream(out.toByteArray())));
        Set unknownPackageNames = unknownPackageNames(reader);
        if (unknownPackageNames.size() > 0)
        {
            String uniquePackageNames = uniquePackageNames(unknownPackageNames);
            System.out.println("ProGuard is released under the GNU General Public License. The authors of all");
            System.out.println("programs or plugins that link to it ("+uniquePackageNames+"...) therefore");
            System.out.println("must ensure that these programs carry the GNU General Public License as well.");
        }
    }
    private static Set unknownPackageNames(LineNumberReader reader)
    {
        Set packageNames = new HashSet();
        try
        {
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                line = line.trim();
                if (line.startsWith("at "))
                {
                    line = line.substring(2).trim();
                    line = trimSuffix(line, '(');
                    line = trimSuffix(line, '.');
                    line = trimSuffix(line, '.');
                    if (line.length() > 0 && !isKnown(line))
                    {
                        packageNames.add(line);
                    }
                }
            }
        }
        catch (IOException ex)
        {
        }
        return packageNames;
    }
    private static String uniquePackageNames(Set packageNames)
    {
        StringBuffer buffer = new StringBuffer();
        Iterator iterator = packageNames.iterator();
        while (iterator.hasNext())
        {
            String packageName = (String)iterator.next();
            if (!containsPrefix(packageNames, packageName))
            {
                buffer.append(packageName).append(", ");
            }
        }
        return buffer.toString();
    }
    private static String trimSuffix(String string, char separator)
    {
        int index = string.lastIndexOf(separator);
        return index < 0 ? "" : string.substring(0, index);
    }
    private static boolean containsPrefix(Set set, String name)
    {
        int index = 0;
        while (!set.contains(name.substring(0, index)))
        {
            index = name.indexOf('.', index + 1);
            if (index < 0)
            {
                return false;
            }
        }
        return true;
    }
    private static boolean isKnown(String packageName)
    {
        return packageName.startsWith("java")                   ||
               packageName.startsWith("sun.reflect")            ||
               packageName.startsWith("proguard")               ||
               packageName.startsWith("org.apache.tools.ant")   ||
               packageName.startsWith("org.apache.tools.maven") ||
               packageName.startsWith("org.eclipse")            ||
               packageName.startsWith("org.netbeans")           ||
               packageName.startsWith("com.sun.kvem")           ||
               packageName.startsWith("net.certiv.proguarddt")  ||
               packageName.startsWith("eclipseme")              ||
               packageName.startsWith("jg.j2me")                ||
               packageName.startsWith("jg.common")              ||
               packageName.startsWith("jg.buildengine");
    }
    public static void main(String[] args)
    {
        LineNumberReader reader = new LineNumberReader(
                                  new InputStreamReader(System.in));
        Set unknownPackageNames = unknownPackageNames(reader);
        if (unknownPackageNames.size() > 0)
        {
            String uniquePackageNames = uniquePackageNames(unknownPackageNames);
            System.out.println(uniquePackageNames);
        }
    }
}
