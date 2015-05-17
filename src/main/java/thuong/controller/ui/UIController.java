package thuong.controller.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JTextArea;

import thuong.controller.device.TTDeviceManager;
import thuong.controller.device.TTHostDevice;
import thuong.controller.module.TTSendPacketThread;
import thuong.packet.struct.TTBasePacketCustom;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;

public class UIController extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					UIController frame = new UIController();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	public JTextArea textLog = new JTextArea();
	public JLabel numberHost;
	
	
	public TTSendPacketThread sendPacketThread;
	private JTextField sendString;
	private JTextField sendHost;

	/**
	 * Create the frame.
	 */
	public UIController(TTSendPacketThread sendPacketThread) {
		this.sendPacketThread = sendPacketThread;
		setTitle("Openflow Controller");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textLog.setBounds(28, 162, 265, 87);
		contentPane.add(textLog);
		
		sendString = new JTextField();
		sendString.setBounds(193, 128, 100, 19);
		contentPane.add(sendString);
		sendString.setColumns(10);
		
		JButton sendStringBtn = new JButton("SendString");
		sendStringBtn.setBackground(Color.ORANGE);
		sendStringBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendStringToHost();
			}
		});
		sendStringBtn.setBounds(305, 125, 131, 25);
		contentPane.add(sendStringBtn);
		
		JLabel lblString = new JLabel("String");
		lblString.setBounds(223, 109, 70, 15);
		contentPane.add(lblString);
		
		numberHost = new JLabel("0");
		numberHost.setBackground(Color.RED);
		numberHost.setBounds(392, 12, 24, 15);
		contentPane.add(numberHost);
		
		JLabel lblHost = new JLabel("Host");
		lblHost.setBounds(342, 12, 38, 15);
		contentPane.add(lblHost);
		
		sendHost = new JTextField();
		sendHost.setBounds(123, 128, 38, 19);
		contentPane.add(sendHost);
		sendHost.setColumns(10);
		
		JLabel lblHost_1 = new JLabel("HostID");
		lblHost_1.setBounds(123, 109, 47, 15);
		contentPane.add(lblHost_1);
		
		JButton getInforBtn = new JButton("GetInfor");
		getInforBtn.setBackground(Color.ORANGE);
		getInforBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getInfrorHost();
			}
		});
		getInforBtn.setBounds(324, 32, 100, 25);
		contentPane.add(getInforBtn);
		
		JButton stopHostBtn = new JButton("StopAllHost");
		stopHostBtn.setBackground(Color.ORANGE);
		stopHostBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopAllHost();
			}
		});
		stopHostBtn.setBounds(305, 224, 131, 25);
		contentPane.add(stopHostBtn);
		
		JLabel lblBiuKhin = new JLabel("    Bộ Điều Khiển Sử Dụng Openflow");
		lblBiuKhin.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 13));
		lblBiuKhin.setBackground(Color.RED);
		lblBiuKhin.setBounds(28, 12, 278, 85);
		contentPane.add(lblBiuKhin);
	}
	
	//Gui yeu cau Lay infor toi cac host
	public void getInfrorHost(){
		for(int i = 0; i< TTDeviceManager.listDevice.size(); i++){
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(TTBasePacketCustom.PACKET_TYPE_GET_INFOR, "".getBytes()), TTDeviceManager.listDevice.get(i));
		}
	}
	
	//Gui string den host
	public void sendStringToHost(){
		String text = sendString.getText();
		for(int i = 0; i< TTDeviceManager.listDevice.size(); i++){
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(TTBasePacketCustom.PACKET_TYPE_SEND_STRING, text.getBytes()), TTDeviceManager.listDevice.get(i));
		}
	}
	
	//gui lenh stop den tat ca server
	public void stopAllHost(){
		for(int i = 0; i< TTDeviceManager.listDevice.size(); i++){
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(TTBasePacketCustom.PACKET_TYPE_STOP_SERVER, "".getBytes()), TTDeviceManager.listDevice.get(i));
		}
	}
	
	public void newHostConnect(){
		numberHost.setText("" + TTDeviceManager.listDevice.size());
	}
}
