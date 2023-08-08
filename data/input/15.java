public class SquareCoordinatesPanel extends BoardModifyingPrefsPanel {
    private final JRadioButton none;
    private final JRadioButton rim;
    private final JRadioButton outside;
    private final JRadioButton everySquare;
    private final ColorChooser coordsColor;
    public SquareCoordinatesPanel(BoardManager boardManager, JinBoard previewBoard) {
        super(boardManager, previewBoard);
        int coordsDisplayStyle = boardManager.getCoordsDisplayStyle();
        none = new JRadioButton("None", coordsDisplayStyle == JBoard.NO_COORDS);
        rim = new JRadioButton("On the rim of the board (like winboard)", coordsDisplayStyle == JBoard.RIM_COORDS);
        outside = new JRadioButton("Outside the board", coordsDisplayStyle == JBoard.OUTSIDE_COORDS);
        everySquare = new JRadioButton("In every square", coordsDisplayStyle == JBoard.ARROW_MOVE_HIGHLIGHTING);
        coordsColor = new ColorChooser("Coordinates' text color:", boardManager.getCoordsDisplayColor());
        none.setToolTipText("No square coordinates are displayed on the board");
        rim.setToolTipText("Row and column coordinates are displayed on the " + "border of the leftmost and bottommost squares");
        outside.setToolTipText("Row and column coordinates are displayed outside " + "the board, along its left and bottom borders");
        everySquare.setToolTipText("Square coordinates are displayed in each square");
        coordsColor.setToolTipText("The color of the coordinates' text");
        ButtonGroup group = new ButtonGroup();
        group.add(none);
        group.add(rim);
        group.add(outside);
        group.add(everySquare);
        none.setMnemonic('N');
        rim.setMnemonic('r');
        outside.setMnemonic('O');
        everySquare.setMnemonic('I');
        coordsColor.setMnemonic('C');
        setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel contentPanel = new PreferredSizedPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Square Coordinates"), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        none.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        rim.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        outside.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        everySquare.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        coordsColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(none);
        contentPanel.add(rim);
        contentPanel.add(outside);
        contentPanel.add(everySquare);
        contentPanel.add(coordsColor);
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(contentPanel);
        ActionListener styleListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                SquareCoordinatesPanel.this.previewBoard.setCoordsDisplayStyle(getCoordsDisplayStyle());
                fireStateChanged();
            }
        };
        none.addActionListener(styleListener);
        rim.addActionListener(styleListener);
        outside.addActionListener(styleListener);
        everySquare.addActionListener(styleListener);
        coordsColor.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                SquareCoordinatesPanel.this.previewBoard.setCoordsDisplayColor(coordsColor.getColor());
                fireStateChanged();
            }
        });
    }
    private int getCoordsDisplayStyle() {
        if (none.isSelected()) return JBoard.NO_COORDS; else if (rim.isSelected()) return JBoard.RIM_COORDS; else if (outside.isSelected()) return JBoard.OUTSIDE_COORDS; else if (everySquare.isSelected()) return JBoard.EVERY_SQUARE_COORDS; else throw new IllegalStateException("None of the radio buttons are selected");
    }
    public void initPreviewBoard() {
        previewBoard.setCoordsDisplayStyle(getCoordsDisplayStyle());
        previewBoard.setCoordsDisplayColor(coordsColor.getColor());
    }
    public void applyChanges() throws BadChangesException {
        boardManager.setCoordsDisplayStyle(getCoordsDisplayStyle());
        boardManager.setCoordsDisplayColor(coordsColor.getColor());
    }
}
