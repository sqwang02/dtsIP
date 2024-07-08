package softtest.registery.file;

import javax.swing.JOptionPane;

public class RegTest {
	public static void main(String[] args) {
		int result = Register.verify();

		if (result == Register.UNREGISTERED) {
			RegViewer.launch();
			return;
		}

		if (result == Register.ERROR) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(null, "用户操作不当引起错误", "ERROR!",
						JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null,
						"ERROR caused by improper operations", "ERROR!",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		System.out.println("start DTS");
	}
}
