import static java.util.logging.Level.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import com.google.gson.Gson;


// 서버와 통신, 클라이언트에서 발생하는 이벤트 처리
public class MultiChatController implements Runnable {
	// 뷰 클래스 참조 객체
	private final MultiChatUI v;
	// 모델 클래스 참조 객체
	private final MultiChatData chatData;

	// 메시지 파싱을 위한 객체 생성
	private Gson gson = new Gson(); // Sample Message {"id":"user1","msg":"hahaha","type":"msg","rcvId",""};
	private Message m;

	private final String ip = "localhost";
	private Socket socket = null;
	private BufferedReader inMsg = null;
	private PrintWriter outMsg = null;

	private boolean status;

	private Logger logger;

	private Thread thread; // 메시지 수신을 위한 스레드

	/**
	 * 모델과 뷰 객체를 파라미터로 하는 생성자
	 * 
	 * @param chatData
	 * @param v
	 */
	public MultiChatController(MultiChatUI v, MultiChatData chatData) {
		// 뷰와 모델 클래스 참조
		this.v = v;
		this.chatData = chatData;

		// 로거 객체 초기화
		logger = Logger.getLogger(this.getClass().getName());
	}

	// 어플리케이션 메인 실행 메서드
	public void appMain() {
		// 데이터 객체에서 데이터 변화를 처리할 UI 객체 추가
		chatData.addObj(v.msgOut);

		v.addButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object obj = e.getSource();

				if (obj == v.exitButton) {
					System.exit(0);
				} else if (obj == v.loginButton) {
					v.id = v.idInput.getText();
					v.outLabel.setText(" 대화명 : " + v.id);
					v.cardLayout.show(v.tab, "logout"); // 로그아웃 패널로 전환
					connectServer(); // 서버와 연결하고 로그인
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
			logger.log(INFO, "[Client]Server 연결 성공!!");

			inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outMsg = new PrintWriter(socket.getOutputStream(), true);

			// 서버에 로그인 메시지 전달
			m = new Message(v.id, "", "login", "");
			outMsg.println(gson.toJson(m));

			// 메시지 수신을 위한 스레드
			thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			logger.log(WARNING, "[MultiChatController]connectServer() Exception 발생!!");
			e.printStackTrace();
		}
	}

	// 메시지 수신을 독립적으로 처리하기 위한 스레드 실행
	@Override
	public void run() {
		String msg;
		status = true;

		while (status) {
			try {
				msg = inMsg.readLine(); // 서버로부터 메시지 수신
				m = gson.fromJson(msg, Message.class);

				if(Thread.interrupted()) {
					logger.info("[MultiChatController]" + thread.getName() + "로그 아웃으로 메시지 수신 스레드 종료됨!");
					return;
				}
				// MultiChatData 객체로 데이터 갱신
				chatData.refreshData(m.getId() + "> " + m.getMsg() + "\n");

				// 커서를 현재 대화 메시지에 표시
				v.msgOut.setCaretPosition(v.msgOut.getDocument().getLength());
			}
			catch (IOException e) {
				logger.log(WARNING, "[MultiChatController]메시지 스트림 종료!");
			}
		}
	}

	public static void main(String[] args) {
		new MultiChatController(new MultiChatUI(), new MultiChatData()).appMain();
	}
}
