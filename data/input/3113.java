public class DocletInvoker {
    private final Class<?> docletClass;
    private final String docletClassName;
    private final ClassLoader appClassLoader;
    private final Messager messager;
    private static class DocletInvokeException extends Exception {
        private static final long serialVersionUID = 0;
    }
    private String appendPath(String path1, String path2) {
        if (path1 == null || path1.length() == 0) {
            return path2 == null ? "." : path2;
        } else if (path2 == null || path2.length() == 0) {
            return path1;
        } else {
            return path1  + File.pathSeparator + path2;
        }
    }
    public DocletInvoker(Messager messager,
                         String docletClassName, String docletPath,
                         ClassLoader docletParentClassLoader) {
        this.messager = messager;
        this.docletClassName = docletClassName;
        String cpString = null;   
        cpString = appendPath(System.getProperty("env.class.path"), cpString);
        cpString = appendPath(System.getProperty("java.class.path"), cpString);
        cpString = appendPath(docletPath, cpString);
        URL[] urls = com.sun.tools.javac.file.Paths.pathToURLs(cpString);
        if (docletParentClassLoader == null)
            appClassLoader = new URLClassLoader(urls, getDelegationClassLoader(docletClassName));
        else
            appClassLoader = new URLClassLoader(urls, docletParentClassLoader);
        Class<?> dc = null;
        try {
            dc = appClassLoader.loadClass(docletClassName);
        } catch (ClassNotFoundException exc) {
            messager.error(null, "main.doclet_class_not_found", docletClassName);
            messager.exit();
        }
        docletClass = dc;
    }
    private ClassLoader getDelegationClassLoader(String docletClassName) {
        ClassLoader ctxCL = Thread.currentThread().getContextClassLoader();
        ClassLoader sysCL = ClassLoader.getSystemClassLoader();
        if (sysCL == null)
            return ctxCL;
        if (ctxCL == null)
            return sysCL;
        try {
            sysCL.loadClass(docletClassName);
            try {
                ctxCL.loadClass(docletClassName);
            } catch (ClassNotFoundException e) {
                return sysCL;
            }
        } catch (ClassNotFoundException e) {
        }
        try {
            if (getClass() == sysCL.loadClass(getClass().getName())) {
                try {
                    if (getClass() != ctxCL.loadClass(getClass().getName()))
                        return sysCL;
                } catch (ClassNotFoundException e) {
                    return sysCL;
                }
            }
        } catch (ClassNotFoundException e) {
        }
        return ctxCL;
    }
    public boolean start(RootDoc root) {
        Object retVal;
        String methodName = "start";
        Class<?>[] paramTypes = { RootDoc.class };
        Object[] params = { root };
        try {
            retVal = invoke(methodName, null, paramTypes, params);
        } catch (DocletInvokeException exc) {
            return false;
        }
        if (retVal instanceof Boolean) {
            return ((Boolean)retVal).booleanValue();
        } else {
            messager.error(null, "main.must_return_boolean",
                           docletClassName, methodName);
            return false;
        }
    }
    public int optionLength(String option) {
        Object retVal;
        String methodName = "optionLength";
        Class<?>[] paramTypes = { String.class };
        Object[] params = { option };
        try {
            retVal = invoke(methodName, new Integer(0), paramTypes, params);
        } catch (DocletInvokeException exc) {
            return -1;
        }
        if (retVal instanceof Integer) {
            return ((Integer)retVal).intValue();
        } else {
            messager.error(null, "main.must_return_int",
                           docletClassName, methodName);
            return -1;
        }
    }
    public boolean validOptions(List<String[]> optlist) {
        Object retVal;
        String options[][] = optlist.toArray(new String[optlist.length()][]);
        String methodName = "validOptions";
        DocErrorReporter reporter = messager;
        Class<?>[] paramTypes = { String[][].class, DocErrorReporter.class };
        Object[] params = { options, reporter };
        try {
            retVal = invoke(methodName, Boolean.TRUE, paramTypes, params);
        } catch (DocletInvokeException exc) {
            return false;
        }
        if (retVal instanceof Boolean) {
            return ((Boolean)retVal).booleanValue();
        } else {
            messager.error(null, "main.must_return_boolean",
                           docletClassName, methodName);
            return false;
        }
    }
    public LanguageVersion languageVersion() {
        try {
            Object retVal;
            String methodName = "languageVersion";
            Class<?>[] paramTypes = new Class<?>[0];
            Object[] params = new Object[0];
            try {
                retVal = invoke(methodName, JAVA_1_1, paramTypes, params);
            } catch (DocletInvokeException exc) {
                return JAVA_1_1;
            }
            if (retVal instanceof LanguageVersion) {
                return (LanguageVersion)retVal;
            } else {
                messager.error(null, "main.must_return_languageversion",
                               docletClassName, methodName);
                return JAVA_1_1;
            }
        } catch (NoClassDefFoundError ex) { 
            return null;
        }
    }
    private Object invoke(String methodName, Object returnValueIfNonExistent,
                          Class<?>[] paramTypes, Object[] params)
        throws DocletInvokeException {
            Method meth;
            try {
                meth = docletClass.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException exc) {
                if (returnValueIfNonExistent == null) {
                    messager.error(null, "main.doclet_method_not_found",
                                   docletClassName, methodName);
                    throw new DocletInvokeException();
                } else {
                    return returnValueIfNonExistent;
                }
            } catch (SecurityException exc) {
                messager.error(null, "main.doclet_method_not_accessible",
                               docletClassName, methodName);
                throw new DocletInvokeException();
            }
            if (!Modifier.isStatic(meth.getModifiers())) {
                messager.error(null, "main.doclet_method_must_be_static",
                               docletClassName, methodName);
                throw new DocletInvokeException();
            }
            ClassLoader savedCCL =
                Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(appClassLoader);
                return meth.invoke(null , params);
            } catch (IllegalArgumentException exc) {
                messager.error(null, "main.internal_error_exception_thrown",
                               docletClassName, methodName, exc.toString());
                throw new DocletInvokeException();
            } catch (IllegalAccessException exc) {
                messager.error(null, "main.doclet_method_not_accessible",
                               docletClassName, methodName);
                throw new DocletInvokeException();
            } catch (NullPointerException exc) {
                messager.error(null, "main.internal_error_exception_thrown",
                               docletClassName, methodName, exc.toString());
                throw new DocletInvokeException();
            } catch (InvocationTargetException exc) {
                Throwable err = exc.getTargetException();
                if (err instanceof java.lang.OutOfMemoryError) {
                    messager.error(null, "main.out.of.memory");
                } else {
                messager.error(null, "main.exception_thrown",
                               docletClassName, methodName, exc.toString());
                    exc.getTargetException().printStackTrace();
                }
                throw new DocletInvokeException();
            } finally {
                Thread.currentThread().setContextClassLoader(savedCCL);
            }
    }
}
