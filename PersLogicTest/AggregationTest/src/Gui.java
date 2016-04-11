import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public class Gui {

	private JFrame frame;

	private JPanel chartPanel;
	
	private JPanel controlPanel;
	
	private JSpinner anxietySpinner;
	private JSpinner dutifulnessSpinner;
	private JSpinner selfdisciplineSpinner;
	private JSpinner activitySpinner;
	private JSpinner modestySpinner;
	private JSpinner angryhostilitySpinner;
	private JSpinner straightforwardnessSpinner;
	private JSpinner depressionSpinner;
	private JSpinner impulsivenessSpinner;
	private JSpinner achievementstrivingSpinner;
	private JSpinner deliberationSpinner;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		this.initialize();
		
		this.frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.chartPanel = new JPanel();
		this.controlPanel = new JPanel();
		
		this.controlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		// Anxiety
		{
			c.gridx = 0;
			c.gridy = 0;
			this.controlPanel.add(new JLabel("Anxiety: "), c);
			c.gridx = 1;
			c.gridy = 0;
			this.controlPanel.add(this.anxietySpinner = new JSpinner(), c);
		}
		
		// Dutifulness
		{
			c.gridx = 0;
			c.gridy = 1;
			this.controlPanel.add(new JLabel("Dutifulness: "), c);
			c.gridx = 1;
			c.gridy = 1;
			this.controlPanel.add(this.dutifulnessSpinner = new JSpinner(), c);
		}
		
		// Self-discipline
		{
			c.gridx = 0;
			c.gridy = 2;
			this.controlPanel.add(new JLabel("Self-discipline: "), c);
			c.gridx = 1;
			c.gridy = 2;
			this.controlPanel.add(this.selfdisciplineSpinner = new JSpinner(), c);
		}
		
		// Activity
		{
			c.gridx = 0;
			c.gridy = 3;
			this.controlPanel.add(new JLabel("Activity: "), c);
			c.gridx = 1;
			c.gridy = 3;
			this.controlPanel.add(this.activitySpinner = new JSpinner(), c);
		}
		
		// Modesty
		{
			c.gridx = 0;
			c.gridy = 4;
			this.controlPanel.add(new JLabel("Modesty: "), c);
			c.gridx = 1;
			c.gridy = 4;
			this.controlPanel.add(this.modestySpinner = new JSpinner(), c);
		}
		
		// Angry-hostility
		{
			c.gridx = 0;
			c.gridy = 5;
			this.controlPanel.add(new JLabel("Angry-hostility: "), c);
			c.gridx = 1;
			c.gridy = 5;
			this.controlPanel.add(this.angryhostilitySpinner = new JSpinner(), c);
		}
		
		// Straightforwardness
		{
			c.gridx = 0;
			c.gridy = 6;
			this.controlPanel.add(new JLabel("Straightforwardness: "), c);
			c.gridx = 1;
			c.gridy = 6;
			this.controlPanel.add(this.straightforwardnessSpinner = new JSpinner(), c);
		}
		
		// Depression
		{
			c.gridx = 0;
			c.gridy = 7;
			this.controlPanel.add(new JLabel("Depression: "), c);
			c.gridx = 1;
			c.gridy = 7;
			this.controlPanel.add(this.depressionSpinner = new JSpinner(), c);
		}
		
		// Impulsiveness
		{
			c.gridx = 0;
			c.gridy = 8;
			this.controlPanel.add(new JLabel("Impulsiveness: "), c);
			c.gridx = 1;
			c.gridy = 8;
			this.controlPanel.add(this.impulsivenessSpinner = new JSpinner(), c);
		}
		
		// Achievement-striving
		{
			c.gridx = 0;
			c.gridy = 9;
			this.controlPanel.add(new JLabel("Achievement-striving: "), c);
			c.gridx = 1;
			c.gridy = 9;
			this.controlPanel.add(this.achievementstrivingSpinner = new JSpinner(), c);
		}
		
		// Deliberation
		{
			c.gridx = 0;
			c.gridy = 10;
			this.controlPanel.add(new JLabel("Deliberation: "), c);
			c.gridx = 1;
			c.gridy = 10;
			this.controlPanel.add(this.deliberationSpinner = new JSpinner(), c);
		}
		
		this.frame.add(this.controlPanel);
	}

}
