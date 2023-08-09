public class DiagramConnectionWidget extends ConnectionWidget {
    private static Stroke DASHED_STROKE = new BasicStroke(
            1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            0,
            new float[]{2},
            0);
    private static Stroke NORMAL_STROKE = new BasicStroke(1);
    private static Stroke BOLD_STROKE = new BasicStroke(3);
    public static int WHITE_FACTOR = 5;
    private Connection connection;
    private Color color;
    private Point lastSourceAnchor;
    private Point lastTargetAnchor;
    private List<Point> controlPoints;
    private Rectangle clientArea;
    private boolean split;
    private int[] xPoints;
    private int[] yPoints;
    private int pointCount;
    public DiagramConnectionWidget(Connection connection, Scene scene) {
        super(scene);
        this.connection = connection;
        color = connection.getColor();
        if (connection.getStyle() == Connection.ConnectionStyle.DASHED) {
            this.setStroke(DASHED_STROKE);
        } else if (connection.getStyle() == Connection.ConnectionStyle.BOLD) {
            this.setStroke(BOLD_STROKE);
        } else {
            this.setStroke(NORMAL_STROKE);
        }
        this.setCheckClipping(true);
        clientArea = new Rectangle();
        updateControlPoints();
    }
    public Connection getConnection() {
        return connection;
    }
    public void updateControlPoints() {
        List<Point> newControlPoints = connection.getControlPoints();
        Connection c = connection;
        Figure f = c.getInputSlot().getFigure();
        Point p = new Point(f.getPosition());
        p.translate(c.getInputSlot().getRelativePosition().x, f.getSize().height / 2);
        Point p4 = new Point(f.getPosition());
        p4.translate(c.getInputSlot().getRelativePosition().x, c.getInputSlot().getRelativePosition().y);
        Figure f2 = c.getOutputSlot().getFigure();
        Point p2 = new Point(f2.getPosition());
        p2.translate(c.getOutputSlot().getRelativePosition().x, f2.getSize().height / 2);
        Point p3 = new Point(f2.getPosition());
        p3.translate(c.getOutputSlot().getRelativePosition().x, c.getOutputSlot().getRelativePosition().y);
        this.controlPoints = newControlPoints;
        pointCount = newControlPoints.size();
        xPoints = new int[pointCount];
        yPoints = new int[pointCount];
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        split = false;
        for (int i = 0; i < pointCount; i++) {
            if (newControlPoints.get(i) == null) {
                split = true;
            } else {
                int curX = newControlPoints.get(i).x;
                int curY = newControlPoints.get(i).y;
                this.xPoints[i] = curX;
                this.yPoints[i] = curY;
                minX = Math.min(minX, curX);
                maxX = Math.max(maxX, curX);
                minY = Math.min(minY, curY);
                maxY = Math.max(maxY, curY);
            }
        }
        this.clientArea = new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
    @Override
    protected void paintWidget() {
        Graphics2D g = this.getGraphics();
        if (xPoints.length == 0 || Math.abs(xPoints[0] - xPoints[xPoints.length - 1]) > 2000) {
            return;
        }
        DiagramScene ds = (DiagramScene) this.getScene();
        boolean shouldHide = false;
        Composite oldComposite = null;
        if (shouldHide) {
            Color c = new Color(255 - (255 - color.getRed()) / WHITE_FACTOR, 255 - (255 - color.getGreen()) / WHITE_FACTOR, 255 - (255 - color.getBlue()) / WHITE_FACTOR);
            g.setPaint(c);
        } else {
            g.setPaint(color);
        }
        if (split) {
            for (int i = 1; i < controlPoints.size(); i++) {
                Point prev = controlPoints.get(i - 1);
                Point cur = controlPoints.get(i);
                if (cur == null || prev == null) {
                    continue;
                }
                g.drawLine(prev.x, prev.y, cur.x, cur.y);
            }
        } else {
            g.drawPolyline(xPoints, yPoints, pointCount);
        }
        if (xPoints.length >= 2) {
            Graphics2D g2 = (Graphics2D) g.create();
            int xOff = xPoints[xPoints.length - 2] - xPoints[xPoints.length - 1];
            int yOff = yPoints[yPoints.length - 2] - yPoints[yPoints.length - 1];
            if (xOff == 0 && yOff == 0 && yPoints.length >= 3) {
                xOff = xPoints[xPoints.length - 3] - xPoints[xPoints.length - 1];
                yOff = yPoints[yPoints.length - 3] - yPoints[yPoints.length - 1];
            }
            g2.translate(xPoints[xPoints.length - 1], yPoints[yPoints.length - 1]);
            g2.rotate(Math.atan2(yOff, xOff));
            g2.scale(0.55, 0.80);
            AnchorShape.TRIANGLE_FILLED.paint(g2, false);
        }
    }
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isHovered() != state.isHovered()) {
            color = connection.getColor();
            if (state.isHovered()) {
                this.setStroke(BOLD_STROKE);
            } else {
                this.setStroke(NORMAL_STROKE);
            }
            if (state.isHovered()) {
                this.setStroke(BOLD_STROKE);
            } else {
                this.setStroke(NORMAL_STROKE);
            }
            repaint();
        }
        super.notifyStateChanged(previousState, state);
    }
    @Override
    public List<Point> getControlPoints() {
        if (split) {
            ArrayList<Point> result = new ArrayList<Point>();
            for (Point p : controlPoints) {
                if (p != null) {
                    result.add(p);
                }
            }
            return result;
        } else {
            return controlPoints;
        }
    }
    @Override
    public String toString() {
        return "ConnectionWidget[" + connection + "]";
    }
    @Override
    protected Rectangle calculateClientArea() {
        Rectangle result = new Rectangle(clientArea);
        result.grow(10, 10);
        return result;
    }
}
