public class bug4987336 extends JApplet {
    private static final String IMAGE_RES = "box.gif";
    private static final String ANIM_IMAGE_RES = "cupanim.gif";
    public void init() {
        JPanel pnLafs = new JPanel();
        pnLafs.setLayout(new BoxLayout(pnLafs, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        pnLafs.setBorder(new TitledBorder("Available Lafs"));
        for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            LafRadioButton comp = new LafRadioButton(lafInfo);
            pnLafs.add(comp);
            group.add(comp);
        }
        JPanel pnContent = new JPanel();
        pnContent.setLayout(new BoxLayout(pnContent, BoxLayout.Y_AXIS));
        pnContent.add(pnLafs);
        pnContent.add(createSlider(true, IMAGE_RES, IMAGE_RES, ANIM_IMAGE_RES, ANIM_IMAGE_RES));
        pnContent.add(createSlider(false, IMAGE_RES, IMAGE_RES, ANIM_IMAGE_RES, ANIM_IMAGE_RES));
        pnContent.add(createSlider(true, ANIM_IMAGE_RES, null, IMAGE_RES, IMAGE_RES));
        pnContent.add(createSlider(false, ANIM_IMAGE_RES, null, IMAGE_RES, IMAGE_RES));
        getContentPane().add(new JScrollPane(pnContent));
    }
    private static JSlider createSlider(boolean enabled,
                                        String firstEnabledImage, String firstDisabledImage,
                                        String secondEnabledImage, String secondDisabledImage) {
        Hashtable<Integer, JComponent> dictionary = new Hashtable<Integer, JComponent>();
        dictionary.put(0, createLabel(firstEnabledImage, firstDisabledImage));
        dictionary.put(1, createLabel(secondEnabledImage, secondDisabledImage));
        JSlider result = new JSlider(0, 1);
        result.setLabelTable(dictionary);
        result.setPaintLabels(true);
        result.setEnabled(enabled);
        return result;
    }
    private static JLabel createLabel(String enabledImage, String disabledImage) {
        ImageIcon enabledIcon = enabledImage == null ? null :
                new ImageIcon(bug4987336.class.getResource(enabledImage));
        ImageIcon disabledIcon = disabledImage == null ? null :
                new ImageIcon(bug4987336.class.getResource(disabledImage));
        JLabel result = new JLabel(enabledImage == null && disabledImage == null ? "No image" : "Image",
                enabledIcon, SwingConstants.LEFT);
        result.setDisabledIcon(disabledIcon);
        return result;
    }
    private class LafRadioButton extends JRadioButton {
        public LafRadioButton(final UIManager.LookAndFeelInfo lafInfo) {
            super(lafInfo.getName(), lafInfo.getName().equals(UIManager.getLookAndFeel().getName()));
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        UIManager.setLookAndFeel(lafInfo.getClassName());
                        SwingUtilities.updateComponentTreeUI(bug4987336.this);
                    } catch (Exception ex) {
                        System.out.println("Cannot set LAF " + lafInfo.getName());
                    }
                }
            });
        }
    }
}
