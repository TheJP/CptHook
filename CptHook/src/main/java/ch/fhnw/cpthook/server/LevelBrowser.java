package ch.fhnw.cpthook.server;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class LevelBrowser extends JFrame {

	private ServerApi api = new ServerApi();
	private JList<LevelResource> levelList = new JList<>();
	private JButton loadButton = new JButton("load");
	private JButton cancelButton = new JButton("cancel");
	
	private String result = null;
	
	public LevelBrowser(CountDownLatch latch) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		add(levelList, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();	
		panel.add(loadButton);
		panel.add(cancelButton);
		add(panel, BorderLayout.SOUTH);
		
		loadButton.addActionListener(this::load);
		cancelButton.addActionListener(this::cancel);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (latch != null) {
					latch.countDown();
				}
			}
		});
		
		refreshData();
		setSize(200, 200);
		pack();
		setVisible(true);
	}
	
	public void load(ActionEvent e) {
		if (levelList.getSelectedValue() != null) {
			result = api.getLevel(levelList.getSelectedValue().getId());
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	public void cancel(ActionEvent e) {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	public void refreshData() {
		List<LevelResource> levels = api.getLevels();
		DefaultListModel<LevelResource> model = new DefaultListModel<>();
		levels.forEach(l -> model.addElement(l));
		levelList.setModel(model);
	}

	public String getResult() {
		return result;
	}
	
}
