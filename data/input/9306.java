public class TranslucentShapedFrameTest extends javax.swing.JFrame {
    Frame testFrame;
    static CountDownLatch done;
    static volatile boolean failed = false;
    GraphicsConfiguration gcToUse = null;
    public TranslucentShapedFrameTest() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}
        initComponents();
        checkEffects();
        SwingUtilities.updateComponentTreeUI(this);
    }
    private void initComponents() {
        createDisposeGrp = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        transparencySld = new javax.swing.JSlider();
        shapedCb = new javax.swing.JCheckBox();
        nonOpaqueChb = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        passedBtn = new javax.swing.JButton();
        failedBtn = new javax.swing.JButton();
        createFrameBtn = new javax.swing.JToggleButton();
        disposeFrameBtn = new javax.swing.JToggleButton();
        useSwingCb = new javax.swing.JCheckBox();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TranslucentShapedFrameTest");
        jLabel1.setText("Frame Opacity:");
        transparencySld.setMajorTickSpacing(10);
        transparencySld.setMinorTickSpacing(5);
        transparencySld.setPaintLabels(true);
        transparencySld.setPaintTicks(true);
        transparencySld.setValue(100);
        transparencySld.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                transparencySldStateChanged(evt);
            }
        });
        shapedCb.setText("Shaped Frame");
        shapedCb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        shapedCb.setMargin(new java.awt.Insets(0, 0, 0, 0));
        shapedCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shapedCbActionPerformed(evt);
            }
        });
        nonOpaqueChb.setText("Non Opaque Frame");
        nonOpaqueChb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nonOpaqueChb.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nonOpaqueChb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nonOpaqueChbActionPerformed(evt);
            }
        });
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Create translucent and/or shaped, or\nnon-opaque frame. Make sure it behaves\ncorrectly (no artifacts left on the screen\nwhen dragging - if dragging is possible).\nClick \"Passed\" if the test behaves correctly,\n\"Falied\" otherwise.");
        jScrollPane1.setViewportView(jTextArea1);
        jLabel2.setText("Instructions:");
        passedBtn.setBackground(new Color(129, 255, 100));
        passedBtn.setText("Passed");
        passedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passedBtnActionPerformed(evt);
            }
        });
        failedBtn.setBackground(Color.red);
        failedBtn.setText("Failed");
        failedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                failedBtnActionPerformed(evt);
            }
        });
        createDisposeGrp.add(createFrameBtn);
        createFrameBtn.setText("Create Frame");
        createFrameBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createFrameBtnActionPerformed(evt);
            }
        });
        createDisposeGrp.add(disposeFrameBtn);
        disposeFrameBtn.setSelected(true);
        disposeFrameBtn.setText("Dispose Frame");
        disposeFrameBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disposeFrameBtnActionPerformed(evt);
            }
        });
        useSwingCb.setText("Use JFrame");
        useSwingCb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useSwingCb.setMargin(new java.awt.Insets(0, 0, 0, 0));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(transparencySld, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(shapedCb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nonOpaqueChb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useSwingCb)
                        .addContainerGap(102, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 314, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passedBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(failedBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(createFrameBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(disposeFrameBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transparencySld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shapedCb)
                    .addComponent(nonOpaqueChb)
                    .addComponent(useSwingCb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(disposeFrameBtn)
                    .addComponent(createFrameBtn))
                .addGap(17, 17, 17)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passedBtn)
                    .addComponent(failedBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }
    private void nonOpaqueChbActionPerformed(java.awt.event.ActionEvent evt) {
        if (testFrame != null) {
            testFrame.setBackground(new Color(0, 0, 0, nonOpaqueChb.isSelected() ? 0 : 255));
        }
    }
    private void shapedCbActionPerformed(java.awt.event.ActionEvent evt) {
        if (testFrame != null) {
            Shape s = null;
            if (shapedCb.isSelected()) {
                s = new Ellipse2D.Double(0, 0,
                                         testFrame.getWidth(),
                                         testFrame.getHeight());
            }
            testFrame.setShape(s);
        }
    }
    private void transparencySldStateChanged(javax.swing.event.ChangeEvent evt) {
        JSlider source = (JSlider)evt.getSource();
            int transl = transparencySld.getValue();
            if (testFrame != null) {
                testFrame.setOpacity((float)transl/100f);
            }
    }
    private void failedBtnActionPerformed(java.awt.event.ActionEvent evt) {
        disposeFrameBtnActionPerformed(evt);
        dispose();
        failed = true;
        done.countDown();
    }
    private void disposeFrameBtnActionPerformed(java.awt.event.ActionEvent evt) {
        TSFrame.stopThreads();
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
    }
    private void createFrameBtnActionPerformed(java.awt.event.ActionEvent evt) {
        disposeFrameBtnActionPerformed(evt);
        int transl = transparencySld.getValue();
        testFrame = TSFrame.createGui(
                useSwingCb.isSelected(), shapedCb.isSelected(),
                (transl < 100), nonOpaqueChb.isSelected(),
                (float)transl/100f);
    }
    private void passedBtnActionPerformed(java.awt.event.ActionEvent evt) {
        disposeFrameBtnActionPerformed(evt);
        dispose();
        done.countDown();
    }
    public static void main(String args[]) {
        done = new CountDownLatch(1);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TranslucentShapedFrameTest().setVisible(true);
            }
        });
        try {
            done.await();
        } catch (InterruptedException ex) {}
        if (failed) {
            throw new RuntimeException("Test FAILED");
        }
        System.out.println("Test PASSED");
    }
    private void checkEffects() {
        GraphicsDevice gd = getGraphicsConfiguration().getDevice();
        if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            shapedCb.setEnabled(false);
        }
        if (!gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
            transparencySld.setEnabled(false);
        }
        if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
            nonOpaqueChb.setEnabled(false);
        }
    }
    private javax.swing.ButtonGroup createDisposeGrp;
    private javax.swing.JToggleButton createFrameBtn;
    private javax.swing.JToggleButton disposeFrameBtn;
    private javax.swing.JButton failedBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JCheckBox nonOpaqueChb;
    private javax.swing.JButton passedBtn;
    private javax.swing.JCheckBox shapedCb;
    private javax.swing.JSlider transparencySld;
    private javax.swing.JCheckBox useSwingCb;
}
