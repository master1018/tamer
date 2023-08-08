public class GrabberTray {
    private static TrayIcon tray = null;
    private static Point savedLocation = new Point();
    private static Dimension savedSize = new Dimension();
    private GrabberTray() {
        SystemTray sysTray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();
        MenuItem dirItem = new MenuItem("Set a default directory to save images");
        MenuItem clearDirItem = new MenuItem("Clear the default directory");
        MenuItem minimizedToggle = new MenuItem("Toggle start up minimized");
        MenuItem borderItem = new MenuItem("Change border width");
        MenuItem colorItem = new MenuItem("Change border color");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");
        dirItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JDirectoryChooser();
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    setDefaultDir(fc.getSelectedFile());
                }
            }
        });
        clearDirItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDefaultDir(null);
            }
        });
        minimizedToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMinimized();
            }
        });
        borderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog d = new JDialog((JFrame) null, "Change Border Width", false);
                final Integer borderWidth = (Integer) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.BorderSize.getID()), Integer.class);
                final JSlider borderSlider = new JSlider(1, 20, borderWidth);
                JButton btOk = new JButton("OK");
                JButton btCancel = new JButton("Cancel");
                borderSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        setBorderWidth(borderSlider.getValue());
                    }
                });
                btOk.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        d.dispose();
                    }
                });
                btCancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBorderWidth(borderWidth);
                        d.dispose();
                    }
                });
                borderSlider.setMajorTickSpacing(2);
                borderSlider.setMinorTickSpacing(1);
                borderSlider.setPaintLabels(true);
                borderSlider.setPaintTicks(true);
                JPanel tmpPanel = new JPanel(new BorderLayout());
                tmpPanel.add(btOk, BorderLayout.WEST);
                tmpPanel.add(btCancel, BorderLayout.EAST);
                tmpPanel.add(Box.createHorizontalStrut(50), BorderLayout.CENTER);
                d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
                d.getContentPane().add(borderSlider);
                d.getContentPane().add(tmpPanel);
                d.setIconImage(GrabberGraphics.saveImg);
                d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                d.pack();
                d.setLocationRelativeTo(null);
                d.setVisible(true);
            }
        });
        colorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color col = (Color) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.Color.getID()), Color.class);
                Color selection = JColorChooser.showDialog(null, "Choose a color for the window", col);
                if (selection != null) {
                    setBorderColor(selection);
                }
            }
        });
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.showDialog();
            }
        });
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                if (GrabberWindow.getWindow().getLocation().equals(new Point(screen.width, screen.height))) {
                    GrabberWindow.getWindow().setLocation(savedLocation);
                    GrabberWindow.getWindow().setSize(savedSize);
                }
                new ExitAnimation().getAnimator(200).start();
            }
        });
        popup.add(dirItem);
        popup.add(clearDirItem);
        popup.add(minimizedToggle);
        popup.addSeparator();
        popup.add(borderItem);
        popup.add(colorItem);
        popup.addSeparator();
        popup.add(aboutItem);
        popup.add(exitItem);
        tray = new TrayIcon(GrabberGraphics.saveImg, "JScreenGrabber", popup);
        tray.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (!GrabberWindow.getWindow().isVisible()) {
                        GrabberWindow.getWindow().setVisible(true);
                        new RestoreCompositeAnimator(savedLocation, savedSize).start();
                    } else {
                        GrabberTray.setSavedLocation(GrabberWindow.getWindow().getLocation());
                        GrabberTray.setSavedSize(GrabberWindow.getWindow().getSize());
                        new MinimizeCompositeAnimator().start();
                    }
                }
            }
        });
        tray.setImageAutoSize(true);
        try {
            sysTray.add(tray);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
    }
    private void toggleMinimized() {
        boolean current = (Boolean) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.StartMinimized.getID()), Boolean.class);
        OptionsEngine.replace(Options.StartMinimized.getID(), Boolean.toString(!current));
        String message = null;
        if (!current) {
            message = "From now on the program will start minimized.";
        } else {
            message = "From now on the program will not be minimized when it starts.";
        }
        tray.displayMessage("Start up minimized behaviour changed", message, TrayIcon.MessageType.INFO);
    }
    private void setDefaultDir(File dir) {
        String s = "";
        if (dir != null) s = dir.getAbsolutePath();
        OptionsEngine.replace(Options.Directory.getID(), s);
        if (s.equals("")) tray.displayMessage("Cleared default directory", "The default directory has been cleared. Next time you try to save an image, a dialog will be displayed to allow you to select a location.", TrayIcon.MessageType.INFO); else tray.displayMessage("Default directory saved", s + " has been set as the default directory. From now on when you save an image, it will be saved in that location automatically with an incremental name.", TrayIcon.MessageType.INFO);
    }
    private void setBorderWidth(int width) {
        if (width <= 0) return;
        OptionsEngine.replace(Options.BorderSize.getID(), width + "");
        GrabberWindow.getWindow().getContentPane().dispatchEvent(new ComponentEvent(GrabberWindow.getWindow().getContentPane(), ComponentEvent.COMPONENT_RESIZED));
    }
    private void setBorderColor(Color c) {
        OptionsEngine.replace(Options.Color.getID(), c.getRed() + ":" + c.getGreen() + ":" + c.getBlue());
        GrabberGraphics.createImages();
        GrabberWindow.getWindow().getContentPane().repaint();
    }
    public static TrayIcon getTray() {
        if (tray == null) new GrabberTray();
        return tray;
    }
    public static void setSavedLocation(Point p) {
        savedLocation = p;
    }
    public static void setSavedSize(Dimension s) {
        savedSize = s;
    }
}
