package logdruid.data.mine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MineData {
	public final List<FileMineResult> fileMineResultArray =   Collections.synchronizedList( new ArrayList<FileMineResult>());
	int blocks=0;
	
	public MineData() {
		// TODO Auto-generated constructor stub
	}
	public void clear(){
		fileMineResultArray.clear();
	}
	public void setBlocks(int blocks) {
		this.blocks = blocks;
	}
	public int getBlocks() {
		return blocks;
	}
}
