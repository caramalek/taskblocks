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

package taskblocks.modelimpl;

import java.awt.Color;

import taskblocks.utils.Colors;

public class TaskImpl implements Cloneable {
	
	private String _name;
	private long _startTime;
	private long _effort;
    private long _workedTime;
	private TaskImpl[] _predecessors = new TaskImpl[0];
	private ManImpl _man;
	private String _comment;
	private String _objective;
	
	private ColorLabel _colorLabel;
	
	/** Used for bugzilla export */
	private String _bugId;
	
	/** Used only when saving */
	public String _id;
	
	public TaskImpl clone() {
		try {
			TaskImpl t = (TaskImpl)super.clone();
			return t;
		} catch(CloneNotSupportedException e) {
			// NEVER GET HERE
			throw new RuntimeException(e);
		}
	}
	
	public void updateFrom(TaskImpl t) {
		_name = t._name;
		_bugId = t._bugId;
		_startTime = t._startTime;
		_effort = t._effort;
		_workedTime = t._workedTime;
		_predecessors = t._predecessors;
		_man = t._man;
		_comment = t._comment;
		_colorLabel = t._colorLabel;
		_objective = t._objective;
	}
	
	public long getEffort() {
		return _effort;
	}
	public void setEffort(long effort) {
		if(effort < 1) {
			effort = 1;
		}
		this._effort = effort;
	}
    
	public long getWorkedTime() {
		return _workedTime;
	}
	
	public void setWorkedTime(long workedTime) {
		if(workedTime < 0) {
			workedTime = 0;
		}
		this._workedTime = workedTime;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String _name) {
		this._name = _name;
	}
	
	public long geStartTime() {
		return _startTime;
	}
	
	public void setStartTime(long time) {
		_startTime = time;
	}
	
	public long getStartTime() {
		return _startTime;
	}
	
	public TaskImpl[] getPredecessors() {
		return _predecessors;
	}
	
	public void setPredecessors(TaskImpl[] preds) {
		_predecessors = preds;
	}
	
	public void setMan(ManImpl man) {
		_man = man;
	}
	public ManImpl getMan() {
		return _man;
	}
	
	public Color getColor() {
		if(_colorLabel == null) {
			return Colors.TASK_COLOR;
		} else {
			return _colorLabel._color;
		}
	}
	
	public ColorLabel getColorLabel() {
		return _colorLabel;
	}
	public void setColorLabel(ColorLabel cl) {
		_colorLabel = cl;
	}
	
	public void setComment( String comment ){
		if( comment == null ){
			comment = "";
		}
		_comment = comment;
	}
	
	public String getComment(){
		return _comment;
	}
	
	public void setObjective( String objective ){
		if( objective == null ){
			objective = "";
		}
		_objective = objective;
	}
	
	public String getObjective(){
		return _objective;
	}
	
	public String getBugId() {
		return _bugId;
	}
	
	public void setBugId(String bugId) {
		_bugId = bugId;
	}
	
	public double getWorkload() {
		return _man.getWorkload();
	}
	
	public String toString() {
		return "<" + _name + ">";
	}
}
