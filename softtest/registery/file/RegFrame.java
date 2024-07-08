package softtest.registery.file;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class RegFrame extends JFrame {
	private JLabel serialNumLabel;

	private JTextField serialNumField;

	private JLabel messageLabel;

	private JLabel space1;

	private JLabel space2;

	private JLabel messageLabel2;

	private JButton enterLicenseButton;

	public RegFrame() {
		if (softtest.config.java.Config.LANGUAGE == 0) {
			serialNumLabel = new JLabel("序列号 ");
			serialNumField = new JTextField();
			serialNumField.setEditable(false);
			enterLicenseButton = new JButton("输入授权文件");
			enterLicenseButton.setActionCommand("LICENSE");
			messageLabel = new JLabel("请先登录www.dtstesting.com完成产品注册");
			space1 = new JLabel("");
			space2 = new JLabel("");
			messageLabel2 = new JLabel("我们会给注册成功的产品发放授权文件");
		} else {
			serialNumLabel = new JLabel("Serial Number ");
			serialNumField = new JTextField();
			serialNumField.setEditable(false);
			enterLicenseButton = new JButton("Import License");
			enterLicenseButton.setActionCommand("LICENSE");
			messageLabel = new JLabel(
					"Please login www.dtstesting.com to complete the registration.");
			space1 = new JLabel("");
			space2 = new JLabel("");
			messageLabel2 = new JLabel(
					"We will offer a license to the successfully registered product.");
		}

		Container c = this.getContentPane();
		GroupLayout layout = new GroupLayout(c);
		c.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup hpg1 = layout.createSequentialGroup();
		hpg1.addComponent(serialNumLabel);
		hpg1.addComponent(serialNumField);

		GroupLayout.ParallelGroup hpg2 = layout
				.createParallelGroup(GroupLayout.Alignment.CENTER);
		hpg2.addComponent(space1);
		hpg2.addGroup(hpg1);
		hpg2.addComponent(space2);
		hpg2.addComponent(messageLabel);
		hpg2.addComponent(messageLabel2);

		GroupLayout.ParallelGroup hpg = layout
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		hpg.addGroup(hpg2);
		hpg.addComponent(enterLicenseButton);

		layout.setHorizontalGroup(hpg);

		GroupLayout.ParallelGroup vpg1 = layout
				.createParallelGroup(GroupLayout.Alignment.CENTER);
		vpg1.addComponent(serialNumLabel);
		vpg1.addComponent(serialNumField);

		GroupLayout.SequentialGroup vpg = layout.createSequentialGroup();
		vpg.addComponent(space1);
		vpg.addGroup(vpg1);
		vpg.addComponent(space2);
		vpg.addComponent(messageLabel);
		vpg.addComponent(messageLabel2);
		vpg.addComponent(enterLicenseButton);

		layout.linkSize(SwingConstants.VERTICAL, new Component[] {
				serialNumLabel, serialNumField });

		layout.setVerticalGroup(vpg);

		if (softtest.config.java.Config.LANGUAGE == 0) {
			this.setTitle("DTS注册");
		} else {
			this.setTitle("DTS Register");
		}
		this.setBounds(400, 200, 490, 155);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		// this.setVisible(true);

	}

	public void setActionListener(ActionListener listener) {
		enterLicenseButton.addActionListener(listener);

	}

	public void setSN(String sn) {
		serialNumField.setText(sn);
	}

}
