package logdruid.data;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;

public class ExtendedTimeSeries {

	TimeSeries timeSeries;
	int[] stat={0,0};

	public ExtendedTimeSeries() {
		// TODO Auto-generated constructor stub
	}

	public ExtendedTimeSeries(TimeSeries ts, int[] stats) {
		stat = stats;
		timeSeries = ts;
		// TODO Auto-generated constructor stub
	}

	public ExtendedTimeSeries(String name, Class<FixedMillisecond> class1) {
		timeSeries = new TimeSeries(name, class1);
		// TODO Auto-generated constructor stub
	}

	public TimeSeries getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(TimeSeries timeSeries) {
		this.timeSeries = timeSeries;
	}

	public int[] getStat() {
		return stat;
	}

	public void setStat(int[] stat) {
		this.stat = stat;
	}

}
