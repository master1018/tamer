public class PainterGenerator {
    private int colorCounter = 1;
    private int gradientCounter = 1;
    private int radialCounter = 1;
    private int pathCounter = 1;
    private int rectCounter = 1;
    private int roundRectCounter = 1;
    private int ellipseCounter = 1;
    private int stateTypeCounter = 1;
    private Map<String, String> colors = new HashMap<String, String>();
    private Map<String, String> methods = new HashMap<String, String>();
    private StringBuilder stateTypeCode = new StringBuilder();
    private StringBuilder switchCode = new StringBuilder();
    private StringBuilder paintingCode = new StringBuilder();
    private StringBuilder getExtendedCacheKeysCode = new StringBuilder();
    private StringBuilder gradientsCode = new StringBuilder();
    private StringBuilder colorCode = new StringBuilder();
    private StringBuilder shapesCode = new StringBuilder();
    private Map<String, List<ComponentColor>> componentColorsMap =
            new LinkedHashMap<String, List<ComponentColor>>();
    private List<ComponentColor> componentColors = null;
    PainterGenerator(UIRegion r) {
        generate(r);
    }
    private void generate(UIRegion r) {
        for (UIState state : r.getBackgroundStates()) {
            Canvas canvas = state.getCanvas();
            String type = (r instanceof UIIconRegion ? r.getKey() : "Background");
            generate(state, canvas, type);
        }
        for (UIState state : r.getForegroundStates()) {
            Canvas canvas = state.getCanvas();
            generate(state, canvas, "Foreground");
        }
        for (UIState state : r.getBorderStates()) {
            Canvas canvas = state.getCanvas();
            generate(state, canvas, "Border");
        }
        for (UIRegion sub : r.getSubRegions()) {
            if (sub instanceof UIIconRegion) {
                generate(sub);
            }
        }
        if (!componentColorsMap.isEmpty()) {
            getExtendedCacheKeysCode
                    .append("    protected Object[] getExtendedCacheKeys(JComponent c) {\n")
                    .append("        Object[] extendedCacheKeys = null;\n")
                    .append("        switch(state) {\n");
            for (Map.Entry<String, List<ComponentColor>> entry : componentColorsMap.entrySet()) {
                getExtendedCacheKeysCode
                    .append("            case ")
                    .append(entry.getKey()).append(":\n")
                    .append("                extendedCacheKeys = new Object[] {\n");
                for (int i=0; i<entry.getValue().size(); i++) {
                    ComponentColor cc = entry.getValue().get(i);
                    cc.write(getExtendedCacheKeysCode);
                    if (i + 1 < entry.getValue().size()) {
                        getExtendedCacheKeysCode.append("),\n");
                    } else {
                        getExtendedCacheKeysCode.append(")");
                    }
                }
                getExtendedCacheKeysCode.append("};\n")
                    .append("                break;\n");
            }
            getExtendedCacheKeysCode
                    .append("        }\n")
                    .append("        return extendedCacheKeys;\n")
                    .append("    }");
        }
    }
    private void generate(UIState state, Canvas canvas, String type) {
        String states = state.getStateKeys();
        String stateType = Utils.statesToConstantName(type + "_" + states);
        String paintMethodName = "paint" + type + Utils.statesToClassName(states);
        componentColors = new ArrayList<ComponentColor>();
        stateTypeCode.append("    static final int ").append(stateType).append(" = ").append(stateTypeCounter++).append(";\n");
        if (canvas.isBlank()) {
            return;
        }
        switchCode.append("            case ").append(stateType).append(": ").append(paintMethodName).append("(g); break;\n");
        paintingCode.append("    private void ").append(paintMethodName).append("(Graphics2D g) {\n");
        Insets in = canvas.getStretchingInsets();
        float a = in.left;
        float b = canvas.getSize().width - in.right;
        float c = in.top;
        float d = canvas.getSize().height - in.bottom;
        float width = canvas.getSize().width;
        float height = canvas.getSize().height;
        float cw = b - a;
        float ch = d - c;
        Layer[] layers = canvas.getLayers().toArray(new Layer[0]);
        for (int index=layers.length-1; index >= 0; index--) {
            Layer layer = layers[index];
            List<Shape> shapes = layer.getShapes();
            for (int i=shapes.size()-1; i>=0; i--) {
                Shape shape = shapes.get(i);
                Paint paint = shape.getPaint();
                String shapeMethodName = null; 
                String shapeVariable = null; 
                String shapeMethodBody = null;
                if (shape instanceof Rectangle) {
                    Rectangle rshape = (Rectangle) shape;
                    float x1 = encode((float)rshape.getX1(), a, b, width);
                    float y1 = encode((float)rshape.getY1(), c, d, height);
                    float x2 = encode((float)rshape.getX2(), a, b, width);
                    float y2 = encode((float)rshape.getY2(), c, d, height);
                    if (rshape.isRounded()) {
                        float rounding = (float)rshape.getRounding();
                        shapeMethodBody =
                                "        roundRect.setRoundRect(" +
                                writeDecodeX(x1) + ", 
                                "                               " + writeDecodeY(y1) + ", 
                                "                               " + writeDecodeX(x2) + " - " + writeDecodeX(x1) + ", 
                                "                               " + writeDecodeY(y2) + " - " + writeDecodeY(y1) + ", 
                                "                               " + rounding + "f, " + rounding + "f); 
                        shapeVariable = "roundRect";
                    } else {
                        shapeMethodBody =
                                "            rect.setRect(" +
                                writeDecodeX(x1) + ", 
                                "                         " + writeDecodeY(y1) + ", 
                                "                         " + writeDecodeX(x2) + " - " + writeDecodeX(x1) + ", 
                                "                         " + writeDecodeY(y2) + " - " + writeDecodeY(y1) + "); 
                        shapeVariable = "rect";
                    }
                } else if (shape instanceof Ellipse) {
                    Ellipse eshape = (Ellipse) shape;
                    float x1 = encode((float)eshape.getX1(), a, b, width);
                    float y1 = encode((float)eshape.getY1(), c, d, height);
                    float x2 = encode((float)eshape.getX2(), a, b, width);
                    float y2 = encode((float)eshape.getY2(), c, d, height);
                    shapeMethodBody =
                            "        ellipse.setFrame(" +
                            writeDecodeX(x1) + ", 
                            "                         " + writeDecodeY(y1) + ", 
                            "                         " + writeDecodeX(x2) + " - " + writeDecodeX(x1) + ", 
                            "                         " + writeDecodeY(y2) + " - " + writeDecodeY(y1) + "); 
                    shapeVariable = "ellipse";
                } else if (shape instanceof Path) {
                    Path pshape = (Path) shape;
                    List<Point> controlPoints = pshape.getControlPoints();
                    Point first, last;
                    first = last = controlPoints.get(0);
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("        path.reset();\n");
                    buffer.append("        path.moveTo(" + writeDecodeX(encode((float)first.getX(), a, b, width)) + ", " + writeDecodeY(encode((float)first.getY(), c, d, height)) + ");\n");
                    for (int j=1; j<controlPoints.size(); j++) {
                        Point cp = controlPoints.get(j);
                        if (last.isP2Sharp() && cp.isP1Sharp()) {
                            float x = encode((float)cp.getX(), a, b, width);
                            float y = encode((float)cp.getY(), c, d, height);
                            buffer.append("        path.lineTo(" + writeDecodeX(x) + ", " + writeDecodeY(y) + ");\n");
                        } else {
                            float x1 = encode((float)last.getX(), a, b, width);
                            float y1 = encode((float)last.getY(), c, d, height);
                            float x2 = encode((float)cp.getX(), a, b, width);
                            float y2 = encode((float)cp.getY(), c, d, height);
                            buffer.append(
                                    "        path.curveTo(" + writeDecodeBezierX(x1, last.getX(), last.getCp2X()) + ", "
                                                            + writeDecodeBezierY(y1, last.getY(), last.getCp2Y()) + ", "
                                                            + writeDecodeBezierX(x2, cp.getX(), cp.getCp1X()) + ", "
                                                            + writeDecodeBezierY(y2, cp.getY(), cp.getCp1Y()) + ", "
                                                            + writeDecodeX(x2) + ", " + writeDecodeY(y2) + ");\n");
                        }
                        last = cp;
                    }
                    if (last.isP2Sharp() && first.isP1Sharp()) {
                        float x = encode((float)first.getX(), a, b, width);
                        float y = encode((float)first.getY(), c, d, height);
                        buffer.append("        path.lineTo(" + writeDecodeX(x) + ", " + writeDecodeY(y) + ");\n");
                    } else {
                        float x1 = encode((float)last.getX(), a, b, width);
                        float y1 = encode((float)last.getY(), c, d, height);
                        float x2 = encode((float)first.getX(), a, b, width);
                        float y2 = encode((float)first.getY(), c, d, height);
                        buffer.append(
                                "        path.curveTo(" + writeDecodeBezierX(x1, last.getX(), last.getCp2X()) + ", "
                                                        + writeDecodeBezierY(y1, last.getY(), last.getCp2Y()) + ", "
                                                        + writeDecodeBezierX(x2, first.getX(), first.getCp1X()) + ", "
                                                        + writeDecodeBezierY(y2, first.getY(), first.getCp1Y()) + ", "
                                                        + writeDecodeX(x2) + ", " + writeDecodeY(y2) + ");\n");
                    }
                    buffer.append("        path.closePath();");
                    shapeMethodBody = buffer.toString();
                    shapeVariable = "path";
                } else {
                    throw new RuntimeException("Cannot happen unless a new Shape has been defined");
                }
                shapeMethodName = methods.get(shapeMethodBody);
                String returnType = null;
                if (shapeMethodName == null) {
                    if ("rect".equals(shapeVariable)) {
                        shapeMethodName = "decodeRect" + rectCounter++;
                        returnType = "Rectangle2D";
                    } else if ("roundRect".equals(shapeVariable)) {
                        shapeMethodName = "decodeRoundRect" + roundRectCounter++;
                        returnType = "RoundRectangle2D";
                    } else if ("ellipse".equals(shapeVariable)) {
                        shapeMethodName = "decodeEllipse" + ellipseCounter++;
                        returnType = "Ellipse2D";
                    } else {
                        shapeMethodName = "decodePath" + pathCounter++;
                        returnType = "Path2D";
                    }
                    methods.put(shapeMethodBody, shapeMethodName);
                    shapesCode.append("    private ").append(returnType).append(" ").append(shapeMethodName).append("() {\n");
                    shapesCode.append(shapeMethodBody);
                    shapesCode.append("\n");
                    shapesCode.append("        return " + shapeVariable + ";\n");
                    shapesCode.append("    }\n\n");
                }
                paintingCode.append("        ").append(shapeVariable).append(" = ").append(shapeMethodName).append("();\n");
                if (paint instanceof Matte) {
                    String colorVariable = encodeMatte((Matte)paint);
                    paintingCode.append("        g.setPaint(").append(colorVariable).append(");\n");
                } else if (paint instanceof Gradient) {
                    String gradientMethodName = encodeGradient(shape, (Gradient)paint);
                    paintingCode.append("        g.setPaint(").append(gradientMethodName).append("(").append(shapeVariable).append("));\n");
                } else if (paint instanceof RadialGradient) {
                    String radialMethodName = encodeRadial(shape, (RadialGradient)paint);
                    paintingCode.append("        g.setPaint(").append(radialMethodName).append("(").append(shapeVariable).append("));\n");
                }
                paintingCode.append("        g.fill(").append(shapeVariable).append(");\n");
            }
        }
        paintingCode.append("\n    }\n\n");
        if (!componentColors.isEmpty()) {
            componentColorsMap.put(stateType, componentColors);
            componentColors = null;
        }
    }
    private float encode(float x, float a, float b, float w) {
        float r = 0;
        if (x < a) {
            r = (x / a);
        } else if (x > b) {
            r = 2 + ((x - b) / (w - b));
        } else if (x == a && x == b) {
            return 1.5f;
        } else {
            r = 1 + ((x - a) / (b - a));
        }
        if (Float.isNaN(r)) {
            System.err.println("[Error] Encountered NaN: encode(" + x + ", " + a + ", " + b + ", " + w + ")");
            return 0;
        } else if (Float.isInfinite(r)) {
            System.err.println("[Error] Encountered Infinity: encode(" + x + ", " + a + ", " + b + ", " + w + ")");
            return 0;
        } else if (r < 0) {
            System.err.println("[Error] encoded value was less than 0: encode(" + x + ", " + a + ", " + b + ", " + w + ")");
            return 0;
        } else if (r > 3) {
            System.err.println("[Error] encoded value was greater than 3: encode(" + x + ", " + a + ", " + b + ", " + w + ")");
            return 3;
        } else {
            return r;
        }
    }
    private String writeDecodeX(float encodedX) {
        return "decodeX(" + encodedX + "f)";
    }
    private String writeDecodeY(float encodedY) {
        return "decodeY(" + encodedY + "f)";
    }
    private static String writeDecodeBezierX(double ex, double x, double cpx) {
        return "decodeAnchorX(" + ex + "f, " + (cpx - x) + "f)";
    }
    private static String writeDecodeBezierY(double ey, double y, double cpy) {
        return "decodeAnchorY(" + ey + "f, " + (cpy - y) + "f)";
    }
    private String encodeMatte(Matte m) {
        String declaration = m.getDeclaration();
        String variableName = colors.get(declaration);
        if (variableName == null) {
            variableName = "color" + colorCounter++;
            colors.put(declaration, variableName);
            colorCode.append(String.format("    private Color %s = %s;\n",
                                           variableName, declaration));
        }
        if (m.getComponentPropertyName() != null) {
            ComponentColor cc = m.createComponentColor(variableName);
            int index = componentColors.indexOf(cc);
            if (index == -1) {
                index = componentColors.size();
                componentColors.add(cc);
            }
            return "(Color)componentColors[" + index + "]";
        } else {
            return variableName;
        }
    }
    private String encodeGradient(Shape ps, Gradient g) {
        StringBuilder b = new StringBuilder();
        float x1 = (float)ps.getPaintX1();
        float y1 = (float)ps.getPaintY1();
        float x2 = (float)ps.getPaintX2();
        float y2 = (float)ps.getPaintY2();
        b.append("        return decodeGradient((");
        b.append(x1);
        b.append("f * w) + x, (");
        b.append(y1);
        b.append("f * h) + y, (");
        b.append(x2);
        b.append("f * w) + x, (");
        b.append(y2);
        b.append("f * h) + y,\n");
        encodeGradientColorsAndFractions(g,b);
        b.append(");");
        String methodBody = b.toString();
        String methodName = methods.get(methodBody);
        if (methodName == null) {
            methodName = "decodeGradient" + gradientCounter++;
            gradientsCode.append("    private Paint ").append(methodName).append("(Shape s) {\n");
            gradientsCode.append("        Rectangle2D bounds = s.getBounds2D();\n");
            gradientsCode.append("        float x = (float)bounds.getX();\n");
            gradientsCode.append("        float y = (float)bounds.getY();\n");
            gradientsCode.append("        float w = (float)bounds.getWidth();\n");
            gradientsCode.append("        float h = (float)bounds.getHeight();\n");
            gradientsCode.append(methodBody);
            gradientsCode.append("\n    }\n\n");
            methods.put(methodBody, methodName);
        }
        return methodName;
    }
    private void encodeGradientColorsAndFractions(AbstractGradient g,
                                                    StringBuilder b) {
        List<GradientStop> stops = g.getStops();
        float[] fractions = new float[stops.size() + stops.size() - 1];
        String[] colors = new String[fractions.length];
        int index = 0; 
        for (int i = 0; i < stops.size(); i++) {
            GradientStop s = stops.get(i);
            colors[index] = encodeMatte(s.getColor());
            fractions[index] = s.getPosition();
            if (index < fractions.length - 1) {
                float f1 = s.getPosition();
                float f2 = stops.get(i + 1).getPosition();
                index++;
                fractions[index] = f1 + (f2 - f1) * s.getMidpoint();
                colors[index] = "decodeColor("+
                        colors[index - 1]+","+
                        encodeMatte(stops.get(i + 1).getColor())+",0.5f)";
            }
            index++;
        }
        for (int i = 1; i < fractions.length; i++) {
            if (fractions[i] <= fractions[i - 1]) {
                fractions[i] = fractions[i - 1] + .000001f;
            }
        }
        int outOfBoundsIndex = -1;
        for (int i = 0; i < fractions.length; i++) {
            if (fractions[i] > 1) {
                outOfBoundsIndex = i;
                break;
            }
        }
        if (outOfBoundsIndex >= 0) {
            float[] f = fractions;
            String[] c = colors;
            fractions = new float[outOfBoundsIndex];
            colors = new String[outOfBoundsIndex];
            System.arraycopy(f, 0, fractions, 0, outOfBoundsIndex);
            System.arraycopy(c, 0, colors, 0, outOfBoundsIndex);
        }
        b.append("                new float[] { ");
        for (int i = 0; i < fractions.length; i++) {
            if (i>0)b.append(',');
            b.append(fractions[i]);
            b.append('f');
        }
        b.append(" },\n                new Color[] { ");
        for (int i = 0; i < colors.length; i++) {
            if (i>0) b.append(",\n                            ");
            b.append(colors[i]);
        }
        b.append("}");
    }
    private String encodeRadial(Shape ps, RadialGradient g) {
        float centerX1 = (float)ps.getPaintX1();
        float centerY1 = (float)ps.getPaintY1();
        float x2 = (float)ps.getPaintX2();
        float y2 = (float)ps.getPaintY2();
        float radius = (float)Point2D.distance(centerX1, centerY1, x2, y2);
        StringBuilder b = new StringBuilder();
        b.append("        return decodeRadialGradient((");
        b.append(centerX1);
        b.append("f * w) + x, (");
        b.append(centerY1);
        b.append("f * h) + y, ");
        b.append(radius);
        b.append("f,\n");
        encodeGradientColorsAndFractions(g,b);
        b.append(");");
        String methodBody = b.toString();
        String methodName = methods.get(methodBody);
        if (methodName == null) {
            methodName = "decodeRadial" + radialCounter++;
            gradientsCode.append("    private Paint ").append(methodName).append("(Shape s) {\n");
            gradientsCode.append("        Rectangle2D bounds = s.getBounds2D();\n");
            gradientsCode.append("        float x = (float)bounds.getX();\n");
            gradientsCode.append("        float y = (float)bounds.getY();\n");
            gradientsCode.append("        float w = (float)bounds.getWidth();\n");
            gradientsCode.append("        float h = (float)bounds.getHeight();\n");
            gradientsCode.append(methodBody);
            gradientsCode.append("\n    }\n\n");
            methods.put(methodBody, methodName);
        }
        return methodName;
    }
    public static void writePainter(UIRegion r, String painterName) {
        PainterGenerator gen = new PainterGenerator(r);
        System.out.println("Generating source file: " + painterName + ".java");
        Map<String, String> variables = Generator.getVariables();
        variables.put("PAINTER_NAME", painterName);
        variables.put("STATIC_DECL", gen.stateTypeCode.toString());
        variables.put("COLORS_DECL", gen.colorCode.toString());
        variables.put("DO_PAINT_SWITCH_BODY", gen.switchCode.toString());
        variables.put("PAINTING_DECL", gen.paintingCode.toString());
        variables.put("GET_EXTENDED_CACHE_KEYS", gen.getExtendedCacheKeysCode.toString());
        variables.put("SHAPES_DECL", gen.shapesCode.toString());
        variables.put("GRADIENTS_DECL", gen.gradientsCode.toString());
        Generator.writeSrcFile("PainterImpl", variables, painterName);
    }
}
