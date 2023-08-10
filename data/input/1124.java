public class Edge implements DoublyLinkedListEntry {
    public long getID() {
        return ID;
    }
    public Node startNode;
    public Node endNode;
    public void initializeEdge() {
    }
    public void cleanUp() {
    }
    public int getNumberOfMessagesOnThisEdge() {
        return numberOfMessagesOnThisEdge;
    }
    public Edge getOppositeEdge() {
        return oppositeEdge;
    }
    public boolean equals(Edge e) {
        return ((this.startNode.ID == e.startNode.ID) && (this.endNode.ID == e.endNode.ID));
    }
    public String toString() {
        return "Type: " + Global.toShortName(this.getClass().getName());
    }
    public static Color defaultEdgeColorPassive = Color.BLACK;
    public static Color defaultEdgeColorActive = Color.RED;
    {
        try {
            String s = Configuration.getStringParameter("Edge/PassiveColor");
            defaultEdgeColorPassive = Color.decode(s);
        } catch (CorruptConfigurationEntryException e) {
        } catch (NumberFormatException e) {
            Main.fatalError("Invalid color specification for the configuration entry Edge/PassiveColor. Expected a hexadecimal number of the form 0xrrggbb");
        }
        try {
            String s = Configuration.getStringParameter("Edge/ActiveColor");
            defaultEdgeColorActive = Color.decode(s);
        } catch (CorruptConfigurationEntryException e) {
        } catch (NumberFormatException e) {
            Main.fatalError("Invalid color specification for the configuration entry Edge/ActiveColor. Expected a hexadecimal number of the form 0xrrggbb");
        }
    }
    public Color defaultColor = defaultEdgeColorPassive;
    private Color sendingColor = defaultEdgeColorActive;
    public Color getColor() {
        if (this.numberOfMessagesOnThisEdge > 0) {
            return sendingColor;
        } else {
            return defaultColor;
        }
    }
    public void draw(Graphics g, PositionTransformation pt) {
        Position p1 = startNode.getPosition();
        pt.translateToGUIPosition(p1);
        int fromX = pt.guiX, fromY = pt.guiY;
        Position p2 = endNode.getPosition();
        pt.translateToGUIPosition(p2);
        if ((this.numberOfMessagesOnThisEdge == 0) && (this.oppositeEdge != null) && (this.oppositeEdge.numberOfMessagesOnThisEdge > 0)) {
            Arrow.drawArrowHead(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
        } else {
            Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
        }
    }
    public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
        pt.translateToGUIPosition(startNode.getPosition());
        double eSX = pt.guiXDouble;
        double eSY = pt.guiYDouble;
        pt.translateToGUIPosition(endNode.getPosition());
        Color c = getColor();
        pw.setColor(c.getRed(), c.getGreen(), c.getBlue());
        pw.setLineWidth(0.5);
        if (Configuration.drawArrows) {
            pw.drawArrow(eSX, eSY, pt.guiXDouble, pt.guiYDouble);
        } else {
            pw.drawLine(eSX, eSY, pt.guiXDouble, pt.guiYDouble);
        }
    }
    private long ID = 0;
    public Edge oppositeEdge = null;
    public int numberOfMessagesOnThisEdge = 0;
    public void addMessageForThisEdge(Message msg) {
        numberOfMessagesOnThisEdge++;
    }
    public void removeMessageForThisEdge(Message msg) {
        numberOfMessagesOnThisEdge--;
    }
    public boolean valid = false;
    protected final void findOppositeEdge() {
        Iterator<Edge> edgeIter = endNode.outgoingConnections.iterator();
        while (edgeIter.hasNext()) {
            Edge e = edgeIter.next();
            if ((e.startNode.ID == endNode.ID) && (e.endNode.ID == startNode.ID)) {
                this.oppositeEdge = e;
                e.oppositeEdge = this;
                return;
            }
        }
        this.oppositeEdge = null;
    }
    public boolean isInside(int xCoord, int yCoord, PositionTransformation pt) {
        Position p1 = startNode.getPosition();
        pt.translateToGUIPosition(p1);
        int fromX = pt.guiX, fromY = pt.guiY;
        Position p2 = endNode.getPosition();
        pt.translateToGUIPosition(p2);
        double dist = Line2D.ptSegDist(fromX, fromY, pt.guiX, pt.guiY, xCoord, yCoord);
        return dist < 3;
    }
    private static EdgePool freeEdges = new EdgePool();
    private static long nextId = 1;
    private static Constructor<?> constructor = null;
    private static String nameOfSearchedEdge = "";
    public static int numEdgesOnTheFly = 0;
    public static final Edge fabricateEdge(Node from, Node to) {
        Edge edge = freeEdges.get();
        if (edge != null) {
            if (edge.startNode != null || edge.endNode != null) {
                Main.fatalError(Logging.getCodePosition() + " Edge factory failed! About to return an edge that was already returned. (Probably, free() was called > 1 on this edge.)");
            }
        } else try {
            if (Configuration.hasEdgeTypeChanged() || constructor == null) {
                constructor = null;
                nameOfSearchedEdge = Configuration.getEdgeType();
                Class<?> edgeClass = Class.forName(nameOfSearchedEdge);
                Constructor<?>[] list = edgeClass.getDeclaredConstructors();
                for (Constructor<?> c : list) {
                    Class<?>[] paramClasses = c.getParameterTypes();
                    if (paramClasses.length != 0) {
                        continue;
                    } else {
                        constructor = c;
                        break;
                    }
                }
                if (constructor == null) {
                    throw new NoSuchMethodException("Did not find a valid constructor for the " + nameOfSearchedEdge + " class.");
                }
                Configuration.setEdgeTypeChanged(false);
            }
            edge = (Edge) constructor.newInstance();
        } catch (ClassNotFoundException cNFE) {
            Main.fatalError("The implementation of the edge '" + nameOfSearchedEdge + "' could not be found.\n" + "Change the Type in the XML-File or implement it." + "");
        } catch (IllegalArgumentException e) {
            Main.fatalError("Exception caught while creating edge '" + nameOfSearchedEdge + "'.\n" + e);
        } catch (InstantiationException e) {
            Main.fatalError("Exception caught while creating edge '" + nameOfSearchedEdge + "'.\n" + e);
        } catch (IllegalAccessException e) {
            Main.fatalError("Exception caught while creating edge '" + nameOfSearchedEdge + "'.\n" + e);
        } catch (InvocationTargetException e) {
            Main.fatalError("Exception caught while creating edge '" + nameOfSearchedEdge + "'.\n" + e.getCause());
        } catch (SecurityException e) {
            Main.fatalError("Exception caught while creating edge '" + nameOfSearchedEdge + "'.\n" + e);
        } catch (NoSuchMethodException e) {
            Main.fatalError("Cannot instanciate an edge of type '" + nameOfSearchedEdge + "' for two nodes of type \n(" + from.getClass().getName() + ", " + to.getClass().getName() + ").\n" + "To select a different edge type, change the config.xml file\n" + "or use the settings dialog in the GUI.");
        }
        edge.startNode = from;
        edge.endNode = to;
        edge.oppositeEdge = null;
        edge.sendingColor = defaultEdgeColorActive;
        edge.defaultColor = defaultEdgeColorPassive;
        edge.valid = false;
        edge.numberOfMessagesOnThisEdge = 0;
        edge.ID = getNextFreeID();
        edge.findOppositeEdge();
        edge.initializeEdge();
        numEdgesOnTheFly++;
        return edge;
    }
    public final void removeEdgeFromGraph() {
        if (Configuration.asynchronousMode) {
            Runtime.eventQueue.invalidatePacketEventsForThisEdge(this);
        } else {
            this.endNode.getInboxPacketBuffer().invalidatePacketsSentOverThisEdge(this);
        }
        this.cleanUp();
    }
    public final void free() {
        if (oppositeEdge != null) {
            if (oppositeEdge.oppositeEdge == this) {
                oppositeEdge.oppositeEdge = null;
            }
            oppositeEdge = null;
        }
        this.startNode = null;
        this.endNode = null;
        this.defaultColor = null;
        this.sendingColor = null;
        this.oppositeEdge = null;
        numEdgesOnTheFly--;
        freeEdges.add(this);
    }
    private DLLFingerList dllFingerList = new DLLFingerList();
    public DLLFingerList getDoublyLinkedListFinger() {
        return dllFingerList;
    }
    private static long getNextFreeID() {
        if (nextId == 0) {
            Main.fatalError("The Edge ID counter overflowed.");
        }
        return Edge.nextId++;
    }
}
