/**
 * 
 */
package gexfWebservice;

/**
 * @author joerg
 *
 */
public class Tools {
	/**
	 * This methods rounds a given double value 
	 * @param value
	 * @return rounded double value
	 */
	public static double roundTwoD(double value) {
		double result = value * 100;
		result = Math.round(result);
		result = result / 100;
		return result;
	}
	
}
