class TableScrollPaneCorner extends JComponent implements UIResource{
    @Override protected void paintComponent(Graphics g) {
        Painter painter = (Painter) UIManager.get(
            "TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter");
        if (painter != null){
            if (g instanceof Graphics2D){
                painter.paint((Graphics2D)g,this,getWidth()+1,getHeight());
            } else {
                BufferedImage img =  new BufferedImage(getWidth(),getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = (Graphics2D)img.getGraphics();
                painter.paint(g2,this,getWidth()+1,getHeight());
                g2.dispose();
                g.drawImage(img,0,0,null);
                img = null;
            }
        }
    }
}
