/**
 * Liquid Object used to hold the name of the drink and the amount
 * @author Cameron
 */


public class Liquid {
  /*
    A class representing each liquid in the system
  */
	private String liquidName;
	private double drinkStrength;
	private double ounce;
	
	
	public Liquid (){
		// TODO Auto-generated constructor stub
	}
	
	public Liquid (String liquidName, double drinkStrength, double ounce){
		// TODO Auto-generated constructor stub
		super();
		this.liquidName = liquidName;
		this.drinkStrength = drinkStrength;
		this.ounce = ounce;
	}

	public String getLiquidName() {
		return liquidName;
	}

	public void setLiquidName(String liquidName) {
		this.liquidName = liquidName;
	}

	public double getDrinkStrength() {
		return drinkStrength;
	}

	public void setDrinkStrength(double drinkStrength) {
		this.drinkStrength = drinkStrength;
	}

	public double getOunce() {
		return ounce;
	}

	public void setOunce(double ounce) {
		this.ounce = ounce;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name:" + getLiquidName()+",");
		sb.append("Strength:" + getDrinkStrength()+",");
		sb.append("Ounces:" + getOunce()+"\n");
		
		return sb.toString();		
	}
}
