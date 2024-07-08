package softtest.registery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import softtest.config.java.Config;

public class RegisteryClient extends JFrame {

	final JLabel promptLabel = new JLabel();

	final JLabel hardWareSNLabel = new JLabel();

	final JLabel softWareSNLabel = new JLabel();

	final JTextField hardWareSNTextField = new JTextField();

	final JTextField softWareSNTextField = new JTextField();

	final JButton regButton = new JButton();

	String hardWareSNSysStr = "";

	private static RegisteryClient current = null;

	public void launchFrame() {
		current = this;
		hardWareSNSysStr = SysInfo.getHardSN();

		getContentPane().setLayout(null);
		if (softtest.config.java.Config.LANGUAGE == 0) {
			if (Config.PHASE_REGISTER) {
				this.setTitle("���ע��(" + SysInfo.getPhase() + "/"
						+ Config.PHASE_NUMUBER + ")");
			} else {
				this.setTitle("���ע��");
			}
			setBounds(100, 100, 423, 196);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			promptLabel.setText("���������վ���ȡע���룺http://www.dtstesting.com");
			promptLabel.setBounds(10, 13, 366, 18);
			getContentPane().add(promptLabel);

			hardWareSNLabel.setText("���кţ�");
			hardWareSNLabel.setBounds(10, 43, 66, 18);
			getContentPane().add(hardWareSNLabel);

			softWareSNLabel.setText("ע���룺");
			softWareSNLabel.setBounds(10, 72, 66, 18);
			getContentPane().add(softWareSNLabel);

			hardWareSNTextField.setText(hardWareSNSysStr);
			hardWareSNTextField.setBounds(94, 41, 302, 22);
			hardWareSNTextField.setEditable(false);
			getContentPane().add(hardWareSNTextField);

			softWareSNTextField.setText("");
			softWareSNTextField.setBounds(94, 70, 302, 22);
			getContentPane().add(softWareSNTextField);
		} else {
			if (Config.PHASE_REGISTER) {
				this.setTitle("Software Register(" + SysInfo.getPhase() + "/"
						+ Config.PHASE_NUMUBER + ")");
			} else {
				this.setTitle("Software Register");
			}
			setBounds(100, 100, 526, 196);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			promptLabel
					.setText("Please visit the following website to obtain registration code��http://www.dtstesting.com");
			promptLabel.setBounds(10, 13, 506, 18);
			getContentPane().add(promptLabel);

			hardWareSNLabel.setText("Serial Number��");
			hardWareSNLabel.setBounds(10, 43, 116, 18);
			getContentPane().add(hardWareSNLabel);

			softWareSNLabel.setText("Registration Code��");
			softWareSNLabel.setBounds(10, 72, 116, 18);
			getContentPane().add(softWareSNLabel);

			hardWareSNTextField.setText(hardWareSNSysStr);
			hardWareSNTextField.setBounds(124, 41, 302, 22);
			hardWareSNTextField.setEditable(false);
			getContentPane().add(hardWareSNTextField);

			softWareSNTextField.setText("");
			softWareSNTextField.setBounds(124, 70, 302, 22);
			getContentPane().add(softWareSNTextField);
		}

		regButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				String hardWareSNStr = hardWareSNTextField.getText();

				String softWareSNStr = softWareSNTextField.getText();
				String softWareSNSysStr = Encrypt
						.encryptHardInfo(hardWareSNSysStr);
				boolean rightRe=(null!=softWareSNSysStr&&
                        null!=softWareSNStr&&
                        softWareSNStr.equals(softWareSNSysStr));
	            SuccessRe.setR(rightRe);
	            if(rightRe){
					// System.out.println("ע��ɹ�!");
					Registery.writeValue(Config.version, hardWareSNStr,
							softWareSNStr);
					if (softtest.config.java.Config.LANGUAGE == 0) {
						JOptionPane.showMessageDialog(null, "ע��ɹ�,��л����ʹ�ã�");
					} else {
						JOptionPane
								.showMessageDialog(null,
										"Registered successfully, thanks for your use��");
					}
					RegisteryClient.current.dispose();
				} else {
					// System.out.println("ע��ʧ�ܣ��빺������!");
					if (softtest.config.java.Config.LANGUAGE == 0) {
						JOptionPane.showMessageDialog(null, "ע��ʧ�ܣ��빺�����棡",
								"���ע��", JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane
								.showMessageDialog(
										null,
										"Registered failure, please purchase the legal copy!",
										"Software Register",
										JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});
		if (softtest.config.java.Config.LANGUAGE == 0) {
			regButton.setText("ע��");
		} else {
			regButton.setText("Register");
		}
		regButton.setBounds(129, 107, 106, 28);
		getContentPane().add(regButton);

	} 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (Config.PHASE_REGISTER) {
			if (SysInfo.getPhase() < 0) {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					JOptionPane.showMessageDialog(null, "������������");
				} else {
					JOptionPane.showMessageDialog(null,
							"Please insert the encryption lock��");
				}
				return;
			}
		}
		RegisteryClient client = new RegisteryClient();
		client.launchFrame();
		client.setVisible(true);
	}

}
