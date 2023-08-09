public class ClosureFinder {
    private Collection roots;            
    private Map        visitedClasses;   
    private String     classPath;        
    private String[]   pathComponents;   
    private static final boolean isWindows = File.separatorChar != '/';
    public ClosureFinder(Collection roots, String classPath) {
        this.roots = roots;
        this.classPath = classPath;
        parseClassPath();
    }
    private void parseClassPath() {
        List paths = new ArrayList();
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens())
            paths.add(st.nextToken());
        Object[] arr = paths.toArray();
        pathComponents = new String[arr.length];
        System.arraycopy(arr, 0, pathComponents, 0, arr.length);
    }
    public Map find() {
        if (visitedClasses == null) {
            visitedClasses = new HashMap();
            computeClosure();
        }
        return visitedClasses;
    }
    private void computeClosure() {
        for (Iterator rootsItr = roots.iterator(); rootsItr.hasNext();) {
            String name = (String) rootsItr.next();
            name = name.substring(0, name.indexOf(".class"));
            computeClosure(name);
        }
    }
    private String lookupClassFile(String classNameAsPath) {
        for (int i = 0; i < pathComponents.length; i++) {
            File f =  new File(pathComponents[i] + File.separator +
                               classNameAsPath + ".class");
            if (f.exists()) {
                if (isWindows) {
                    String name = f.getName();
                    if (name.compareToIgnoreCase("AUX.class") == 0 ||
                        name.compareToIgnoreCase("NUL.class") == 0 ||
                        name.compareToIgnoreCase("CON.class") == 0) {
                        return null;
                    }
                }
                return pathComponents[i];
            }
        }
        return null;
    }
    private static final int CONSTANT_Class = 7;
    private static final int CONSTANT_FieldRef = 9;
    private static final int CONSTANT_MethodRef = 10;
    private static final int CONSTANT_InterfaceMethodRef = 11;
    private static final int CONSTANT_String = 8;
    private static final int CONSTANT_Integer = 3;
    private static final int CONSTANT_Float = 4;
    private static final int CONSTANT_Long = 5;
    private static final int CONSTANT_Double = 6;
    private static final int CONSTANT_NameAndType = 12;
    private static final int CONSTANT_Utf8 = 1;
    private boolean mayBeClassName(String internalClassName) {
        int len = internalClassName.length();
        for (int s = 0; s < len; s++) {
            char c = internalClassName.charAt(s);
            if (!Character.isJavaIdentifierPart(c) && c != '/')
                return false;
        }
        return true;
    }
    private void computeClosure(String className) {
        if (visitedClasses.get(className) != null) return;
        String basePath = lookupClassFile(className);
        if (basePath != null) {
            visitedClasses.put(className, basePath);
            try {
                File classFile = new File(basePath + File.separator + className + ".class");
                FileInputStream fis = new FileInputStream(classFile);
                DataInputStream dis = new DataInputStream(fis);
                if (dis.readInt() != 0xcafebabe) {
                    System.err.println(classFile.getAbsolutePath() + " is not a valid .class file");
                    return;
                }
                dis.readShort();
                dis.readShort();
                int numConsts = (int) dis.readShort();
                String[] strings = new String[numConsts];
                for (int cpIndex = 1; cpIndex < numConsts; cpIndex++) {
                    int constType = (int) dis.readByte();
                    switch (constType) {
                    case CONSTANT_Class:
                    case CONSTANT_String:
                        dis.readShort(); 
                        break;
                    case CONSTANT_FieldRef:
                    case CONSTANT_MethodRef:
                    case CONSTANT_InterfaceMethodRef:
                    case CONSTANT_NameAndType:
                    case CONSTANT_Integer:
                    case CONSTANT_Float:
                        dis.readInt();
                        break;
                    case CONSTANT_Long:
                    case CONSTANT_Double:
                        dis.readLong();
                        cpIndex++;
                        break;
                    case CONSTANT_Utf8: {
                        strings[cpIndex] = dis.readUTF();
                        break;
                    }
                    default:
                        System.err.println("invalid constant pool entry");
                        return;
                    }
                }
            for (int s = 0; s < numConsts; s++) {
                if (strings[s] != null && mayBeClassName(strings[s]))
                    computeClosure(strings[s].replace('/', File.separatorChar));
            }
            } catch (IOException exp) {
            }
        }
    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: ClosureFinder <root class file> <class path>");
            System.exit(1);
        }
        List roots = new ArrayList();
        try {
            FileInputStream fis = new FileInputStream(args[0]);
            DataInputStream dis = new DataInputStream(fis);
            String line = null;
            while ((line = dis.readLine()) != null) {
                if (isWindows) {
                    line = line.replace('/', File.separatorChar);
                }
                roots.add(line);
            }
        } catch (IOException exp) {
            System.err.println(exp.getMessage());
            System.exit(2);
        }
        ClosureFinder cf = new ClosureFinder(roots, args[1]);
        Map out = cf.find();
        Iterator res = out.keySet().iterator();
        for(; res.hasNext(); ) {
            String className = (String) res.next();
            System.out.println(className + ".class");
        }
    }
}
