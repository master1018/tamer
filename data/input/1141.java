public class SalvarConfiguracaoAction extends AbstractAction {
    public static final String KEY_IP = "IP";
    public static final String KEY_NICK = "NICK";
    public void actionPerformed(ActionEvent e) {
        JTextComponent ip = (JTextComponent) getValue(KEY_IP);
        JTextComponent nick = (JTextComponent) getValue(KEY_NICK);
        if (ip != null && nick != null) {
            SintoniaDoAmor.setConfiguracao(new ConfiguracaoVO(ip.getText(), nick.getText()));
        } else {
            throw new IllegalArgumentException("Configura��o inv�lida: ip=" + ip + ", nick=" + nick);
        }
    }
}
