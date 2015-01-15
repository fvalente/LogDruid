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
