import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// ������ Ŭ������, ȭ�鿡 �ʿ��� �����͸� �����ϰ� ������Ʈ �ϴ� ���
public class MultiChatData {
	// ������ ������ �ʿ��� ������Ʈ
	private JTextArea msgOut;

	// �����͸� ������ �� ������Ʈ�� UI ������Ʈ�� ���
	public void addObj(JComponent comp) {
		this.msgOut = (JTextArea) comp;
	}

	// UI�� �����͸� ������Ʈ
	public void refreshData(String msg) {
		msgOut.append(msg);
	}
}
