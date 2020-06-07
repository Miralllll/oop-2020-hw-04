// JCount.java

/*
 Basic GUI/Threading exercise.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JCount extends JPanel {

	private static final String DEF_START = "0", DEF_END = "100000000";
	private static final String START = "start", STOP = "stop";
	private JTextField txField;
	private JLabel label;
	private JButton start, stop;
	private WorkerThread worker;

	public JCount() {
		super();
		// Set the JCount to use Box layout
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		txField = new JTextField(DEF_END); // 100,000,000
		label = new JLabel(DEF_START);
		add(txField); // add text field :))) easy
		add(label); // add label :\\\
		initStartButton();
		initStopButton();
		add(Box.createRigidArea(new Dimension(0,40)));
	}

	/* initializes stop button and its listener locally :)) */
	private void initStopButton() {
		stop = new JButton(STOP);
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(worker == null) return;;
				worker.interrupt();
				worker = null;
			}
		});
		add(stop);
	}

	/* initializes start button and its listener locally :)) */
	private void initStartButton() {
		start = new JButton(START);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(worker != null) worker.interrupt();
				int goal = Integer.valueOf(txField.getText());
				worker = new WorkerThread(goal);
				worker.start();
			}
		});
		add(start);
	}

	/* only sending swing thread some stuff to do */
	static public void main(String[] args)  {
		SwingUtilities.invokeLater(() -> {
			createAndShowGUI();
		});
	}

	/* job for SwingUtilities --- organising JFrame, panels... */
	private static void createAndShowGUI() {
		// Creates a frame with 4 JCounts in it.
		// (provided)
		JFrame frame = new JFrame("The Count");
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private class WorkerThread extends Thread {
		private int goal; // from JTextField

		public WorkerThread(int goal){
			this.goal = goal;
		}
		@Override
		public void run(){
			for(int i = 0; i <= goal; i++){
				if(isInterrupted()) {
					doLabelUpdate(i);
					return;
				}
				// checks if it is time to update panels
				if(i % UPDATE_TIME == 0)
					if(!doPauseLogic()) return;
					else doLabelUpdate(i);
			}
			doLabelUpdate(goal);
		}

		/* sends a new job to Swing thread in the queue */
		private void doLabelUpdate(int newInteger) {
			SwingUtilities.invokeLater(() -> {
				label.setText(String.valueOf(newInteger));
			});
		}

		/* pause for constant milliseconds */
		private boolean doPauseLogic() {
			try{
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e){
				return false;
			}
			return true;
		}

		private static final int UPDATE_TIME = 10000;
		private static final int SLEEP_TIME = 100; // 1000 is perfect for testing
	}
}

