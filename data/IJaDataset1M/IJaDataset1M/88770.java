package org.galab.frame;

import java.lang.reflect.*;
import org.galab.util.*;
import org.galab.saveableobject.*;
import org.galab.saveableobject.bot.*;
import org.galab.saveableobject.world.*;

public class MainMenu extends javax.swing.JFrame implements Launcher {

    public MainMenu() {
        initComponents();
        pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {
        labWelcome = new javax.swing.JLabel();
        butTraining = new javax.swing.JButton();
        butTesting = new javax.swing.JButton();
        butPopulation = new javax.swing.JButton();
        butLevelEditor = new javax.swing.JButton();
        butBodyLab = new javax.swing.JButton();
        butGALab = new javax.swing.JButton();
        butSettings = new javax.swing.JButton();
        butAnalysis = new javax.swing.JButton();
        getContentPane().setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        setTitle("GA - Main Menu");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        labWelcome.setText("Welcome to Genetic Algorithm");
        labWelcome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labWelcome.setFont(new java.awt.Font("Dialog", 0, 18));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 5, 0);
        getContentPane().add(labWelcome, gridBagConstraints1);
        butTraining.setPreferredSize(new java.awt.Dimension(150, 31));
        butTraining.setMaximumSize(new java.awt.Dimension(150, 31));
        butTraining.setFont(new java.awt.Font("Dialog", 0, 14));
        butTraining.setText("Training");
        butTraining.setMinimumSize(new java.awt.Dimension(150, 31));
        butTraining.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTrainingActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 2);
        getContentPane().add(butTraining, gridBagConstraints1);
        butTesting.setPreferredSize(new java.awt.Dimension(150, 31));
        butTesting.setMaximumSize(new java.awt.Dimension(150, 31));
        butTesting.setFont(new java.awt.Font("Dialog", 0, 14));
        butTesting.setText("Testing");
        butTesting.setMinimumSize(new java.awt.Dimension(150, 31));
        butTesting.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTestingActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 2, 5, 0);
        getContentPane().add(butTesting, gridBagConstraints1);
        butPopulation.setPreferredSize(new java.awt.Dimension(150, 25));
        butPopulation.setMaximumSize(new java.awt.Dimension(150, 25));
        butPopulation.setFont(new java.awt.Font("Dialog", 0, 10));
        butPopulation.setText("Population Handler");
        butPopulation.setMinimumSize(new java.awt.Dimension(150, 25));
        butPopulation.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPopulationActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(10, 0, 5, 2);
        getContentPane().add(butPopulation, gridBagConstraints1);
        butLevelEditor.setPreferredSize(new java.awt.Dimension(150, 25));
        butLevelEditor.setMaximumSize(new java.awt.Dimension(150, 25));
        butLevelEditor.setFont(new java.awt.Font("Dialog", 0, 10));
        butLevelEditor.setText("World Lab");
        butLevelEditor.setMinimumSize(new java.awt.Dimension(150, 25));
        butLevelEditor.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLevelEditorActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(10, 2, 5, 0);
        getContentPane().add(butLevelEditor, gridBagConstraints1);
        butBodyLab.setPreferredSize(new java.awt.Dimension(150, 25));
        butBodyLab.setMaximumSize(new java.awt.Dimension(150, 25));
        butBodyLab.setFont(new java.awt.Font("Dialog", 0, 10));
        butBodyLab.setText("Bot Lab");
        butBodyLab.setMinimumSize(new java.awt.Dimension(150, 25));
        butBodyLab.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butBodyLabActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 2);
        getContentPane().add(butBodyLab, gridBagConstraints1);
        butGALab.setPreferredSize(new java.awt.Dimension(150, 25));
        butGALab.setMaximumSize(new java.awt.Dimension(150, 25));
        butGALab.setFont(new java.awt.Font("Dialog", 0, 10));
        butGALab.setText("GA Lab");
        butGALab.setMinimumSize(new java.awt.Dimension(150, 25));
        butGALab.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butGALabActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(0, 2, 5, 0);
        getContentPane().add(butGALab, gridBagConstraints1);
        butSettings.setPreferredSize(new java.awt.Dimension(150, 25));
        butSettings.setMaximumSize(new java.awt.Dimension(150, 25));
        butSettings.setFont(new java.awt.Font("Dialog", 0, 10));
        butSettings.setText("Settings");
        butSettings.setMinimumSize(new java.awt.Dimension(150, 25));
        butSettings.setEnabled(false);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 2);
        getContentPane().add(butSettings, gridBagConstraints1);
        butAnalysis.setPreferredSize(new java.awt.Dimension(150, 25));
        butAnalysis.setMaximumSize(new java.awt.Dimension(150, 25));
        butAnalysis.setFont(new java.awt.Font("Dialog", 0, 10));
        butAnalysis.setText("Analysis Lab");
        butAnalysis.setMinimumSize(new java.awt.Dimension(150, 25));
        butAnalysis.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAnalysisActionPerformed(evt);
            }
        });
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.insets = new java.awt.Insets(0, 2, 0, 0);
        getContentPane().add(butAnalysis, gridBagConstraints1);
        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setSize(new java.awt.Dimension(350, 250));
        setLocation((screenSize.width - 350) / 2, (screenSize.height - 250) / 2);
    }

    private void butAnalysisActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new Analysis(this).show();
    }

    private void butGALabActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new GALab(this, new GARunner()).show();
    }

    private void butBodyLabActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new BodyLab(this, new Bot()).show();
    }

    private void butLevelEditorActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new LevelEditor(this, new World()).show();
    }

    private void butPopulationActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new PopulationHandler(this, new Population()).show();
    }

    private void butTestingActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new Testing(this).show();
    }

    private void butTrainingActionPerformed(java.awt.event.ActionEvent evt) {
        hide();
        new Training(this).show();
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        Util.processCommandLineArgs(args);
        new MainMenu().show();
    }

    public void launcherUpdate(Object launchee) {
        show();
    }

    public void launcherUpdate(Object launchee, int level) {
        launcherUpdate(launchee);
    }

    private javax.swing.JLabel labWelcome;

    private javax.swing.JButton butTraining;

    private javax.swing.JButton butTesting;

    private javax.swing.JButton butPopulation;

    private javax.swing.JButton butLevelEditor;

    private javax.swing.JButton butBodyLab;

    private javax.swing.JButton butGALab;

    private javax.swing.JButton butSettings;

    private javax.swing.JButton butAnalysis;
}
