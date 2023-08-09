class Builder {
	private static Builder singleton = new Builder();
	public static Builder getInstance() {
		return singleton;
	}
	public AddressList buildAddressList(ASTaddress_list node) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ASTaddress childNode = (ASTaddress) node.jjtGetChild(i);
			Address address = buildAddress(childNode);
			list.add(address);
		}
		return new AddressList(list, true);
	}
	private Address buildAddress(ASTaddress node) {
		ChildNodeIterator it = new ChildNodeIterator(node);
		Node n = it.nextNode();
		if (n instanceof ASTaddr_spec) {
			return buildAddrSpec((ASTaddr_spec)n);
		}
		else if (n instanceof ASTangle_addr) {
			return buildAngleAddr((ASTangle_addr)n);
		}
		else if (n instanceof ASTphrase) {
			String name = buildString((ASTphrase)n, false);
			Node n2 = it.nextNode();
			if (n2 instanceof ASTgroup_body) {
				return new Group(name, buildGroupBody((ASTgroup_body)n2));
			}
			else if (n2 instanceof ASTangle_addr) {
                name = DecoderUtil.decodeEncodedWords(name);
				return new NamedMailbox(name, buildAngleAddr((ASTangle_addr)n2));
			}
			else {
				throw new IllegalStateException();
			}
		}
		else {
			throw new IllegalStateException();
		}
	}
	private MailboxList buildGroupBody(ASTgroup_body node) {
		ArrayList results = new ArrayList();
		ChildNodeIterator it = new ChildNodeIterator(node);
		while (it.hasNext()) {
			Node n = it.nextNode();
			if (n instanceof ASTmailbox)
				results.add(buildMailbox((ASTmailbox)n));
			else
				throw new IllegalStateException();
		}
		return new MailboxList(results, true);
	}
	private Mailbox buildMailbox(ASTmailbox node) {
		ChildNodeIterator it = new ChildNodeIterator(node);
		Node n = it.nextNode();
		if (n instanceof ASTaddr_spec) {
			return buildAddrSpec((ASTaddr_spec)n);
		}
		else if (n instanceof ASTangle_addr) {
			return buildAngleAddr((ASTangle_addr)n);
		}
		else if (n instanceof ASTname_addr) {
			return buildNameAddr((ASTname_addr)n);
		}
		else {
			throw new IllegalStateException();
		}
	}
	private NamedMailbox buildNameAddr(ASTname_addr node) {
		ChildNodeIterator it = new ChildNodeIterator(node);
		Node n = it.nextNode();
		String name;
		if (n instanceof ASTphrase) {
			name = buildString((ASTphrase)n, false);
		}
		else {
			throw new IllegalStateException();
		}
		n = it.nextNode();
		if (n instanceof ASTangle_addr) {
            name = DecoderUtil.decodeEncodedWords(name);
			return new NamedMailbox(name, buildAngleAddr((ASTangle_addr) n));
		}
		else {
			throw new IllegalStateException();
		}
	}
	private Mailbox buildAngleAddr(ASTangle_addr node) {
		ChildNodeIterator it = new ChildNodeIterator(node);
		DomainList route = null;
		Node n = it.nextNode();
		if (n instanceof ASTroute) {
			route = buildRoute((ASTroute)n);
			n = it.nextNode();
		}
		else if (n instanceof ASTaddr_spec)
			; 
		else
			throw new IllegalStateException();
		if (n instanceof ASTaddr_spec)
			return buildAddrSpec(route, (ASTaddr_spec)n);
		else
			throw new IllegalStateException();
	}
	private DomainList buildRoute(ASTroute node) {
		ArrayList results = new ArrayList(node.jjtGetNumChildren());
		ChildNodeIterator it = new ChildNodeIterator(node);
		while (it.hasNext()) {
			Node n = it.nextNode();
			if (n instanceof ASTdomain)
				results.add(buildString((ASTdomain)n, true));
			else
				throw new IllegalStateException();
		}
		return new DomainList(results, true);
	}
	private Mailbox buildAddrSpec(ASTaddr_spec node) {
		return buildAddrSpec(null, node);
	}
	private Mailbox buildAddrSpec(DomainList route, ASTaddr_spec node) {
		ChildNodeIterator it = new ChildNodeIterator(node);
		String localPart = buildString((ASTlocal_part)it.nextNode(), true);
		String domain = buildString((ASTdomain)it.nextNode(), true);
		return new Mailbox(route, localPart, domain);		
	}
	private String buildString(SimpleNode node, boolean stripSpaces) {
		Token head = node.firstToken;
		Token tail = node.lastToken;
		StringBuffer out = new StringBuffer();
		while (head != tail) {
			out.append(head.image);
			head = head.next;
			if (!stripSpaces)
				addSpecials(out, head.specialToken);
		}
		out.append(tail.image);			
		return out.toString();
	}
	private void addSpecials(StringBuffer out, Token specialToken) {
		if (specialToken != null) {
			addSpecials(out, specialToken.specialToken);
			out.append(specialToken.image);
		}
	}
	private static class ChildNodeIterator implements Iterator {
		private SimpleNode simpleNode;
		private int index;
		private int len;
		public ChildNodeIterator(SimpleNode simpleNode) {
			this.simpleNode = simpleNode;
			this.len = simpleNode.jjtGetNumChildren();
			this.index = 0;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public boolean hasNext() {
			return index < len;
		}
		public Object next() {
			return nextNode();
		}
		public Node nextNode() {
			return simpleNode.jjtGetChild(index++);
		}
	}
}
