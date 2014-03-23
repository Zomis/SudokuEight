package net.zomis.common;

public final class ImmutablePoint {

	private final int x;
	private final int y;

	public ImmutablePoint(ImmutablePoint pos) {
		this(pos.getX(), pos.getY());
	}
	
	public ImmutablePoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ImmutablePoint))
			return false;
		ImmutablePoint other = (ImmutablePoint) obj;
		return x == other.getX() && y == other.getY();
	}
	
    public ImmutablePoint dxdy(int dx, int dy) {
    	return new ImmutablePoint(x + dx, y + dy);
    }
    
    public ImmutablePoint dxdy(ImmutablePoint delta) {
    	return new ImmutablePoint(x + delta.getX(), y + delta.getY());
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
