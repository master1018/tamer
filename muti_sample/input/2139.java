public class bug7036148 extends JFrame {
    public bug7036148() {
        JMenuBar bar = new JMenuBar();
        Action menuAction = new AbstractAction(null, null){
            public void actionPerformed(ActionEvent e) {
            }
        };
        JMenu menu = new JMenu(menuAction);
        menu.add(new JMenuItem("test"));
        bar.add(menu);
        setJMenuBar(bar);
        pack();
    }
       public static void main(String[] args) {
            new bug7036148();
       }
}
