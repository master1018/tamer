        IClassDescriptor getSuperclass();
        String getSimpleName();
        IClassDescriptor getEnclosingClass();
        IClassDescriptor[] getDeclaredClasses();
        boolean isInstantiable();
    }
    public HashMap<String, ArrayList<IClassDescriptor>> findClassesDerivingFrom(
            String rootPackage, String[] superClasses)
        throws IOException, InvalidAttributeValueException, ClassFormatError;
    public IClassDescriptor getClass(String className) throws ClassNotFoundException;
    public String getSource();
}
