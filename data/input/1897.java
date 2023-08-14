import com.sun.tools.classfile.AccessFlags;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
public class FindNativeFiles {
    public static void main(String[] args) throws IOException, ConstantPoolException {
        new FindNativeFiles().run(args);
    }
    public void run(String[] args) throws IOException, ConstantPoolException {
        JarFile jar = new JarFile(args[0]);
        Set<JarEntry> entries = getNativeClasses(jar);
        for (JarEntry e: entries) {
            String name = e.getName();
            String className = name.substring(0, name.length() - 6).replace("/", ".");
            System.out.println(className);
        }
    }
    Set<JarEntry> getNativeClasses(JarFile jar) throws IOException, ConstantPoolException {
        Set<JarEntry> results = new TreeSet<JarEntry>(new Comparator<JarEntry>() {
            public int compare(JarEntry o1, JarEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Enumeration<JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (isNativeClass(jar, je))
                results.add(je);
        }
        return results;
    }
    boolean isNativeClass(JarFile jar, JarEntry entry) throws IOException, ConstantPoolException {
        String name = entry.getName();
        if (name.startsWith("META-INF") || !name.endsWith(".class"))
            return false;
        InputStream in = jar.getInputStream(entry);
        ClassFile cf = ClassFile.read(in);
        in.close();
        for (int i = 0; i < cf.methods.length; i++) {
            Method m = cf.methods[i];
            if (m.access_flags.is(AccessFlags.ACC_NATIVE)) {
                return true;
            }
        }
        return false;
    }
}
