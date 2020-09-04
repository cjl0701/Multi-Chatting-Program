import static java.util.logging.Level.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import com.google.gson.Gson;


// ������ ���, Ŭ���̾�Ʈ���� �߻��ϴ� �̺�Ʈ ó��
public class MultiChatController implements Runnable {
	// �� Ŭ���� ���� ��ü
	private final MultiChatUI v;
	// �� Ŭ���� ���� ��ü
	private final MultiChatData chatData;

	// �޽��� �Ľ��� ���� ��ü ����
	private Gson gson = new Gson(); // Sample Message {"id":"user1","msg":"hahaha","type":"msg","rcvId",""};
	private Message m;

	private final String ip = "localhost";
	private Socket socket = null;
	private BufferedReader inMsg = null;
	private PrintWriter outMsg = null;

	private boolean status;

	private Logger logger;

	private Thread thread; // �޽��� ������ ���� ������

	/**
	 * �𵨰� �� ��ü�� �Ķ���ͷ� �ϴ� ������
	 * 
	 * @param chatData
	 * @param v
	 */
	public MultiChatController(MultiChatUI v, MultiChatData chatData) {
		// ��� �� Ŭ���� ����
		this.v = v;
		this.chatData = chatData;

		// �ΰ� ��ü �ʱ�ȭ
		logger = Logger.getLogger(this.getClass().getName());
	}

	// ���ø����̼� ���� ���� �޼���
	public void appMain() {
		// ������ ��ü���� ������ ��ȭ�� ó���� UI ��ü �߰�
		chatData.addObj(v.msgOut);

		v.addButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object obj = e.getSource();

				if (obj == v.exitButton) {
					System.exit(0);
				} else if (obj == v.loginButton) {
					v.id = v.idInput.getText();
					v.outLabel.setText(" ��ȭ�� : " + v.id);
					v.cardLayout.show(v.tab, "logout"); // �α׾ƿ� �гη� ��ȯ
					connectServer(); // ������ �����ϰ� �α���
				} else if (obj == v.logoutButton) {
					outMsg.println(gson.toJson(new Message(v.id, "", "logout","")));
					thread.interrupt();
					v.msgOut.setText("");
					v.cardLayout.show(v.tab, "login");					
					outMsg.close();
					try {
						inMsg.close();
						socket.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					status = false;
				} else if (obj == v.msgInput) {
					Message sendMsg = new Message(v.id, v.msgInput.getText(), "msg","");
					if (v.msgInput.getText().charAt(0) == '/') {
						sendMsg.setType("secret");
						StringTokenizer st = new StringTokenizer(v.msgInput.getText());
						String rcvId = st.nextToken(); // /id 
						sendMsg.setRcvId(rcvId.substring(1));
						sendMsg.setMsg(v.msgInput.getText().substring(rcvId.length()+1));
					}
					outMsg.println(gson.toJson(sendMsg));
					v.msgInput.setText("");
				}
			}
		});
	} // appMain()

	public void connectServer() {
		try {
			socket = new Socket(ip, 8888);
			logger.log(INFO, "[Client]Server ���� ����!!");

			inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outMsg = new PrintWriter(socket.getOutputStream(), true);

			// ������ �α��� �޽��� ����
			m = new Message(v.id, "", "login", "");
			outMsg.println(gson.toJson(m));

			// �޽��� ������ ���� ������
			thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			logger.log(WARNING, "[MultiChatController]connectServer() Exception �߻�!!");
			e.printStackTrace();
		}
	}

	// �޽��� ������ ���������� ó���ϱ� ���� ������ ����
	@Override
	public void run() {
		String msg;
		status = true;

		while (status) {
			try {
				msg = inMsg.readLine(); // �����κ��� �޽��� ����
				m = gson.fromJson(msg, Message.class);

				if(Thread.interrupted()) {
					logger.info("[MultiChatController]" + thread.getName() + "�α� �ƿ����� �޽��� ���� ������ �����!");
					return;
				}
				// MultiChatData ��ü�� ������ ����
				chatData.refreshData(m.getId() + "> " + m.getMsg() + "\n");

				// Ŀ���� ���� ��ȭ �޽����� ǥ��
				v.msgOut.setCaretPosition(v.msgOut.getDocument().getLength());
			}
			catch (IOException e) {
				logger.log(WARNING, "[MultiChatController]�޽��� ��Ʈ�� ����!");
			}
		}
	}

	public static void main(String[] args) {
		new MultiChatController(new MultiChatUI(), new MultiChatData()).appMain();
	}
}
