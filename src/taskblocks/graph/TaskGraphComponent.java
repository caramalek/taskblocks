/*
 * Copyright (C) Jakub Neubauer, 2007
 *
 * This file is part of TaskBlocks
 *
 * TaskBlocks is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TaskBlocks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package taskblocks.graph;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Line2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;

import taskblocks.utils.Colors;
import taskblocks.utils.Pair;
import taskblocks.utils.Utils;

public class TaskGraphComponent extends JComponent implements ComponentListener, AdjustmentListener {
	
	static int DEFAULT_ROW_HEIGHT = 30;
	private static int DEFAULT_DAY_WIDTH = 10;
	static int CONN_PADDING_FACTOR = 6;
	private static int DEFAULT_HEADER_HEIGHT = 20;
	static int DEFAULT_FONT_SIZE = 14;
	
	// how far from task left/right boundary will the mouse press will be recognized as pressing the boundary?
	private static final int TOLERANCE = 5;
	// constant indicating left boundary of task 
	static Integer LEFT = Integer.valueOf(0);
	// constant indicating left boundary of task 
	static Integer RIGHT = Integer.valueOf(1);
	
	// Task row height (can change when Increasing or Decreasing Height)
	int _rowHeight = DEFAULT_ROW_HEIGHT;
	int _headerHeight = DEFAULT_HEADER_HEIGHT;
	
	// Font size (can change when Increasing or Decreasing Height)
	int _fontSize = DEFAULT_FONT_SIZE;
	int _dateFontSize = DEFAULT_FONT_SIZE;
	
	/**
	 * original model of tasks. It is not updated when doing changes in graph. Explicit
	 * call of {@link TaskGraphRepresentation#updateModel()} must be done.
	 */
	TaskModel _model;
	
	/**
	 * This is the representation of Graph data (tasks and rows). It has more pre-counted
	 * informations in comparison to original _model.
	 */
	TaskGraphRepresentation _builder;
	
	/** Painter used to paint tasks and workers */
	TaskGraphPainter _painter;
	
	/** Listener on user interaction in this graph.
	 * Currently, just mouse click event is sent to outside world
	 */
	GraphActionListener _grActListener;
	
	/** Current width of one day column in pixels */
	int _dayWidth = DEFAULT_DAY_WIDTH;
	
	/** first visible day (on left border) */
	long _firstDay;
	
	/** left position of graph area */
	int _graphLeft;
	/** top position of graph area */
	private int _graphTop;
	/** width of graph area */
	private int _graphWidth;
	/** height of graph area */
	int _graphHeight;
	/** Width of the left column with workers */
	int _headerWidth;
	/** Handler of mouse events */
	GraphMouseHandler _mouseHandler;
	JScrollBar _verticalScroll;
	
	/**
	 * Contains the bounds of the whole content displayed in the component.
	 * Is used to display scrollbars at right position and with the right size
	 */
	Rectangle _contentBounds = new Rectangle();
	
	int _scrollTop;

	/** Cursor - means shadow version of task that is being moved */
	Task _cursorTempTask;
	
	public TaskGraphComponent(TaskModel model, TaskGraphPainter painter) {
		_painter = painter;

		setModel(model);
		_mouseHandler = new GraphMouseHandler(this);
		_verticalScroll = new JScrollBar(JScrollBar.VERTICAL);
		this.add(_verticalScroll);
		_verticalScroll.addAdjustmentListener(this);
		
		setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
		this.addMouseMotionListener(_mouseHandler);
		this.addMouseListener(_mouseHandler);
		this.addMouseWheelListener(_mouseHandler);
		this.addKeyListener(_mouseHandler);
		this.addComponentListener(this);
		this.setFocusable(true);
		ToolTipManager.sharedInstance().setDismissDelay(8000);
		ToolTipManager.sharedInstance().setReshowDelay(3000);
	}
	
	public TaskGraphRepresentation getGraphRepresentation() {
		return _builder;
	}
	
	public void moveRight() {
		_firstDay +=2;
		_builder.setPaintDirty();
		repaint();
	}
	
	public void moveLeft() {
		_firstDay-=2;
		_builder.setPaintDirty();
		repaint();
	}
	
	void recountBounds() {
		Insets insets = getInsets();
		_graphTop = _headerHeight;
		_graphTop += insets.top;
		_graphHeight = getHeight() - _headerHeight;
		_graphLeft = _headerWidth;
		_graphWidth = getWidth() - _headerWidth;
	
		_graphLeft += insets.left;
		_graphHeight -= insets.top + insets.bottom;
		_graphWidth -= insets.left + insets.right;

	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//_headerWidth = 100;
		g2.setColor(Color.white);
		g2.fillRect(0,0,getWidth(), getHeight());
		
		synchronized(_builder) {
			Insets insets = getInsets();

			// recount boundaries and positions and paddings if neccessary
			recountBounds();

			// if cursor should be painted...
			if(_mouseHandler._cursorTaskRow != null && _mouseHandler._cursorTime >= 0 && _mouseHandler._pressedTask != null) {
				if(_cursorTempTask == null) {
					_cursorTempTask = new Task(null, null);
				}
				Task taskToMove = _mouseHandler._pressedTask;
				_cursorTempTask._builder = this._builder;
				_cursorTempTask._userObject = taskToMove._userObject;
				_cursorTempTask._row = _mouseHandler._cursorTaskRow;
				_cursorTempTask.setEffort(taskToMove.getEffort());
				_cursorTempTask.setStartTime(_mouseHandler._cursorTime);
				_builder.setPaintDirty();
			} else {
				_cursorTempTask = null;
			}
			
			if(_builder.isPaintDirty()) {
				TaskLayouter.recountBounds(_graphTop, _rowHeight, _builder, this, g2);
			}
			
			g2.clipRect(insets.left, insets.top, getWidth()-insets.left-insets.right, getHeight()-insets.top-insets.bottom);
			
			// reset content bounds
			_contentBounds.y = Integer.MAX_VALUE;
			_contentBounds.height = -1;
			
			// paint rows
			for(TaskRow row: _builder._rows) {
				row._bounds.x = insets.left;
				row._bounds.y = row._topPosition-3;
				row._bounds.width = row._selected ? _headerWidth + _graphWidth : _headerWidth;
				row._bounds.height = _rowHeight+6;
				_painter.paintRowHeader(row._userManObject, g2, row._bounds, row._selected, _fontSize);
				
				if(row._index > 0) {
					g2.setColor(Color.lightGray);
					int lineY = row._topPosition-row._topPadding*CONN_PADDING_FACTOR;
					g2.drawLine(insets.left, lineY, 2000, lineY);
				}
				
				// adjust vertical size of the content bounds
				if(_contentBounds.y > row._bounds.y) {
					if(_contentBounds.height != -1) {
						_contentBounds.height += (_contentBounds.y - row._bounds.y);
					}
					_contentBounds.y = row._bounds.y;
				}
				if(_contentBounds.y + _contentBounds.height < row._bounds.y + row._bounds.height + CONN_PADDING_FACTOR) {
					_contentBounds.height = row._bounds.y + row._bounds.height - _contentBounds.y + CONN_PADDING_FACTOR;
				}
			}
			
			// left header vertical line
			g2.setColor(Color.DARK_GRAY);
			g2.drawLine(_graphLeft, _graphTop-_headerHeight, _graphLeft, _graphTop + _graphHeight+_headerHeight);
			Color lightHeaderCol = Colors.TASKS_TOP_HEADER_COLOR.brighter().brighter();
			g2.setColor(lightHeaderCol);
			g2.drawLine(_graphLeft+1, _graphTop-_headerHeight, _graphLeft+1, _graphTop-1);
			//g2.drawLine(_graphLeft+2, _graphTop-_headerHeight, _graphLeft+2, _graphTop + _graphHeight+_headerHeight);
			
			paintWorkerHeader(g2);

			// paint tasks
			g2.clipRect(_graphLeft+2, _graphTop - _headerHeight, _graphWidth-3, _graphHeight+_headerHeight);
			Task t;
			for(int i = _builder._tasks.length-1; i >= 0; i--) {
				t = _builder._tasks[i];
				_painter.paintTask(t._userObject, g2, t._bounds, t._selected || t == _mouseHandler._pressedTask, _fontSize);
				// adjust the holder of content size
				
				if(_contentBounds.y > t._bounds.y) {_contentBounds.y = t._bounds.y;}
				if(_contentBounds.y + _contentBounds.height < t._bounds.y + t._bounds.height) {
					_contentBounds.height = t._bounds.y + t._bounds.height - _contentBounds.y;
				}
			}
			// paint weekends and header
			paintHeaderAndWeekends(g2, _dateFontSize);

			// paint connections
			for(Connection c: _builder._connections) {
				paintConnection(g2, c);
			}
			
			// paint the insertion cursor
			paintCursor(g2);
			
			// paint just being created conection
			if(_mouseHandler._dragMode == GraphMouseHandler.DM_NEW_CONNECTION) {
				g2.setColor(Color.RED);
				if(_mouseHandler._destTask != null && _mouseHandler._destTask != _mouseHandler._pressedTask) {
					_painter.paintTask(_mouseHandler._destTask._userObject, g2, _mouseHandler._destTask._bounds, true, _fontSize);
				}
				g2.drawLine(_mouseHandler._pressX, _mouseHandler._pressY, _mouseHandler.getLastMouseX(), _mouseHandler.getLastMouseY());
			}
		}
		
		adjustScrolls();
		
		// paint children, at least scroll bar(s)
		paintChildren(g);
	}
	
	public void scaleDown() {
		long mouseDay = xToTime(_mouseHandler.getLastMouseX());

		_dayWidth -=1;
		if(_dayWidth < 4) {
			_dayWidth = 4;
		}

		long newMouseDay = xToTime(_mouseHandler.getLastMouseX());
		if(newMouseDay != mouseDay) {
			_firstDay -= newMouseDay-mouseDay;
		}

		_builder.setPaintDirty();
		repaint();
	}
	
	public void scaleUp() {
		long mouseDay = xToTime(_mouseHandler.getLastMouseX());
		
		_dayWidth += 1;
		if(_dayWidth > 50) {
			_dayWidth = 50;
		}
		
		long newMouseDay = xToTime(_mouseHandler.getLastMouseX());
		if(newMouseDay != mouseDay) {
			_firstDay -= newMouseDay-mouseDay;
		}
		_builder.setPaintDirty();
		repaint();
	}
	
	public void decreaseHeight() {		
		_rowHeight -= 5;
		if (_rowHeight < DEFAULT_ROW_HEIGHT - 10){
			_rowHeight = DEFAULT_ROW_HEIGHT - 10;
		}
		_fontSize = (int) (0.6 * _rowHeight - 4.0);	

		_builder.setPaintDirty();
		repaint();
	}
	
	public void increaseHeight() {		
		_rowHeight += 5;
		if (_rowHeight > DEFAULT_ROW_HEIGHT + 30){
			_rowHeight = DEFAULT_ROW_HEIGHT + 30;
		}
		_fontSize = (int) (0.6 * _rowHeight - 4.0);		
		
		_builder.setPaintDirty();
		repaint();
	}

	public void setGraphActionListener(GraphActionListener list) {
		_grActListener = list;
	}
	
	public void setGraphChangeListener(ChangeListener changeListener) {
		_builder.setGraphChangeListener(changeListener);
	}

	public void focusOnToday() {
		_firstDay = System.currentTimeMillis()/Utils.MILLISECONDS_PER_DAY;
		_builder.setPaintDirty();
		repaint();
	}
	
	public void scrollToTaskVisible(Object task) {
		// find the task
		Task taskToFocus = null;
		for(Task t: _builder._tasks) {
			if(t._userObject == task) {
				taskToFocus = t;
				break;
			}
		}
		if(taskToFocus == null) {
			// not found
			return;
		}
		
		// scroll to the task.
		// find the most right visible day.
		long lastVisibleDay = xToTime(getWidth()-getInsets().right);
		
		// is the task outside the right border?
		if(taskToFocus.getFinishTime() > lastVisibleDay) {
			_firstDay += (taskToFocus.getFinishTime() - lastVisibleDay);
			_builder.setPaintDirty();
		}
		// is the task outside the left border?
		if(taskToFocus.getStartTime() < _firstDay) {
			_firstDay = taskToFocus.getStartTime();
			_builder.setPaintDirty();
		}
		
		// select the focused task
		_mouseHandler.clearSelection();
		_mouseHandler._selection.add(taskToFocus);
		taskToFocus._selected = true;

		repaint();
	}
	
	public void setModel(TaskModel model) {
		if(model == _model) {
			// just rebuilds the model
			_builder.buildFromModel();
			return;
		}
		TaskGraphRepresentation oldBuilder = _builder;
		_model = model;
		_builder = new TaskGraphRepresentation(_model);
		if(oldBuilder != null) {
			_builder.setGraphChangeListener(oldBuilder.getGraphChangeListener());
			oldBuilder.setGraphChangeListener(null); // not neccessary, just to be sure
		}
		_builder.buildFromModel();

		// find the minimum task start time and use it as first day.
		_firstDay = Long.MAX_VALUE;
		for(Task t: _builder._tasks) {
			if(_firstDay > t.getStartTime()) {
				_firstDay = t.getStartTime();
			}
		}
		if(_firstDay == Long.MAX_VALUE) {
			_firstDay = System.currentTimeMillis()/Utils.MILLISECONDS_PER_DAY;
		}
		
		repaint();
	}
	
	TaskRow findRow(int y) {
		for(TaskRow row: _builder._rows) {
			if(y >= row._topPosition-row._topPadding*CONN_PADDING_FACTOR && y <= row._topPosition + _rowHeight + row._bottomPadding*CONN_PADDING_FACTOR) {
				return row;
			}
		}
		return null;
	}

	TaskRow findNearestRow(int y) {
		TaskRow myRow = null;
		int minDist = Integer.MAX_VALUE;
		for(TaskRow row: _builder._rows) {
			if(y > row._topPosition && y < row._topPosition + _rowHeight) {
				myRow = row;
				break;
			} else {
				int dist = Math.min(
						Math.abs(y-row._topPosition),
						Math.abs(y-(row._topPosition + _rowHeight))
				);
				if(dist < minDist) {
					myRow = row;
					minDist = dist;
				}
			}
		}
		return myRow;
	}
	
	void changeCursor(Object o) {
		if(o instanceof Task || o instanceof Connection) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if(o instanceof Pair) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	Object findObjectOnPo(int x, int y) {

		if(x < _graphLeft) {
			return findRow(y);
		}
		
		for(Task t: _builder._tasks) {
			
			if(y > t._bounds.y && y < (t._bounds.y + t._bounds.height)) {
				if(x > (t._bounds.x-TOLERANCE) && x < (t._bounds.x + TOLERANCE)) {
					return new Pair<Task, Integer>(t, LEFT);
				}
				if(x > (t._bounds.x+t._bounds.width-TOLERANCE) && x < (t._bounds.x + t._bounds.width + TOLERANCE)) {
					return new Pair<Task, Integer>(t, RIGHT);
				}
			}

			if(t._bounds.contains(x, y)) {
				return t;
			}
		}
		
		for(Connection c: _builder._connections) {
			// check the distance from the path
			double d = Line2D.ptSegDistSq(c._path.xpoints[0], c._path.ypoints[0], c._path.xpoints[1], c._path.ypoints[1], x, y);
			d = Math.min(d, Line2D.ptSegDistSq(c._path.xpoints[1], c._path.ypoints[1], c._path.xpoints[2], c._path.ypoints[2], x, y));
			d = Math.min(d, Line2D.ptSegDistSq(c._path.xpoints[2], c._path.ypoints[2], c._path.xpoints[3], c._path.ypoints[3], x, y));
			if(d < 5*5) {
				return c;
			}
		}
		
		return null;
	}

	private void paintConnection(Graphics2D g2, Connection c) {
		if(c._selected) {
			g2.setColor(Colors.SELECTION_COLOR);
		} else {
			g2.setColor(Colors.CONNECTION_COLOR);
		}
		
		int x2 = c._path.xpoints[3];
		int y2 = c._path.ypoints[3];
		if(y2 > c._path.ypoints[0]) {
			g2.drawLine(x2-3, y2-5, x2, y2);
			g2.drawLine(x2+3, y2-5, x2, y2);
		} else {
			g2.drawLine(x2-3, y2+5, x2, y2);
			g2.drawLine(x2+3, y2+5, x2, y2);
		}
		g2.drawLine(c._path.xpoints[0], c._path.ypoints[0], c._path.xpoints[1], c._path.ypoints[1]);
		g2.drawLine(c._path.xpoints[1], c._path.ypoints[1], c._path.xpoints[2], c._path.ypoints[2]);
		g2.drawLine(c._path.xpoints[2], c._path.ypoints[2], c._path.xpoints[3], c._path.ypoints[3]);
	}
	
	private void paintCursor(Graphics2D g2) {
		if(_cursorTempTask != null) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			_painter.paintTask(_cursorTempTask._userObject, g2, _cursorTempTask._bounds, true, _fontSize);
		}
	}

	private void paintWorkerHeader(Graphics2D g2) {
		g2.setColor(Colors.TASKS_TOP_HEADER_COLOR);
		
		FontMetrics fm = g2.getFontMetrics();
		
		g2.fillRect(_graphLeft-_headerWidth, _graphTop-_headerHeight, _headerWidth, _headerHeight);
		g2.setColor(Color.WHITE);
		g2.drawString("Worker", _graphLeft-_headerWidth + 8, _graphTop-_headerHeight + (fm.getHeight() + _headerHeight)/2 - fm.getDescent() );
		
		Color darkHeaderCol = Colors.TASKS_TOP_HEADER_COLOR.darker().darker();
		Color lightHeaderCol = Colors.TASKS_TOP_HEADER_COLOR.brighter().brighter();
		g2.setColor(darkHeaderCol);
		g2.drawRect(_graphLeft-_headerWidth, _graphTop-_headerHeight, _graphWidth+_headerWidth-1, _headerHeight+_graphHeight-1);
		g2.drawLine(_graphLeft-_headerWidth, _graphTop, _graphLeft+_graphWidth, _graphTop);
		g2.setColor(lightHeaderCol);
		g2.drawLine(_graphLeft-_headerWidth+1, _graphTop-1, _graphLeft-_headerWidth+1, _graphTop-_headerHeight+1);
		g2.drawLine(_graphLeft-_headerWidth+1, _graphTop-_headerHeight+1, _graphLeft-1, _graphTop-_headerHeight+1);
	}
	
	private void paintHeaderAndWeekends(Graphics2D g2, int fontSize) {
		Color lightHeaderCol = Colors.TASKS_TOP_HEADER_COLOR.brighter().brighter();
		Color darkHeaderCol = Colors.TASKS_TOP_HEADER_COLOR.darker().darker();
		FontMetrics fm = g2.getFontMetrics();
		
		Font font = new Font("Sans Serif", Font.PLAIN, 14);
	    g2.setFont(font);

		
		g2.setColor(Colors.TASKS_TOP_HEADER_COLOR);
		g2.fillRect(_graphLeft+1, _graphTop-_headerHeight+1, _graphWidth-2, _headerHeight-1);
		g2.setColor(Color.WHITE);
		

		int skip = 1;
		if(_dayWidth < 10) {
			skip = 2;
		}

		int x = 0, x1, x2;
		int mostRight = _graphLeft + _graphWidth;
		
		g2.setColor(darkHeaderCol);
		
		// Iterate through columns and alternate day color based on month
		int j = 0;
		int j2;
		for(int i = 0; j < mostRight; i++) {
			long time = _firstDay + i;
			j = timeToX(time);
			j2 = timeToX(time + skip*7);
			int dayInWeek = Utils.getDayInWeek(time);
			g2.setColor(darkHeaderCol);

			DateFormat df = new SimpleDateFormat("M");
			String monthStr = df.format(new Date(time*Utils.MILLISECONDS_PER_DAY));
			int monthInt = Integer.parseInt(monthStr);
			if (monthInt %2 == 0) {
				g2.setColor(Colors.TASKS_TOP_HEADER_COLOR_B);
				g2.fillRect(j, _graphTop-_headerHeight+1, _dayWidth, _headerHeight-1);
			}
		}
		
		
		int firstDayInWeek = Utils.getDayInWeek(_firstDay);
		for(int i = -firstDayInWeek; x < mostRight; i+=7) {
			long time = _firstDay + i;
			x = timeToX(time);
			if(x >= mostRight) {
				break;
			}
			
			// draw weekend column
			g2.setColor(Colors.WEEKEND_COLOR);
			x1 = timeToX(time+5);
			x2 = timeToX(time+7);
			g2.fillRect(x1, _graphTop, x2-x1, _graphHeight);
			
			// draw the date string
			if((time/7) % skip == 0) {
				DateFormat df = new SimpleDateFormat("M/d");
				String timeFormatted = df.format(new Date(time*Utils.MILLISECONDS_PER_DAY));
				g2.setColor(Color.WHITE);
				
				g2.drawString(timeFormatted, x, _graphTop-_headerHeight + (DEFAULT_FONT_SIZE + _headerHeight)/2);
				g2.setColor(darkHeaderCol);
			}
		}
		
		// paint the scale on the top
		g2.setColor(darkHeaderCol);
		x = 0;
		for(int i = 0; x < mostRight; i++) {
			long time = _firstDay + i;
			x = timeToX(time);
			x2 = timeToX(time + skip*7);
			int dayInWeek = Utils.getDayInWeek(time);
			g2.setColor(darkHeaderCol);
			
			// Draw each day hashmark
			g2.setColor(darkHeaderCol);
			g2.drawLine(x, _graphTop-3, x, _graphTop-1);
		
		}		
		
		// paint today line
		long time = System.currentTimeMillis()/ Utils.MILLISECONDS_PER_DAY;
		g2.setColor(new Color(255,0,0,100));
		x = timeToX(time);
		g2.drawLine(x, _graphTop, x, _graphTop + _graphHeight);
		
	}
	
	/** Converts time to the component x-coordinate in pixels */
	int timeToX(long time) {
		int relativeTime = (int)(time - _firstDay);
		return _graphLeft + relativeTime * _dayWidth;
	}

	long xToTime(int x) {
		x+= _dayWidth/2; // we want the nearest day, not the one rounded down.
		return (x - _graphLeft) / _dayWidth + _firstDay;
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		recountBounds();
		Insets insets = getInsets();
		_verticalScroll.setBounds(
				getWidth() - _verticalScroll.getWidth() - insets.right,
				_graphTop+1,
				_verticalScroll.getPreferredSize().width,
				getHeight() - _graphTop - insets.bottom-2
				);
	}

	public void componentShown(ComponentEvent e) {
	}

	boolean _adjustingScrolls;
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(_adjustingScrolls) {
			return;
		}
		if(_contentBounds.height == -1) {
			return;
		}
		if(e.getSource() == _verticalScroll) {
			// helper variable
			Rectangle b = new Rectangle(_contentBounds);
			b.y-=10;
			b.height+=20;
			int top = _graphTop;
			int bottom = getHeight() - getInsets().bottom; // TODO + horizScroll.height
			int canScrollUp = Math.max(0, Math.max(bottom - b.y - b.height, top - b.y));
			
			int diff = _verticalScroll.getValue() - canScrollUp;
			_scrollTop -= diff;
			_builder.setPaintDirty();
			repaint();
		}
	}
	
	private void adjustScrolls() {
		// helper variable
		_adjustingScrolls = true;
		if(_contentBounds.height == -1) {
			return;
		}
		try {
			Rectangle b = new Rectangle(_contentBounds);
			b.y-=10;
			b.height+=20;
			int top = _graphTop;
			int bottom = getHeight() - getInsets().bottom; // TODO + horizScroll.height
			
			int canScrollDown = Math.max(0, Math.max(b.y-top, b.y + b.height - bottom));
			int canScrollUp = Math.max(0, Math.max(bottom - (b.y + b.height), top - b.y));
			
			//System.out.println(_contentBounds.y + ", " + contentBottom + ", " + top + ", " + bottom + " : " + canScrollUp + ", " + canScrollDown);
	        _verticalScroll.setMaximum(canScrollUp + canScrollDown);
	        _verticalScroll.setBlockIncrement((canScrollUp + canScrollDown) / 5);
	        _verticalScroll.setValue(canScrollUp);
			//System.out.println(_verticalScroll.getMinimum() + ", " + _verticalScroll.getMaximum() + ", " + _verticalScroll.getValue());
		} finally {
			_adjustingScrolls = false;
		}
	}
	
	public void deleteSelection() {
		_mouseHandler.deleteSelection();
	}

}
