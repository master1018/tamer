public class DomainList {
	private ArrayList domains;
	public DomainList(ArrayList domains, boolean dontCopy) {
		if (domains != null)
			this.domains = (dontCopy ? domains : (ArrayList) domains.clone());
		else
			this.domains = new ArrayList(0);
	}
	public int size() {
		return domains.size();
	}
	public String get(int index) {
		if (0 > index || size() <= index)
			throw new IndexOutOfBoundsException();
		return (String) domains.get(index);
	}
	public String toRouteString() {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < domains.size(); i++) {
			out.append("@");
			out.append(get(i));
			if (i + 1 < domains.size())
				out.append(",");
		}
		return out.toString();
	}
}
