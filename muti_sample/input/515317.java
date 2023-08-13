public class MultiMappingProcessor implements MappingProcessor
{
    private final MappingProcessor[] mappingProcessors;
    public MultiMappingProcessor(MappingProcessor[] mappingProcessors)
    {
        this.mappingProcessors = mappingProcessors;
    }
    public boolean processClassMapping(String className,
                                       String newClassName)
    {
        boolean result = false;
        for (int index = 0; index < mappingProcessors.length; index++)
        {
            result |= mappingProcessors[index].processClassMapping(className,
                                                                   newClassName);
        }
        return result;
    }
    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName)
    {
        for (int index = 0; index < mappingProcessors.length; index++)
        {
            mappingProcessors[index].processFieldMapping(className,
                                                         fieldType,
                                                         fieldName,
                                                         newFieldName);
        }
    }
    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodName,
                                     String methodArguments,
                                     String newMethodName)
    {
        for (int index = 0; index < mappingProcessors.length; index++)
        {
            mappingProcessors[index].processMethodMapping(className,
                                                          firstLineNumber,
                                                          lastLineNumber,
                                                          methodReturnType,
                                                          methodName,
                                                          methodArguments,
                                                          newMethodName);
        }
    }
}
