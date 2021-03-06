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
import javax.swing.JComboBox;

public class UIController extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// UIController frame = new UIController();
	// frame.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// }

	public JTextArea textLog = new JTextArea();
	public JLabel numberHost;
	public JComboBox hostComboBox;

	public TTSendPacketThread sendPacketThread;
	private JTextField sendString;

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

		textLog.setBounds(171, 174, 265, 87);
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
		lblString.setBounds(136, 130, 70, 15);
		contentPane.add(lblString);

		numberHost = new JLabel("0");
		numberHost.setBackground(Color.RED);
		numberHost.setBounds(392, 12, 24, 15);
		contentPane.add(numberHost);

		JLabel lblHost = new JLabel("Host");
		lblHost.setBounds(342, 12, 38, 15);
		contentPane.add(lblHost);

		JLabel lblHost_1 = new JLabel("Choose Host");
		lblHost_1.setBounds(12, 93, 90, 15);
		contentPane.add(lblHost_1);

		JButton getInforBtn = new JButton("GetInfor");
		getInforBtn.setBackground(Color.ORANGE);
		getInforBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getInfrorHost();
			}
		});
		getInforBtn.setBounds(305, 88, 131, 25);
		contentPane.add(getInforBtn);

		JButton stopHostBtn = new JButton("StopHost");
		stopHostBtn.setBackground(Color.ORANGE);
		stopHostBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopAllHost();
			}
		});
		stopHostBtn.setBounds(305, 51, 131, 25);
		contentPane.add(stopHostBtn);

		JLabel lblBiuKhin = new JLabel("    Bộ Điều Khiển Sử Dụng Openflow");
		lblBiuKhin.setForeground(Color.RED);
		lblBiuKhin.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 13));
		lblBiuKhin.setBackground(Color.RED);
		lblBiuKhin.setBounds(28, 0, 278, 85);
		contentPane.add(lblBiuKhin);

		hostComboBox = new JComboBox();
		hostComboBox.setBounds(113, 72, 80, 46);
		hostComboBox.addItem("All Host");
		contentPane.add(hostComboBox);

		JLabel lblLogController = new JLabel("Log Controller");
		lblLogController.setBounds(28, 194, 110, 39);
		contentPane.add(lblLogController);
	}

	// Gui yeu cau Lay infor toi cac host
	public void getInfrorHost() {

		if (hostComboBox.getSelectedIndex() == 0) {
			for (int i = 0; i < TTDeviceManager.listDevice.size(); i++) {
				TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
				sendPacketThread.addQueue(new TTBasePacketCustom(
						TTBasePacketCustom.PACKET_TYPE_GET_INFOR, "".getBytes()),
						TTDeviceManager.listDevice.get(i));
			}
			return;
		}
		int i = hostComboBox.getSelectedIndex() - 1;
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(
				TTBasePacketCustom.PACKET_TYPE_GET_INFOR, "".getBytes()),
				TTDeviceManager.listDevice.get(i));
	}

	// Gui string den host
	public void sendStringToHost() {
		String text = sendString.getText();

		if (hostComboBox.getSelectedIndex() == 0) {
			for (int i = 0; i < TTDeviceManager.listDevice.size(); i++) {
				TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
				sendPacketThread.addQueue(
						new TTBasePacketCustom(
								TTBasePacketCustom.PACKET_TYPE_SEND_STRING,
								(text).getBytes()), TTDeviceManager.listDevice
								.get(i));
			}
			return;
		}
		int i = hostComboBox.getSelectedIndex() - 1;
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(
				TTBasePacketCustom.PACKET_TYPE_SEND_STRING, (text).getBytes()),
				TTDeviceManager.listDevice.get(i));
	}

	// gui lenh stop den tat ca server
	public void stopAllHost() {

		if (hostComboBox.getSelectedIndex() == 0) {
			for (int i = 0; i < TTDeviceManager.listDevice.size(); i++) {
				TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
				sendPacketThread.addQueue(
						new TTBasePacketCustom(
								TTBasePacketCustom.PACKET_TYPE_STOP_SERVER, ""
										.getBytes()),
						TTDeviceManager.listDevice.get(i));
			}
			TTDeviceManager.listDevice.clear();
			hostComboBox.removeAllItems();
			hostComboBox.addItem("All Host");
			numberHost.setText("0");
			return;
		}
		
		int i = hostComboBox.getSelectedIndex() - 1;
		TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
		sendPacketThread.addQueue(new TTBasePacketCustom(
				TTBasePacketCustom.PACKET_TYPE_STOP_SERVER, "".getBytes()),
				TTDeviceManager.listDevice.get(i));
		TTDeviceManager.listDevice.remove(i);
		hostComboBox.removeItemAt(i+1);
		numberHost.setText("" + TTDeviceManager.listDevice.size());
	}

	public void newHostConnect() {
		numberHost.setText("" + TTDeviceManager.listDevice.size());
		
		hostComboBox.addItem("Host " + TTDeviceManager.listDevice.size());

		textLog.setText("Host:  " + TTDeviceManager.listDevice.size() + "   vua connect");
	}
}
