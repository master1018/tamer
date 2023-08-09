public class MemoryViewer extends JPanel {
  public MemoryViewer(final Debugger debugger, boolean is64Bit) {
    super();
    final MemoryPanel memory = new MemoryPanel(debugger, is64Bit);
    memory.setBorder(GraphicsUtilities.newBorder(5));
    JPanel addressPanel = new JPanel();
    addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));
    addressPanel.add(new JLabel("Address: "));
    final JTextField addressField = new JTextField(20);
    addressPanel.add(addressField);
    addressField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            memory.makeVisible(debugger.parseAddress(addressField.getText()));
          } catch (NumberFormatException ex) {
          }
        }
      });
    setLayout(new BorderLayout());
    add(addressPanel, BorderLayout.NORTH);
    add(memory, BorderLayout.CENTER);
  }
}
