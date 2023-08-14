public final class SectionHelper {
    static public class ManifestSectionPart extends SectionPart {
        public ManifestSectionPart(Composite body, FormToolkit toolkit) {
            this(body, toolkit, 0, false);
        }
        public ManifestSectionPart(Composite body, FormToolkit toolkit,
                int extra_style, boolean use_description) {
            super(body, toolkit, extra_style |
                    Section.TITLE_BAR |
                    (use_description ? Section.DESCRIPTION : 0));
        }
        public Composite createTableLayout(FormToolkit toolkit, int numColumns) {
            return SectionHelper.createTableLayout(getSection(), toolkit, numColumns);
        }
        public Label createLabel(Composite parent, FormToolkit toolkit, String label,
                String tooltip) {
            return SectionHelper.createLabel(parent, toolkit, label, tooltip);
        }
        public Text createLabelAndText(Composite parent, FormToolkit toolkit, String label,
                String value, String tooltip) {
            return SectionHelper.createLabelAndText(parent, toolkit, label, value, tooltip);
        }
        public FormText createFormText(Composite parent, FormToolkit toolkit, boolean isHtml,
                String label, boolean setupLayoutData) {
            return SectionHelper.createFormText(parent, toolkit, isHtml, label, setupLayoutData);
        }
        public void layoutChanged() {
            Section section = getSection();
            try {
                Method reflow;
                reflow = section.getClass().getDeclaredMethod("reflow", (Class<?>[])null);
                reflow.setAccessible(true);
                reflow.invoke(section);
            } catch (Exception e) {
                AdtPlugin.log(e, "Error when invoking Section.reflow");
            }
            section.layout(true , true );
        }
    }
    static public Composite createTableLayout(Composite composite, FormToolkit toolkit,
            int numColumns) {
        Composite table = toolkit.createComposite(composite);
        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = numColumns;
        table.setLayout(layout);
        toolkit.paintBordersFor(table);
        if (composite instanceof Section) {
            ((Section) composite).setClient(table);
        }
        return table;
    }
    static public Composite createGridLayout(Composite composite, FormToolkit toolkit,
            int numColumns) {
        Composite grid = toolkit.createComposite(composite);
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        grid.setLayout(layout);
        toolkit.paintBordersFor(grid);
        if (composite instanceof Section) {
            ((Section) composite).setClient(grid);
        }
        return grid;
    }
    static public Text createLabelAndText(Composite parent, FormToolkit toolkit, String label_text,
            String value, String tooltip) {
        Label label = toolkit.createLabel(parent, label_text);
        label.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
        Text text = toolkit.createText(parent, value);
        text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE));
        addControlTooltip(label, tooltip);
        return text;
    }
    static public Label createLabel(Composite parent, FormToolkit toolkit, String label_text,
            String tooltip) {
        Label label = toolkit.createLabel(parent, label_text);
        TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
        if (parent.getLayout() instanceof TableWrapLayout) {
            twd.colspan = ((TableWrapLayout) parent.getLayout()).numColumns;
        }
        label.setLayoutData(twd);
        addControlTooltip(label, tooltip);
        return label;
    }
    static public void addControlTooltip(final Control control, String tooltip) {
        if (control == null || tooltip == null || tooltip.length() == 0) {
            return;
        }
        if (control instanceof Button) {
            control.setToolTipText(tooltip);
            return;
        }
        control.setToolTipText(null);
        final DefaultInformationControl ic = new DefaultInformationControl(control.getShell());
        ic.setInformation(tooltip);
        Point sz = ic.computeSizeHint();
        ic.setSize(sz.x, sz.y);
        ic.setVisible(false); 
        control.addMouseTrackListener(new MouseTrackListener() {
            public void mouseEnter(MouseEvent e) {
            }
            public void mouseExit(MouseEvent e) {
                ic.setVisible(false);
            }
            public void mouseHover(MouseEvent e) {
                ic.setLocation(control.toDisplay(10, 25));  
                ic.setVisible(true);
            }
        });
    }
    static public FormText createFormText(Composite parent, FormToolkit toolkit,
            boolean isHtml, String label, boolean setupLayoutData) {
        FormText text = toolkit.createFormText(parent, true );
        if (setupLayoutData) {
            TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
            twd.maxWidth = AndroidEditor.TEXT_WIDTH_HINT;
            if (parent.getLayout() instanceof TableWrapLayout) {
                twd.colspan = ((TableWrapLayout) parent.getLayout()).numColumns;
            }
            text.setLayoutData(twd);
        }
        text.setWhitespaceNormalized(true);
        text.setText(label, isHtml , isHtml );
        return text;
    }
}
