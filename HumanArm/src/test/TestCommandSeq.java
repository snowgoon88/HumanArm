/**
 * 
 */
package test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import viewer.JCommandSequence;

import model.Command;
import model.CommandSequence;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class TestCommandSeq {

	
	public void testCreate() {
		CommandSequence com = new CommandSequence();
		System.out.println(com.toString());
		
		com.add(new Command(1.0, 0.8));
		System.out.println(com.toString());
		
		com.add(new Command(0.0, 0.0));
		System.out.println(com.toString());
		
		com.add(new Command(0.3, 0.3));
		System.out.println(com.toString());
		
		com.add(new Command(2.0, 0.1));
		System.out.println(com.toString());
		
		for( double t=-1.0; t < 2.2; t+=0.05) {
			System.out.println(t+":"+Double.toString(com.getValAtTime(t)));
		}
	}
	public void testAddFocus() {
		CommandSequence com = new CommandSequence();		
		com.add(new Command(1.0, 0.8));
		com.add(new Command(0.0, 0.0));
		com.add(new Command(0.3, 0.3));
		com.add(new Command(2.0, 0.1));
		System.out.println(com.toString());
		
		System.out.println("1.2 : "+com.getValAtTime(1.2));
		System.out.println(com.toString());
		System.out.println("0.95 F "+com.focusAtTime(0.95));
		System.out.println(com.toString());
		
		com.add(new Command(0.9, 0.75));
		System.out.println(com.toString());
		System.out.println("0.95 F "+com.focusAtTime(0.95));
		System.out.println(com.toString());
		
		for( double t=-1.0; t < 2.2; t+=0.05) {
			System.out.println(t+":"+Double.toString(com.getValAtTime(t)));
		}
		
		long m_starttime = System.currentTimeMillis();
		double v = 0.0;
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
		
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.000000001; t < 2.3; t+=0.0001) {
			v+= com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.000000001; t < 2.3; t+=0.0001) {
			v+= com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.000000001; t < 2.3; t+=0.0001) {
			v+= com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		
		com = new CommandSequence();
		com.add(new Command(1.1, -0.8));
		com.add(new Command(0.0, 2.0));
		com.add(new Command(0.34, 0.7));
		com.add(new Command(2.1, 0.1));
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=2.2; t >= 0; t -= 0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=2.2; t >= 0; t -= 0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
		
	}
	
	public void testSpeed() {
		CommandSequence com = new CommandSequence();	
		// On va créer un compliqué
		for (int i = 0; i < 250; i++) {
			com.add(new Command( Math.random() * 2, Math.random()));
		}
		
		long m_starttime = System.currentTimeMillis();
		double v = 0.0;
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
		v  = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		v  = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		m_starttime = System.currentTimeMillis();
		for( double t=0.0; t < 2.2; t+=0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=2.2; t >= 0; t -= 0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=2.2; t >= 0; t -= 0.0001) {
			v += com.getValAtTime(t);
		}
		System.out.println("Delta = "+(System.currentTimeMillis() - m_starttime));
		
		v = 0.0;
		m_starttime = System.currentTimeMillis();
		for( double t=2.2; t >= 0; t -= 0.0001) {
			v += com.getValAtTimeFocussed(t);
		}
		System.out.println("FDelta = "+(System.currentTimeMillis() - m_starttime));
	}
	
	public void testGraphic() {
		// Setup window
		JFrame frame = new JFrame("Arm - Java2D API");
		frame.setSize(600,600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setLayout(new BorderLayout());
		
		CommandSequence com = new CommandSequence();		
		com.add(new Command(1.0, 0.8));
		com.add(new Command(0.0, 0.0));
		com.add(new Command(0.3, 0.3));
		com.add(new Command(2.0, 0.1));
		
		// Graphical part
		JCommandSequence seqViewer = new JCommandSequence(com);
		frame.add( seqViewer, BorderLayout.CENTER);
		
		frame.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestCommandSeq app = new TestCommandSeq();
		//app.testCreate();
		//app.testAddFocus();
		//app.testSpeed();
		app.testGraphic();
	}

}
