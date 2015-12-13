/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014, 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.util;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import logdruid.data.record.Recording;

//import org.apache.log4j.Logger;

public class ColorCache {
//	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private Map<String, Color> colorMap = new HashMap<String, Color>();
	private Color[] colorArray;
	private int colorIndex=0;
	
	public ColorCache(Color[] colors) {
		colorArray=colors;
	}
	
public Color getColor(String color) {
	Color toReturn = colorMap.get(color);
	if (toReturn==null){
		if (colorIndex>=colorArray.length){
			colorIndex=0;
		}
		colorMap.put(color, colorArray[colorIndex++]);
		return colorMap.get(color);
	}else {
	return toReturn;
	}	
	}

}