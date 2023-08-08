public class RuleView extends JDialog {
    private static final long serialVersionUID = -1990732757069341877L;
    private GraphZoomScrollPane gzsp;
    private RuleModel ruleModel;
    private boolean canceled;
    private JTextArea japeString;
    private JFormattedTextField priority;
    private JTextField ruleName;
    private TreeLayout layout;
    public RuleView(Frame parent, PxRule model) {
        ruleModel = new RuleModel(model);
        initComponents();
        initEventHandling();
    }
    private void initComponents() {
        SparseTree graph = ruleModel.getGraph();
        Renderer renderer = ruleModel.getRenderer();
        layout = new TreeLayout(graph);
        VisualizationViewer vv = new VisualizationViewer(layout, renderer);
        gzsp = new GraphZoomScrollPane(vv);
        gzsp.setDoubleBuffered(true);
        japeString = new JTextArea();
        japeString.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        japeString.setTabSize(3);
        priority = BasicComponentFactory.createIntegerField(ruleModel.getBufferedModel(PxRule.PROPERTYNAME_PRIORITY));
        ruleName = BasicComponentFactory.createTextField(ruleModel.getBufferedModel(PxRule.PROPERTYNAME_RULE_NAME));
        DefaultModalGraphMouse graphMouse;
        graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(Mode.EDITING);
        vv.setGraphMouse(graphMouse);
        vv.addGraphMouseListener(ruleModel.getGraphMouseListener());
    }
    private void initEventHandling() {
        PropertyConnector.connect(ruleModel, RuleModel.PROPERTYNAME_SELECTED_JAPE_ELEMENT, japeString, "text").updateProperty2();
    }
    public JComponent buildPanel() {
        FormLayout mainFormLayout = new FormLayout("fill:350px:grow, fill:250px:grow", "fill:100px, fill:400px:grow");
        FormLayout subFormLayout = new FormLayout("fill:50px:grow", "20px, 20px");
        DefaultFormBuilder mainPanelBuilder = new DefaultFormBuilder(mainFormLayout);
        DefaultFormBuilder subPanelBuilder = new DefaultFormBuilder(subFormLayout);
        CellConstraints cc = new CellConstraints();
        mainPanelBuilder.setDefaultDialogBorder();
        mainPanelBuilder.add(gzsp, cc.xywh(1, 1, 1, 2));
        mainPanelBuilder.add(japeString, cc.xy(2, 2));
        mainPanelBuilder.setLineGapSize(Sizes.ZERO);
        mainPanelBuilder.setBorder(BorderFactory.createLineBorder(Color.black));
        subPanelBuilder.append("Priority", priority);
        subPanelBuilder.append("Rule Name", ruleName);
        mainPanelBuilder.add(subPanelBuilder.getPanel(), cc.xy(2, 1));
        return mainPanelBuilder.getPanel();
    }
    public static void showRule(PxRule rule) {
        if (rule == null) throw new NullPointerException("Rule can't be null");
        JFrame jf = new JFrame();
        RuleView view = new RuleView(jf, rule);
        JComponent panel = view.buildPanel();
        jf.getContentPane().add(panel);
        jf.setVisible(true);
        jf.setSize(850, 600);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setResizable(true);
        Utils.locateOnOpticalScreenCenter(jf);
    }
    public static void main(String[] args) throws IOException, ParseException {
        URL japeFile = Loader.getResource("url_pre.jape");
        ParseCpsl cpslParser = Factory.newJapeParser(japeFile, "ISO-8859-1");
        JapeTransducerWrapper transducer = new JapeTransducerWrapper(cpslParser.SinglePhaseTransducer());
        PxRule pxRule = transducer.getRule(0);
        showRule(pxRule);
    }
    private void build() {
        setContentPane(buildPanel());
        pack();
        setResizable(false);
        Utils.locateOnOpticalScreenCenter(this);
    }
    public void open() {
        build();
        canceled = false;
        setVisible(true);
    }
    public void close() {
        dispose();
    }
    public boolean hasBeenCanceled() {
        return canceled;
    }
}
