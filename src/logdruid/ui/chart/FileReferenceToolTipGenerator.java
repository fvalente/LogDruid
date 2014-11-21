package logdruid.ui.chart;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

public class FileReferenceToolTipGenerator implements XYToolTipGenerator {

	public FileReferenceToolTipGenerator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
        DecimalFormat form = new DecimalFormat("#,##0.00"); 
        //new DecimalFormat("00.0"); 
        SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss");
        Number x = arg0.getX(arg1, arg2);
        return (sdf.format(x))
        + "-----------------------------------\n"
        + "Maxi\t\t: " + form.format(arg0.getYValue(0, arg2)) + "\n"
      //  + "Moyenne\t: " + form.format(arg0.getYValue(1, arg2)) + "\n"
      //  + "Mini\t\t: " + form.format(arg0.getYValue(0, arg2)) + "\n"
      //  + "Temp\t\t: " + arg0.getYValue(3, arg2) 
        //+ "\n"
        ;
	}

}
