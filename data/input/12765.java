public final class EditorTopComponent extends TopComponent implements ChangedListener<RangeSliderModel>, PropertyChangeListener {
    private DiagramScene scene;
    private InstanceContent content;
    private FindPanel findPanel;
    private EnableBlockLayoutAction blockLayoutAction;
    private OverviewAction overviewAction;
    private PredSuccAction predSuccAction;
    private boolean notFirstTime;
    private ExtendedSatelliteComponent satelliteComponent;
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private RangeSlider rangeSlider;
    private JToggleButton overviewButton;
    private static final String PREFERRED_ID = "EditorTopComponent";
    private static final String SATELLITE_STRING = "satellite";
    private static final String SCENE_STRING = "scene";
    private DiagramViewModel rangeSliderModel;
    private ExportCookie exportCookie = new ExportCookie() {
        public void export(File f) {
            Graphics2D svgGenerator = BatikSVG.createGraphicsObject();
            if (svgGenerator == null) {
                NotifyDescriptor message = new NotifyDescriptor.Message("For export to SVG files the Batik SVG Toolkit must be intalled.", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(message);
            } else {
                scene.paint(svgGenerator);
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(f);
                    Writer out = new OutputStreamWriter(os, "UTF-8");
                    BatikSVG.printToStream(svgGenerator, out, true);
                } catch (FileNotFoundException e) {
                    NotifyDescriptor message = new NotifyDescriptor.Message("For export to SVG files the Batik SVG Toolkit must be intalled.", NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(message);
                } catch (UnsupportedEncodingException e) {
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    };
    private void updateDisplayName() {
        setDisplayName(getDiagram().getName());
    }
    public EditorTopComponent(Diagram diagram) {
        FilterChain filterChain = null;
        FilterChain sequence = null;
        FilterChainProvider provider = Lookup.getDefault().lookup(FilterChainProvider.class);
        if (provider == null) {
            filterChain = new FilterChain();
            sequence = new FilterChain();
        } else {
            filterChain = provider.getFilterChain();
            sequence = provider.getSequence();
        }
        setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));
        Action[] actions = new Action[]{
            PrevDiagramAction.get(PrevDiagramAction.class),
            NextDiagramAction.get(NextDiagramAction.class),
            null,
            ExtractAction.get(ExtractAction.class),
            ShowAllAction.get(HideAction.class),
            ShowAllAction.get(ShowAllAction.class),
            null,
            ZoomInAction.get(ZoomInAction.class),
            ZoomOutAction.get(ZoomOutAction.class),
            null,
            ExpandPredecessorsAction.get(ExpandPredecessorsAction.class),
            ExpandSuccessorsAction.get(ExpandSuccessorsAction.class)
        };
        initComponents();
        ActionMap actionMap = getActionMap();
        ToolbarPool.getDefault().setPreferredIconSize(16);
        Toolbar toolBar = new Toolbar();
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); 
        toolBar.setBorder(b);
        JPanel container = new JPanel();
        this.add(container, BorderLayout.NORTH);
        container.setLayout(new BorderLayout());
        container.add(BorderLayout.NORTH, toolBar);
        rangeSliderModel = new DiagramViewModel(diagram.getGraph().getGroup(), filterChain, sequence);
        rangeSliderModel.selectGraph(diagram.getGraph());
        rangeSlider = new RangeSlider();
        rangeSlider.setModel(rangeSliderModel);
        rangeSliderModel.getChangedEvent().addListener(this);
        container.add(BorderLayout.CENTER, rangeSlider);
        scene = new DiagramScene(actions, rangeSliderModel);
        content = new InstanceContent();
        this.associateLookup(new ProxyLookup(new Lookup[]{scene.getLookup(), new AbstractLookup(content)}));
        content.add(exportCookie);
        content.add(rangeSliderModel);
        findPanel = new FindPanel(diagram.getFigures());
        findPanel.setMaximumSize(new Dimension(200, 50));
        toolBar.add(findPanel);
        toolBar.add(NodeFindAction.get(NodeFindAction.class));
        toolBar.addSeparator();
        toolBar.add(NextDiagramAction.get(NextDiagramAction.class));
        toolBar.add(PrevDiagramAction.get(PrevDiagramAction.class));
        toolBar.addSeparator();
        toolBar.add(ExtractAction.get(ExtractAction.class));
        toolBar.add(ShowAllAction.get(HideAction.class));
        toolBar.add(ShowAllAction.get(ShowAllAction.class));
        toolBar.addSeparator();
        toolBar.add(ShowAllAction.get(ZoomInAction.class));
        toolBar.add(ShowAllAction.get(ZoomOutAction.class));
        blockLayoutAction = new EnableBlockLayoutAction();
        JToggleButton button = new JToggleButton(blockLayoutAction);
        button.setSelected(true);
        toolBar.add(button);
        blockLayoutAction.addPropertyChangeListener(this);
        overviewAction = new OverviewAction();
        overviewButton = new JToggleButton(overviewAction);
        overviewButton.setSelected(false);
        toolBar.add(overviewButton);
        overviewAction.addPropertyChangeListener(this);
        predSuccAction = new PredSuccAction();
        button = new JToggleButton(predSuccAction);
        button.setSelected(true);
        toolBar.add(button);
        predSuccAction.addPropertyChangeListener(this);
        toolBar.addSeparator();
        toolBar.add(UndoAction.get(UndoAction.class));
        toolBar.add(RedoAction.get(RedoAction.class));
        centerPanel = new JPanel();
        this.add(centerPanel, BorderLayout.CENTER);
        cardLayout = new CardLayout();
        centerPanel.setLayout(cardLayout);
        centerPanel.add(SCENE_STRING, scene.getScrollPane());
        centerPanel.setBackground(Color.WHITE);
        satelliteComponent = new ExtendedSatelliteComponent(scene);
        satelliteComponent.setSize(200, 200);
        centerPanel.add(SATELLITE_STRING, satelliteComponent);
        CallbackSystemAction callFindAction = (CallbackSystemAction) SystemAction.get(FindAction.class);
        NodeFindAction findAction = NodeFindAction.get(NodeFindAction.class);
        Object key = callFindAction.getActionMapKey();
        actionMap.put(key, findAction);
        scene.getScrollPane().addKeyListener(keyListener);
        scene.getView().addKeyListener(keyListener);
        satelliteComponent.addKeyListener(keyListener);
        scene.getScrollPane().addHierarchyBoundsListener(new HierarchyBoundsListener() {
            public void ancestorMoved(HierarchyEvent e) {
            }
            public void ancestorResized(HierarchyEvent e) {
                if (!notFirstTime && scene.getScrollPane().getBounds().width > 0) {
                    notFirstTime = true;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Figure f = EditorTopComponent.this.scene.getModel().getDiagramToView().getRootFigure();
                            if (f != null) {
                                scene.setUndoRedoEnabled(false);
                                scene.gotoFigure(f);
                                scene.setUndoRedoEnabled(true);
                            }
                        }
                    });
                }
            }
        });
        updateDisplayName();
    }
    private KeyListener keyListener = new KeyListener() {
        public void keyTyped(KeyEvent e) {
        }
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_S) {
                EditorTopComponent.this.overviewButton.setSelected(true);
                EditorTopComponent.this.overviewAction.setState(true);
            }
        }
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_S) {
                EditorTopComponent.this.overviewButton.setSelected(false);
                EditorTopComponent.this.overviewAction.setState(false);
            }
        }
    };
    public DiagramViewModel getDiagramModel() {
        return scene.getModel();
    }
    private void showSatellite() {
        cardLayout.show(centerPanel, SATELLITE_STRING);
        satelliteComponent.requestFocus();
    }
    private void showScene() {
        cardLayout.show(centerPanel, SCENE_STRING);
        scene.getView().requestFocus();
    }
    public void findNode() {
        findPanel.find();
    }
    public void zoomOut() {
        double zoom = scene.getZoomFactor();
        Point viewPosition = scene.getScrollPane().getViewport().getViewPosition();
        double newZoom = zoom / DiagramScene.ZOOM_INCREMENT;
        if (newZoom > DiagramScene.ZOOM_MIN_FACTOR) {
            scene.setZoomFactor(newZoom);
            scene.validate();
            scene.getScrollPane().getViewport().setViewPosition(new Point((int) (viewPosition.x / DiagramScene.ZOOM_INCREMENT), (int) (viewPosition.y / DiagramScene.ZOOM_INCREMENT)));
            this.satelliteComponent.update();
        }
    }
    public void zoomIn() {
        double zoom = scene.getZoomFactor();
        Point viewPosition = scene.getScrollPane().getViewport().getViewPosition();
        double newZoom = zoom * DiagramScene.ZOOM_INCREMENT;
        if (newZoom < DiagramScene.ZOOM_MAX_FACTOR) {
            scene.setZoomFactor(newZoom);
            scene.validate();
            scene.getScrollPane().getViewport().setViewPosition(new Point((int) (viewPosition.x * DiagramScene.ZOOM_INCREMENT), (int) (viewPosition.y * DiagramScene.ZOOM_INCREMENT)));
            this.satelliteComponent.update();
        }
    }
    public void showPrevDiagram() {
        int fp = getModel().getFirstPosition();
        int sp = getModel().getSecondPosition();
        if (fp != 0) {
            fp--;
            sp--;
            getModel().setPositions(fp, sp);
        }
    }
    public DiagramViewModel getModel() {
        return scene.getModel();
    }
    public FilterChain getFilterChain() {
        return this.scene.getModel().getFilterChain();
    }
    public static EditorTopComponent getActive() {
        Set<? extends Mode> modes = WindowManager.getDefault().getModes();
        for (Mode m : modes) {
            TopComponent tc = m.getSelectedTopComponent();
            if (tc instanceof EditorTopComponent) {
                return (EditorTopComponent) tc;
            }
        }
        return null;
    }
        private void initComponents() {
                jCheckBox1 = new javax.swing.JCheckBox();
                org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, "jCheckBox1");
                jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
                setLayout(new java.awt.BorderLayout());
        }
        private javax.swing.JCheckBox jCheckBox1;
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    @Override
    public void componentOpened() {
    }
    @Override
    public void componentClosed() {
    }
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    public void changed(RangeSliderModel model) {
        updateDisplayName();
    }
    public boolean showPredSucc() {
        return (Boolean) predSuccAction.getValue(PredSuccAction.STATE);
    }
    public void setSelection(PropertyMatcher matcher) {
        Properties.PropertySelector<Figure> selector = new Properties.PropertySelector<Figure>(scene.getModel().getDiagramToView().getFigures());
        List<Figure> list = selector.selectMultiple(matcher);
        boolean b = scene.getUndoRedoEnabled();
        scene.setUndoRedoEnabled(false);
        scene.gotoFigures(list);
        scene.setUndoRedoEnabled(b);
        scene.setSelection(list);
    }
    public void setSelectedNodes(Set<InputNode> nodes) {
        List<Figure> list = new ArrayList<Figure>();
        Set<Integer> ids = new HashSet<Integer>();
        for (InputNode n : nodes) {
            ids.add(n.getId());
        }
        for (Figure f : scene.getModel().getDiagramToView().getFigures()) {
            for (InputNode n : f.getSource().getSourceNodes()) {
                if (ids.contains(n.getId())) {
                    list.add(f);
                    break;
                }
            }
        }
        scene.gotoFigures(list);
        scene.setSelection(list);
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.predSuccAction) {
            boolean b = (Boolean) predSuccAction.getValue(PredSuccAction.STATE);
            this.getModel().setShowNodeHull(b);
        } else if (evt.getSource() == this.overviewAction) {
            boolean b = (Boolean) overviewAction.getValue(OverviewAction.STATE);
            if (b) {
                showSatellite();
            } else {
                showScene();
            }
        } else if (evt.getSource() == this.blockLayoutAction) {
            boolean b = (Boolean) blockLayoutAction.getValue(EnableBlockLayoutAction.STATE);
            System.out.println("Showblocks = " + b);
            this.getModel().setShowBlocks(b);
        } else {
            assert false : "Unknown event source";
        }
    }
    public void extract() {
        scene.showOnly(scene.getSelectedNodes());
    }
    public void hideNodes() {
        Set<Integer> selectedNodes = this.scene.getSelectedNodes();
        HashSet<Integer> nodes = new HashSet<Integer>(scene.getModel().getHiddenNodes());
        nodes.addAll(selectedNodes);
        this.scene.showNot(nodes);
    }
    public void expandPredecessors() {
        Set<Figure> oldSelection = scene.getSelectedFigures();
        Set<Figure> figures = new HashSet<Figure>();
        for (Figure f : this.getDiagramModel().getDiagramToView().getFigures()) {
            boolean ok = false;
            if (oldSelection.contains(f)) {
                ok = true;
            } else {
                for (Figure pred : f.getSuccessors()) {
                    if (oldSelection.contains(pred)) {
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) {
                figures.add(f);
            }
        }
        scene.showAll(figures);
    }
    public void expandSuccessors() {
        Set<Figure> oldSelection = scene.getSelectedFigures();
        Set<Figure> figures = new HashSet<Figure>();
        for (Figure f : this.getDiagramModel().getDiagramToView().getFigures()) {
            boolean ok = false;
            if (oldSelection.contains(f)) {
                ok = true;
            } else {
                for (Figure succ : f.getPredecessors()) {
                    if (oldSelection.contains(succ)) {
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) {
                figures.add(f);
            }
        }
        scene.showAll(figures);
    }
    public void showAll() {
        scene.showNot(new HashSet<Integer>());
    }
    public Diagram getDiagram() {
        return getDiagramModel().getDiagramToView();
    }
    @Override
    protected void componentActivated() {
    }
    @Override
    public void requestFocus() {
        super.requestFocus();
        scene.getView().requestFocus();
    }
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return scene.getView().requestFocusInWindow();
    }
    @Override
    public UndoRedo getUndoRedo() {
        return scene.getUndoRedo();
    }
}
