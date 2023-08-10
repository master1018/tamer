public class BatikImageWriter implements ImageWriter {
    protected static class GraphicFormatSVG extends GraphicFormat {
        public String getName() {
            return "Scalable Vector Graphics";
        }
        public String[] getExtensions() {
            String[] retVal = new String[1];
            retVal[0] = "svg";
            return retVal;
        }
        public ImageWriter getWriter() {
            return singleton;
        }
    }
    private static BatikImageWriter singleton;
    private BatikImageWriter() {
    }
    public static void initialize() {
        singleton = new BatikImageWriter();
        GraphicFormatRegistry.registerType(new GraphicFormatSVG());
    }
    public void exportGraphic(DrawingCanvas canvas, DiagramExportSettings settings, File outputFile) throws ImageGenerationException {
        if (settings.usesAutoMode()) {
            settings.setImageSize(canvas.getWidth(), canvas.getHeight());
        }
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.setSVGCanvasSize(new Dimension(settings.getImageWidth(), settings.getImageHeight()));
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, settings.getImageWidth(), settings.getImageHeight());
        svgGenerator.setPaint(canvas.getBackground());
        svgGenerator.fill(bounds);
        AffineTransform transform = canvas.scaleToFit(svgGenerator, bounds);
        svgGenerator.transform(transform);
        canvas.paintCanvas(svgGenerator);
        boolean useCSS = true;
        try {
            FileOutputStream outStream = new FileOutputStream(outputFile);
            Writer out = new OutputStreamWriter(outStream, "UTF-8");
            svgGenerator.stream(out, useCSS);
            outStream.close();
        } catch (Exception e) {
            throw new ImageGenerationException("Error while generating '" + outputFile.getPath() + "' - writing SVG error: " + e.getMessage(), e);
        }
    }
}
