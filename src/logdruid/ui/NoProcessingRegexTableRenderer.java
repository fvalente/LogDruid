/*******************************************************************************
 * LogDruid : Generate charts and reports using data gathered in log files
 * Copyright (C) 2016 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

public class NoProcessingRegexTableRenderer extends DefaultTableCellRenderer {
	private static Logger logger = Logger.getLogger(NoProcessingRegexTableRenderer.class.getName());
	 @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);	
	        
	        //inside type
	        Object type = table.getValueAt(row, 2);
	        Object regex = table.getValueAt(row, 3);
	        
	        if  (column==1 || column==4){
       		 Color clr = new Color(233, 255, 229);
       		 component.setBackground(clr);
	        }
	        
	        if (type != null && column==3) {
	        	if(((String)type).equals("manual")){
	     	        	//logger.info(((String)type));
	        		 Color clr = new Color(233, 255, 229);
	        		 component.setBackground(clr);
	        	}else {
	        		  Color clr = new Color(255, 229, 229);
		        		 component.setBackground(clr);
	        	}
	        }
	        return component;
	    }
}