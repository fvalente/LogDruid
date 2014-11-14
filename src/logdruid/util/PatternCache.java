package logdruid.util;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PatternCache {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private static HashMap<String, Pattern> pattern = new HashMap<String, Pattern>();

	synchronized public static Pattern getPattern(String regexp) {
		if (!pattern.containsKey(regexp)) {
			pattern.put(regexp, Pattern.compile(regexp));
		}
		return pattern.get(regexp);
	}

	synchronized public static int getSize() {
		logger.info("pattern cache content" + pattern);
		return pattern.size();

	}

}