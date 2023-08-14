public class test {
    private JPasswordField getJpPassword() {
        if (jpPassword == null) {
            jpPassword = new JPasswordField(Config.getConfig().password);
            jpPassword.setPreferredSize(new Dimension(150, 20));
            jpPassword.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    if (validateSettings()) {
                        byte[] data = new String(jpPassword.getPassword()).getBytes();
                        if (data[0] == '$' && data[1] == '1' && data[2] == '$') {
                            Config.getConfig().password = new String(data);
                        } else {
                            try {
                                MessageDigest algorithm = MessageDigest.getInstance("MD5");
                                algorithm.reset();
                                algorithm.update(data);
                                byte messageDigest[] = algorithm.digest();
                                StringBuffer hexString = new StringBuffer();
                                for (int i = 0; i < messageDigest.length; i++) {
                                    String s = Integer.toHexString(0xFF & messageDigest[i]);
                                    if (s.length() <= 1) s = "0" + s;
                                    hexString.append(s);
                                }
                                Config.getConfig().password = "$1$" + hexString.toString();
                            } catch (NoSuchAlgorithmException ex) {
                                getJlPassword().setForeground(Color.RED);
                            }
                        }
                    }
                }
            });
            jpPassword.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    getJpPassword().selectAll();
                }
            });
            jpPassword.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        doLogin();
                    }
                }
            });
        }
        return jpPassword;
    }
}
