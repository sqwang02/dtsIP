package softtest.registery;


import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import softtest.config.java.Config;

public class AuthenticationFrame extends JFrame
{
	private JLabel serverIPLabel;
	private JTextField serverIPTextField;
	private JButton enterIPButton;
	
	private JLabel cellAddrLabel;
	private Choice cellAddrChoice;
	private JButton enterAddrButton;
	
	
	private JLabel warningMessageLabel;
	
	public AuthenticationFrame()
	{	
		if (Config.LANGUAGE == 0) {
			serverIPLabel = new JLabel("IP地址：");
			serverIPTextField = new JTextField();
			enterIPButton = new JButton("输入IP地址");
			enterIPButton.setActionCommand("IP");
			serverIPTextField.setFocusable(true);

			cellAddrLabel = new JLabel("序列号：");
			cellAddrChoice = new Choice();
			enterAddrButton = new JButton("输入序列号");
			enterAddrButton.setActionCommand("ADDR");
			cellAddrChoice.setEnabled(false);
			enterAddrButton.setEnabled(false);
		}
		if (Config.LANGUAGE == 1) {
			serverIPLabel = new JLabel("IP address：");
			serverIPTextField = new JTextField();
			enterIPButton = new JButton("Enter IP address");
			enterIPButton.setActionCommand("IP");
			serverIPTextField.setFocusable(true);

			cellAddrLabel = new JLabel("Serial Number:");
			cellAddrChoice = new Choice();
			enterAddrButton = new JButton("Enter serial number");
			enterAddrButton.setActionCommand("ADDR");
			cellAddrChoice.setEnabled(false);
			enterAddrButton.setEnabled(false);
		}
		
//		warningMessageLabel = new JLabel("欢迎您的使用,请先输入网络锁的IP地址,然后点选序列号,来完成注册");
		warningMessageLabel = new JLabel();
		
		Container c = this.getContentPane();
	    GroupLayout layout = new GroupLayout(c);
	    c.setLayout(layout);

	    layout.setAutoCreateGaps(true);
	    layout.setAutoCreateContainerGaps(true);
	    
	    GroupLayout.ParallelGroup h1a = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
	    h1a.addComponent(serverIPLabel);
	    h1a.addComponent(cellAddrLabel);
	    
	    GroupLayout.ParallelGroup h1b = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
	    h1b.addComponent(serverIPTextField);
	    h1b.addComponent(cellAddrChoice);
	    
	    GroupLayout.ParallelGroup h1c = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
	    h1c.addComponent(enterIPButton);
	    h1c.addComponent(enterAddrButton);
	    
	    GroupLayout.SequentialGroup h1 = layout.createSequentialGroup();
	    h1.addGroup(h1a);
	    h1.addGroup(h1b);
	    h1.addGroup(h1c);
	    
	    GroupLayout.ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
	    h2.addGroup(h1);
	    h2.addComponent(warningMessageLabel);
	    
	    layout.setHorizontalGroup(h2);
	    
	    layout.linkSize(SwingConstants.HORIZONTAL,new Component[] { enterIPButton, enterAddrButton });
	    
	    GroupLayout.ParallelGroup v1 = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
	    v1.addComponent(serverIPLabel);
	    v1.addComponent(serverIPTextField);
	    v1.addComponent(enterIPButton);
	    
	    GroupLayout.ParallelGroup v2 = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
	    v2.addComponent(cellAddrLabel);
	    v2.addComponent(cellAddrChoice);
	    v2.addComponent(enterAddrButton);
	    
	    layout.setVerticalGroup(layout.createSequentialGroup().addGroup(v1).addGroup(v2).addComponent(warningMessageLabel));
	    
	    layout.linkSize(SwingConstants.VERTICAL,new Component[] { serverIPTextField, cellAddrChoice });
		
	    try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this); 	
		}
		catch (Exception ex){}
	    
		if (Config.LANGUAGE == 0)
			this.setTitle("DTSSoftware注册");
		if (Config.LANGUAGE == 1)
			this.setTitle("DTSSoftware Register");
	    this.setBounds(400, 200, 450, 130);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setVisible(true);

	}
	
	public String getIPField()
	{
		return serverIPTextField.getText().trim(); 
	}
	
	public String getAddrChoice()
	{
		return cellAddrChoice.getSelectedItem().trim();
	}
	
	public void setWarningMessage(String message)
	{
		warningMessageLabel.setText(message);
	}
	
	public void addAddressItem(int addr)
	{
		cellAddrChoice.add(Integer.toString(addr));
	}
	
	public void addAddressItem(String item)
	{
		cellAddrChoice.add(item);
	}
	
	public void setActionListener(ActionListener listener)
	{
		enterIPButton.addActionListener(listener);
		enterAddrButton.addActionListener(listener);
	}
	
	
	public void stateAfterIPInput()
	{
		serverIPTextField.setEnabled(false);
		enterIPButton.setEnabled(false);
		cellAddrChoice.setEnabled(true);
		enterAddrButton.setEnabled(true);
		warningMessageLabel.setText("");
	}
	
	
	public void stateAfterAddrInput()
	{
		serverIPTextField.setEnabled(false);
		enterIPButton.setEnabled(false);
		cellAddrChoice.setEnabled(false);
		enterAddrButton.setEnabled(false);
	}
	
	public static void main(String[] args)
	{
		AuthenticationFrame aFrame = new AuthenticationFrame();
	}

}
