import java.net.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.*;
import com.google.gson.Gson;

public class MultiChatServer {
	// 서버 소켓 및 클라이언트 연결 소켓
	private ServerSocket ss = null;
	private Socket s = null;

	// 연결된 클라이언트 스레드를 관리하는 ArrayList
	private ArrayList<ChatThread> chatThreads = new ArrayList<>();

	// 로거 객체
	private Logger logger;

	// 멀티 채팅 메인 프로그램 부분
	public void start() {
		logger = Logger.getLogger(this.getClass().getName());

		try {
			ss = new ServerSocket(8888);
			logger.info("MultiChatServer Start");

			// 무한 루프롤 돌면서 클라이언트 연결을 기다린다.
			while (true) {
				s = ss.accept();
				// 연결된 클라이언트에 대해 스레드 클래스 생성
				ChatThread chat = new ChatThread(s);
				// 클라이언트 리스트에 추가
				chatThreads.add(chat);
				chat.start();
			}
		} catch (Exception e) {
			logger.info("[MultiChatServer] start() Excetption 발생!!");
			e.printStackTrace();
		}
	}

	class ChatThread extends Thread {
		private String id;
		private String msg;
		private Message m = new Message();
		private Gson gson = new Gson(); // JSON 파서
		// Sample Message {"id":"user1","msg":"hahaha","type":"msg"};
		private BufferedReader inMsg = null;
		private PrintWriter outMsg = null;
		private Socket socket = null;
		private boolean status;

		public ChatThread(Socket socket) {
			this.socket = socket;
			status = true;
			try {
				// 입출력 스트림 생성
				inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 클라이언트로부터의 입력 스트림
				outMsg = new PrintWriter(socket.getOutputStream(), true); // 클라이언트로의 출력 스트림
			} catch (IOException e) {
				logger.info("[MultiChatServer] ChatThread() Exception 발생!!");
				e.printStackTrace();
			}
			logger.info(this.getName() + " 연결됨");
		}

		@Override
		public void run() {
			// 상태 정보가 true이면 루프를 돌면서 사용자에게서 수신된 메시지 처리
			try {
				while (status) {
					msg = inMsg.readLine(); // JSON 형태의 문자열
					m = gson.fromJson(msg, Message.class); // Message 객체로 매핑

					// 로그아웃 메시지일 때
					if (m.getType().equals("logout")) {
						chatThreads.remove(this);
						msgSendAll(gson.toJson(new Message(m.getId(), "님이 종료했습니다.", "server", "")));
						status = false;
						id = null;
					}
					// 로그인 메시지일 때
					else if (m.getType().equals("login")) {
						id = m.getId();
						msgSendAll(gson.toJson(new Message(m.getId(), "님이 로그인했습니다.", "server", "")));
						logger.info(this.getName() + "(" + id + ") 로그인!");
					}
					// 귓속말일 때
					else if (m.getType().equals("secret")) {
						m.setMsg("[secret]" + m.getMsg());
						secretMsg(m.getRcvId(), gson.toJson(m));
					}
					// 그 밖의 경우, 즉 일반 메시지일 때
					else {
						msgSendAll(msg);
					}
				}
			} catch (Exception e) {
				logger.info("[MultiChatServer] run() Exception 발생!!");
				e.printStackTrace();
			} finally {
				logger.info(this.getName() + " 종료됨!");
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
