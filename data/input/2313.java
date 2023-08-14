public final class DelegatingShape implements Shape {
    Shape delegate;
    public DelegatingShape(Shape delegate) {
        this.delegate = delegate;
    }
    public Rectangle getBounds() {
        return delegate.getBounds(); 
    }
    public Rectangle2D getBounds2D() {
        return delegate.getBounds2D();  
    }
    public boolean contains(double x, double y) {
        return delegate.contains(x, y);
    }
    public boolean contains(Point2D p) {
        return delegate.contains(p);
    }
    public boolean intersects(double x, double y, double w, double h) {
        return delegate.intersects(x, y, w, h);
    }
    public boolean intersects(Rectangle2D r) {
        return delegate.intersects(r);
    }
    public boolean contains(double x, double y, double w, double h) {
        return delegate.contains(x, y, w, h);
    }
    public boolean contains(Rectangle2D r) {
        return delegate.contains(r);
    }
    public PathIterator getPathIterator(AffineTransform at) {
        return delegate.getPathIterator(at);
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return delegate.getPathIterator(at, flatness);
    }
}
