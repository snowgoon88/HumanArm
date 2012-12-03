/**
 * 
 */
package viewer;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import utils.IconLoader;

import model.Command;
import model.CommandSequence;
import model.CommandSequenceListener;

/**
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JCommandSequence extends JPanel implements CommandSequenceListener {

	/** The actual graph */
	Chart2D _chart;
	/** The trace of consign points */
	ITrace2D _trace;
	/** The model (ie: sequence of Commands) */
	CommandSequence _comSeq;
	/** The selected Command */
	Command _selected;
	/** To change Command more precisely */
	JCommand _comPanel;
	
	/** Action to add a new Command */
	Action _addCommandAct;
	/** Action to remove the selected Command */
	Action _removeCommandAct;
	/** Flag that allow dragging Command */
	boolean _fg_drag_mode = false;
	
	/**
	 * 
	 */
	public JCommandSequence( CommandSequence model) {
		super();
		_comSeq = model;
		_selected = null;
		
		buildActions();
		buildGUI();
		
		_chart.setComponentPopupMenu( new PopUpChart() );
	}

	/**
	 * Build Chart2D and Trace2D. Fill Trace2D with points.
	 */
	private void buildGUI() {
		// Set Layout
		this.setLayout(new BorderLayout());
		// Build Chart
		_chart = new Chart2D();
		MyMouseListener ml = new MyMouseListener();
		_chart.addMouseListener(ml);
		_chart.addMouseMotionListener(ml);
	
		_trace = new Trace2DSimple();
		_chart.addTrace(_trace);
		this.add(_chart, BorderLayout.CENTER);
		
		updateTrace();
		_trace.setVisible(true);
		
		// At the bottom
		JPanel bottomPanel = new JPanel();
		JButton addButton = new JButton(_addCommandAct);
		addButton.setHideActionText(true);
		bottomPanel.add( addButton );
		JButton removeButton = new JButton(_removeCommandAct);
		removeButton.setHideActionText(true);
		bottomPanel.add( removeButton );
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setVisible(true);
		bottomPanel.add( sep );
		_comPanel = new JCommand(null, _comSeq);
		bottomPanel.add( _comPanel );
		this.add(bottomPanel, BorderLayout.SOUTH);
	}
	private void updateTrace() {
		_trace.removeAllPoints();

		// Add points
		Command last = null;
		for (Command com : _comSeq) {
			if (last != null) {	
				_trace.addPoint(com.time, last.val);
			}
			_trace.addPoint(com.time, com.val);
			last = com;				
		}
		_trace.addPoint(last.time+1.0,last.val);
		
		// Put Circle around selected Commands
		if (_selected != null) {
			for (Iterator<ITracePoint2D> it = _trace.iterator(); it.hasNext();) {
				ITracePoint2D pt = (ITracePoint2D) it.next();
				
				// Matches with selected Command
				if (pt.getX() == _selected.time && pt.getY() == _selected.val) {
					pt.addAdditionalPointPainter(new PointPainterDisc(10));
					// and the next with normal circle
					pt = (ITracePoint2D) it.next();
					pt.addAdditionalPointPainter(new PointPainterDisc());
					break;
				}
			}
		}
	}
	private void buildActions() {
		IconLoader iconLoader = new IconLoader();
		_addCommandAct = new AddCommandAction("Add",
				iconLoader.createImageIcon("list-add.png", "Add"),
				"Ajouter un nouveau point de consigne",
				new Integer(KeyEvent.VK_A));
		
		_removeCommandAct = new RemoveCommandAction("Del",
				iconLoader.createImageIcon("list-remove.png", "Remove"),
				"Effacer un point de consigne",
				new Integer(KeyEvent.VK_X));
	}

	/**
	 * Permet de modifier une CommandSequence avec la souris.
	 * - pressed : sélectionne un segment -> une Command
	 * - dragged : modifie la valeur d'un segment -> une Command
	 * - released : nouvelle valeur transmise à Command
	 */
	class MyMouseListener implements MouseListener, MouseMotionListener {
		/** Memory of where the mouse was pressed */
		int _mouseY;
		/** Memory of y position ot selected points */
		double _yBase = 0;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// Do nothing if not LEFT button
			if (e.getButton() != MouseEvent.BUTTON1) {
				_fg_drag_mode = false;
				return;
			}
			// otherwise, allow dragging
			_fg_drag_mode = true;
			
			// If points where selected, deselect them
			_selected = null;
			
//			if (_ptEnd != null ) {
//				_ptBegin.removeAllAdditionalPointPainters();
//				_ptEnd.removeAllAdditionalPointPainters();
//			}
//			_fg_needUpdate = false;
			
			// Memorize Mouse y-position
			_mouseY = e.getY();
			
			ITracePoint2D ptPressed = _chart.getNearestPointEuclid(e);
			// Could get original trace : ITrace2D tr = ptPressed.getListener();
			//                            if (tr==_trace) System.out.println("Same TRACE");
			// Memorize Val of selected Points
			_yBase = ptPressed.getY();
			
			//System.out.println("Selected point at "+ptPressed.getX()+", "+ptPressed.getY());
			
			// Compute Start and End of segment. Command is "linked" on the Start point.
			Iterator<ITracePoint2D> it = _trace.iterator();
			ITracePoint2D _ptBegin = null;
			for (; it.hasNext();) {
				ITracePoint2D pt = (ITracePoint2D) it.next();
				if (pt == ptPressed) {
					if (_ptBegin != null ) {
						if (_ptBegin.getY() == pt.getY()) {
							break;
						}
					}
					else {
						// ptPressed must be the first point
						_ptBegin = pt;
						break;
					}
				}
				else if (_ptBegin == ptPressed) {
					// now we are after
					break;
					
				}
				_ptBegin = pt;
			}
			
//			// highlight the 2 points
//			_ptBegin.addAdditionalPointPainter(new PointPainterDisc());
//			_ptEnd.addAdditionalPointPainter(new PointPainterDisc());
			
			// Find Command
			_selected = _comSeq.finds(_ptBegin.getX(), _ptBegin.getY());
			if (_selected != null) {
				System.out.println("Found "+_selected.toStringP());
			}
			
			// Alert the JCommand
			_comPanel.setCommand(_selected);
			
			updateTrace();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			_fg_drag_mode = false;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// Do nothing if not in drag mode
			if (_fg_drag_mode != true) {
				return;
			}
			// Compute the vertical (Y) displacement compared to initial segment position.
			int curY = e.getY();
			double deltaY = (double)(curY - _mouseY) / (double)_chart.getHeight();
			//System.out.println("delta ="+(double)(curY - _mouseY)+" height="+(double)_chart.getHeight());
			
//			_ptBegin.setLocation(_ptBegin.getX(), _yBase-deltaY);
//			_ptEnd.setLocation(_ptEnd.getX(), _yBase-deltaY);
//			
//			_fg_needUpdate = true;
			_comSeq.changeCommand(_selected, _selected.time, _yBase-deltaY);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public void modelChanged(CommandSequence model) {
		// TODO Auto-generated method stub
		updateTrace();
	}
	
	/**
	 * Add a new Command, use a JDialog.
	 */
	class AddCommandAction extends AbstractAction {
		public AddCommandAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// potential new Command
			Command newCommand = new Command();
			// possible actions
			Object[] options = {"Ajouter","Annuler"};
			
			// create a Panel for setting new Command fields
			JPanel ptPanel = new JPanel();
			JLabel txtLabel = new JLabel( "Coordonnées du point à ajouter");
			ptPanel.add( txtLabel );
			JCommand commandPanel = new JCommand(newCommand, _comSeq);
			ptPanel.add( commandPanel );
			
			// Use a customized JOptionPane to get user's answer
			int choice = JOptionPane.showOptionDialog(_chart, ptPanel, 
					"Ajout Pt Consigne", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			System.out.println("Dialog = "+choice);
			if (choice == 0) {
				_comSeq.add(newCommand);
			}
		}
	}
	/**
	 * Remove a selected Command, use JDialog to confirm.
	 */
	class RemoveCommandAction extends AbstractAction {
		public RemoveCommandAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (_selected != null) {
				int choice = JOptionPane.showConfirmDialog(_chart, "Effacer le point?",
						"Effacer Pt Consigne", JOptionPane.OK_CANCEL_OPTION);
				if (choice == 0) {
					_comSeq.remove(_selected);
				}
			}
		}
	}

	/** A popup menu over charts */
	class PopUpChart extends JPopupMenu {
		JMenuItem anItem;
		public PopUpChart() {
			anItem = new JMenuItem(_addCommandAct);
			add(anItem);
			anItem = new JMenuItem(_removeCommandAct);
			add(anItem);
		}
	}
}
