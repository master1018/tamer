package br.com.siodoni.stompboxgraphic.main;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Form extends javax.swing.JFrame {

    /** Creates new form Form */
    public Form() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        rtNomeEfeito = new javax.swing.JLabel();
        nomeEfeito = new javax.swing.JTextField();
        btGerar = new javax.swing.JButton();
        parametro1 = new javax.swing.JTextField();
        valor1 = new javax.swing.JSpinner();
        parametro2 = new javax.swing.JTextField();
        valor2 = new javax.swing.JSpinner();
        parametro3 = new javax.swing.JTextField();
        valor3 = new javax.swing.JSpinner();
        parametro4 = new javax.swing.JTextField();
        valor4 = new javax.swing.JSpinner();
        parametro5 = new javax.swing.JTextField();
        valor5 = new javax.swing.JSpinner();
        parametro6 = new javax.swing.JTextField();
        valor6 = new javax.swing.JSpinner();
        parametro7 = new javax.swing.JTextField();
        valor7 = new javax.swing.JSpinner();
        parametro8 = new javax.swing.JTextField();
        valor8 = new javax.swing.JSpinner();
        parametro9 = new javax.swing.JTextField();
        valor9 = new javax.swing.JSpinner();
        parametro10 = new javax.swing.JTextField();
        valor10 = new javax.swing.JSpinner();
        localArquivo = new javax.swing.JTextField();
        parametro11 = new javax.swing.JTextField();
        valor11 = new javax.swing.JSpinner();
        listaKnob = new javax.swing.JComboBox();
        rtTpKnob = new javax.swing.JLabel();
        rtParametro = new javax.swing.JLabel();
        rtValor = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        corFundo = new javax.swing.JComboBox();
        corFonte = new javax.swing.JComboBox();
        rtCorFundo = new javax.swing.JLabel();
        rtCorFonte = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Stomp Box Graphic");
        rtNomeEfeito.setText("Nome do Efeito");
        nomeEfeito.setFont(new java.awt.Font("Tahoma", 0, 14));
        nomeEfeito.setText("nome");
        btGerar.setText("Gerar");
        btGerar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGerarActionPerformed(evt);
            }
        });
        parametro1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro1ActionPerformed(evt);
            }
        });
        valor1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro2ActionPerformed(evt);
            }
        });
        valor2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro3ActionPerformed(evt);
            }
        });
        valor3.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro4.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro4ActionPerformed(evt);
            }
        });
        valor4.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro5.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro5ActionPerformed(evt);
            }
        });
        valor5.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro6ActionPerformed(evt);
            }
        });
        valor6.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro7.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro7ActionPerformed(evt);
            }
        });
        valor7.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro8.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro8ActionPerformed(evt);
            }
        });
        valor8.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro9.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro9ActionPerformed(evt);
            }
        });
        valor9.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro10.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro10ActionPerformed(evt);
            }
        });
        valor10.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        parametro11.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parametro11ActionPerformed(evt);
            }
        });
        valor11.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        listaKnob.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Jazz Bass", "Boss", "Chicken Head", "Linear", "Boutique" }));
        listaKnob.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listaKnobActionPerformed(evt);
            }
        });
        rtTpKnob.setText("Tipo de Knob");
        rtParametro.setText("Parametro");
        rtValor.setText("Valor");
        jLabel1.setText("Local da geração do arquivo");
        corFundo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Preto", "Branco", "Cinza", "Azul", "Verde", "Vermelho", "Amarelo" }));
        corFundo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corFundoActionPerformed(evt);
            }
        });
        corFonte.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Preto", "Branco", "Cinza", "Azul", "Verde", "Vermelho", "Amarelo" }));
        corFonte.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corFonteActionPerformed(evt);
            }
        });
        rtCorFundo.setText("Cor Fundo");
        rtCorFonte.setText("Cor Fonte");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(nomeEfeito, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(localArquivo).addComponent(parametro11, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jLabel1)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(btGerar, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE).addComponent(valor11, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))).addComponent(parametro2, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(parametro3, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(parametro4, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor4, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro5, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor5, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro6, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor6, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro7, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor7, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro8, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor8, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro9, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor9, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(parametro10, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(valor10, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(parametro1, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE).addComponent(rtParametro)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(53, 53, 53).addComponent(rtValor)).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(valor2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE).addComponent(valor1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE).addComponent(valor3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))))).addGroup(layout.createSequentialGroup().addComponent(rtNomeEfeito).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 317, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(listaKnob, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(rtTpKnob)).addGap(12, 12, 12).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(corFundo, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(rtCorFundo)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(rtCorFonte).addComponent(corFonte, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(3, 3, 3))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(rtNomeEfeito).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(nomeEfeito, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(rtTpKnob).addComponent(rtCorFundo).addComponent(rtCorFonte)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(listaKnob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(corFonte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(corFundo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(rtParametro).addComponent(rtValor)).addGap(2, 2, 2).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parametro11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(valor11, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(11, 11, 11).addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE).addComponent(localArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(btGerar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        pack();
    }

    private void parametro11ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void listaKnobActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void corFundoActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void corFonteActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void btGerarActionPerformed(java.awt.event.ActionEvent evt) {
        String parametro[] = { parametro1.getText(), parametro2.getText(), parametro3.getText(), parametro4.getText(), parametro5.getText(), parametro6.getText(), parametro7.getText(), parametro8.getText(), parametro9.getText(), parametro10.getText(), parametro11.getText() };
        int valor[] = { Integer.parseInt(valor1.getValue().toString()), Integer.parseInt(valor2.getValue().toString()), Integer.parseInt(valor3.getValue().toString()), Integer.parseInt(valor4.getValue().toString()), Integer.parseInt(valor5.getValue().toString()), Integer.parseInt(valor6.getValue().toString()), Integer.parseInt(valor7.getValue().toString()), Integer.parseInt(valor8.getValue().toString()), Integer.parseInt(valor9.getValue().toString()), Integer.parseInt(valor10.getValue().toString()), Integer.parseInt(valor11.getValue().toString()) };
        Gerador gerador = new Gerador(parametro, valor, nomeEfeito.getText(), localArquivo.getText(), listaKnob.getSelectedIndex());
        JFrame frm = new JFrame(nomeEfeito.getText());
        JPanel pan = new JPanel();
        JLabel lbl = new JLabel(gerador.gerar());
        pan.add(lbl);
        frm.getContentPane().add(pan);
        frm.pack();
        frm.show();
    }

    private void parametro1ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro2ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro3ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro4ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro5ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro6ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro7ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro8ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro9ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void parametro10ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Form().setVisible(true);
            }
        });
    }

    private javax.swing.JButton btGerar;

    private javax.swing.JComboBox corFonte;

    private javax.swing.JComboBox corFundo;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JComboBox listaKnob;

    private javax.swing.JTextField localArquivo;

    private javax.swing.JTextField nomeEfeito;

    private javax.swing.JTextField parametro1;

    private javax.swing.JTextField parametro10;

    private javax.swing.JTextField parametro11;

    private javax.swing.JTextField parametro2;

    private javax.swing.JTextField parametro3;

    private javax.swing.JTextField parametro4;

    private javax.swing.JTextField parametro5;

    private javax.swing.JTextField parametro6;

    private javax.swing.JTextField parametro7;

    private javax.swing.JTextField parametro8;

    private javax.swing.JTextField parametro9;

    private javax.swing.JLabel rtCorFonte;

    private javax.swing.JLabel rtCorFundo;

    private javax.swing.JLabel rtNomeEfeito;

    private javax.swing.JLabel rtParametro;

    private javax.swing.JLabel rtTpKnob;

    private javax.swing.JLabel rtValor;

    private javax.swing.JSpinner valor1;

    private javax.swing.JSpinner valor10;

    private javax.swing.JSpinner valor11;

    private javax.swing.JSpinner valor2;

    private javax.swing.JSpinner valor3;

    private javax.swing.JSpinner valor4;

    private javax.swing.JSpinner valor5;

    private javax.swing.JSpinner valor6;

    private javax.swing.JSpinner valor7;

    private javax.swing.JSpinner valor8;

    private javax.swing.JSpinner valor9;
}