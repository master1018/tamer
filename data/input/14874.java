package build.tools.makeclasslist;
import java.io.*;
import java.util.*;
import java.util.jar.*;
public class MakeClasslist {
  public static void main(String[] args) throws IOException {
    List classes = new ArrayList();
    String origJavaHome = System.getProperty("java.home");
    String javaHome     = origJavaHome.toLowerCase();
    if (javaHome.endsWith("jre")) {
      origJavaHome = origJavaHome.substring(0, origJavaHome.length() - 4);
      javaHome     = javaHome.substring(0, javaHome.length() - 4);
    }
    for (int i = 0; i < args.length; i++) {
      try {
        File file = new File(args[i]);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = reader.readLine()) != null) {
          StringTokenizer tok = new StringTokenizer(line, "[ \t\n\r\f");
          if (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            if (t.equals("Loading")) {
              t = tok.nextToken();
              t = t.replace('.', '/');
              if (tok.hasMoreTokens()) {
                String tmp = tok.nextToken();
                if (tmp.equals("from")) {
                  if (tok.hasMoreTokens()) {
                    tmp = tok.nextToken().toLowerCase();
                    if (tmp.startsWith(javaHome)) {
                      classes.add(t);
                    }
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        System.err.println("Error reading file " + args[i]);
        throw(e);
      }
    }
    Set  seenClasses = new HashSet();
    for (Iterator iter = classes.iterator(); iter.hasNext(); ) {
      String str = (String) iter.next();
      if (seenClasses.add(str)) {
        System.out.println(str);
      }
    }
  }
  private static void completePackage(Set seenClasses,
                                      JarFile jar,
                                      String packageName) {
    int len = packageName.length();
    Enumeration entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = (JarEntry) entries.nextElement();
      String name = entry.getName();
      if (name.startsWith(packageName) &&
          name.endsWith(".class") &&
          name.lastIndexOf('/') == len) {
        name = name.substring(0, name.length() - 6);
        if (seenClasses.add(name)) {
          System.out.println(name);
        }
      }
    }
  }
}
