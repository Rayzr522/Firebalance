package hydrogenn.firebalance;

import java.util.ArrayList;

public class ChunkSpec {
	int x;
	int y;
	int z;
	byte nation;
	String owner;
	boolean national;
	boolean outpost;
	ArrayList<String> shared;
	public ChunkSpec (int x, int y, int z, byte nation, String owner, boolean national, boolean outpost, ArrayList<String> shared) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.nation = nation;
		this.owner = owner;
		this.national = national;
		this.outpost = outpost;
		this.shared = shared;
	}
	public ChunkSpec(int x, int y, int z, byte nation, String owner, boolean national, boolean outpost) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.nation = nation;
		this.owner = owner;
		this.national = national;
		this.outpost = outpost;
		this.shared = new ArrayList<String>();
	}
}