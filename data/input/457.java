public class CARS_DefaultNodeIterator extends CARS_DefaultRangeIterator implements NodeIterator {
    public Node nextNode() {
        try {
            return (Node) next();
        } catch (ArrayIndexOutOfBoundsException ae) {
            throw new NoSuchElementException();
        }
    }
}
