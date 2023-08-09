public class ConfigurationElement extends DataType
{
    public void appendTo(Configuration configuration)
    {
        if (!isReference())
        {
            throw new BuildException("Nested element <configuration> must have a refid attribute");
        }
        ConfigurationTask configurationTask =
            (ConfigurationTask)getCheckedRef(ConfigurationTask.class,
                                             ConfigurationTask.class.getName());
        configurationTask.appendTo(configuration);
    }
}
