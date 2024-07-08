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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ResetFrame extends JFrame {
	private JLabel requestLabel;

	private JTextField requestFiled;

	private JLabel respondLabel;

	private JTextField respondFiled;

	private JLabel space1;

	private JLabel space2;

	private JLabel message1;

	// private JLabel message2;

	private JButton enter;

	public ResetFrame() {
		if (softtest.config.java.Config.LANGUAGE == 0) {
			requestLabel = new JLabel("重置请求码：");
			requestFiled = new JTextField();
			requestFiled.setEditable(false);

			respondLabel = new JLabel("重置响应码：");
			respondFiled = new JTextField();
			respondFiled.setFocusable(true);

			space1 = new JLabel("");
			space2 = new JLabel("");

			message1 = new JLabel("---用户操作不当引起错误   请拨打电话010-62246981联系解决---");

			enter = new JButton("重置");
			enter.setActionCommand("enter");
		} else {
			requestLabel = new JLabel("Reset request code: ");
			requestFiled = new JTextField();
			requestFiled.setEditable(false);

			respondLabel = new JLabel("Reset response code:");
			respondFiled = new JTextField();
			respondFiled.setFocusable(true);

			space1 = new JLabel("");
			space2 = new JLabel("");

			message1 = new JLabel(
					"ERROR caused by improper operations.Please call 010-62246981 to slove.");

			enter = new JButton("Reset");
			enter.setActionCommand("enter");
		}

		Container c = this.getContentPane();
		GroupLayout layout = new GroupLayout(c);
		c.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup h1a = layout.createSequentialGroup();
		h1a.addComponent(requestLabel);
		h1a.addComponent(requestFiled);

		GroupLayout.SequentialGroup h1b = layout.createSequentialGroup();
		h1b.addComponent(respondLabel);
		h1b.addComponent(respondFiled);

		GroupLayout.ParallelGroup h1 = layout
				.createParallelGroup(GroupLayout.Alignment.CENTER);
		h1.addComponent(space1);
		h1.addGroup(h1a);
		h1.addGroup(h1b);
		h1.addComponent(space2);
		h1.addComponent(message1);

		GroupLayout.ParallelGroup h = layout
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h.addGroup(h1);
		h.addComponent(enter);

		layout.setHorizontalGroup(h);

		GroupLayout.ParallelGroup v1 = layout
				.createParallelGroup(GroupLayout.Alignment.CENTER);
		v1.addComponent(requestLabel);
		v1.addComponent(requestFiled);

		GroupLayout.ParallelGroup v2 = layout
				.createParallelGroup(GroupLayout.Alignment.CENTER);
		v2.addComponent(respondLabel);
		v2.addComponent(respondFiled);

		GroupLayout.SequentialGroup v3 = layout.createSequentialGroup();
		v3.addComponent(space1);
		v3.addGroup(v1);
		v3.addGroup(v2);
		v3.addComponent(space2);
		v3.addComponent(message1);
		v3.addComponent(enter);

		layout.setVerticalGroup(v3);

		layout.linkSize(SwingConstants.VERTICAL, new Component[] {
				requestFiled, respondFiled });

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ex) {
		}

		if (softtest.config.java.Config.LANGUAGE == 0) {
			this.setTitle("错误提示");
		} else {
			this.setTitle("Error Info");			
		}
		this.setBounds(400, 200, 450, 165);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	public void addActionLisener(ActionListener listener) {

		enter.addActionListener(listener);
	}

	public String getRespondField() {
		return respondFiled.getText().trim();
	}

	public void setReqField(String meg) {
		requestFiled.setText(meg);
	}

}
