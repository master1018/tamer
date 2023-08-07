public class ChannelCreateCommand extends EditElementCommand {
    private final EObject source;
    private final EObject target;
    private final Network container;
    public ChannelCreateCommand(CreateRelationshipRequest request, EObject source, EObject target) {
        super(request.getLabel(), null, request);
        this.source = source;
        this.target = target;
        container = deduceContainer(source, target);
    }
    public boolean canExecute() {
        if (source == null && target == null) {
            return false;
        }
        if (source != null && false == source instanceof Node) {
            return false;
        }
        if (target != null && false == target instanceof Node) {
            return false;
        }
        if (getSource() == null) {
            return true;
        }
        if (getContainer() == null) {
            return false;
        }
        return NetworkBaseItemSemanticEditPolicy.getLinkConstraints().canCreateChannel_4003(getContainer(), getSource(), getTarget());
    }
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        if (!canExecute()) {
            throw new ExecutionException("Invalid arguments in create link command");
        }
        Channel newElement = NetworkFactory.INSTANCE.createChannel();
        getContainer().getChannels().add(newElement);
        newElement.setSource(getSource());
        newElement.setTarget(getTarget());
        doConfigure(newElement, monitor, info);
        ((CreateElementRequest) getRequest()).setNewElement(newElement);
        return CommandResult.newOKCommandResult(newElement);
    }
    protected void doConfigure(Channel newElement, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        IElementType elementType = ((CreateElementRequest) getRequest()).getElementType();
        ConfigureRequest configureRequest = new ConfigureRequest(getEditingDomain(), newElement, elementType);
        configureRequest.setClientContext(((CreateElementRequest) getRequest()).getClientContext());
        configureRequest.addParameters(getRequest().getParameters());
        configureRequest.setParameter(CreateRelationshipRequest.SOURCE, getSource());
        configureRequest.setParameter(CreateRelationshipRequest.TARGET, getTarget());
        ICommand configureCommand = elementType.getEditCommand(configureRequest);
        if (configureCommand != null && configureCommand.canExecute()) {
            configureCommand.execute(monitor, info);
        }
    }
    protected void setElementToEdit(EObject element) {
        throw new UnsupportedOperationException();
    }
    protected Node getSource() {
        return (Node) source;
    }
    protected Node getTarget() {
        return (Node) target;
    }
    public Network getContainer() {
        return container;
    }
    private static Network deduceContainer(EObject source, EObject target) {
        for (EObject element = source; element != null; element = element.eContainer()) {
            if (element instanceof Network) {
                return (Network) element;
            }
        }
        return null;
    }
}
