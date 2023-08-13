public final class DexWrapper {
    private final static String DEX_MAIN = "com.android.dx.command.dexer.Main"; 
    private final static String DEX_CONSOLE = "com.android.dx.command.DxConsole"; 
    private final static String DEX_ARGS = "com.android.dx.command.dexer.Main$Arguments"; 
    private final static String MAIN_RUN = "run"; 
    private Method mRunMethod;
    private Constructor<?> mArgConstructor;
    private Field mArgOutName;
    private Field mArgVerbose;
    private Field mArgJarOutput;
    private Field mArgFileNames;
    private Field mConsoleOut;
    private Field mConsoleErr;
    public synchronized IStatus loadDex(String osFilepath) {
        try {
            File f = new File(osFilepath);
            if (f.isFile() == false) {
                return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID, String.format(
                        Messages.DexWrapper_s_does_not_exists, osFilepath));
            }
            URL url = f.toURL();
            URLClassLoader loader = new URLClassLoader(new URL[] { url },
                    DexWrapper.class.getClassLoader());
            Class<?> mainClass = loader.loadClass(DEX_MAIN);
            Class<?> consoleClass = loader.loadClass(DEX_CONSOLE);
            Class<?> argClass = loader.loadClass(DEX_ARGS);
            try {
                mRunMethod = mainClass.getMethod(MAIN_RUN, argClass);
                mArgConstructor = argClass.getConstructor();
                mArgOutName = argClass.getField("outName"); 
                mArgJarOutput = argClass.getField("jarOutput"); 
                mArgFileNames = argClass.getField("fileNames"); 
                mArgVerbose = argClass.getField("verbose"); 
                mConsoleOut = consoleClass.getField("out"); 
                mConsoleErr = consoleClass.getField("err"); 
            } catch (SecurityException e) {
                return createErrorStatus(Messages.DexWrapper_SecuryEx_Unable_To_Find_API, e);
            } catch (NoSuchMethodException e) {
                return createErrorStatus(Messages.DexWrapper_SecuryEx_Unable_To_Find_Method, e);
            } catch (NoSuchFieldException e) {
                return createErrorStatus(Messages.DexWrapper_SecuryEx_Unable_To_Find_Field, e);
            }
            return Status.OK_STATUS;
        } catch (MalformedURLException e) {
            return createErrorStatus(
                    String.format(Messages.DexWrapper_Failed_to_load_s, osFilepath), e);
        } catch (ClassNotFoundException e) {
            return createErrorStatus(
                    String.format(Messages.DexWrapper_Failed_to_load_s, osFilepath), e);
        }
    }
    public synchronized int run(String osOutFilePath, String[] osFilenames,
            boolean verbose, PrintStream outStream, PrintStream errStream) throws CoreException {
        try {
            mConsoleErr.set(null , errStream);
            mConsoleOut.set(null , outStream);
            Object args = mArgConstructor.newInstance();
            mArgOutName.set(args, osOutFilePath);
            mArgFileNames.set(args, osFilenames);
            mArgJarOutput.set(args, false);
            mArgVerbose.set(args, verbose);
            Object res = mRunMethod.invoke(null , args);
            if (res instanceof Integer) {
                return ((Integer)res).intValue();
            }
            return -1;
        } catch (IllegalAccessException e) {
            throw new CoreException(createErrorStatus(
                    String.format(Messages.DexWrapper_Unable_To_Execute_Dex_s, e.getMessage()), e));
        } catch (InstantiationException e) {
            throw new CoreException(createErrorStatus(
                    String.format(Messages.DexWrapper_Unable_To_Execute_Dex_s, e.getMessage()), e));
        } catch (InvocationTargetException e) {
            throw new CoreException(createErrorStatus(
                    String.format(Messages.DexWrapper_Unable_To_Execute_Dex_s, e.getMessage()), e));
        }
    }
    private static IStatus createErrorStatus(String message, Exception e) {
        AdtPlugin.log(e, message);
        AdtPlugin.printErrorToConsole(Messages.DexWrapper_Dex_Loader, message);
        return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID, message, e);
    }
}
