package industry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import peopleHouseholds.Person;
import peopleHouseholds.RegionHouseholds;


public class Industry {
	
	private int id;
	private String industryName;

	// across all industries	
	private int[] householdDemand;
	
	private static int[] occupationNumber;
	private static String[] occupationNames;
			
	private static double[] occupationRLI;
	private static double[] occupationMedianWage;

	private double sumOfWages;

	ArrayList<ArrayList<Integer>> allEmployeesByOccupation = new ArrayList<ArrayList<Integer>>();
	private double overallWorkforceCount;
	private int[] currentNumberOfEmployeesPerOccupation;
	
	private int numberOfFurloughedWorkers = 0;
	
	private double requiredCurrentOutput;
	
	private double derivedWorkforcePerUnitOutput;
	private double[] derivedWorkforceInOccupationsPerUnitOutput;
	
	private double[] requiredWorkforceInOccupationsToMeetOutput;
	private double[] requiredChangeWorkforceInOccupationsToMeetOutput;
	private double requiredTotalWorkforceToMeetOutput;
	
	private double targetInventoryLevel;
	

	public Industry() {
		// initialise

	}

	public void placeOnFurlough(double thresholdRLI, RegionHouseholds regionHouseholds) {
		// all RLI
		// go through employed list and place all RLI within furlough list
		// change their status
		// change their salary
		
		// find occupation with RLI <= threshold
		for(int occupation = 0; occupation  < this.occupationRLI.length; occupation ++) {
			
			// place on furlough as cannot do work under social distancing rules
			if(this.occupationRLI[occupation ] <=thresholdRLI) {
				
				int employeesInOccupation = this.currentNumberOfEmployeesPerOccupation[occupation];
				
				// for each employees in occupation
				for(int employee = 0; employee < employeesInOccupation; employee++) {
					//get person
					Person currentPerson;
					
					int personId = this.allEmployeesByOccupation.get(occupation).get(employee);
					
					currentPerson = regionHouseholds.getPopulationSet().get(personId);
					currentPerson.setIncome( (int) (0.8*this.occupationMedianWage[occupation]));   // not sure if this is wise
					
					// get household + change status to furloughed member(s)
				
				}
				this.numberOfFurloughedWorkers += employeesInOccupation;
				
			}
		}
	}
	
	public void removeFromFurlogh(double thresholdRLI, RegionHouseholds regionHouseholds) {
			// all RLI
			// go through employed list and place all RLI within furlough list
			// change their status
			// change their salary
			
			// find occupation with RLI <= threshold
			for(int occupation = 0; occupation  < this.occupationRLI.length; occupation ++) {
				
				// place on furlough as cannot do work under social distancing rules
				if(this.occupationRLI[occupation ] <=thresholdRLI) {
					
					int employeesInOccupation = this.currentNumberOfEmployeesPerOccupation[occupation];
					
					// for each employees in occupation
					for(int employee = 0; employee < employeesInOccupation; employee++) {
						//get person
						Person currentPerson;
						
						int personId = this.allEmployeesByOccupation.get(occupation).get(employee);
						
						currentPerson = regionHouseholds.getPopulationSet().get(personId);
						currentPerson.setIncome( (int) (this.occupationMedianWage[occupation]));   // not sure if this is wise
						
						// get household + change status to furloughed member(s)
					
					}
					this.numberOfFurloughedWorkers -= employeesInOccupation;
					
				}
			}
		}
	
	public void hireFire(RegionHouseholds regionHouseholds, double multiple){
		// pick n* people at random from population
		System.out.println("hiring and firing and updating workforce counts");
		
		ArrayList<Person> randomlySelectedArrayList = new ArrayList<Person>();
		
		double[] changeInOccupationMatrix = this.requiredChangeWorkforceInOccupationsToMeetOutput; 
		
		Random rand = new Random();
		int randomNumber;
		int numberOfCandidatesInSample = 0;
		int personId;
		Person randomPerson;
		int occupation;
		int industry;
		
		// for each occupation
		for(int i = 0; i < changeInOccupationMatrix.length; i++) {
			int requiredEmployees = (int) changeInOccupationMatrix[i];
			int numberOfEmployeesInOccupation = this.currentNumberOfEmployeesPerOccupation[i];
			int shortTermUnemployedNumber = regionHouseholds.getShortTermUnemployedList().size();

			if(requiredEmployees < 0) {
				
				requiredEmployees = (int) (-1 * requiredEmployees);
				System.out.println("Fire people: " + requiredEmployees);
				
				for(int j = 0; j < requiredEmployees; j++) {
					System.out.println(j + "th person to be fired" );
					
				// get randomPerson from occupation within industry to fire
					randomNumber = rand.nextInt(numberOfEmployeesInOccupation - j);
					
					personId = this.allEmployeesByOccupation.get(i).remove(randomNumber);
					System.out.println("Person id: " + personId);
					
					this.overallWorkforceCount -= 1;
					this.currentNumberOfEmployeesPerOccupation[i] -= 1;
					
					// get random Person from population set and change their status to reflect unemployed
					randomPerson = regionHouseholds.getPopulationSet().get(personId);
					
					System.out.println("person id of random person" + randomPerson.getId() + "searching for" + personId);
					
					randomPerson.setIndustry(-1);
					randomPerson.setIncome(randomPerson.getUnemployedIncome());   // not sure if this is wise
					randomPerson.setStatus(regionHouseholds.getLabels()[1]);
					randomPerson.setCurrentEmploymentStatus(false);
					
					// remove person from employed set
					//regionHouseholds.getEmployedSet().removeIf(x -> (x == personId));
					System.out.println("size of employed set pre-firing: "+ regionHouseholds.getEmployedSet().size());

					
					if(regionHouseholds.getEmployedSet().contains(personId)==false) {
						System.out.println("Does not contain the person sought!!");
					}
					else {
						for(int deletePerson = 0; deletePerson < regionHouseholds.getEmployedSet().size(); deletePerson++) {
							if(regionHouseholds.getEmployedSet().get(deletePerson)== personId) {
								regionHouseholds.getEmployedSet().remove(deletePerson);
								System.out.println("found person: " + personId);
							}	
						}
					}
//					if(regionHouseholds.getShortTermUnemployedList().contains(personId)==true) {
//						System.out.println("short term list contains the person " + personId);
//					}
//					if(regionHouseholds.getRetiredSet().contains(personId)==true) {
//						System.out.println("retired list contains the person "+ personId);
//					}
//					if(regionHouseholds.getLongTermUnemployedSet().contains(personId)== true) {
//						System.out.println("long term unemployed list contains the person "+ personId);
//					}
					
					
					System.out.println("Removed person from employed set");
					System.out.println("size of employed set post-firing : "+ regionHouseholds.getEmployedSet().size());
					Collections.sort(regionHouseholds.getEmployedSet());

									
					// update short term unemployed. Add unemployed person
					regionHouseholds.getShortTermUnemployedList().add(personId);
					Collections.sort(regionHouseholds.getShortTermUnemployedList());
					
					//no need to add one as have now added entry to the list
					//regionHouseholds.setShortTermUnemployedPopulation(shortTermUnemployedNumber + 1);
					
					//update employed list. Remove person from employment
					System.out.println("size of employed set: "+ regionHouseholds.getEmployedSet().size());
					System.out.println("size of population: "+ regionHouseholds.getPopulationSet().size());
					System.out.println("size of short term unemployed set: "+ regionHouseholds.getShortTermUnemployedList().size());
					System.out.println("size of long term unemployed set: "+ regionHouseholds.getLongTermUnemployedSet().size());
					System.out.println("size of retired set: "+ regionHouseholds.getRetiredSet().size());
					
					//regionHouseholds.getEmployedSet().remove(personId);
					//Collections.sort(regionHouseholds.getEmployedSet());
	
					// updateHousehold status
					regionHouseholds.determineHhldStatus();
				}
			}
			else if(requiredEmployees > 0) {
				numberOfCandidatesInSample = (int) (requiredEmployees*multiple);
				
				if(numberOfCandidatesInSample > regionHouseholds.getShortTermUnemployedList().size()) {
					shortTermUnemployedNumber = regionHouseholds.getShortTermUnemployedList().size();
					numberOfCandidatesInSample = shortTermUnemployedNumber;

				}
				System.out.println("Interview people: " + numberOfCandidatesInSample);
				
				if(numberOfCandidatesInSample > 0) {
					for(int interviewCount = 0; interviewCount < numberOfCandidatesInSample; interviewCount++) {
						System.out.println("Number interviewed for hire: " + interviewCount);
						
						shortTermUnemployedNumber = regionHouseholds.getShortTermUnemployedList().size() - 1;

						randomNumber = rand.nextInt(shortTermUnemployedNumber); //select person from short-term unemployed list at random
						
						System.out.println("random number: " + randomNumber);
						System.out.println("short term unemployed number: " +  shortTermUnemployedNumber);
						System.out.println("short term unemployed size: " +  regionHouseholds.getShortTermUnemployedList().size());

						personId = regionHouseholds.getShortTermUnemployedList().get(randomNumber);
						randomPerson = regionHouseholds.getPopulationSet().get(personId);
						
						
						//if(randomPerson.getIndustry()==-1) {
							System.out.println("Found unemployed person");
							
							if(randomPerson.getOccupation() == i) {
								System.out.println("Found person with matching skills. Occupation sought matches person's desired occupation");
								
								// add person to occupations
								//ArrayList<ArrayList<Integer>> allEmployeesByOccupation = new ArrayList<ArrayList<Integer>>();
								//private double overallWorkforceCount;
								//private int[] currentNumberOfEmployeesPerOccupation;
								randomPerson.setIndustry(this.getId());
								System.out.println("Industry hiring is: " + this.getId());
								
								randomPerson.setIncome((int) this.occupationMedianWage[i]);
								randomPerson.setStatus(regionHouseholds.getLabels()[0]);
								randomPerson.setCurrentEmploymentStatus(true);

	
								this.allEmployeesByOccupation.get(i).add(personId);
								this.overallWorkforceCount += 1;
								this.currentNumberOfEmployeesPerOccupation[i] += 1;
								
								// update short term unemployed
								regionHouseholds.getShortTermUnemployedList().remove(randomNumber);
								regionHouseholds.setShortTermUnemployedPopulation(shortTermUnemployedNumber -1);
								
								//update employed list
								regionHouseholds.getEmployedSet().add(randomNumber);
								Collections.sort(regionHouseholds.getEmployedSet());
		
								// updateHousehold status
								regionHouseholds.determineHhldStatus();
								
						}
					}
				}
				//}
			
			}
		}
		
		
		
		//if unemployed AND have same occupation, hire
		
		// else, no worker found
		
		// update person employment status
		
		// update household status to reflect changes
	}

	public void initialiseWorkforcePerUnitOutput(double requiredInitialOutput) {
	System.out.println("Setting workforce required to produce initial output. \n"
			+ "This conditions the industry productivity metric on starting condition");
	
		this.requiredCurrentOutput = requiredInitialOutput;
		
		
		this.derivedWorkforcePerUnitOutput = this.overallWorkforceCount/ this.requiredCurrentOutput;
		
		System.out.println("Overall workforce in industry: " + this.overallWorkforceCount);
		System.out.println("required initial current output: " + this.requiredCurrentOutput );
		
		System.out.println("derived workforce per unit input: " + this.derivedWorkforcePerUnitOutput);
		
		int occupations = this.currentNumberOfEmployeesPerOccupation.length;
		
		this.derivedWorkforceInOccupationsPerUnitOutput = new double[occupations];
		
		for(int occupation = 0; occupation < occupations; occupation++) {
			this.derivedWorkforceInOccupationsPerUnitOutput[occupation] =
					this.currentNumberOfEmployeesPerOccupation[occupation]/ this.requiredCurrentOutput;
		// multiply this by proportion of worforce in given occupatio
			
			System.out.println("Overall workforce in industry: " + this.currentNumberOfEmployeesPerOccupation[occupation]);
			System.out.println("required initial current output: " + this.requiredCurrentOutput );
			
			System.out.println("derived workforce in occupations per unit output " + 
					this.derivedWorkforceInOccupationsPerUnitOutput[occupation]);
		}
	}
	
	public void requiredWorkforceAndOccupationsToMeetOutput(boolean roundDown) {
		System.out.println("Make sure update required output beforehand!!");
		
		int size = this.currentNumberOfEmployeesPerOccupation.length;
		int sum = 0;
		
		this.requiredWorkforceInOccupationsToMeetOutput = new double[size];
		
		
		for(int i = 0; i < size; i++) {
			if(roundDown == false) {
				this.requiredWorkforceInOccupationsToMeetOutput[i] =  
						this.derivedWorkforceInOccupationsPerUnitOutput[i] * this.requiredCurrentOutput;

			}
			else {
				//round the value down
				this.requiredWorkforceInOccupationsToMeetOutput[i] = (double) 
						((int)(this.derivedWorkforceInOccupationsPerUnitOutput[i] * this.requiredCurrentOutput));

			}
			System.out.println("required current output: " + this.requiredCurrentOutput);
			System.out.println("derived workforce in occupation per unit outpt: " + this.derivedWorkforceInOccupationsPerUnitOutput[i] );
			
			System.out.println("Current number of employees in occupation: " + this.currentNumberOfEmployeesPerOccupation[i]);
			System.out.println("Required workforce in occupation: " + this.requiredWorkforceInOccupationsToMeetOutput[i]);
			System.out.println("Required workforce in occupation rounded down: " + (double)(int) this.requiredWorkforceInOccupationsToMeetOutput[i]);
			System.out.println("Required workforce in occupation rounded down: " + (double)((int) this.requiredWorkforceInOccupationsToMeetOutput[i]));

		}
		
		sum = 0;

		for(double occupation : this.requiredWorkforceInOccupationsToMeetOutput)
			sum+= occupation;
			System.out.println("Derived workforce needed to meet output: " + sum);
			System.out.println("Actual workforce: " + this.overallWorkforceCount);
			
			this.requiredTotalWorkforceToMeetOutput = sum;
	}
	
	public void requiredChangeInEmploymentToMeetOutput() {
		
		int size = this.currentNumberOfEmployeesPerOccupation.length;
		this.requiredChangeWorkforceInOccupationsToMeetOutput = new double[size];
		
		double changeInWorkforce = this.requiredTotalWorkforceToMeetOutput - this.overallWorkforceCount;
		
		if(changeInWorkforce > 0){
			System.out.println("Must hire people");
			System.out.println("Current level: " + this.overallWorkforceCount);
			System.out.println("Desired level: " + this.requiredTotalWorkforceToMeetOutput);
			System.out.println("Change in employment: " + changeInWorkforce);
		}
		else if(changeInWorkforce < 0) {
			System.out.println("Must fire people");
			System.out.println("Current level: " + this.overallWorkforceCount);
			System.out.println("Desired level: " + this.requiredTotalWorkforceToMeetOutput);
			System.out.println("Change in employment: " + changeInWorkforce);
		}
		else {
			System.out.println("No change in employment level needed");
			System.out.println("Current level: " + this.overallWorkforceCount);
			System.out.println("Desired level: " + this.requiredTotalWorkforceToMeetOutput);
			System.out.println("Change in employment: " + changeInWorkforce);
		}
		
		for(int i = 0; i < size; i++) {
			this.requiredChangeWorkforceInOccupationsToMeetOutput[i] =
					this.requiredWorkforceInOccupationsToMeetOutput[i] - this.currentNumberOfEmployeesPerOccupation[i];
			System.out.println("Unrounded change in occupation " + i + " required: " + this.requiredChangeWorkforceInOccupationsToMeetOutput[i]);
			System.out.println("current employees: " + this.currentNumberOfEmployeesPerOccupation[i]);
			System.out.println("required employees: " + this.requiredWorkforceInOccupationsToMeetOutput[i]);
		}
		
	}
	
	public int[] getCurrentNumberOfEmployeesPerOccupation() {
		return currentNumberOfEmployeesPerOccupation;
	}

	public void setCurrentNumberOfEmployeesPerOccupation(int[] currentNumberOfEmployeesPerOccupation) {
		this.currentNumberOfEmployeesPerOccupation = currentNumberOfEmployeesPerOccupation;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getIndustryName() {
		return industryName;
	}

	public void setIndustryName(String industryName) {
		this.industryName = industryName;
	}

	public int[] getHouseholdDemand() {
		return householdDemand;
	}

	public void setHouseholdDemand(int[] householdDemand) {
		this.householdDemand = householdDemand;
	}

	public static int[] getOccupationNumber() {
		return occupationNumber;
	}

	public static void setOccupationNumber(int[] occupationNumber) {
		Industry.occupationNumber = occupationNumber;
	}

	public static String[] getOccupationNames() {
		return occupationNames;
	}

	public static void setOccupationNames(String[] occupationNames) {
		Industry.occupationNames = occupationNames;
	}

	public double getOverallWorkforceCount() {
		return overallWorkforceCount;
	}

	public void setOverallWorkforceCount(double overallWorkforceCount) {
		this.overallWorkforceCount = overallWorkforceCount;
	}

	public static double[] getOccupationRLI() {
		return occupationRLI;
	}

	public static void setOccupationRLI(double[] occupationRLI) {
		Industry.occupationRLI = occupationRLI;
	}

	public static double[] getOccupationMedianWage() {
		return occupationMedianWage;
	}

	public static void setOccupationMedianWage(double[] occupationMedianWage) {
		Industry.occupationMedianWage = occupationMedianWage;
	}

	public double getSumOfWages() {
		return sumOfWages;
	}

	public void setSumOfWages(double sumOfWages) {
		this.sumOfWages = sumOfWages;
	}

	public ArrayList<ArrayList<Integer>> getAllEmployeesByOccupation() {
		return allEmployeesByOccupation;
	}

	public void setAllEmployeesByOccupation(ArrayList<ArrayList<Integer>> allEmployeesByOccupation) {
		this.allEmployeesByOccupation = allEmployeesByOccupation;
	}

	public double getTargetInventoryLevel() {
		return targetInventoryLevel;
	}

	public void setTargetInventoryLevel(double targetInventoryLevel) {
		this.targetInventoryLevel = targetInventoryLevel;
	}
	
	public double getRequiredCurrentOutput() {
		return requiredCurrentOutput;
	}


	public void setRequiredCurrentOutput(double requiredCurrentOutput) {
		this.requiredCurrentOutput = requiredCurrentOutput;
	}


	public double getDerivedWorkforcePerUnitOutput() {
		return derivedWorkforcePerUnitOutput;
	}


	public void setDerivedWorkforcePerUnitOutput(double derivedWorkforcePerUnitOutput) {
		this.derivedWorkforcePerUnitOutput = derivedWorkforcePerUnitOutput;
	}


	public double[] getDerivedWorkforceInOccupationsPerUnitOutput() {
		return derivedWorkforceInOccupationsPerUnitOutput;
	}


	public void setDerivedWorkforceInOccupationsPerUnitOutput(double[] derivedWorkforceInOccupationsPerUnitOutput) {
		this.derivedWorkforceInOccupationsPerUnitOutput = derivedWorkforceInOccupationsPerUnitOutput;
	}


	public double[] getRequiredWorkforceInOccupationsToMeetOutput() {
		return requiredWorkforceInOccupationsToMeetOutput;
	}


	public void setRequiredWorkforceInOccupationsToMeetOutput(double[] requiredWorkforceInOccupationsToMeetOutput) {
		this.requiredWorkforceInOccupationsToMeetOutput = requiredWorkforceInOccupationsToMeetOutput;
	}


	public double getRequiredTotalWorkforceToMeetOutput() {
		return requiredTotalWorkforceToMeetOutput;
	}


	public void setRequiredTotalWorkforceToMeetOutput(double requiredTotalWorkforceToMeetOutput) {
		this.requiredTotalWorkforceToMeetOutput = requiredTotalWorkforceToMeetOutput;
	}
	
}

