package peopleHouseholds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

//import people.EconomicallyActive;

public class RegionHouseholds {

private String name;
private int id;
private static boolean lockdownImposed = false;

private ArrayList<Household> households = new ArrayList<Household>(); 
private ArrayList<Integer> retiredHhld = new ArrayList<Integer>();
private ArrayList<Integer> longTermUnemployedHhld = new ArrayList<Integer>();

private ArrayList<Integer> economicallyActiveHhld = new ArrayList<Integer>();
private ArrayList<Integer> shortTermUnemployedHhld = new ArrayList<Integer>();
private ArrayList<Integer> mixedHlhd = new ArrayList<Integer>();
private ArrayList<Integer> employedHhld = new ArrayList<Integer>();

private int householdWithFurloughedMembers = 0;

private int shortTermUnemployedHhldCount = 0;
private int mixedHlhdCount = 0;
private int employedHhldCount = 0;

private ArrayList<Person> populationSet = new ArrayList<Person>();
private String[] labels = {"employed", "short term unemployed", "long term unemployed", "retired"};

private ArrayList<Integer> longTermUnemployedSet = new ArrayList<Integer>();	// there should be no duplicates
private ArrayList<Integer> retiredSet= new ArrayList<Integer>();
private ArrayList<Integer> employedSet= new ArrayList<Integer>();
private ArrayList<Integer> shortTermUnemployedList= new ArrayList<Integer>();
private int populationExcludingChildren;


double pctRetired = 20;
double pctChildren = 20;
double pctWorkingAge = 100 - pctRetired - pctChildren;


double pctEconomicallyActive  = 60;
double pctEmployed = 76;
double pctShortTermUnemployed = 4;
double pctWorkless = 100 - pctEmployed - pctShortTermUnemployed;

private int economicallyActivePopulation;
private int employedPopulation;
private int shortTermUnemployedPopulation;
private int retiredPopulation;
private int longTermUnemployedPopulation;

private static double[] annualSumOfAllHouseholdDemandFor55Industries;
private static double[] annualSumOfAllHouseholdDemandFor21Industries = new double[21];

private double[] weeklySumOfAllHouseholdDemandFor55Industries;
private double[] weeklySumOfAllHouseholdDemandFor21Industries = new double[21];

private double[] monthlySumOfAllHouseholdDemandFor55Industries;
private double[] monthlySumOfAllHouseholdDemandFor21Industries = new double[21];

public static void main(String[] args) throws Exception {
	
	RegionHouseholds ukHouseholds = new RegionHouseholds("UK", 1000, 8000, 20000, 12000);
}

public RegionHouseholds(String name, double population, int unemployedIncome, int retiredIncome, int longTermUnemployed) throws Exception {
	this.id = 0;
	this.name = name;
	
	// create people and sort people in this population
	this.initialisePopulationCounts(population);
	this.createPeople(unemployedIncome, retiredIncome, longTermUnemployed);
	this.sortWholePopulation();
	
	// create households + sort people into households
		// to strings needed
	int householdNumber = createWorkingAndMixedHouseholds();
	System.out.println("******household number is " + householdNumber);
	householdNumber = createRetiredHouseholds(householdNumber);
	System.out.println("******household number is " + householdNumber);
	householdNumber = createLongTermUnemployedHouseholds(householdNumber);
	System.out.println("******household number is " + householdNumber);
	
	sortEconomicallyActiveHouseholds(this.households, this.populationSet);
	
	determineHhldStatus();
	
	//computeOverallHouseholdDemand();
	
	//setHhldIncomeAnddisplay();
	//setHouseholdInitialIncomes();
	
	//initialiseRetiredAndLongTermWages();
	
	//computeHouseholdIncome();
}

public void imposeLockdownMeasures() {
	this.lockdownImposed = true;
}

public void removeLockdownMeasures() {
	this.lockdownImposed = false;
}

public void computeAnnualWeeklyMonthlyDemand() {
	double[] annual55 = this.annualSumOfAllHouseholdDemandFor55Industries;
	double[] annual21 = this.annualSumOfAllHouseholdDemandFor21Industries;
	
	for(int i = 0; i < annual55.length; i++) {
		this.weeklySumOfAllHouseholdDemandFor55Industries[i] = annual55[i]/52;
		this.monthlySumOfAllHouseholdDemandFor55Industries[i] = annual55[i]/12;	
	}
	
	for(int j = 0; j < annual21.length; j++) {
		this.weeklySumOfAllHouseholdDemandFor21Industries[j] = annual21[j]/52;
		this.monthlySumOfAllHouseholdDemandFor21Industries[j] = annual21[j]/12;
	}
	
	System.out.println("computing annual, weekly, monthly demand based on current figures");
	
}
public void map55IndustriesTo21Industries(String[] industryNames) {
	// 3, 1 , 19, 1, 2,1,3,5, 1, 4, 3,1,5,1,1,1,1 Q, 0.5, 0.5,1
	int[] map55To21 = {3,1,19,1,2,1,3,5,1,4,3,1,5,1,1,1,1,-1,-1,1};
	int industries = map55To21.length;
	
	double sumDemand;
	int count = 0;
	
	int position;
	int numberOfIndustriesInGroup;
	
	for(int industryGroup = 0; industryGroup < industries;) {
		numberOfIndustriesInGroup = map55To21[industryGroup];
		
		sumDemand = 0;
		
		if(numberOfIndustriesInGroup > 0) {
			for(int industry = 0; industry < numberOfIndustriesInGroup; industry++) {
				sumDemand += this.annualSumOfAllHouseholdDemandFor55Industries[industry + count];
			}
			
		this.annualSumOfAllHouseholdDemandFor21Industries[industryGroup] = sumDemand;	
		count += numberOfIndustriesInGroup;
		industryGroup +=1;
		}
		else {
			// -1
			// store + run 2 loops
			// industries R and S
			//set R and set S
			// increment counter by 2
			
			
			double store = this.annualSumOfAllHouseholdDemandFor55Industries[count] * 0.5; 
			
			this.annualSumOfAllHouseholdDemandFor21Industries[industryGroup] = store;
			this.annualSumOfAllHouseholdDemandFor21Industries[industryGroup + 1] = store;
			
			count += 1;	// advance offset on large vector of 55 industries by 1 as past RS now
			industryGroup += 2; // advance index on vector of 21 industires by 2!!
		}
	}
	
	// Set final industry U to 0 --> ignoring extra-territorial!!
	this.annualSumOfAllHouseholdDemandFor21Industries[20] = 0;

	System.out.println("Mapping process to annual complete");
	for(int i = 0; i < 21; i++ ) {
		System.out.println("Industry annual sum of all demand" + i + " demand: "+ this.annualSumOfAllHouseholdDemandFor21Industries[i]);
		System.out.println( industryNames[i] + " annual sum of all demand: "+ this.annualSumOfAllHouseholdDemandFor21Industries[i]);
	}
}

public void updateAnnualIncomesToReflectChanges() {
	
	for(Household householdNumber: this.households) 
		householdNumber.updateWeeklyMonthlyIncomes();

}
public void computeOverallHouseholdDemand(double rescaleFeelingPoorSpending, double behaviourChangeThreshold) {
	
	
	// compute demand for each house
	this.households.get(0).readSpendingHabitsCsvs();
	this.households.get(0).rescaleFeelingPoorSpendingToReducePerceivedIncome(rescaleFeelingPoorSpending);

	//this.sumOfAllHouseholdDemandFor55Industries;
	int industries = households.get(0).getFeelingPoorerSpendingHabitsPercentOn55Industries().length;
	double[] sumOfAllAnnualDemand = new double[industries];
	
	for(double entry:sumOfAllAnnualDemand)
		entry = 0;
	
	double[] sumOfAllWeeklyDemand = sumOfAllAnnualDemand;
	double[] sumOfAllMonthlyDemand = sumOfAllAnnualDemand;

	
	for(Household householdNumber: this.households) {
		householdNumber.updateWeeklyMonthlyIncomes();
		double[][] selectedSpendingPattern = householdNumber.chooseSpendingPatternGivenCurrentState(behaviourChangeThreshold);
		householdNumber.setSelectedSpendingHabitPercenton55Industries(selectedSpendingPattern);
		householdNumber.computeHouseholdDemandGivenSpendingPattern();
		System.out.println("Now have annual annualHousehold55IndustryDemand for each house");

		
		for(int industry = 0; industry < industries; industry++) {
			// add demand to total demand matrix
			sumOfAllAnnualDemand[industry] +=
					householdNumber.getAnnualHousehold55IndustryDemand()[industry];
			sumOfAllWeeklyDemand[industry] +=
					householdNumber.getWeeklyHousehold55IndustryDemand()[industry];
			sumOfAllMonthlyDemand[industry] +=
					householdNumber.getMonthlyHousehold55IndustryDemand()[industry];
					
		}
		
	}
	//add to sumOfAllHouseholdDemand
	System.out.println("Now have to annualHousehold55IndustryDemand for all houses");
			this.annualSumOfAllHouseholdDemandFor55Industries = sumOfAllAnnualDemand;
			this.weeklySumOfAllHouseholdDemandFor55Industries = sumOfAllWeeklyDemand;
			this.monthlySumOfAllHouseholdDemandFor55Industries = sumOfAllMonthlyDemand;
	
//	int count = 0;
//	for(double entry:sumOfAllAnnualDemand) {
//		count +=1;
//		System.out.println("Industry " + count + " annual demand: " + entry);
//		
//	}
//	
//	count = 0;
//	for(double entry:sumOfAllMonthlyDemand) {
//		count +=1;
//		System.out.println("Industry " + count + " Monthly demand: " + entry);
//		
//	}
//	
//	count = 0;
//	for(double entry:sumOfAllWeeklyDemand) {
//		count +=1;
//		System.out.println("Industry " + count + " Monthly demand: " + entry);
//		
//	}
}

public void setHouseholdInitialIncomes() {
	
	//for each house, if intiial income = =1, set to equal income
	for(Household householdNumber: households) {
		
		//set Optimism level
		householdNumber.initialisieFinancialOptimismLevel();
		System.out.println("Financial Optimism Level is: " + householdNumber.getIntrinsicFinancialOptimism());
		if(householdNumber.getInitialIncome() == -1)
			System.out.println(" household income is: " + householdNumber.getIncome());
			householdNumber.setInitialIncome(householdNumber.getIncome());
		
		System.out.println("Initial income is: " + householdNumber.getInitialIncome());
	}
}

/**
 * this method creates mixed and working households
 * It combines short term unemployed people and employed people
 * and then distributes them randomly into households based on 
 * some distribution
 */
public int createWorkingAndMixedHouseholds() {

	// combine employed and short term unemployed people to make new group
	ArrayList<Integer> ableToWork = new ArrayList<Integer>();
	ableToWork.addAll(shortTermUnemployedList);
	System.out.println("size of unempl set is" + ableToWork.size());
	ableToWork.addAll(employedSet);
	
	System.out.println("size of combined set is " + ableToWork.size());
	
	
	Random rand = new Random();
	int householdNumber = 0;
	
	//do until ableToWork empty
	while(ableToWork.isEmpty()== false) {
		int randomNumber = rand.nextInt(8)+1; // random number between 1 and 8
		
		if(ableToWork.size() >= 3) {
			if(randomNumber == 1 ||randomNumber ==  2) {
				//pick 1 person
				int people = 1;
				
				// create new household
				Household newHouse = new Household(householdNumber);

				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(ableToWork.size());
					//get random person 
					int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				
				//update householdNumber
				householdNumber += 1;
			}
			else if(randomNumber == 3 ||randomNumber ==  4||randomNumber ==  5 ||randomNumber ==  6) {
				// pick 2 people
				int people = 2;
				
				// create new household
				Household newHouse = new Household(householdNumber);

				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(ableToWork.size());
					//get random person 
					int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				
				//update householdNumber
				householdNumber += 1;
			}
			else {
				// pick 3 people
				int people = 3;
				
				// create new household
				Household newHouse = new Household(householdNumber);

				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(ableToWork.size());
					//get random person 
					int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				
				//update householdNumber
				householdNumber += 1;
			}
		} 
		else if (ableToWork.size() == 2) {
			// pick either 1 or 2 people
			//flip a coin
			int randomFlip = rand.nextInt(2);
			if(randomFlip == 1) {
				//pick 2 people
				int people = 2;
				
				// create new household
				Household newHouse = new Household(householdNumber);

				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(ableToWork.size());
					//get random person 
					int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				
				//update householdNumber
				householdNumber += 1;
				
			}
			else {
				// pick 1 person
				int people = 1;
				
				// create new household
				Household newHouse = new Household(householdNumber);

				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(ableToWork.size());
					//get random person 
					int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				
				//update householdNumber
				householdNumber += 1;
			}
		}
		else {
			// pick 1 person
			int people = 1;
			
			// create new household
			Household newHouse = new Household(householdNumber);

			// add n people to household
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(ableToWork.size());
				//get random person 
				int chosenRandomPerson = ableToWork.remove(chosenRandomIndex);
				// add to household
				newHouse.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse);
			
			//update householdNumber
			householdNumber += 1;
		}
	}
	return householdNumber;
}

public int createRetiredHouseholds(int householdNumber) {
	
	// create retired households + longterm unemployed
	ArrayList<Integer> retiredList = new ArrayList<Integer>(this.retiredSet);
		
	//for each retired person
	// create household
	// flip coin to determine how many members to pick 
	Random rand = new Random();

	while(retiredList.isEmpty()==false) {
		
		
		if(retiredList.size()>=3) {
		
			// add 2 people 
			int people = 2;
			
			Household newHouse = new Household(householdNumber);
			
			// add n people to household
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(retiredList.size());
				//get random person 
				int chosenRandomPerson = retiredList.remove(chosenRandomIndex);
				// add to household
				newHouse.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse);
			this.retiredHhld.add(householdNumber);

			//update householdNumber
			householdNumber += 1;
			
			// add 1 person
			people = 1;

			Household newHouse2 = new Household(householdNumber);
			
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(retiredList.size());
				//get random person 
				int chosenRandomPerson = retiredList.remove(chosenRandomIndex);
				// add to household
				newHouse2.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse2);
			this.retiredHhld.add(householdNumber);
			
			//update householdNumber
			householdNumber += 1;
			
		}
		else if(retiredList.size() ==2) {
			// pick 2 at random
			// add 2 people 
						int people = 2;
						
						Household newHouse = new Household(householdNumber);
						
						// add n people to household
						for(int i=0; i< people; i++) {
							int chosenRandomIndex= rand.nextInt(retiredList.size());
							//get random person 
							int chosenRandomPerson = retiredList.remove(chosenRandomIndex);
							// add to household
							newHouse.getMembers().add(chosenRandomPerson);
						}
						
						// add household to group of households
						this.households.add(newHouse);
						this.retiredHhld.add(householdNumber);
						
						//update householdNumber
						householdNumber += 1;
		}
		else {
			// pick 1 person
			int people = 1;

			Household newHouse = new Household(householdNumber);
			
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(retiredList.size());
				//get random person 
				int chosenRandomPerson = retiredList.remove(chosenRandomIndex);
				// add to household
				newHouse.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse);
			this.retiredHhld.add(householdNumber);
			
			//update householdNumber
			householdNumber += 1;
			
		}
	}
	return householdNumber;
	
}

public int createLongTermUnemployedHouseholds(int householdNumber) {
	// 25%, 50%, 25% allocation of 1 person, 2 person and 3 person households
	ArrayList<Integer> longTermUnemployedList = new ArrayList<Integer>(this.longTermUnemployedSet);
	Random rand = new Random();

	
	while(longTermUnemployedList.isEmpty() == false) {
	
		//if >=3
		if(longTermUnemployedList.size()>=3) {
			int randomNumber = rand.nextInt(4);
			
			if(randomNumber == 0) {
				//pick 1
				int people = 1;
				
				Household newHouse = new Household(householdNumber);
				
				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(longTermUnemployedList.size());
					//get random person 
					int chosenRandomPerson = longTermUnemployedList.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				this.longTermUnemployedHhld.add(householdNumber);
				
				//update householdNumber
				householdNumber += 1;
			}
			
			else if(randomNumber == 1 || randomNumber == 2) {
				// pick 2
				int people = 2;
				
				Household newHouse = new Household(householdNumber);
				
				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(longTermUnemployedList.size());
					//get random person 
					int chosenRandomPerson = longTermUnemployedList.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				this.longTermUnemployedHhld.add(householdNumber);
				
				//update householdNumber
				householdNumber += 1;
			}
			else {
				// pick 3
				int people = 3;
				
				Household newHouse = new Household(householdNumber);
				
				// add n people to household
				for(int i=0; i< people; i++) {
					int chosenRandomIndex= rand.nextInt(longTermUnemployedList.size());
					//get random person 
					int chosenRandomPerson = longTermUnemployedList.remove(chosenRandomIndex);
					// add to household
					newHouse.getMembers().add(chosenRandomPerson);
				}
				
				// add household to group of households
				this.households.add(newHouse);
				this.longTermUnemployedHhld.add(householdNumber);
				
				//update householdNumber
				householdNumber += 1;	
			}
		}
		else if(longTermUnemployedList.size() ==2) {
			// if  == 2	
			int people = 2;
			
			Household newHouse = new Household(householdNumber);
			
			// add n people to household
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(longTermUnemployedList.size());
				//get random person 
				int chosenRandomPerson = longTermUnemployedList.remove(chosenRandomIndex);
				// add to household
				newHouse.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse);
			this.longTermUnemployedHhld.add(householdNumber);
			
			//update householdNumber
			householdNumber += 1;
		}
		else {
			//if == 1
			int people = 1;
			
			Household newHouse = new Household(householdNumber);
			
			// add n people to household
			for(int i=0; i< people; i++) {
				int chosenRandomIndex= rand.nextInt(longTermUnemployedList.size());
				//get random person 
				int chosenRandomPerson = longTermUnemployedList.remove(chosenRandomIndex);
				// add to household
				newHouse.getMembers().add(chosenRandomPerson);
			}
			
			// add household to group of households
			this.households.add(newHouse);
			this.longTermUnemployedHhld.add(householdNumber);
			
			//update householdNumber
			householdNumber += 1;
		}
	}
	return householdNumber;
}

public void sortEconomicallyActiveHouseholds(ArrayList<Household> households, ArrayList<Person> population) {
	
	Collections.sort(households);
	Collections.sort(population);
	ArrayList<Household> tempHouseholds = households;
	ArrayList<Person> tempPopulation = population;
	
	
	//remove households that are economically inactive
	
	for(Household i: tempHouseholds) {
		int id = i.getId();
		
		if(this.longTermUnemployedHhld.contains(id) || this.retiredHhld.contains(id)) {
			
		}
		else {
			// put into economically active array
			//add id to economically active
			this.economicallyActiveHhld.add(id);
		}
		
		System.out.println("number of economically active households is " 
				+ this.economicallyActiveHhld.size());
	}
}

public void determineHhldStatus() {
	/**
	 * 
	 * count all members
	 * sum employed status
	 * 
	 * if sum = count, all employed
	 * if sum = 0, none employed
	 * else mixed 
	 */
	for(int i: this.economicallyActiveHhld) {
		//get household
		Household household = this.households.get(i);
		ArrayList<Integer> members = household.getMembers();
		
		// for each member
		int membersCount = members.size();
		int sum  = 0;
		
		Collections.sort(this.populationSet);
		
		for(int j: members) {
			// get person
			Person hhldMember = populationSet.get(j);
			// check initial status
			if(hhldMember.isInitialiEmploymentStatus()==true) {
				sum = sum +1;
			}else {
				
			}
		}
		
		if(sum == 0) {
			//no members employed
			//household workless
			household.setWorkless();
		}
		else if(sum ==  membersCount) {
			// all members employed
			// household working
			household.setWorking();
		}
		else {
			// mixed household
			household.setMixed();
		}
		
		
	}
	
	
	//if all members initially employed 
	
	// if at least initially 1 member employed
	
	// if no members employed initially
	
}

public void determineCurrentHhldStatus() {
	/**
	 * 
	 * count all members
	 * sum employed status
	 * 
	 * if sum = count, all employed
	 * if sum = 0, none employed
	 * else mixed 
	 */
	// reset household counts
	this.employedHhldCount = 0;
	this.mixedHlhdCount = 0;
	this.shortTermUnemployedHhldCount = 0; 
			
	for(int i: this.economicallyActiveHhld) {
		//get household
		Household household = this.households.get(i);
		ArrayList<Integer> members = household.getMembers();
		
		
		// for each member
		int membersCount = members.size();
		int sum  = 0;
		
		Collections.sort(this.populationSet);
		
		for(int j: members) {
			// get person
			Person hhldMember = populationSet.get(j);
			// check initial status
			if(hhldMember.getStatus()== this.getLabels()[0]) {	// employed
				sum = sum +1;
			}else {
				
			}
		}
		
		if(sum == 0) {
			//no members employed
			//household workless
			household.setCurrentWorkless();
			this.shortTermUnemployedHhldCount += 1; 
		}
		else if(sum ==  membersCount) {
			// all members employed
			// household working
			household.setCurrentWorking();
			this.employedHhldCount += 1;

		}
		else {
			// mixed household
			household.setCurrentMixed();
			this.mixedHlhdCount += 1;

		}
		
		
	}
	
	
	//if all members initially employed 
	
	// if at least initially 1 member employed
	
	// if no members employed initially
	
}

public void initialisePopulationCounts(double population){

	// get numbers for initialisation
	this.economicallyActivePopulation = (int) (pctEconomicallyActive*population/100);	
	this.employedPopulation = (int) (pctEmployed*economicallyActivePopulation/100);	
	this.retiredPopulation = (int) (pctRetired*population/100);
	this.longTermUnemployedPopulation = (int) (economicallyActivePopulation* pctWorkless/100);
	this.shortTermUnemployedPopulation = this.economicallyActivePopulation - this.longTermUnemployedPopulation - this.employedPopulation;	

	this.populationExcludingChildren = this.economicallyActivePopulation + this.retiredPopulation;
	
	System.out.println("economically active population is " + economicallyActivePopulation);
	System.out.println("employed population is " + employedPopulation);
	System.out.println("short-term unemployed population " + shortTermUnemployedPopulation);
	System.out.println("retired population is " + retiredPopulation);
	System.out.println("long term unemployed population is " + longTermUnemployedPopulation);
	
	System.out.println("population excl children " + populationExcludingChildren);
	

	// sort into lists based on label
	
}

public void createPeople(int unemployedIncome, int retiredIncome, int longTermUnemployed) {
	// only initialise economicallyActive
	
		// create people and label according to status + put into list
		int personId = 0;
		
		for(int i = 0; i< this.employedPopulation; i++) {
			// employed
			//Person newPerson = new Person(personId, "employed",true);
			Person newPerson = new Person(personId, this.labels[0],true);
			newPerson.setUnemployedIncome(unemployedIncome);
			populationSet.add(newPerson);
			personId += 1;
		}
		
		for(int i = 0; i< this.shortTermUnemployedPopulation; i++) {	
			// short term unemp
			//Person newPerson = new Person(personId, "short term unemployed",false);
			Person newPerson = new Person(personId, this.labels[1],false);
			newPerson.setUnemployedIncome(unemployedIncome);
			populationSet.add(newPerson);
			personId += 1;
		}
		
		for(int i = 0; i< this.longTermUnemployedPopulation; i++) {	
			//long term unemp
			//Person newPerson = new Person(personId, "long term unemployed",false);
			Person newPerson = new Person(personId, this.labels[2],false);
			//newPerson.setIncome(newPerson.getLongTermUnemployed());
			newPerson.setIncome(longTermUnemployed);
			populationSet.add(newPerson);
			personId += 1;
		}
		
		for(int i = 0; i< this.retiredPopulation; i++) {
			// retired
			//Person newPerson = new Person(personId, "retired",false);
			Person newPerson = new Person(personId, this.labels[3],false);
			//newPerson.setIncome(newPerson.getRetiredIncome());
			newPerson.setIncome(retiredIncome);
			populationSet.add(newPerson);
			personId += 1;
		}	
		
		System.out.println("Hash set size " + populationSet.size());
}

public void sortWholePopulation() throws Exception {
	//for each, sort by label
	for(Person i: this.populationSet) {
		String label = i.getStatus();
		int id = i.getId();

		if(label == this.labels[0]) {
			//put unique id into employed collection
			if(this.employedSet.isEmpty()) {
				this.employedSet.add(id);
				System.out.println("adding to employed set");
			}
			else {
				if(this.employedSet.contains(id)==false) {
					this.employedSet.add(id);
				}
				else {
					//throw new Exception("clashing IDs not allowed"); 
				}
			}
		}
		else if(label == this.labels[1]) {
			//put unique id into employed collection
			if(this.shortTermUnemployedList.isEmpty()) {
				this.shortTermUnemployedList.add(id);
			}
			else {
				if(this.shortTermUnemployedList.contains(id)==false) {
					this.shortTermUnemployedList.add(id);
				}
				else {
					//throw new Exception("clashing IDs not allowed"); 
				}
			}
		}
		else if(label == this.labels[2]) {
			if(this.longTermUnemployedSet.isEmpty()) {
				this.longTermUnemployedSet.add(id);
			}
			else {
				if(this.longTermUnemployedSet.contains(id)==false) {
					this.longTermUnemployedSet.add(id);
				}
				else {
					//throw new Exception("clashing IDs not allowed"); 
				}
			}
		}
		else {
			if(this.retiredSet.isEmpty()) {
				this.retiredSet.add(id);
			}
			else {
				if(this.retiredSet.contains(id)==false) {
					this.retiredSet.add(id);
				}
				else {
					//throw new Exception("clashing IDs not allowed"); 
				}
			}
		}
	}
	System.out.println("economically active population is " + this.getEconomicallyActivePopulation());
	System.out.println("employed population is " + this.employedSet.size());
	System.out.println("short-term unemployed population " + this.getShortTermUnemployedList().size());
	System.out.println("retired population is " + this.retiredSet.size());
	System.out.println("long term unemployed population is " + this.longTermUnemployedSet.size());
}

public void sortEconomicallyActivePopulation() throws Exception {
	//for each, sort by label
	for(Person i: this.populationSet) {
		int industry = i.getIndustry();
		int id = i.getId();

		if(industry != -1) {
			//put unique id into employed collection
			if(this.employedSet.isEmpty()) {
				this.employedSet.add(id);
			}
			else {
				if(this.employedSet.contains(id)==false) {
					this.employedSet.add(id);
					System.out.println("Updating employed set");
				}
				else {
					System.out.println("already contained in set. No need to add");; 
				}
			}
		}
		else if(industry == -1) {
			//put unique id into employed collection
			if(this.shortTermUnemployedList.isEmpty()) {
				this.shortTermUnemployedList.add(id);
			}
			else {
				if(this.shortTermUnemployedList.contains(id)==false) {
					this.shortTermUnemployedList.add(id);
					System.out.println("Updating short term unemployed set");

				}
				else {
					System.out.println("already contained in set. No need to add");; 
				}
			}
		}
	}
	System.out.println("economically active population is " + this.getEconomicallyActivePopulation());
	System.out.println("employed population is " + this.employedSet.size());
	System.out.println("short-term unemployed population " + this.getShortTermUnemployedList().size());
	System.out.println("retired population is " + this.retiredSet.size());
	System.out.println("long term unemployed population is " + this.longTermUnemployedSet.size());
}

public void REDUNDANTinitialiseRetiredAndLongTermWages() {
	
	// get retired households --> 0.75*median
	//get long term unemployed households 0.5*median
	
	ArrayList<Integer> retiredSet = this.retiredSet;
	ArrayList<Integer> longTermUnemployedSet = this.longTermUnemployedSet;
	
	double medianWage = 26000;
	double retiredIncome = 0.75*medianWage;
	double longTermUnemployedIncome = 0.6*medianWage;
	
	// get people
	for(int retiredPersonId: retiredSet) {
		//Person retiredPerson = 
		//retiredPerson.setIncome( (int) retiredIncome);
		
		this.populationSet.get(retiredPersonId).setIncome((int) retiredIncome);
		
		
		System.out.println("Wage being paid: " + (int) retiredIncome );
		System.out.println("Wage check: " + this.populationSet.get(retiredPersonId).getIncome());

		
		System.out.println("retiredPerson number: " + retiredPersonId);
		System.out.println("Check retiredPerson id: " + this.populationSet.get(retiredPersonId).getId());
		
		System.out.println("retired");
		System.out.println("status check: " + this.populationSet.get(retiredPersonId).getStatus());
	}
	
	for(int longTermUnemployedPersonId: retiredSet) {
		this.populationSet.get(longTermUnemployedPersonId).setIncome((int) longTermUnemployedIncome);
		//Person longTermUnemployedPerson = this.populationSet.get(longTermUnemployedPersonId);
		//longTermUnemployedPerson.setIncome( (int) longTermUnemployedIncome);
		System.out.println("Wage being paid: " + (int) longTermUnemployedIncome );
		System.out.println("Wage check: " + this.populationSet.get(longTermUnemployedPersonId).getIncome());
		System.out.println("long term unempl");
		System.out.println("status check: " + this.populationSet.get(longTermUnemployedPersonId).getStatus());
	}
	// set annual wage to median wage for retired
	
	
}

public void computeHouseholdIncome() {
	
	
	int totalIncome;
	int count = 0;
	
	// get each household
	for(int household = 0; household < this.households.size(); household++) {
		
		Household currentHousehold = this.households.get(household);
		ArrayList<Integer> members = currentHousehold.getMembers();
		totalIncome = 0;
		count += 1;
		
		// find members and add income to income
		for(int current = 0; current < members.size(); current++) {
		
			int personId = members.get(current); 
			Person currentPerson = this.populationSet.get(personId);
			
			System.out.println("personId: " + personId);
			totalIncome = totalIncome + currentPerson.getIncome();
			
			System.out.println("total income: " + totalIncome);
			System.out.println("Person occupation: " + currentPerson.getOccupation());
			System.out.println("Person status: " + currentPerson.getStatus());
			System.out.println("Person income: " + currentPerson.getIncome());
		}
		
		System.out.println("House number: " + count);
		System.out.println("House members: " + members.size());
		System.out.println("total household income: " + totalIncome);
		this.households.get(household).setIncome(totalIncome);
	}
	System.out.println("Print output all income of households to give flavour of what is going on");
}

public void setHhldIncomeAnddisplay() {
	
	
	int totalIncome;
	int count = 0;
	
	// get each household
	for(int household = 0; household < this.households.size(); household++) {
		
		Household currentHousehold = this.households.get(household);
		ArrayList<Integer> members = currentHousehold.getMembers();
		totalIncome = 0;
		count += 1;
		
		// find members and add income to income
		for(int current = 0; current < members.size(); current++) {
		
			int personId = members.get(current); 
			Person currentPerson = this.populationSet.get(personId);
			totalIncome += currentPerson.getIncome();
			System.out.println("personId: " + personId);
			System.out.println("total income: " + totalIncome);
			System.out.println("Person occupation: " + currentPerson.getOccupation());
			System.out.println("Person status: " + currentPerson.getStatus());
			System.out.println("Person income: " + currentPerson.getIncome());
		}
		
		System.out.println("House number: " + count);
		System.out.println("House members: " + members.size());
		System.out.println("total household income: " + totalIncome);
		this.households.get(household).setIncome(totalIncome);
	}
	System.out.println("Print output all income of households to give flavour of what is going on");
}

public int getPopulationExcludingChildren() {
	return populationExcludingChildren;
}
public void setPopulationExcludingChildren(int populationExcludingChildren) {
	this.populationExcludingChildren = populationExcludingChildren;
}
public double getPctRetired() {
	return pctRetired;
}
public void setPctRetired(double pctRetired) {
	this.pctRetired = pctRetired;
}
public double getPctChildren() {
	return pctChildren;
}
public void setPctChildren(double pctChildren) {
	this.pctChildren = pctChildren;
}
public double getPctWorkingAge() {
	return pctWorkingAge;
}
public void setPctWorkingAge(double pctWorkingAge) {
	this.pctWorkingAge = pctWorkingAge;
}
public double getPctEconomicallyActive() {
	return pctEconomicallyActive;
}
public void setPctEconomicallyActive(double pctEconomicallyActive) {
	this.pctEconomicallyActive = pctEconomicallyActive;
}
public double getPctEmployed() {
	return pctEmployed;
}
public void setPctEmployed(double pctEmployed) {
	this.pctEmployed = pctEmployed;
}
public double getPctShortTermUnemployed() {
	return pctShortTermUnemployed;
}
public void setPctShortTermUnemployed(double pctShortTermUnemployed) {
	this.pctShortTermUnemployed = pctShortTermUnemployed;
}
public double getPctWorkless() {
	return pctWorkless;
}
public void setPctWorkless(double pctWorkless) {
	this.pctWorkless = pctWorkless;
}
public int getEconomicallyActivePopulation() {
	return economicallyActivePopulation;
}
public void setEconomicallyActivePopulation(int economicallyActivePopulation) {
	this.economicallyActivePopulation = economicallyActivePopulation;
}
public int getEmployedPopulation() {
	return employedPopulation;
}
public void setEmployedPopulation(int employedPopulation) {
	this.employedPopulation = employedPopulation;
}
public int getShortTermUnemployedPopulation() {
	return shortTermUnemployedPopulation;
}
public void setShortTermUnemployedPopulation(int shortTermUnemployedPopulation) {
	this.shortTermUnemployedPopulation = shortTermUnemployedPopulation;
}
public int getRetiredPopulation() {
	return retiredPopulation;
}
public void setRetiredPopulation(int retiredPopulation) {
	this.retiredPopulation = retiredPopulation;
}
public int getLongTermUnemployedPopulation() {
	return longTermUnemployedPopulation;
}
public void setLongTermUnemployedPopulation(int longTermUnemployedPopulation) {
	this.longTermUnemployedPopulation = longTermUnemployedPopulation;
}

public ArrayList<Integer> getEmployedSet() {
	return employedSet;
}
public void setEmployedSet(ArrayList<Integer> employedSet) {
	this.employedSet = employedSet;
}
public ArrayList<Integer> getShortTermUnemployedList() {
	return shortTermUnemployedList;
}
public void setShortTermUnemployedList(ArrayList<Integer> shortTermUnemployedList) {
	this.shortTermUnemployedList = shortTermUnemployedList;
}
	
public ArrayList<Person> getPopulationSet() {
	return populationSet;
}
public void setPopulationSet(ArrayList<Person> populationSet) {
	this.populationSet = populationSet;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public ArrayList<Household> getHouseholds() {
	return households;
}
public void setHouseholds(ArrayList<Household> households) {
	this.households = households;
}
public ArrayList<Integer> getRetiredHhld() {
	return retiredHhld;
}
public void setRetiredHhld(ArrayList<Integer> retiredHhld) {
	this.retiredHhld = retiredHhld;
}
public ArrayList<Integer> getLongTermUnemployedHhld() {
	return longTermUnemployedHhld;
}
public void setLongTermUnemployedHhld(ArrayList<Integer> longTermUnemployedHhld) {
	this.longTermUnemployedHhld = longTermUnemployedHhld;
}
public ArrayList<Integer> getEconomicallyActiveHhld() {
	return economicallyActiveHhld;
}
public void setEconomicallyActiveHhld(ArrayList<Integer> economicallyActiveHhld) {
	this.economicallyActiveHhld = economicallyActiveHhld;
}
public ArrayList<Integer> getShortTermUnemployedHhld() {
	return shortTermUnemployedHhld;
}
public void setShortTermUnemployedHhld(ArrayList<Integer> shortTermUnemployedHhld) {
	this.shortTermUnemployedHhld = shortTermUnemployedHhld;
}
public ArrayList<Integer> getMixedHlhd() {
	return mixedHlhd;
}
public void setMixedHlhd(ArrayList<Integer> mixedHlhd) {
	this.mixedHlhd = mixedHlhd;
}
public ArrayList<Integer> getEmployedHhld() {
	return employedHhld;
}
public void setEmployedHhld(ArrayList<Integer> employedHhld) {
	this.employedHhld = employedHhld;
}
public String[] getLabels() {
	return labels;
}
public void setLabels(String[] labels) {
	this.labels = labels;
}
public ArrayList<Integer> getLongTermUnemployedSet() {
	return longTermUnemployedSet;
}
public void setLongTermUnemployedSet(ArrayList<Integer> longTermUnemployedSet) {
	this.longTermUnemployedSet = longTermUnemployedSet;
}
public ArrayList<Integer> getRetiredSet() {
	return retiredSet;
}
public void setRetiredSet(ArrayList<Integer> retiredSet) {
	this.retiredSet = retiredSet;
}
public double[] getAnnualSumOfAllHouseholdDemandFor55Industries() {
	return annualSumOfAllHouseholdDemandFor55Industries;
}
public void setAnnualSumOfAllHouseholdDemandFor55Industries(double[] annualSumOfAllHouseholdDemandFor55Industries) {
	this.annualSumOfAllHouseholdDemandFor55Industries = annualSumOfAllHouseholdDemandFor55Industries;
}
public double[] getAnnualSumOfAllHouseholdDemandFor21Industries() {
	return annualSumOfAllHouseholdDemandFor21Industries;
}
public void setAnnualSumOfAllHouseholdDemandFor21Industries(double[] annualSumOfAllHouseholdDemandFor21Industries) {
	this.annualSumOfAllHouseholdDemandFor21Industries = annualSumOfAllHouseholdDemandFor21Industries;
}
public double[] getWeeklySumOfAllHouseholdDemandFor55Industries() {
	return weeklySumOfAllHouseholdDemandFor55Industries;
}
public void setWeeklySumOfAllHouseholdDemandFor55Industries(double[] weeklySumOfAllHouseholdDemandFor55Industries) {
	this.weeklySumOfAllHouseholdDemandFor55Industries = weeklySumOfAllHouseholdDemandFor55Industries;
}
public double[] getWeeklySumOfAllHouseholdDemandFor21Industries() {
	return weeklySumOfAllHouseholdDemandFor21Industries;
}
public void setWeeklySumOfAllHouseholdDemandFor21Industries(double[] weeklySumOfAllHouseholdDemandFor21Industries) {
	this.weeklySumOfAllHouseholdDemandFor21Industries = weeklySumOfAllHouseholdDemandFor21Industries;
}
public double[] getMonthlySumOfAllHouseholdDemandFor55Industries() {
	return monthlySumOfAllHouseholdDemandFor55Industries;
}
public void setMonthlySumOfAllHouseholdDemandFor55Industries(double[] monthlySumOfAllHouseholdDemandFor55Industries) {
	this.monthlySumOfAllHouseholdDemandFor55Industries = monthlySumOfAllHouseholdDemandFor55Industries;
}
public double[] getMonthlySumOfAllHouseholdDemandFor21Industries() {
	return monthlySumOfAllHouseholdDemandFor21Industries;
}
public void setMonthlySumOfAllHouseholdDemandFor21Industries(double[] monthlySumOfAllHouseholdDemandFor21Industries) {
	this.monthlySumOfAllHouseholdDemandFor21Industries = monthlySumOfAllHouseholdDemandFor21Industries;
}
public static boolean isLockdownImposed() {
	return lockdownImposed;
}

public static void setLockdownImposed(boolean lockdownImposed) {
	RegionHouseholds.lockdownImposed = lockdownImposed;
}

public int getShortTermUnemployedHhldCount() {
	return shortTermUnemployedHhldCount;
}

public void setShortTermUnemployedHhldCount(int shortTermUnemployedHhldCount) {
	this.shortTermUnemployedHhldCount = shortTermUnemployedHhldCount;
}

public int getMixedHlhdCount() {
	return mixedHlhdCount;
}

public void setMixedHlhdCount(int mixedHlhdCount) {
	this.mixedHlhdCount = mixedHlhdCount;
}

public int getEmployedHhldCount() {
	return employedHhldCount;
}

public void setEmployedHhldCount(int employedHhldCount) {
	this.employedHhldCount = employedHhldCount;
}

public int getHouseholdWithFurloughedMembers() {
	return householdWithFurloughedMembers;
}

public void setHouseholdWithFurloughedMembers(int householdWithFurloughedMembers) {
	this.householdWithFurloughedMembers = householdWithFurloughedMembers;
}

}
