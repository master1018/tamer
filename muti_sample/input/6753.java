public abstract class LayoutPath {
    public abstract boolean pointToPath(Point2D point, Point2D location);
    public abstract void pathToPoint(Point2D location, boolean preceding,
                                     Point2D point);
}
