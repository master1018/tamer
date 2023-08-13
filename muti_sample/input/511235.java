class InterfaceAddress {
    final InetAddress address;
    final int index;
    final String name;
    InterfaceAddress(int index, String name, InetAddress address) {
        this.index = index;
        this.name = name;
        this.address = address;
    }
}
