package softtest.registery.file;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class RegViewer implements ActionListener {
	private static RegViewer viewer; // ???

	private JFileChooser fileChooser;

	private ExtensionFileFilter filter;

	private RegFrame rFrame;

	static {
		try {
			// javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			javax.swing.UIManager.setLookAndFeel(UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

	}

	public static void launch() {
		viewer = new RegViewer(); // ???
	}

	private RegViewer() {
		filter = new ExtensionFileFilter();
		filter.addExtension("dts");
		if (softtest.config.java.Config.LANGUAGE == 0) {
			filter.setDescription("DTS��Ȩ�ļ�");
		} else {
			filter.setDescription("DTS Lincense ");
		}

		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView()
				.getDefaultDirectory());
		// FileSystemView::getHomeDirectory() ��ȡ����·���ķ���
		// FileSystemView::getDefaultDirectory() ��ȡ�ҵ��ĵ�·���ķ���

		rFrame = new RegFrame();
		String sn = Register.generateSN();
		if (sn == null) // ����߼��ᷢ��ô
		{
			if (softtest.config.java.Config.LANGUAGE == 0) {
				sn = "�û�����������������޷��������кţ�";
			} else {
				sn = "ERROR caused by improper operations and CANNOT generate the serial number!";
			}
		}
		rFrame.setSN(sn);
		rFrame.setActionListener(this);

		rFrame.setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		int select = fileChooser.showOpenDialog(rFrame);
		if (select == JFileChooser.APPROVE_OPTION) {
			File chooser = fileChooser.getSelectedFile();
			doRegister(chooser);
		}

	}

	private void doRegister(File chooser) {
		boolean result = Register.login(chooser);

		if (result) {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(rFrame, "ע��ɹ�", "SUCCESS!",
						JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(rFrame, "Register Successfully",
						"SUCCESS!", JOptionPane.PLAIN_MESSAGE);
			}
		} else {
			if (softtest.config.java.Config.LANGUAGE == 0) {
				JOptionPane.showMessageDialog(rFrame, "ע��ʧ��", "ERROR!",
						JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(rFrame, "Register Failed",
						"ERROR!", JOptionPane.ERROR_MESSAGE);
			}
		}

		rFrame.dispose();
	}

}
