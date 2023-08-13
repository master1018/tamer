class Children {
    Child[] children = new Child[16];
    Element getOrCreate(Element parent, String uri, String localName) {
        int hash = uri.hashCode() * 31 + localName.hashCode();
        int index = hash & 15;
        Child current = children[index];
        if (current == null) {
            current = new Child(parent, uri, localName, parent.depth + 1, hash);
            children[index] = current;
            return current;
        } else {
            Child previous;
            do {
                if (current.hash == hash
                        && current.uri.compareTo(uri) == 0
                        && current.localName.compareTo(localName) == 0) {
                    return current;
                }
                previous = current;
                current = current.next;
            } while (current != null);
            current = new Child(parent, uri, localName, parent.depth + 1, hash);
            previous.next = current;
            return current;         
        }
    }
    Element get(String uri, String localName) {
        int hash = uri.hashCode() * 31 + localName.hashCode();
        int index = hash & 15;
        Child current = children[index];
        if (current == null) {
            return null;
        } else {
            do {
                if (current.hash == hash
                        && current.uri.compareTo(uri) == 0
                        && current.localName.compareTo(localName) == 0) {
                    return current;
                }
                current = current.next;
            } while (current != null);
            return null;
        }
    }
    static class Child extends Element {
        final int hash;
        Child next;
        Child(Element parent, String uri, String localName, int depth,
                int hash) {
            super(parent, uri, localName, depth);
            this.hash = hash;
        }
    }
}
