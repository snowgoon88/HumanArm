/**
 * 
 */
package test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import viewer.CommandSequenceTableModel;
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
		JFrame frame = new JFrame("Test JCommandSequence");
		frame.setSize(600,600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setLayout(new BorderLayout());
		
		CommandSequence com = new CommandSequence("com_0");		
		com.add(new Command(1.0, 0.8));
		com.add(new Command(0.0, 0.0));
		com.add(new Command(0.3, 0.3));
		com.add(new Command(2.0, 0.1));
		
		// Graphical part
		JCommandSequence seqViewer = new JCommandSequence();
		seqViewer.add(com);
		com.addObserver(seqViewer);
		frame.add( seqViewer, BorderLayout.CENTER);
		
		frame.setVisible(true);
		
		CommandSequence com2 = new CommandSequence();

		try {
			com2.read("data/essai_command.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		com2.setName("com_1");
		seqViewer.add(com2);
		com2.addObserver(seqViewer);

	}
	
	public void testAsTable() {
		// Setup window
		JFrame frame = new JFrame("Test JTable");
		frame.setSize(600,600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setLayout(new BorderLayout());
		
		CommandSequence com = new CommandSequence("com_0");		
		com.add(new Command(1.0, 0.8));
		com.add(new Command(0.0, 0.0));
		com.add(new Command(0.3, 0.3));
		com.add(new Command(2.0, 0.1));
		
		
		CommandSequenceTableModel comTableModel = new CommandSequenceTableModel(com);
		JTable comTable = new JTable( comTableModel );
		comTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		comTable.setRowSelectionAllowed(false);
		comTable.setColumnSelectionAllowed(false);
		comTable.setCellSelectionEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(comTable);
		comTable.setFillsViewportHeight(true);
		
		frame.add( scrollPane, BorderLayout.CENTER);
		
		frame.setVisible(true);
	}
	
	public void testWrite() {
		CommandSequence com = new CommandSequence("Essai");		
		com.add(new Command(1.1, 0.8));
		com.add(new Command(0.1, 0.0));
		com.add(new Command(0.33, 0.3));
		com.add(new Command(2.2, 0.1));
		
		try {
			com.write("data/essai_command.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testRead() {
		CommandSequence com = new CommandSequence();
		
		try {
			com.read("data/essai_command.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(com.toString());
	}
	
	public void makeExemple() throws IOException {
		// Open up a file
		FileWriter myFile = new FileWriter( "data/consigne_example.data" );
		BufferedWriter myWriter = new BufferedWriter( myFile );

		CommandSequence com = new CommandSequence();
		
		// Biceps_court => flexion du coude.
		com.clear();
		com.setName("0-BicepsCourt");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// Triceps latéral => extension du coude.
		com.clear();
		com.setName("1-TricepsLat");
		com.add(new Command(0, 0.1));
		com.add(new Command(0.3, 0.0));
		com.write(myWriter);
		
		// Deltoid anterieur => flexion de l'épaule
		com.clear();
		com.setName("2-DeltoidAnt");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// Deltoid posterieur => extension de l'épaule
		com.clear();
		com.setName("3-DeltoidPost");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// biceps long => flexion (épaule+coude)
		com.clear();
		com.setName("4-BicepsLong");
		com.add(new Command(0, 0.1));
		com.add(new Command(0.3, 0.0));
		com.write(myWriter);
		
		// triceps long => extension (épaule+coude)
		com.clear();
		com.setName("5-TricepsLong");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		myWriter.close();
        myFile.close();
	}
	
	void testComputeVal() {
		CommandSequence com0 = new CommandSequence();
		// Biceps_court => flexion du coude.
		com0.clear();
		com0.setName("0-BicepsCourt");
		com0.add(new Command(0, 0));
		System.out.println(com0.toString());
		
		CommandSequence com1 = new CommandSequence();
		// Triceps latéral => extension du coude.
		com1.clear();
		com1.setName("1-TricepsLat");
		com1.add(new Command(0, 0.1));
		com1.add(new Command(0.3, 0.0));
		System.out.println(com1.toString());
		
		
		CommandSequence com2 = new CommandSequence();
		// Triceps latéral => extension du coude.
		com2.clear();
		com2.setName("2-DeltoidAnt");
		com2.add(new Command(0, 0.1));
		com2.add(new Command(0.3, 0.3));
		com2.add(new Command(0.7, 0.21));
		System.out.println(com2.toString());
		
		for (double t=0; t<1.0; t += 0.1 ) {
			System.out.println(t+" : "+com0.getValAtTimeFocussed(t)+"\t"+com1.getValAtTimeFocussed(t)+"\t"+com2.getValAtTimeFocussed(t));
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		TestCommandSeq app = new TestCommandSeq();
		//app.testCreate();
		//app.testAddFocus();
		//app.testSpeed();
		//app.testGraphic();
		app.testAsTable();
		//app.testWrite();
		//app.testRead();
		//app.makeExemple();
		//app.testComputeVal();
	}

}
