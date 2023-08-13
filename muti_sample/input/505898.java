public class Mailbox extends Address {
	private DomainList route;
	private String localPart;
	private String domain;
	public Mailbox(String localPart, String domain) {
		this(null, localPart, domain);
	}
	public Mailbox(DomainList route, String localPart, String domain) {
		this.route = route;
		this.localPart = localPart;
		this.domain = domain;
	}
	public DomainList getRoute() {
		return route;
	}
	public String getLocalPart() {
		return localPart;
	}
	public String getDomain() {
		return domain;
	}
	public String getAddressString() {
		return getAddressString(false);
	}
	public String getAddressString(boolean includeRoute) {
		return "<" + (!includeRoute || route == null ? "" : route.toRouteString() + ":") 
			+ localPart
			+ (domain == null ? "" : "@") 
			+ domain + ">";  
	}
	protected final void doAddMailboxesTo(ArrayList results) {
		results.add(this);
	}
	public String toString() {
		return getAddressString();
	}
}
