public class PredictionModelModificationImpl implements PredictionModelModification {
    private List<ResourceContainerSpecification> resourceContainerSpecifications = new LinkedList<ResourceContainerSpecification>();
    private List<UsageScenarioSpecification> usageScenarioSpecifications = new LinkedList<UsageScenarioSpecification>();
    private List<ComponentSpecification> componentSpecifications = new LinkedList<ComponentSpecification>();
    public final List<ResourceContainerSpecification> getResourceContainerSpecifications() {
        return resourceContainerSpecifications;
    }
    public final List<UsageScenarioSpecification> getUsageScenarioSpecifications() {
        return usageScenarioSpecifications;
    }
    public final List<ComponentSpecification> getComponentSpecifications() {
        return componentSpecifications;
    }
}
