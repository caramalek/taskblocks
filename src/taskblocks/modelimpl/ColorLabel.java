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
		new ColorLabel("None", Colors.TASK_COLOR, 0),
		new ColorLabel("Red", new Color(233,87,73), 1),
		new ColorLabel("Orange", new Color(253,143,28), 2),
		new ColorLabel("Yellow", new Color(254,236,104), 3),
		new ColorLabel("Green", new Color(10,198,93), 4),
		new ColorLabel("Silver", new Color(200,200,200), 5),
		new ColorLabel("Gold", new Color(254,195,65), 6),
		new ColorLabel("Sky Blue", new Color(174,209,255), 7),
		new ColorLabel("Aqua", new Color(98,201,198), 8),
		new ColorLabel("Lilac", new Color(193,170,245), 9),
		new ColorLabel("Lime", new Color(182,215,57), 10),
		new ColorLabel("Taupe", new Color(214,192,176), 11),
		new ColorLabel("Rose", new Color(244,133,160), 12),
		new ColorLabel("Purple", new Color(156,85,222), 13),
		new ColorLabel("Pink", new Color(253,195,203), 14),
		new ColorLabel("Gray", new Color(130,130,130), 15),
		new ColorLabel("Magenta", new Color(220,85,222), 32),
		new ColorLabel("Teal", new Color(50,156,165), 34),
		new ColorLabel("Brown", new Color(196,126,62), 36),
		new ColorLabel("BlueLite", new Color(177,195,251), 16),
		new ColorLabel("RedLite", new Color(244,171,164), 17),
		new ColorLabel("OrangeLite", new Color(254,199,141), 18),
		new ColorLabel("YellowLite", new Color(254,245,179), 19),
		new ColorLabel("GreenLite", new Color(132,226,174), 20),
		new ColorLabel("SilverLite", new Color(227,227,227), 21),
		new ColorLabel("GoldLite", new Color(254,225,160), 22),
		new ColorLabel("SkyBlueLite", new Color(214,232,255), 23),
		new ColorLabel("AquaLite", new Color(176,228,226), 24),
		new ColorLabel("LilacLite", new Color(224,212,250), 25),
		new ColorLabel("LimeLite", new Color(218,235,156), 26),
		new ColorLabel("TaupeLite", new Color(234,223,215), 27),
		new ColorLabel("RoseLite", new Color(249,194,207), 28),
		new ColorLabel("PurpleLite", new Color(205,170,238), 29),
		new ColorLabel("PinkLite", new Color(254,225,229), 30),
		new ColorLabel("GrayLite", new Color(192,192,192), 31),
		new ColorLabel("MagentaLite", new Color(237,170,238), 33),
		new ColorLabel("TealLite", new Color(152,205,210), 35),
		new ColorLabel("BrownLite", new Color(225,190,158), 37)
		
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
