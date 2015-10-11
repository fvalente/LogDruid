/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.ui.chart;

import java.text.DecimalFormat;
import org.apache.commons.lang3.time.FastDateFormat;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

public class FileReferenceToolTipGenerator implements XYToolTipGenerator {
	FastDateFormat fdf = null;
	DecimalFormat form = null;
	Number x;

	public FileReferenceToolTipGenerator() {
		// TODO Auto-generated constructor stub
		fdf.getInstance("d-MMM-yyyy HH:mm:ss");
		form = new DecimalFormat("#,##0.00");
	}

	@Override
	public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

		x = arg0.getX(arg1, arg2);
		return (fdf.format(x)) + "-----------------------------------\n" + "Maxi\t\t: " + form.format(arg0.getYValue(0, arg2)) + "\n"
		// + "Moyenne\t: " + form.format(arg0.getYValue(1, arg2)) + "\n"
		// + "Mini\t\t: " + form.format(arg0.getYValue(0, arg2)) + "\n"
		// + "Temp\t\t: " + arg0.getYValue(3, arg2)
		// + "\n"
		;
	}

}
