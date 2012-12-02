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
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.JPanel;

import model.Command;
import model.CommandSequence;

/**
 * @author alain.dutech@loria.fr
 *
 */
public class JCommandSequence extends JPanel {

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
	
	/**
	 * 
	 */
	public JCommandSequence( CommandSequence model) {
		super();
		_comSeq = model;
		_selected = null;
		
		buildGUI();
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
		_trace.setVisible(true);
		
		_comPanel = new JCommand(null, _comSeq);
		this.add(_comPanel, BorderLayout.SOUTH);
	}

	/**
	 * Permet de modifier une CommandSequence avec la souris.
	 * - pressed : sélectionne un segment -> une Command
	 * - dragged : modifie la valeur d'un segment -> une Command
	 * - released : nouvelle valeur transmise à Command
	 */
	class MyMouseListener implements MouseListener, MouseMotionListener {
		/** Start of selected Command */
		private ITracePoint2D _ptBegin = null;
		/** End of select Command */
		private ITracePoint2D _ptEnd = null;
		/** Memory of where the mouse was pressed */
		int _mouseY;
		/** Memory of y position ot selected points */
		double _yBase = 0;
		/** Need to update model ? */
		boolean _fg_needUpdate = false;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// If points where selected, deselect them
			if (_ptEnd != null ) {
				_ptBegin.removeAllAdditionalPointPainters();
				_ptEnd.removeAllAdditionalPointPainters();
			}
			_fg_needUpdate = false;
			
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
			_ptBegin = null;
			_ptEnd = null;
			for (; it.hasNext();) {
				ITracePoint2D pt = (ITracePoint2D) it.next();
				if (pt == ptPressed) {
					if (_ptBegin != null ) {
						if (_ptBegin.getY() == pt.getY()) {
							_ptEnd = pt;
							break;
						}
					}
					else {
						// ptPressed must be the first point
						_ptBegin = pt;
						_ptEnd = (ITracePoint2D) it.next();
						break;
					}
				}
				else if (_ptBegin == ptPressed) {
					// now we are after
					_ptEnd = pt;
					break;
					
				}
				_ptBegin = pt;
			}
			
			// highlight the 2 points
			_ptBegin.addAdditionalPointPainter(new PointPainterDisc());
			_ptEnd.addAdditionalPointPainter(new PointPainterDisc());
			
			// Find Command
			_selected = _comSeq.finds(_ptBegin.getX(), _ptBegin.getY());
			if (_selected != null) {
				System.out.println("Found "+_selected.toStringP());
			}
			
			// Alert the JCommand
			_comPanel.setCommand(_selected);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (_fg_needUpdate) {
				_selected.val = _ptBegin.getY();
			}
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
			// Compute the vertical (Y) displacement compared to initial segment position.
			int curY = e.getY();
			double deltaY = (double)(curY - _mouseY) / (double)_chart.getHeight();
			//System.out.println("delta ="+(double)(curY - _mouseY)+" height="+(double)_chart.getHeight());
			
			_ptBegin.setLocation(_ptBegin.getX(), _yBase-deltaY);
			_ptEnd.setLocation(_ptEnd.getX(), _yBase-deltaY);
			
			_fg_needUpdate = true;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
}
