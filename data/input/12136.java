    public Class<?> loadClass(String className)
            throws ClassNotFoundException;
    public Class<?> loadClassWithout(ClassLoader exclude,
                                     String className)
            throws ClassNotFoundException;
    public Class<?> loadClassBefore(ClassLoader stop,
                                    String className)
            throws ClassNotFoundException;
}
