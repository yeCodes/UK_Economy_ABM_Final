package peopleHouseholds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import industry.RegionIndustries;
import usefulMethods.CsvParser;

public class Household implements Comparable<Household> {
	
	private int id;
	private ArrayList<Integer> members =  new ArrayList<Integer>();
	private static boolean lockdownImposed = false;
	
	private int income;
	private int initialIncome;

	private double incomeAnnual;
	private double incomWeekly;
	private double incomeMonthly;
	
	private String initialStatus;
	private String currentStatus;
	
	private String[] statusArray = {"Working", "Mixed", "Workless"};
	
	private static double[][] normalSpendingHabitsPercentOn55Industries; //spendingHabitsOnIndustryThirdQuintilePattern;
	private static double[][] feelingPoorerSpendingHabitsPercentOn55Industries; 	//SecondQuintilePattern;
	private static double[][] lockdownSpendingHabitsPercentOn55Industries;
	private double[][] selectedSpendingHabitPercenton55Industries;
	
	private double[] annualHousehold55IndustryDemand;
	private double[] weeklyHousehold55IndustryDemand;
	private double[] monthlyHousehold55IndustryDemand;
	

	// internal state of household/ sentiment
	private boolean feelPoorer = false;
	private double expectIncomeRecovery;
	private double intrinsicFinancialOptimism; 
	
	private boolean furloughedMember = false;
	

	//private double spendingHabitsOnIndustryThirdQuintilePatternWeekly;
	//private double spendingHabitsOnIndustrySecondQuintilePatternWeekly;
	//private double spendingHabitsOnIndustryLockdownPatternWeekly;
	 public void readSpendingHabitsCsvs() {
		 
		 System.out.println("Reading in spending habits");
	    	//String lockdownSpendingFile = "src\\peopleHouseholds\\SpendingHabitsLockdownNoLabels.csv";	
	    	//String normalSpendingFile = "src\\peopleHouseholds\\SpendingHabitsMedianNoLabels.csv";	
	    	//String feelingPoorerSpendingFile = "src\\peopleHouseholds\\SpendingHabitsPoorerNoLabels.csv";	
	    	
	    	//String lockdownSpendingFile = "src\\peopleHouseholds\\SpendingHabitsLockdownNoLabelsZeroTest.csv";
	    	String lockdownSpendingFile = "src\\peopleHouseholds\\SpendingHabitsLockdownNoLabelsUnrounded.csv";	
	    	String normalSpendingFile = "src\\peopleHouseholds\\SpendingHabitsMedianNoLabelsUnrounded.csv";	
	    	String feelingPoorerSpendingFile = "src\\peopleHouseholds\\SpendingHabitsPoorerNoLabelsUnrounded.csv";	
	    	
	    	List<List<String>> lockdownSpending = usefulMethods.CsvParser.readCSVtoStringList(lockdownSpendingFile);
	    	List<List<String>> normalSpending= usefulMethods.CsvParser.readCSVtoStringList(normalSpendingFile);
	    	List<List<String>> feelingPoorerSpending= usefulMethods.CsvParser.readCSVtoStringList(feelingPoorerSpendingFile);
	    	
	    	lockdownSpendingHabitsPercentOn55Industries = usefulMethods.CsvParser.convertListtoArray(lockdownSpending);
	    	normalSpendingHabitsPercentOn55Industries = usefulMethods.CsvParser.convertListtoArray(normalSpending);
	    	feelingPoorerSpendingHabitsPercentOn55Industries = usefulMethods.CsvParser.convertListtoArray(feelingPoorerSpending);
	    	
	    	System.out.println("Reading complete");
	    }
	
	 /**
	  * Update weekly and monthly computed household income to reflect any changes in annual income level
	  */
	 public void updateWeeklyMonthlyIncomes() {
		 
		 System.out.println("updating annual weekly and monthly incomes to reflect any changes");
		 
		 this.incomeAnnual = (double) this.income;
		 System.out.println("annual income: " + this.incomeAnnual);
		 System.out.println("annual income integer: " + this.income);


		 int weeklyRoundedDown = (int) this.incomeAnnual/52;
		 int monthlyRoundedDown = (int) this.incomeAnnual/12;

		 this.incomWeekly = (double) weeklyRoundedDown;
		 this.incomeMonthly = (double) monthlyRoundedDown;
	 }
	 
	 /**
	  * compute household demand in £ given household spending pattern
	  */
	 public void computeHouseholdDemandGivenSpendingPattern() {
		 
		 // multiply each industry by income
		 double[][] tempSpendingPattern = this.selectedSpendingHabitPercenton55Industries;
		 
		 
		 int industries = tempSpendingPattern.length;
		 int householdSpendingCategories = tempSpendingPattern[0].length;

		 double[] annualDemandVectorFor55Industries = new double[industries];
		 double[] weeklyDemandVectorFor55Industries= new double[industries];
		 double[] monthlyDemandVectorFor55Industries= new double[industries];
		 
		 double annualRunningTotal;

		 
		 for(int industry = 0; industry< industries; industry++) {
			 
			 annualRunningTotal = 0;
			 
			 for(int householdSpendingCategory = 0; householdSpendingCategory< householdSpendingCategories; householdSpendingCategory++ ) {
				 
				 // multiply every figure by
				 annualRunningTotal = annualRunningTotal +
						 tempSpendingPattern[industry][householdSpendingCategory]*this.incomeAnnual; 
			 }
			 annualDemandVectorFor55Industries[industry] = (double) ((int)annualRunningTotal);	//rounded down
			 weeklyDemandVectorFor55Industries[industry] = (double) ((int)annualRunningTotal/52);
			 monthlyDemandVectorFor55Industries[industry] = (double) ((int)annualRunningTotal/12);
			 //System.out.println("Annual demand for industry " + industry +
			//		 " : "+  annualDemandVectorFor55Industries[industry] );
		 }
		 this.annualHousehold55IndustryDemand = annualDemandVectorFor55Industries;
		 this.weeklyHousehold55IndustryDemand = weeklyDemandVectorFor55Industries;
		 this.monthlyHousehold55IndustryDemand = monthlyDemandVectorFor55Industries;

	 }
	 
	 /**
	  * Select spending pattern/ array given current financial state of house + financial optimism
	  */
	 public double[][] chooseSpendingPatternGivenCurrentState(double behaviourChangeThreshold){
		 		
		 if(this.lockdownImposed == true) {
				//lockdown imposed
			 	// select lockdown spending
				System.out.println("Lockdown has been imposed");
			 	this.lockdownImposed = true;
			 	
				return this.lockdownSpendingHabitsPercentOn55Industries;
							
		 }
		 else{
			 // no lockdown has been imposed
			
			 if(this.income - this.initialIncome < 0) {
					// income has fallen
					 
				 	double likelihoodFeelPoorer = 1- this.intrinsicFinancialOptimism;
				 
					 // recovery expectations
					 //flip a coin 1 - 10
					Random rand = new Random();
					int randomNumber = rand.nextInt(9)+1; // random number between 1 and 10
					//double thresholdBehaviourChange = (double) randomNumber/0.1;
					double thresholdBehaviourChange = behaviourChangeThreshold; 
						if(thresholdBehaviourChange > likelihoodFeelPoorer) {
							// change behaviour
							// if 0.8 optimist and random number, only change behaviour to feel poorer 20% of time
							// lower likelihood of feeling poorer because optimistic about recovery prospects
							
							// select normal spending (do not change spending level)
							this.feelPoorer = false;
							System.out.println("Do not feel poorer");
							return this.normalSpendingHabitsPercentOn55Industries;
							
						}
						else {
							// select feeling poorer spending
							System.out.println("Feel poorer");
							this.feelPoorer = true;
							
							// rescalePoor spending
							return this.feelingPoorerSpendingHabitsPercentOn55Industries;
						}
						
			 }
			 else {
				 System.out.println("Do not feel poorer");
					return normalSpendingHabitsPercentOn55Industries;
				}
		 }
	 }
		
		 
	/**
	 * Rescale spending when feeling poor so that uses 90% of available salary
	 * @param rescaleFactor
	 */
	public void rescaleFeelingPoorSpendingToReducePerceivedIncome(double rescaleFactor) {
		
		double[][] rescaled = feelingPoorerSpendingHabitsPercentOn55Industries;
		int rows = rescaled.length;
		int columns = rescaled[0].length;
		
		for(int i = 0; i< rows; i++) {
			for(int j=0; j < columns; j++) {
				rescaled[i][j] = rescaled[i][j]* rescaleFactor; 
			}
		}
		System.out.println("Successfully rescaled");
		feelingPoorerSpendingHabitsPercentOn55Industries = rescaled;
	}

	 
	public void initialisieFinancialOptimismLevel() {
		// random variable
		// pick random variable between 3 and 8 --> skewed distribution, ideally
		Random rand = new Random();
		int randomNumber = rand.nextInt(4); // random number between 3 and 8
		
		double randomDoubleNumber = ((double) randomNumber)/10;
		
		this.intrinsicFinancialOptimism = randomDoubleNumber;
	}
	public double getIntrinsicFinancialOptimism() {
		return intrinsicFinancialOptimism;
	}
	
	public Household(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Integer> getMembers() {
		return members;
	}

	public void setMembers(ArrayList<Integer> members) {
		this.members = members;
	}

	public int getIncome() {
		return income;
	}

	public void setIncome(int income) {
		this.income = income;
	}

	public int getInitialIncome() {
		return initialIncome;
	}

	public void setInitialIncome(int initialIncome) {
		this.initialIncome = initialIncome;
	}
	
	public String getInitialStatus() {
		return initialStatus;
	}

	public void setInitialStatus(String initialStatus) {
		this.initialStatus = initialStatus;
	}
	
	public void setCurrentWorking(){
		this.currentStatus = this.statusArray[0];
	}
	public void setCurrentMixed(){
		this.currentStatus = this.statusArray[1];
	}
	public void setCurrentWorkless(){
		this.currentStatus = this.statusArray[2];
	}

	public void setWorking(){
		this.initialStatus = this.statusArray[0];
		this.currentStatus = this.statusArray[0];
	}
	public void setMixed(){
		this.initialStatus = this.statusArray[1];
		this.currentStatus = this.statusArray[1];
	}
	public void setWorkless(){
		this.initialStatus = this.statusArray[2];
		this.currentStatus = this.statusArray[2];
	}
	
	public double[] getAnnualHousehold55IndustryDemand() {
		return annualHousehold55IndustryDemand;
	}

	public void setAnnualHousehold55IndustryDemand(double[] annualHousehold55IndustryDemand) {
		this.annualHousehold55IndustryDemand = annualHousehold55IndustryDemand;
	}

	public double[] getWeeklyHousehold55IndustryDemand() {
		return weeklyHousehold55IndustryDemand;
	}

	public void setWeeklyHousehold55IndustryDemand(double[] weeklyHousehold55IndustryDemand) {
		this.weeklyHousehold55IndustryDemand = weeklyHousehold55IndustryDemand;
	}

	public double[] getMonthlyHousehold55IndustryDemand() {
		return monthlyHousehold55IndustryDemand;
	}

	public void setMonthlyHousehold55IndustryDemand(double[] monthlyHousehold55IndustryDemand) {
		this.monthlyHousehold55IndustryDemand = monthlyHousehold55IndustryDemand;
	}


	public double[][] getSelectedSpendingHabitPercenton55Industries() {
		return selectedSpendingHabitPercenton55Industries;
	}

	public void setSelectedSpendingHabitPercenton55Industries(double[][] selectedSpendingHabitPercenton55Industries) {
		this.selectedSpendingHabitPercenton55Industries = selectedSpendingHabitPercenton55Industries;
	}

	public static double[][] getNormalSpendingHabitsPercentOn55Industries() {
		return normalSpendingHabitsPercentOn55Industries;
	}

	public static void setNormalSpendingHabitsPercentOn55Industries(double[][] normalSpendingHabitsPercentOn55Industries) {
		Household.normalSpendingHabitsPercentOn55Industries = normalSpendingHabitsPercentOn55Industries;
	}

	public static double[][] getFeelingPoorerSpendingHabitsPercentOn55Industries() {
		return feelingPoorerSpendingHabitsPercentOn55Industries;
	}

	public static void setFeelingPoorerSpendingHabitsPercentOn55Industries(
			double[][] feelingPoorerSpendingHabitsPercentOn55Industries) {
		Household.feelingPoorerSpendingHabitsPercentOn55Industries = feelingPoorerSpendingHabitsPercentOn55Industries;
	}
	
	public boolean isFurloughedMember() {
		return furloughedMember;
	}

	public void setFurloughedMember(boolean furloughedMember) {
		this.furloughedMember = furloughedMember;
	}
	public static double[][] getLockdownSpendingHabitsPercentOn55Industries() {
		return lockdownSpendingHabitsPercentOn55Industries;
	}

	public static void setLockdownSpendingHabitsPercentOn55Industries(
			double[][] lockdownSpendingHabitsPercentOn55Industries) {
		Household.lockdownSpendingHabitsPercentOn55Industries = lockdownSpendingHabitsPercentOn55Industries;
	}

	public static boolean isLockdownImposed() {
		return lockdownImposed;
	}

	public static void setLockdownImposed(boolean lockdownImposed) {
		Household.lockdownImposed = lockdownImposed;
	}

	@Override
	public int compareTo(Household o) {
		// TODO Auto-generated method stub
		return this.id - o.getId();
	}
	
	
}
