package utils;

public class Methods {

	public static double abs(double d) {
		if(d > 0) {
			return d;
		} else {
			return -d;
		}
	}
	
	public static int sgn(double d) {
		if(d > 0.0) {
			return 1;
		} else if(d < 0.0) {
			return -1;
		} else {
			return 0;
		}
	}
}
