class Globals {
  static boolean testPassed=false;
  static Thread mainThread=null;
}
public class MemoryLeakTest extends Applet {
public static void main(String args[]) throws Exception {
  new TestDialog(new Frame(), "MemoryLeakTest").start();
  new MemoryLeak().start();
  Globals.mainThread = Thread.currentThread();
  try {
    Thread.sleep(300000);
  } catch (InterruptedException e) {
    if (!Globals.testPassed)
      throw new Exception("MemoryLeakTest failed.");
  }
}
}
class TestDialog extends Dialog
    implements ActionListener {
TextArea output;
Button passButton;
Button failButton;
String name;
public TestDialog(Frame frame, String name)
{
  super(frame, name + " Pass/Fail Dialog");
  this.name = name;
  output = new TextArea(11, 50);
  add("North", output);
  output.append("Do the following steps on Solaris only.\n");
  output.append("Maximize and minimize the Memory Leak Test window.\n");
  output.append("Execute the following after minimize.\n");
  output.append("    ps -al | egrep -i 'java|PPID'\n");
  output.append("Examine the size of the process under SZ.\n");
  output.append("Maximize and minimize the Memory Leak Test window again.\n");
  output.append("Execute the following after minimize.\n");
  output.append("    ps -al | egrep -i 'java|PPID'\n");
  output.append("Examine the size of the process under SZ.\n");
  output.append("If the two SZ values are the same, plus or minus one,\n");
  output.append("then click Pass, else click Fail.");
  Panel buttonPanel = new Panel();
  passButton = new Button("Pass");
  failButton = new Button("Fail");
  passButton.addActionListener(this);
  failButton.addActionListener(this);
  buttonPanel.add(passButton);
  buttonPanel.add(failButton);
  add("South", buttonPanel);
  pack();
}
public void start()
{
  show();
}
public void actionPerformed(ActionEvent event)
{
    if ( event.getSource() == passButton ) {
      Globals.testPassed = true;
      System.err.println(name + " Passed.");
    }
    else if ( event.getSource() == failButton ) {
      Globals.testPassed = false;
      System.err.println(name + " Failed.");
    }
    this.dispose();
    if (Globals.mainThread != null)
      Globals.mainThread.interrupt();
}
}
class MemoryLeak extends Frame implements ComponentListener
{
private Image osImage;
public MemoryLeak()
{
    super("Memory Leak Test");
    setSize(200, 200);
    addComponentListener(this);
}
public static void main(String args[])
{
   new MemoryLeak().start();
}
public void start()
{
  show();
}
public void paint(Graphics g) {
    if (osImage != null) {
        g.drawImage(osImage, 0, 0, this);
    }
}
public void update(Graphics g)
{
    paint(g);
}
public void componentResized(ComponentEvent e)
{
    Image oldimage = osImage;
    osImage = createImage(getSize().width, getSize().height);
    Graphics g = osImage.getGraphics();
    if (oldimage != null) {
        g.drawImage(oldimage, 0, 0, getSize().width, getSize().height, this);
        oldimage.flush();
    } else {
        g.setColor(Color.blue);
        g.drawLine(0, 0, getSize().width, getSize().height);
    }
    g.dispose();
}
public void componentMoved(ComponentEvent e) {}
public void componentShown(ComponentEvent e)
{
    osImage = createImage(getSize().width, getSize().height);
    Graphics g = osImage.getGraphics();
    g.setColor(Color.blue);
    g.drawLine(0, 0, getSize().width, getSize().height);
    g.dispose();
}
public void componentHidden(ComponentEvent e) {}
}
