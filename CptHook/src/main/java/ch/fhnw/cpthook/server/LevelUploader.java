package ch.fhnw.cpthook.server;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

public class LevelUploader extends JFrame {

	private ServerApi api = new ServerApi();
	private JTextField nameTextField = new JTextField();
	private JTextField authorTextField = new JTextField();
	private JButton loadButton = new JButton("save");
	private JButton cancelButton = new JButton("cancel");
	
	private String data;
	
	public LevelUploader(String data, CountDownLatch latch) {
		this.data = data;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel textFieldPanel = new JPanel(new GridLayout(2, 2));
		JLabel nameLabel = new JLabel("Name: ");
		textFieldPanel.add(nameLabel);
		nameLabel.setLabelFor(nameTextField);
		textFieldPanel.add(nameTextField);
		JLabel authorLabel = new JLabel("Author: ");
		textFieldPanel.add(authorLabel);
		authorLabel.setLabelFor(authorTextField);
		textFieldPanel.add(authorTextField);
		add(textFieldPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();	
		buttonPanel.add(loadButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		loadButton.addActionListener(this::load);
		cancelButton.addActionListener(this::cancel);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (latch != null) {
					latch.countDown();
				}
			}
		});
		
		setSize(200, 200);
		pack();
		setVisible(true);
	}
	
	public void load(ActionEvent e) {
		if (nameTextField.getText().length() > 0 &&
				authorTextField.getText().length() > 0) {
			api.postLevels(nameTextField.getText(), authorTextField.getText(), data);
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}	
	}
	
	public void cancel(ActionEvent e) {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
}
