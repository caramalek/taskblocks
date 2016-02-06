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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import taskblocks.utils.Colors;

public class ColorLabel {
	
	public static ColorLabel[] COLOR_LABELS = new ColorLabel[] {
		new ColorLabel("Blue", Colors.TASK_COLOR, 0),
		new ColorLabel("Red", new Color(206,32,32), 1),
		new ColorLabel("Orange", new Color(246,121,54), 2),
		new ColorLabel("Yellow", new Color(255, 235,0), 3),
		new ColorLabel("Green", new Color(0,158,25), 4),
		new ColorLabel("Silver", new Color(221,221,221), 5),
		new ColorLabel("Gold", new Color(246,181,35), 6),
		new ColorLabel("Sky", new Color(212,220,247), 7),
		new ColorLabel("Aqua", new Color(15,207,192), 8),
		new ColorLabel("Lilac", new Color(196,179,239), 9),
		new ColorLabel("Lime", new Color(140,214,0), 10),
		new ColorLabel("Taupe", new Color(255,228,207), 11),
		new ColorLabel("Rose", new Color(202,99,119), 12),
		new ColorLabel("Purple", new Color(149,70,222), 13),
		new ColorLabel("Pink", new Color(255,214,222), 14),
		new ColorLabel("Gray", new Color(90,90,90), 15),        
		new ColorLabel("Magenta", new Color(207,53,209), 16),
		new ColorLabel("Teal", new Color(23,132,167), 17),
		new ColorLabel("Brown", new Color(167,116,70), 18),
		new ColorLabel("Cornflower", new Color(155,178,252), 19),
		new ColorLabel("Raspberry", new Color(142,6,58), 20),
		new ColorLabel("Peach", new Color(255,183,148), 21),
		new ColorLabel("Butter", new Color(255,241,159), 22),
		new ColorLabel("Seafoam", new Color(187,232,226), 23),
		new ColorLabel("Smoke", new Color(162,162,162), 24),
		new ColorLabel("Cerulean", new Color(73,110,226), 25),
		new ColorLabel("Army", new Color(125,146,86), 26),
		new ColorLabel("Cucumber", new Color(208,247,137), 27),
		new ColorLabel("Periwinkle", new Color(196,179,239), 28),
		new ColorLabel("Powder", new Color(131,194,220), 29),
		new ColorLabel("Charcoal", new Color(65,65,65), 30),
		new ColorLabel("White", new Color(255,255,255), 31)
		
	};
	
	final public Color _color;
	final public String _name;
	final public Icon  _icon;
	final public int _index;
	public ColorLabel(String name, Color color, int index) {
		_name = name;
		_color = color;
		_index = index;
		BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)img.getGraphics();
		g2.setColor(_color);
		g2.fillRect(0,0,img.getWidth(), img.getHeight());
		_icon = new ImageIcon(img);
	}
	public String toString() {
		return _name;
	}
}
