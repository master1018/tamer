public class SelectToolTask extends Task {
    public void setPropertyFile(File propertyFile) {
        this.propertyFile = propertyFile;
    }
    public void setToolProperty(String toolProperty) {
        this.toolProperty = toolProperty;
    }
    public void setArgsProperty(String argsProperty) {
        this.argsProperty = argsProperty;
    }
    public void setAskIfUnset(boolean askIfUnset) {
        this.askIfUnset = askIfUnset;
    }
    @Override
    public void execute() {
        Project p = getProject();
        Properties props = readProperties(propertyFile);
        toolName = props.getProperty("tool.name");
        if (toolName != null) {
            toolArgs = props.getProperty(toolName + ".args", "");
        }
        if (toolProperty == null ||
            askIfUnset && (toolName == null
                || (argsProperty != null && toolArgs == null))) {
            showGUI(props);
        }
        if (toolProperty != null && !(toolName == null || toolName.equals(""))) {
            p.setProperty(toolProperty, toolName);
            if (argsProperty != null && toolArgs != null)
                p.setProperty(argsProperty, toolArgs);
        }
    }
    void showGUI(Properties fileProps) {
        Properties guiProps = new Properties(fileProps);
        JOptionPane p = createPane(guiProps);
        p.createDialog("Select Tool").setVisible(true);
        toolName = (String) toolChoice.getSelectedItem();
        toolArgs = argsField.getText();
        if (defaultCheck.isSelected()) {
            if (toolName.equals("")) {
                fileProps.remove("tool.name");
            } else {
                fileProps.put("tool.name", toolName);
                fileProps.put(toolName + ".args", toolArgs);
            }
            writeProperties(propertyFile, fileProps);
        }
    }
    JOptionPane createPane(final Properties props) {
        JPanel body = new JPanel(new GridBagLayout());
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets.right = 10;
        lc.insets.bottom = 3;
        GridBagConstraints fc = new GridBagConstraints();
        fc.anchor = GridBagConstraints.WEST;
        fc.gridx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets.bottom = 3;
        JLabel toolLabel = new JLabel("Tool:");
        body.add(toolLabel, lc);
        String[] toolChoices = { "apt", "javac", "javadoc", "javah", "javap" };
        if (true || toolProperty == null) {
            List<String> l = new ArrayList<String>(Arrays.asList(toolChoices));
            l.add(0, "");
            toolChoices = l.toArray(new String[l.size()]);
        }
        toolChoice = new JComboBox(toolChoices);
        if (toolName != null)
            toolChoice.setSelectedItem(toolName);
        toolChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String tn = (String) e.getItem();
                argsField.setText(getDefaultArgsForTool(props, tn));
                if (toolProperty != null)
                    okButton.setEnabled(!tn.equals(""));
            }
        });
        body.add(toolChoice, fc);
        argsField = new JTextField(getDefaultArgsForTool(props, toolName), 40);
        if (toolProperty == null || argsProperty != null) {
            JLabel argsLabel = new JLabel("Args:");
            body.add(argsLabel, lc);
            body.add(argsField, fc);
            argsField.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                }
                public void focusLost(FocusEvent e) {
                    String toolName = (String) toolChoice.getSelectedItem();
                    if (toolName.length() > 0)
                        props.put(toolName + ".args", argsField.getText());
                }
            });
        }
        defaultCheck = new JCheckBox("Set as default");
        if (toolProperty == null)
            defaultCheck.setSelected(true);
        else
            body.add(defaultCheck, fc);
        final JOptionPane p = new JOptionPane(body);
        okButton = new JButton("OK");
        okButton.setEnabled(toolProperty == null || (toolName != null && !toolName.equals("")));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog d = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, p);
                d.setVisible(false);
            }
        });
        p.setOptions(new Object[] { okButton });
        return p;
    }
    Properties readProperties(File file) {
        Properties p = new Properties();
        if (file != null && file.exists()) {
            Reader in = null;
            try {
                in = new BufferedReader(new FileReader(file));
                p.load(in);
                in.close();
            } catch (IOException e) {
                throw new BuildException("error reading property file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new BuildException("cannot close property file", e);
                    }
                }
            }
        }
        return p;
    }
    void writeProperties(File file, Properties p) {
        if (file != null) {
            Writer out = null;
            try {
                File dir = file.getParentFile();
                if (dir != null && !dir.exists())
                    dir.mkdirs();
                out = new BufferedWriter(new FileWriter(file));
                p.store(out, "langtools properties");
                out.close();
            } catch (IOException e) {
                throw new BuildException("error writing property file", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        throw new BuildException("cannot close property file", e);
                    }
                }
            }
        }
    }
    String getDefaultArgsForTool(Properties props, String tn) {
        return (tn == null || tn.equals("")) ? "" : props.getProperty(tn + ".args", "");
    }
    private boolean askIfUnset;
    private String toolProperty;
    private String argsProperty;
    private File propertyFile;
    private JComboBox toolChoice;
    private JTextField argsField;
    private JCheckBox defaultCheck;
    private JButton okButton;
    private String toolName;
    private String toolArgs;
}
