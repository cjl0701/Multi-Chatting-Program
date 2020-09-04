import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// 데이터 클래스로, 화면에 필요한 데이터를 제공하고 업데이트 하는 기능
public class MultiChatData {
	// 데이터 변경이 필요한 컴포넌트
	private JTextArea msgOut;

	// 데이터를 변경할 때 업데이트할 UI 컴포넌트를 등록
	public void addObj(JComponent comp) {
		this.msgOut = (JTextArea) comp;
	}

	// UI에 데이터를 업데이트
	public void refreshData(String msg) {
		msgOut.append(msg);
	}
}
