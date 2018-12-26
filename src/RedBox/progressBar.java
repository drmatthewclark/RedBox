package RedBox;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JProgressBar;


public class progressBar extends JFrame  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 269909562792873403L;
	private JProgressBar pg;


	void setProgress(int progress) {
		pg.setValue(progress);
		validate();
	}

	progressBar(int max) {
		super("Progress");
		init(max);
	}

	public void incMax(int max) {
		pg.setMaximum(pg.getMaximum() + max);
	}
	
	public void end() {
		setVisible(false);
	}

	public void init(int max) {

		setLayout(new BorderLayout());
		setSize(300, 100);
		pg = new JProgressBar(1, max);
		pg.setValue(1);
		pg.setStringPainted(true);
		add(pg, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		validate();
		setVisible(true);
		setAlwaysOnTop(true);
	}
}
