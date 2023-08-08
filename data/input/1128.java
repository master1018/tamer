public class PlotPanel extends GraphicsComponent {
    private boolean axesVisible = true;
    private GraphicsList graphicsList = new GraphicsList();
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintGraphics(g);
    }
    protected void paintGraphics(Graphics g) {
        for (int i = 0; i < graphicsList.size(); i++) if (graphicsList.getGraphicsObject(i) != null) graphicsList.getGraphicsObject(i).paintComponent(g);
    }
    public void setAxesVisible(boolean vis) {
        axesVisible = vis;
    }
    protected boolean getAxesVisible() {
        return axesVisible;
    }
    public void addGraphicsObject(GraphicsComponent obj) {
        graphicsList.addGraphicsObject(obj);
    }
    public void clearGraphicsList() {
        graphicsList = new GraphicsList();
    }
    public int getNumGraphicsObjects() {
        return graphicsList.size();
    }
    public GraphicsComponent getGraphicsObject(int i) {
        return graphicsList.getGraphicsObject(i);
    }
}
