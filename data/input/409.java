public class SQLRouterNotification extends ClusterResourceNotification {
    private static final long serialVersionUID = -7101382528522639737L;
    public SQLRouterNotification(String clusterName, String memberName, String notificationSource, String resourceName, ResourceState resourceState, TungstenProperties resourceProps) {
        super(NotificationStreamID.MONITORING, clusterName, memberName, notificationSource, ResourceType.SQLROUTER, resourceName, resourceState, resourceProps);
        setResource(new SQLRouter(resourceProps));
    }
    public SQLRouter getReplicator() {
        return (SQLRouter) getResource();
    }
}
