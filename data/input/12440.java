        CompositeTypeMaker.make(GetSetBean.class.getName(),
                                GetSetBean.class.getName(),
                                new String[] {"int", "string", "stringArray"},
                                new String[] {"int", "string", "stringArray"},
                                new OpenType[] {
                                    SimpleType.INTEGER,
                                    SimpleType.STRING,
                                    ArrayTypeMaker.make(1, SimpleType.STRING),
                                });
    GetSetBean getGetSet();
    void setGetSet(GetSetBean bean);
    GetSetBean opGetSet(GetSetBean x, GetSetBean y);
    GetterInterface Interface = new GetterInterface() {
        public boolean isWhatsit() {
            return true;
        }
        public int[] getInts() {
            return new int[] {1};
        }
        public String[] getStrings() {
            return new String[] {"x"};
        }
        public GetSetBean getGetSet() {
            return GetSetBean.make(3, "a", new String[] {"b"});
        }
        public boolean equals(Object o) {
            if (!(o instanceof GetterInterface))
                return false;
            GetterInterface i = (GetterInterface) o;
            return isWhatsit() == i.isWhatsit() &&
                   Arrays.equals(getInts(), i.getInts()) &&
                   Arrays.equals(getStrings(), i.getStrings()) &&
                   getGetSet().equals(i.getGetSet());
        }
     };
     CompositeType InterfaceType =
        CompositeTypeMaker.make(GetterInterface.class.getName(),
                                GetterInterface.class.getName(),
                                new String[] {
                                    "ints", "getSet", "strings", "whatsit",
                                },
                                new String[] {
                                    "ints", "getSet", "strings", "whatsit",
                                },
                                new OpenType[] {
                                    ArrayTypeMaker.make(SimpleType.INTEGER, true),
                                    GetSetType,
                                    ArrayTypeMaker.make(1, SimpleType.STRING),
                                    SimpleType.BOOLEAN,
                                });
     GetterInterface getInterface();
     void setInterface(GetterInterface i);
     GetterInterface opInterface(GetterInterface x, GetterInterface y);
    public static class GetSetBean {
        public GetSetBean() {
            this(0, null, null);
        }
        private GetSetBean(int Int, String string, String[] stringArray) {
            this.Int = Int;
            this.string = string;
            this.stringArray = stringArray;
        }
        public static GetSetBean
                make(int Int, String string, String[] stringArray) {
            GetSetBean b = new GetSetBean(Int, string, stringArray);
            return b;
        }
        public int getInt() {
            return Int;
        }
        public String getString() {
            return this.string;
        }
        public String[] getStringArray() {
            return this.stringArray;
        }
        public void setInt(int x) {
            this.Int = x;
        }
        public void setString(String string) {
            this.string = string;
        }
        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }
        public boolean equals(Object o) {
            if (!(o instanceof GetSetBean))
                return false;
            GetSetBean b = (GetSetBean) o;
            return (b.Int == Int &&
                    b.string.equals(string) &&
                    Arrays.equals(b.stringArray, stringArray));
        }
        String string;
        String[] stringArray;
        int Int;
    }
    public static interface GetterInterface {
        public String[] getStrings();
        public int[] getInts();
        public boolean isWhatsit();
        public GetSetBean getGetSet();
        public boolean equals(Object o);
        public int hashCode();
        public String toString();
    }
    public static class ArrayTypeMaker {
        static ArrayType make(int dims, OpenType baseType) {
            try {
                return new ArrayType(dims, baseType);
            } catch (OpenDataException e) {
                throw new Error(e);
            }
        }
        static ArrayType make(SimpleType baseType, boolean primitiveArray) {
            try {
                return new ArrayType(baseType, primitiveArray);
            } catch (OpenDataException e) {
                throw new Error(e);
            }
        }
    }
    public static class CompositeTypeMaker {
        static CompositeType make(String className,
                                  String description,
                                  String[] itemNames,
                                  String[] itemDescriptions,
                                  OpenType[] itemTypes) {
            try {
                return new CompositeType(className,
                                         description,
                                         itemNames,
                                         itemDescriptions,
                                         itemTypes);
            } catch (OpenDataException e) {
                throw new Error(e);
            }
        }
    }
    public static interface GraphMXBean {
        public NodeMXBean[] getNodes();
    }
    public static class Graph implements GraphMXBean {
        public Graph(Node... nodes) {
            for (Node node : nodes)
                node.setGraph(this);
            this.nodes = nodes;
        }
        public NodeMXBean[] getNodes() {
            return nodes;
        }
        private final Node[] nodes;
    }
    public static interface NodeMXBean {
        public String getName();
        public GraphMXBean getGraph();
    }
    public static class Node implements NodeMXBean {
        public Node(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public GraphMXBean getGraph() {
            return graph;
        }
        public void setGraph(Graph graph) {
            this.graph = graph;
        }
        private final String name;
        private Graph graph;
    }
    SimpleType GraphType = SimpleType.OBJECTNAME;
    GraphMXBean getGraph();
    void setGraph(GraphMXBean g);
    GraphMXBean opGraph(GraphMXBean x, GraphMXBean y);
    String GraphObjectName = "test:type=GraphMXBean";
    String NodeAObjectName = "test:type=NodeMXBean,name=a";
    String NodeBObjectName = "test:type=NodeMXBean,name=b";
    Node NodeA = new Node("a");
    Node NodeB = new Node("b");
    GraphMXBean Graph = new Graph(NodeA, NodeB);
    public static class ExoticCompositeData implements CompositeDataView {
        private ExoticCompositeData(String whatsit) {
            this.whatsit = whatsit;
        }
        public static ExoticCompositeData from(CompositeData cd) {
            String whatsit = (String) cd.get("whatsit");
            if (!whatsit.startsWith("!"))
                throw new IllegalArgumentException(whatsit);
            return new ExoticCompositeData(whatsit.substring(1));
        }
        public CompositeData toCompositeData(CompositeType ct) {
            try {
                return new CompositeDataSupport(ct, new String[] {"whatsit"},
                                                new String[] {"!" + whatsit});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public String getWhatsit() {
            return whatsit;
        }
        public boolean equals(Object o) {
            return ((o instanceof ExoticCompositeData) &&
                    ((ExoticCompositeData) o).whatsit.equals(whatsit));
        }
        private final String whatsit;
        public static final CompositeType type;
        static {
            try {
                type =
                    new CompositeType(ExoticCompositeData.class.getName(),
                                      ExoticCompositeData.class.getName(),
                                      new String[] {"whatsit"},
                                      new String[] {"whatsit"},
                                      new OpenType[] {SimpleType.STRING});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    CompositeType ExoticType = ExoticCompositeData.type;
    ExoticCompositeData getExotic();
    void setExotic(ExoticCompositeData ecd);
    ExoticCompositeData opExotic(ExoticCompositeData ecd1,
                                 ExoticCompositeData ecd2);
    ExoticCompositeData Exotic = new ExoticCompositeData("foo");
}
