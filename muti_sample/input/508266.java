public class AppletTest extends JApplet { 
    public void init() { 
        setLayout(null); 
        JButton button = new JButton("button"); 
        JTextField textField = new JTextField("TextField",20); 
        button.setBounds(10,10,100,30); 
        textField.setBounds(10,40,100,40); 
        add(button); 
        add(textField); 
    }    
}
