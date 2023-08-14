public class MacProgressBar extends ProgressBar {
	private JTextField component;
	public MacProgressBar(JTextField component) {
		super();
		this.component= component;
	 }
	protected void updateBarColor() {
		component.setBackground(getStatusColor());
	}
}
