    public void setClassName(String className);
    public void setClassNames(String[] classNames);
    public void setMethodName(String className, String testName);
    public void setTestPackageName(String packageName);
    public void addInstrumentationArg(String name, String value);
    public void addBooleanArg(String name, boolean value);
    public void setLogOnly(boolean logOnly);
    public void setDebug(boolean debug);
    public void setCoverage(boolean coverage);
    public void run(ITestRunListener... listeners);
    public void run(Collection<ITestRunListener> listeners);
    public void cancel();
}
