    public boolean processClassMapping(String className,
                                       String newClassName);
    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName);
    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodName,
                                     String methodArguments,
                                     String newMethodName);
}
