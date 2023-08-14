public class SimpleProvider extends AttachProvider {
    public SimpleProvider() {
    }
    public String name() {
        return "simple";
    }
    public String type() {
        return "none";
    }
    public VirtualMachine attachVirtualMachine(String id)
        throws AttachNotSupportedException, IOException
    {
        if (!id.startsWith("simple:")) {
            throw new AttachNotSupportedException("id not recognized");
        }
        return new SimpleVirtualMachine(this, id);
    }
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        return new ArrayList<VirtualMachineDescriptor>();
    }
}
class SimpleVirtualMachine extends VirtualMachine {
    public SimpleVirtualMachine(AttachProvider provider, String id) {
        super(provider, id);
    }
    public void detach() throws IOException {
    }
    public void loadAgentLibrary(String agentLibrary, String options)
        throws IOException, AgentLoadException, AgentInitializationException
    {
    }
    public void loadAgentPath(String agentLibrary, String options)
        throws IOException, AgentLoadException, AgentInitializationException
    {
    }
    public void loadAgent(String agentLibrary, String options)
        throws IOException, AgentLoadException, AgentInitializationException
    {
    }
    public Properties getSystemProperties() throws IOException {
        return new Properties();
    }
    public Properties getAgentProperties() throws IOException {
        return new Properties();
    }
    public void dataDumpRequest() throws IOException {
    }
}
