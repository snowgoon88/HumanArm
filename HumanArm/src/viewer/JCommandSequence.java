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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import model.Command;
import model.CommandSequence;
import model.Consignes;
import utils.GraphicHelper;

/**
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JCommandSequence extends JPanel implements Observer {

	/** The actual graph */
	Chart2D _chart;
	/** The trace of consign points */
	ArrayList<ITrace2D> _traces;
	/** The model (ie: sequence of Commands) */
	ArrayList<CommandSequence> _comList;
	
	/** The selected CommandSequence */
	CommandSequence _comSeq;
	/** The selected Command */
	Command _selectCom;
	/** The selected Trace */
	ITrace2D _selectTrace;
	
	
	/** To change Command more precisely */
	JCommand _comPanel;
	
	/** Info Label */
	JLabel _infoLabel;
	
	/** PopupMenu */
	PopUpChart _popupMenu;
	/** Ensemble de CheckBox pour les traces */
	JPanel _traceCheckPanel;
	/** Action to add a new Command */
	Action _addCommandAct;
	/** Action to remove the selected Command */
	Action _removeCommandAct;
	/** Flag that allow dragging Command */
	boolean _fg_drag_mode = false;
	
	/** To help in Graphics */
	GraphicHelper _gh = new GraphicHelper();
	
	public JCommandSequence() {
		super();
		_traces = new ArrayList<ITrace2D>();
		_comList = new ArrayList<CommandSequence>();
		_comSeq = null;
		_selectCom = null;
		_selectTrace = null;
		
		_traceCheckPanel = new JPanel();
		_traceCheckPanel.setLayout(new BoxLayout(_traceCheckPanel,BoxLayout.Y_AXIS));
		
		buildActions();
		buildGUI();
		_popupMenu = new PopUpChart();
		_popupMenu.add( _traceCheckPanel );
		_popupMenu.add( new JSeparator());
		_chart.setComponentPopupMenu( _popupMenu );	
	}
	
	/**
	 * Add a new CommandSequence model.
	 * Create ITrace2D, update _comList, _traces and setup PopuMenu.
	 * @param obj
	 * @return true if ok
	 */
	public boolean add(CommandSequence obj) {
		boolean res = _comList.add(obj);
		
		// new trace
		final ITrace2D trace = new Trace2DSimple();
		_chart.addTrace(trace);
		_traces.add( trace );
		trace.setColor(_gh._defColors[_comList.size() % _gh._defColors.length]);
		trace.setVisible(true);
		
		updateTrace(trace, obj);
		//_popupMenu.addTrace(trace, true);
		
		// Maj de _traceCheckPanel
		JCheckBox comBox = new JCheckBox(obj.getName(),true);
		comBox.setSelected(true);
		comBox.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				trace.setVisible(!trace.isVisible());
			}
		});
		_traceCheckPanel.add(comBox);
		return res;
	}
	/**
	 * Clear all CommandSequences.
	 * Update _chart, _comList, _traces and popupMenu.
	 */
	public void clear() {
		_chart.removeAllTraces();
		_traces.clear();
		_comList.clear();
		//_popupMenu.removeAllTrace();
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
	
		this.add(_chart, BorderLayout.CENTER);
		
		// At the bottom
		JPanel bottomPanel = new JPanel();
		_infoLabel = new JLabel("- : ");
		bottomPanel.add(_infoLabel);
		
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
	private void updateTrace(ITrace2D trace, CommandSequence comSeq) {
		trace.removeAllPoints();
		trace.setName(comSeq.getName());
		
		// Add points
		Command last = null;
		for (Command com : comSeq) {
			if (last != null) {	
				trace.addPoint(com.time, last.val);
			}
			trace.addPoint(com.time, com.val);
			last = com;				
		}
		trace.addPoint(last.time+1.0,last.val);
		
		// Put Circle around selected Commands
		if (_comSeq == comSeq && _selectCom != null) {
			for (Iterator<ITracePoint2D> it = trace.iterator(); it.hasNext();) {
				ITracePoint2D pt = (ITracePoint2D) it.next();
				
				// Matches with selected Command
				if (pt.getX() == _selectCom.time && pt.getY() == _selectCom.val) {
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
		GraphicHelper iconLoader = new GraphicHelper();
		_addCommandAct = new AddCommandAction("Add Pt",
				iconLoader.createImageIcon("list-add.png", "Add"),
				"Ajouter un nouveau point de consigne",
				new Integer(KeyEvent.VK_A));
		
		_removeCommandAct = new RemoveCommandAction("Del Pt",
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
		int _mouseX, _mouseY;
		/** Memory of x position of selected points */
		double _xBase = 0;
		/** Memory of y position of selected points */
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
			_selectCom = null;
			if (_comSeq != null) {
				updateTrace(_selectTrace, _comSeq);
			}
			_selectTrace = null;
			_comSeq = null;
			_infoLabel.setText(" - : ");
			
//			if (_ptEnd != null ) {
//				_ptBegin.removeAllAdditionalPointPainters();
//				_ptEnd.removeAllAdditionalPointPainters();
//			}
//			_fg_needUpdate = false;
			
			// Memorize Mouse position
			_mouseX = e.getX();
			_mouseY = e.getY();
			
//			System.out.println("-----------------------------------------------");
//			System.out.println("selectTrace="+_selectTrace);
//			System.out.println("mouse="+e.getX()+" "+e.getY());
			ITracePoint2D ptPressed = _chart.getNearestPointEuclid(e.getX(), e.getY());
			if (ptPressed == null ) {
				return;
			}
//			System.out.println("pressed="+ptPressed.toString());
//			System.out.println("       ="+ptPressed.getScaledX()+" "+ptPressed.getX());
			// look only among visible ITrace
			double minDist = Float.MAX_VALUE;
			ITracePoint2D ptSelected = null;
			for (ITrace2D tr : _traces) {
				//System.out.println("trace="+tr.getName());
				if (tr.isVisible()) {
					//System.out.println("is Visible");
					for (Iterator<ITracePoint2D> it = tr.iterator(); it.hasNext();) {
						ITracePoint2D point = (ITracePoint2D) it.next();
						double d=Math.sqrt((point.getX()-ptPressed.getX())*(point.getX()-ptPressed.getX())
								+(point.getY()-ptPressed.getY())*(point.getY()-ptPressed.getY()));
						if (d < minDist) {
							minDist = d;
							ptSelected = point;
							_selectTrace = tr;
						}
					}
				}
				//System.out.println("d="+minDist+" ptSel="+ptSelected+" tr="+_selectTrace);
			}
			ptPressed = ptSelected;
			if (ptPressed == null ) {
				return;
			}
			//_selectTrace = ptPressed.getListener();
			// Could get original trace : ITrace2D tr = ptPressed.getListener();
			//                            if (tr==_trace) System.out.println("Same TRACE");
			// Memorize Val of selected Points
			_xBase = ptPressed.getX();
			_yBase = ptPressed.getY();
			
			//System.out.println("Selected point at "+ptPressed.getX()+", "+ptPressed.getY());
			
			// Compute Start and End of segment. Command is "linked" on the Start point.
			Iterator<ITracePoint2D> it = _selectTrace.iterator();
			ITracePoint2D ptBegin = null;
			for (; it.hasNext();) {
				ITracePoint2D pt = (ITracePoint2D) it.next();
				if (pt == ptPressed) {
					if (ptBegin != null ) {
						if (ptBegin.getY() == pt.getY()) {
							break;
						}
					}
					else {
						// ptPressed must be the first point
						ptBegin = pt;
						break;
					}
				}
				else if (ptBegin == ptPressed) {
					// now we are after
					break;
					
				}
				ptBegin = pt;
			}
			
//			// highlight the 2 points
//			_ptBegin.addAdditionalPointPainter(new PointPainterDisc());
//			_ptEnd.addAdditionalPointPainter(new PointPainterDisc());
			
			// Find the right CommandSequence
			int index = _traces.indexOf(_selectTrace);
			if (index >= 0) {
				_comSeq = _comList.get(index);
				// Find Command
				_selectCom = _comSeq.finds(ptBegin.getX(), ptBegin.getY());
				if (_selectCom != null) {
					System.out.println("Found "+_selectCom.toStringP());
				}
				
				// Alert the JCommand
				_comPanel.setCommand(_selectCom, _comSeq);
				// Info Panel
				_infoLabel.setText(" "+_comSeq.getName()+" : ");
				
				updateTrace( _selectTrace, _comSeq);
			}
			else {
				System.err.println("[CommandSequence.MyMouseListener.mousePressed] ComSeq not found.");
			}
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
			// Compute the horizontal (X) displacement compared to initial segment position.
			int curX = e.getX();
			double deltaX = 0;
			if (e.isShiftDown() == false ) {
				deltaX= (double)(curX - _mouseX) / (double)_chart.getWidth() *(_selectTrace.getMaxX()-_selectTrace.getMinX());
			}
			System.out.println("deltaX ="+(double)(curX - _mouseX)+" width="+(double)_chart.getWidth());
			// Compute the vertical (Y) displacement compared to initial segment position.
			int curY = e.getY();
			double deltaY = 0;
			if (e.isControlDown() == false ) {
				deltaY = (double)(curY - _mouseY) / (double)_chart.getHeight() * (_selectTrace.getMaxY()-_selectTrace.getMinY());
			}
			System.out.println("deltaY ="+(double)(curY - _mouseY)+" height="+(double)_chart.getHeight());
			
//			_ptBegin.setLocation(_ptBegin.getX(), _yBase-deltaY);
//			_ptEnd.setLocation(_ptEnd.getX(), _yBase-deltaY);
//			

			
//			_fg_needUpdate = true;
			_comSeq.changeCommand(_selectCom, _xBase+deltaX, _yBase-deltaY);
			_comPanel.update();
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void update(Observable model, Object o) {
		System.out.println("JComSeq.update model = "+model.getClass().getName());
		System.out.println("                   arg= "+o);
		// On modifie la command Sequence (ds Table ou mouse)
		if (model instanceof CommandSequence) {
			CommandSequence com = (CommandSequence) model;
			// find the trace
			int index = _comList.indexOf(com);
			if (index >= 0) {
				_selectTrace = _traces.get(index);
				updateTrace(_selectTrace, com);
			} else {
				System.err.println("[CommandSequence.update] model not found.");
			}
		}
		// Toutes les ComSeq sont modifiées
		else if (model instanceof Consignes) {
			Consignes consigne = (Consignes) model;
			clear();
			for (int i = 0; i < consigne.size(); i++) {
				add(consigne.get(i));
				consigne.get(i).addObserver(this);
			}
		}
		else {
			System.err.println("[CommandSequence.update] model of unknown type.");
		}
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
			if (_comSeq != null ) {
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
			else {
				JOptionPane.showMessageDialog(_chart, "Pas de Command choisie!");
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
			if (_comSeq != null && _selectCom != null) {
				int choice = JOptionPane.showConfirmDialog(_chart, "Effacer le point?",
						"Effacer Pt Consigne", JOptionPane.OK_CANCEL_OPTION);
				if (choice == 0) {
					_comSeq.remove(_selectCom);
				}
			}
			else {
				JOptionPane.showMessageDialog(_chart, "Pas de Command choisie!");
			}
		}
	}

	/** A popup menu over charts */
	class PopUpChart extends JPopupMenu {
		
		/**
		 * Build the basic menu with actions.
		 */
		public PopUpChart() {
			// Actions
			JMenuItem anItem;
			anItem = new JMenuItem(_addCommandAct);
			add(anItem);
			anItem = new JMenuItem(_removeCommandAct);
			add(anItem);
			addSeparator();
			add(_traceCheckPanel);
		}
	}
}
