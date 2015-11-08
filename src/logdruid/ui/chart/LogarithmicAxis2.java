package logdruid.ui.chart;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;

public class LogarithmicAxis2 extends LogarithmicAxis {

	public LogarithmicAxis2(String label) {
		super(label);
		// TODO Auto-generated constructor stub
	}
		   
		   public void autoAdjustRange(){
		      Plot plot = getPlot();
		      if(plot != null && plot instanceof ValueAxisPlot){
		         Range r = getDefaultAutoRange();
		         setRange(r, false, false);
		      }
		   }
		   
}
