package alan.chessgame_f.util;

public class Coordinate2D {
	
	private final static int hashStart = 0x1234ABCD;
	
	public final int x;
	public final int y;
	
	public Coordinate2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Coordinate2D) {
			Coordinate2D coord = (Coordinate2D) obj;
			if(coord.x != x) return false;
			else if(coord.y != y) return false;
			else return true;
		}else return false;
	}
	
	@Override
	public int hashCode() {
		int hash = hashStart;
		hash = (hash>>5)^(hash<<27)^x;
		hash = (hash>>5)^(hash<<27)^y;
		/* 
		 * If you want to make it more like an unpredictable hash, remove the annotation
		 * with remaining the code to make it compile.
		hash += hash<<13;
		hash ^= hash>>7;
		hash += hash<<3;
		hash ^= hash>>17;
		hash += hash<<5; 
		 */
		return hash;
	}
	
	public Coordinate2D add(Coordinate2D addon) {
		return new Coordinate2D(x + addon.x, y + addon.y);
	}
	
	public Coordinate2D minus(Coordinate2D subtrahend) {
		return new Coordinate2D(x - subtrahend.x, y - subtrahend.y);
	}
	
	public Coordinate2D invert(){
		return new Coordinate2D(-x, -y);
	}

}
