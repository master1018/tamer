import proguard.classfile.ClassPool;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.ClassVersionSetter;
import java.io.IOException;
import java.util.*;
public class Targeter
{
    private final Configuration configuration;
    public Targeter(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool) throws IOException
    {
        Set newerClassVersions = configuration.warn != null ? null : new HashSet();
        programClassPool.classesAccept(new ClassVersionSetter(configuration.targetClassVersion,
                                                              newerClassVersions));
        if (newerClassVersions != null &&
            newerClassVersions.size() > 0)
        {
            System.err.print("Warning: some classes have more recent versions (");
            Iterator iterator = newerClassVersions.iterator();
            while (iterator.hasNext())
            {
                Integer classVersion = (Integer)iterator.next();
                System.err.print(ClassUtil.externalClassVersion(classVersion.intValue()));
                if (iterator.hasNext())
                {
                    System.err.print(",");
                }
            }
            System.err.println(")");
            System.err.println("         than the target version ("+ClassUtil.externalClassVersion(configuration.targetClassVersion)+").");
            if (!configuration.ignoreWarnings)
            {
                System.err.println("         If you are sure this is not a problem,");
                System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                throw new IOException("Please correct the above warnings first.");
            }
        }
    }
}
