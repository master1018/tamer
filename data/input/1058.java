public class test {
    protected MessageQueueManager getArtifactQueueManager() {
        AbstractCamelMediator mediator = (AbstractCamelMediator) getChannel().getIdentityMediator();
        return mediator.getArtifactQueueManager();
    }
}
