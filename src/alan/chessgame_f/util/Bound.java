package alan.chessgame_f.util;

public final class Bound {
	
	private final static int hashStart = 0x1234ABCD;
	
	public final int maxX;
	public final int maxY;
	public final int minX;
	public final int minY;
	
	public Bound(Coordinate2D boundA, Coordinate2D boundB) {
		if(boundA.x > boundB.x) {
			maxX = boundA.x;
			minX = boundB.x;
		} else {
			maxX = boundB.x;
			minX = boundA.x;
		}
		if(boundA.y > boundB.y) {
			maxY = boundA.y;
			minY = boundB.y;
		} else {
			maxY = boundB.y;
			minY = boundA.y;
		}
	}
	
	private Bound(int maxX, int maxY, int minX, int minY) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.minX = minX;
		this.minY = minY;
	}
	
	public boolean isIn(Coordinate2D coord) {
		return coord.x <= maxX && coord.x >= minX && coord.y <= maxY && coord.y >= minY;
	}
	
	public boolean isXIn(int x) {
		return x <= maxX && x >= minX;
	}
	
	public boolean isYIn(int y) {
		return y <= maxY && y >= minY;
	}
	
	public Bound and(Bound bound) {
		return new Bound(
				maxX > bound.maxX ? bound.maxX : maxX,
				maxY > bound.maxY ? bound.maxY : maxY,
				minX < bound.minX ? bound.minX : minX,
				minY < bound.minY ? bound.minY : minY
				);
	}
	
	public Bound or(Bound bound) {
		return new Bound(
				maxX < bound.maxX ? bound.maxX : maxX,
				maxY < bound.maxY ? bound.maxY : maxY,
				minX > bound.minX ? bound.minX : minX,
				minY > bound.minY ? bound.minY : minY
				);
	}
	
	public Bound add(Coordinate2D coord) {
		return new Bound(
				maxX + coord.x, maxY + coord.y, minX + coord.x, minY + coord.y
				);
	} 
	
	@Override
	public int hashCode(){
		int hash = hashStart;
		hash = (hash>>5)^(hash<<27)^maxX;
		hash = (hash>>5)^(hash<<27)^maxY;
		hash = (hash>>5)^(hash<<27)^minX;
		hash = (hash>>5)^(hash<<27)^minY;
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Bound) {
			Bound bound = (Bound) obj;
			if(bound.maxX != maxX) return false;
			else if(bound.maxY != maxY) return false;
			else if(bound.minX != minX) return false;
			else if(bound.minY != minY) return false;
			else return true;
		}else return false;
	}
	
}
