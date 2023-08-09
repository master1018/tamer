public class AddressList {
	private ArrayList addresses;
	public AddressList(ArrayList addresses, boolean dontCopy) {
		if (addresses != null)
			this.addresses = (dontCopy ? addresses : (ArrayList) addresses.clone());
		else
			this.addresses = new ArrayList(0);
	}
	public int size() {
		return addresses.size();
	}
	public Address get(int index) {
		if (0 > index || size() <= index)
			throw new IndexOutOfBoundsException();
		return (Address) addresses.get(index);
	}
	public MailboxList flatten() {
		boolean groupDetected = false;
		for (int i = 0; i < size(); i++) {
			if (!(get(i) instanceof Mailbox)) {
				groupDetected = true;
				break;
			}
		}
		if (!groupDetected)
			return new MailboxList(addresses, true);
		ArrayList results = new ArrayList();
		for (int i = 0; i < size(); i++) {
			Address addr = get(i);
			addr.addMailboxesTo(results);
		}
		return new MailboxList(results, false);
	}
	public void print() {
		for (int i = 0; i < size(); i++) {
			Address addr = get(i);
			System.out.println(addr.toString());
		}
	}
	public static AddressList parse(String rawAddressList) throws ParseException {
		AddressListParser parser = new AddressListParser(new StringReader(rawAddressList));
		return Builder.getInstance().buildAddressList(parser.parse());
	}
	public static void main(String[] args) throws Exception {
		java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print("> ");
				String line = reader.readLine();
				if (line.length() == 0 || line.toLowerCase().equals("exit") || line.toLowerCase().equals("quit")) {
					System.out.println("Goodbye.");
					return;
				}
				AddressList list = parse(line);
				list.print();
			}
			catch(Exception e) {
				e.printStackTrace();
				Thread.sleep(300);
			}
		}
	}
}
