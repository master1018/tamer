public abstract class SchemaFactoryLoader {
    protected SchemaFactoryLoader() {}
    public abstract SchemaFactory newFactory(String schemaLanguage);
} 
