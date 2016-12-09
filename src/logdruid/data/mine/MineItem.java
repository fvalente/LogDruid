package logdruid.data.mine;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.Recording;
import logdruid.data.record.ReportRecording;
import logdruid.engine.MineProcessor;
import logdruid.ui.MainFrame;

public class MineItem {
	BlockingQueue<ReportItem> mineQueue;
	int offset;
	FileRecord fileRecord;
	ArrayList<String[]> dataBlock;
	Map<Recording, String> recMatch1;
	Repository repo;
	Source source;
	boolean stats; 
	boolean timings; 
	boolean matches; 
	private static Logger logger = Logger.getLogger(MineItem.class.getName());
	
	public MineItem(BlockingQueue<ReportItem> _mineQueue, int _offset, FileRecord _fileRecord, ArrayList<String[]> _dataBlock, Map<Recording, String> _recMatch1, Repository _repo,
			Source _source, boolean _stats, boolean _timings, boolean _matches) {
		// TODO Auto-generated constructor stub
		mineQueue =_mineQueue;
		offset =_offset;
		fileRecord = _fileRecord;
		dataBlock = _dataBlock;
		recMatch1 = _recMatch1;
		repo = _repo;
		source = _source;
		stats = _stats; 
		timings = _timings; 
		matches = _matches; 
	}

	public Source getSource() {
		return source;
	}

	public BlockingQueue<ReportItem> getMineQueue() {
		return mineQueue;
	}

	public int getOffset() {
		return offset;
	}

	public FileRecord getFileRecord() {
		return fileRecord;
	}

	public ArrayList<String[]> getDataBlock() {
		return dataBlock;
	}

	public Map<Recording, String> getRecMatch1() {
		return recMatch1;
	}

	public Repository getRepo() {
		return repo;
	}

	public boolean isStats() {
		return stats;
	}

	public boolean isTimings() {
		return timings;
	}

	public boolean isMatches() {
		return matches;
	}
	
}
