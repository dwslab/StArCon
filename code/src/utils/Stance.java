package utils;

public enum Stance {
	CON, PRO, UNKNOWN;
	
	public Stance toggle() {
		if(this == CON) {
			return PRO;
		} else if(this == PRO){
			return CON;
		} else {
			return UNKNOWN;
		}
	}
}