import java.net.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.*;
import com.google.gson.Gson;

public class MultiChatServer {
	// ���� ���� �� Ŭ���̾�Ʈ ���� ����
	private ServerSocket ss = null;
	private Socket s = null;

	// ����� Ŭ���̾�Ʈ �����带 �����ϴ� ArrayList
	private ArrayList<ChatThread> chatThreads = new ArrayList<>();

	// �ΰ� ��ü
	private Logger logger;

	// ��Ƽ ä�� ���� ���α׷� �κ�
	public void start() {
		logger = Logger.getLogger(this.getClass().getName());

		try {
			ss = new ServerSocket(8888);
			logger.info("MultiChatServer Start");

			// ���� ������ ���鼭 Ŭ���̾�Ʈ ������ ��ٸ���.
			while (true) {
				s = ss.accept();
				// ����� Ŭ���̾�Ʈ�� ���� ������ Ŭ���� ����
				ChatThread chat = new ChatThread(s);
				// Ŭ���̾�Ʈ ����Ʈ�� �߰�
				chatThreads.add(chat);
				chat.start();
			}
		} catch (Exception e) {
			logger.info("[MultiChatServer] start() Excetption �߻�!!");
			e.printStackTrace();
		}
	}

	class ChatThread extends Thread {
		private String id;
		private String msg;
		private Message m = new Message();
		private Gson gson = new Gson(); // JSON �ļ�
		// Sample Message {"id":"user1","msg":"hahaha","type":"msg"};
		private BufferedReader inMsg = null;
		private PrintWriter outMsg = null;
		private Socket socket = null;
		private boolean status;

		public ChatThread(Socket socket) {
			this.socket = socket;
			status = true;
			try {
				// ����� ��Ʈ�� ����
				inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Ŭ���̾�Ʈ�κ����� �Է� ��Ʈ��
				outMsg = new PrintWriter(socket.getOutputStream(), true); // Ŭ���̾�Ʈ���� ��� ��Ʈ��
			} catch (IOException e) {
				logger.info("[MultiChatServer] ChatThread() Exception �߻�!!");
				e.printStackTrace();
			}
			logger.info(this.getName() + " �����");
		}

		@Override
		public void run() {
			// ���� ������ true�̸� ������ ���鼭 ����ڿ��Լ� ���ŵ� �޽��� ó��
			try {
				while (status) {
					msg = inMsg.readLine(); // JSON ������ ���ڿ�
					m = gson.fromJson(msg, Message.class); // Message ��ü�� ����

					// �α׾ƿ� �޽����� ��
					if (m.getType().equals("logout")) {
						chatThreads.remove(this);
						msgSendAll(gson.toJson(new Message(m.getId(), "���� �����߽��ϴ�.", "server", "")));
						status = false;
						id = null;
					}
					// �α��� �޽����� ��
					else if (m.getType().equals("login")) {
						id = m.getId();
						msgSendAll(gson.toJson(new Message(m.getId(), "���� �α����߽��ϴ�.", "server", "")));
						logger.info(this.getName() + "(" + id + ") �α���!");
					}
					// �ӼӸ��� ��
					else if (m.getType().equals("secret")) {
						m.setMsg("[secret]" + m.getMsg());
						secretMsg(m.getRcvId(), gson.toJson(m));
					}
					// �� ���� ���, �� �Ϲ� �޽����� ��
					else {
						msgSendAll(msg);
					}
				}
			} catch (Exception e) {
				logger.info("[MultiChatServer] run() Exception �߻�!!");
				e.printStackTrace();
			} finally {
				logger.info(this.getName() + " �����!");
				try {
					socket.close();
					inMsg.close();
					outMsg.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}// run

		public synchronized void msgSendAll(String msg) { // broadcasting
			for (ChatThread ct : chatThreads) {
				ct.outMsg.println(msg);
			}
		}

		public void secretMsg(String rcvId, String msg) {
			for (ChatThread ct : chatThreads) {
				if (ct.id.equals(rcvId)) {
					ct.outMsg.println(msg);
					break;
				}
			}
			outMsg.println(msg);
		}
	}

	public static void main(String[] args) {
		new MultiChatServer().start();
	}
}
