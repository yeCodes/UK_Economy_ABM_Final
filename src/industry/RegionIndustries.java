package industry;

import org.apache.commons.math3.linear.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import peopleHouseholds.Household;
import peopleHouseholds.Person;
import peopleHouseholds.RegionHouseholds;
import usefulMethods.CsvParser;		

public class RegionIndustries {

	// region can see the high-level industry stats
	// make industry
	private String name;
	private int id;

	private static boolean lockdownImposed = false;
	
	private ArrayList<Industry> regionIndustries= new ArrayList<Industry>();
	
	private ArrayList<Integer> employedSet;
	private ArrayList<Integer> shortTermUnemployedList;
	
	private int overallWorkforceCount;		// across all industries	
	private String[] occupations;

	private double[] householdDemand;
	
	//wage per occupation & industry
		private double[] medianWagesForOccupations;
		private double[] rliOccupations;

		
		private int[] industryKey = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		private static String[] industryNames = {"Agriculture, forestry and fishing",
				"Mining and quarrying", "Manufacturing", "Electricity, gas, air cond supply",
				"Water supply, sewerage, waste", "Construction", "Wholesale, retail, repair of vehicles",
				"Transport and storage", "Accommodation and food services", 
				"Information and communication", "Financial and insurance activities",
				"Real estate activities", "Prof, scientific, technical activ.",
				"Admin and support services", "Public admin and defence", "Education",
				"Health and social work", "Arts, entertainment and recreation",
				"Other service activities", "Households as employers", "Extraterritorial organisations"};

		private double[] percentWorkforceInIndustry; 
			//{0.01, 0.004, 0.085, 0.018, 0.006, 0.073, 0.13,0.053, 
			//		0.047, 0.045, 0.045, 0.009, 0.081, 0.045, 0.064, 0.1, 0.13,
			//		0.025, 0.027, 0.002, 0.002};
		private double[] workforceInIndustryCount = new double[21];		// 21 industries considered. 21st industry has 0 people in employment

		private double[][] workforceOccupationProportionOfIndustry;
		private double[][] workforceOccupationOfIndustryCount;

		private double[] workersPerOutputIndustries;
		
		private double[][] inputOutputNoRecipeCutbacks;
		private double[][] inputOutputRecipeCutbacksSlightlyStressed;
		private double[][] inputOutputRecipeCutbacksMediumStressed;
		private double[][] inputOutputRecipeCutbacksMostStressed;
		
		private RealMatrix requiredOutputAnnualNormal;
		private RealMatrix requiredOutputAnnualLockdownDefaultStressRecipe;
		private RealMatrix requiredLockdownAnnualMediumStressRecipe;
		private RealMatrix requiredLockdownAnnualHighStressRecipe;
		
		/**private RealMatrix requiredMaterialInputsAnnualNormal;
		private RealMatrix requiredMaterialInputsAnnualLockdownDefaultStressRecipe;
		private RealMatrix requiredMaterialInputsLockdownAnnualMediumStressRecipe;
		private RealMatrix requiredMaterialInputsLockdownAnnualHighStressRecipe;
		**/
		private double[] requiredOutputAnnualNormal55Array = new double[55];
		private double[] requiredOutputAnnualLockdownDefaultStressRecipe55Array = new double[55];
		private double[] requiredLockdownAnnualMediumStressRecipe55Array = new double[55];
		private double[] requiredLockdownAnnualHighStressRecipe55Array = new double[55];
	
		private double[] requiredOutputAnnualNormal21Array = new double[21];
		private double[] requiredOutputAnnualLockdownDefaultStressRecipe21Array = new double[21];
		private double[] requiredLockdownAnnualMediumStressRecipe21Array = new double[21];
		private double[] requiredLockdownAnnualHighStressRecipe21Array = new double[21];
	
		private static double[] selectedOutput21;
		//private int productivity;	
	// create empty industries
	// allocate all workers
		
		// population stats
		private ArrayList<Integer> overallPopulation = new ArrayList<Integer>();
		private ArrayList<Integer> populationExcludingChildren = new ArrayList<Integer>();

		// employment stats --> workforce count
		private ArrayList<Integer> longTermUnemployedSetCount = new ArrayList<Integer>();	// there should be no duplicates
		private ArrayList<Integer> retiredSetCount= new ArrayList<Integer>();
		private ArrayList<Integer> employedSetCount= new ArrayList<Integer>();
		private ArrayList<Integer> shortTermUnemployedListCount= new ArrayList<Integer>();
		private ArrayList<Integer> furloughListCount= new ArrayList<Integer>();

		// household stats
		private ArrayList<Integer> economicallyActiveHhldCount = new ArrayList<Integer>();
		private ArrayList<Integer> shortTermUnemployedHhldCount = new ArrayList<Integer>();
		private ArrayList<Integer> mixedHlhdCount = new ArrayList<Integer>();
		private ArrayList<Integer> employedHhldCount = new ArrayList<Integer>();

		// household demand stats
		private ArrayList<ArrayList<Double>> annualSumOfAllHouseholdDemandFor55IndustriesCount = new ArrayList<>();
		private ArrayList<ArrayList<Double>> annualSumOfAllHouseholdDemandFor21IndustriesCount = new ArrayList<>();
		
		
		// industry stats
		// employment by industry
		// output by industry
		private ArrayList<ArrayList<Double>> selectedOutput21History = new ArrayList<>();
		private ArrayList<ArrayList<Double>> overallWorkforceCountPerIndustry = new ArrayList<>();
		
		// which industries hardest hit
		
		
		public static void main(String args[]) throws Exception {
			
			double feelingPoorSpending = 0.5;			// included
			double behaviourChangeThreshold= 0.9;		// 1- if intrinsic financial optimism < threshold, then will not feel poorer
			
			int unemployedIncome = 1000;	// included
			int retiredIncome = 20000;		// included
			int longTermUnemployed = 12000;	// included
			
			int numberofPeopleToInterviewForEachRequiredCandidate = 10; // included
			
			int population = 10000; // included
			
			// 1 selects default lockdown production recipe
			// 2 selects medium stressed lockdown production recipe
			// 3 selects highly stressed lockdown production recipe
			int selectedLockdownRecipe = 1;		
			
			// get working population from peopleHouseholds
			//this.shortTermUnemployedList;
			
			RegionHouseholds ukHouseholds = new RegionHouseholds("UK", population, unemployedIncome, retiredIncome, longTermUnemployed);
			
			RegionIndustries initialisationIndustry = new RegionIndustries();
			initialiseStatics(initialisationIndustry, ukHouseholds);

			System.out.println("CSV time");
			readCsvIndustryProportions(initialisationIndustry);
			readCsvOccupationsInIndustryProportions(initialisationIndustry);
			
			//double tw = initialisationIndustry.workforceInIndustryCount[2];
			
			computeWorkforce(initialisationIndustry, ukHouseholds);
						
			System.out.println("Count time");
			computeOccupationsCountPerIndustry(initialisationIndustry);
			System.out.println("columns is " + initialisationIndustry.workforceOccupationProportionOfIndustry[0].length);
			System.out.println("rows is " + initialisationIndustry.workforceOccupationProportionOfIndustry.length);
			
			adjustOccupationsPerIndustry(initialisationIndustry);

			allocateOccupationsToShortTermUnemployed(initialisationIndustry, ukHouseholds);
			readCsvOccupationMedianWageNameRLI(initialisationIndustry);
			
			allocateOccupationsAndIndustryToEmployedAndSetWages(initialisationIndustry, ukHouseholds);
			
			ukHouseholds.setHhldIncomeAnddisplay();
			// now all households incomes have been computed
			
			//save this data in initial household incomes
			ukHouseholds.setHouseholdInitialIncomes();
			
			//compute household demand levels in terms of industries
			ukHouseholds.computeOverallHouseholdDemand(feelingPoorSpending, behaviourChangeThreshold);
			ukHouseholds.map55IndustriesTo21Industries(industryNames);
			ukHouseholds.computeAnnualWeeklyMonthlyDemand();
			
			// determine required output
			readProductionRecipes(initialisationIndustry);
			computeRequiredOutput(initialisationIndustry, ukHouseholds);
			
			//convert requied output in different scenarios from 1 column matrix to vector/ double[]
			convertMatricesToVector(initialisationIndustry);
			
			// maps required to map demand for 55 industires (derived from hhld demand, to 21 industry demand, so can be operated on
			map55IndustriesTo21Industries(initialisationIndustry.requiredOutputAnnualNormal55Array, 
					initialisationIndustry.requiredOutputAnnualNormal21Array);
			
			map55IndustriesTo21Industries(initialisationIndustry.requiredOutputAnnualLockdownDefaultStressRecipe55Array, 
					initialisationIndustry.requiredOutputAnnualLockdownDefaultStressRecipe21Array);
			
			map55IndustriesTo21Industries(initialisationIndustry.requiredLockdownAnnualMediumStressRecipe55Array, 
					initialisationIndustry.requiredLockdownAnnualMediumStressRecipe21Array);
			
			map55IndustriesTo21Industries(initialisationIndustry.requiredLockdownAnnualHighStressRecipe55Array, 
					initialisationIndustry.requiredLockdownAnnualHighStressRecipe21Array);
			
			imposeLockdownMeasures(initialisationIndustry, ukHouseholds);
			removeLockdownMeasures(initialisationIndustry, ukHouseholds);
			
			Statistics(initialisationIndustry, ukHouseholds);

			// select output level for industry based on external conditions
			selectOutputLevel(initialisationIndustry,selectedLockdownRecipe);
			initialisationIndustry.initialiseWorkersRequiredPerUnitOutput(); 
			initialisationIndustry.findRequiredWorkforceAndOccupstionsToMeetOutput();
			initialisationIndustry.requiredChangeInOccupationsWorkforce();
			
			Statistics(initialisationIndustry, ukHouseholds);
			
//			// print industry workforce
			hireAndFire(initialisationIndustry, ukHouseholds, numberofPeopleToInterviewForEachRequiredCandidate);
			ukHouseholds.updateAnnualIncomesToReflectChanges();
			
			Statistics(initialisationIndustry, ukHouseholds);
			ukHouseholds.determineCurrentHhldStatus();  // classify houses by employed, mixed, short term unemployed

//	*************************************************************************************************************************		
			storeLogOfRunDetails(initialisationIndustry, ukHouseholds);
			
			//for(int i = 0; i < 4; i++) {
			for(int imposeLockdown = 0; imposeLockdown < 12; imposeLockdown++) {
			//for(int i = 0; i < 34; i++) {
			dynamicEconomy(initialisationIndustry, ukHouseholds, true, feelingPoorSpending, numberofPeopleToInterviewForEachRequiredCandidate, behaviourChangeThreshold, selectedLockdownRecipe);
			storeLogOfRunDetails(initialisationIndustry, ukHouseholds);
			}
			
			
			//for(int i = 0; i < 47; i ++) {
			for(int liftLockdown = 0; liftLockdown < 40; liftLockdown++) {
			//for(int i = 0; i < 17; i ++) {
				
				dynamicEconomy(initialisationIndustry, ukHouseholds, false, feelingPoorSpending, numberofPeopleToInterviewForEachRequiredCandidate, behaviourChangeThreshold, selectedLockdownRecipe);
				storeLogOfRunDetails(initialisationIndustry, ukHouseholds);
				
			}
			System.out.println("Outputing CSVs");
			usefulMethods.CsvParser.integerArrayListToCSV(new String("overallPopulation"),initialisationIndustry.overallPopulation);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("populationExcludingChildren"),initialisationIndustry.populationExcludingChildren);

			usefulMethods.CsvParser.integerArrayListToCSV(new String("longTermUnemployedSetCount"),initialisationIndustry.longTermUnemployedSetCount );
			usefulMethods.CsvParser.integerArrayListToCSV(new String("retiredSetCount"),initialisationIndustry.retiredSetCount);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("employedSetCount"),initialisationIndustry.employedSetCount);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("shortTermUnemployedListCount"),initialisationIndustry.shortTermUnemployedListCount);

			usefulMethods.CsvParser.integerArrayListToCSV(new String("economicallyActiveHhldCount"),initialisationIndustry.economicallyActiveHhldCount);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("shortTermUnemployedHhldCount"),initialisationIndustry.shortTermUnemployedHhldCount);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("mixedHlhdCount"),initialisationIndustry.mixedHlhdCount);
			usefulMethods.CsvParser.integerArrayListToCSV(new String("employedHhldCount"),initialisationIndustry.employedHhldCount);

			usefulMethods.CsvParser.doubleNestedArrayListToCSV(new String("selectedOutput21History"),initialisationIndustry.selectedOutput21History);
			usefulMethods.CsvParser.doubleNestedArrayListToCSV(new String("overallWorkforceCountPerIndustry"),initialisationIndustry.overallWorkforceCountPerIndustry);
			
			usefulMethods.CsvParser.doubleNestedArrayListToCSV(new String("annualSumOfAllHouseholdDemandFor55IndustriesCount"),initialisationIndustry.annualSumOfAllHouseholdDemandFor55IndustriesCount);
			usefulMethods.CsvParser.doubleNestedArrayListToCSV(new String("annualSumOfAllHouseholdDemandFor21IndustriesCount"),initialisationIndustry.annualSumOfAllHouseholdDemandFor21IndustriesCount);
			
			System.out.println("Ending run");
		}
		
		public static void storeLogOfRunDetails(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) throws IOException {
			
			regionIndustries.overallPopulation.add(regionHouseholds.getPopulationSet().size());
			
			regionIndustries.populationExcludingChildren.add(regionHouseholds.getPopulationExcludingChildren()); 
			
			// employment stats --> workforce count
			regionIndustries.longTermUnemployedSetCount.add(regionHouseholds.getLongTermUnemployedSet().size()); 
			regionIndustries.retiredSetCount.add(regionHouseholds.getRetiredSet().size());
			regionIndustries.employedSetCount.add(regionHouseholds.getEmployedSet().size());
			regionIndustries.shortTermUnemployedListCount.add(regionHouseholds.getShortTermUnemployedList().size());
			
			// household stats
			regionIndustries.economicallyActiveHhldCount.add(regionHouseholds.getEconomicallyActiveHhld().size()); 
			regionIndustries.shortTermUnemployedHhldCount.add(regionHouseholds.getShortTermUnemployedHhldCount());
			regionIndustries.mixedHlhdCount.add(regionHouseholds.getMixedHlhdCount());
			regionIndustries.employedHhldCount.add(regionHouseholds.getEmployedHhldCount());

			// industry stats
			//initialisie industries
			for(int industry = 0; industry < 20; industry++) {
				regionIndustries.selectedOutput21History.add(new ArrayList<Double>());
				regionIndustries.overallWorkforceCountPerIndustry.add(new ArrayList<Double>());
			}
			
			// further initialisation
			for(int industry = 0; industry < 55; industry++) {
				regionIndustries.annualSumOfAllHouseholdDemandFor55IndustriesCount.add(new ArrayList<Double>());
			}
			for(int industry = 0; industry < 20; industry++) {
				regionIndustries.annualSumOfAllHouseholdDemandFor21IndustriesCount.add(new ArrayList<Double>());
			}

			
			for(int industry = 0; industry < 20; industry++) {
				regionIndustries.selectedOutput21History.get(industry)
				.add(regionIndustries.selectedOutput21[industry]);
				
				regionIndustries.overallWorkforceCountPerIndustry.get(industry)
				.add(regionIndustries.regionIndustries.get(industry).getOverallWorkforceCount());
				
			}
			
			for(int industry = 0; industry < 55; industry++) {
				regionIndustries.annualSumOfAllHouseholdDemandFor55IndustriesCount.get(industry).add(regionHouseholds.getAnnualSumOfAllHouseholdDemandFor55Industries()[industry]);
			}
			
			for(int industry = 0; industry < 20; industry++) {
				regionIndustries.annualSumOfAllHouseholdDemandFor21IndustriesCount.get(industry).add(regionHouseholds.getAnnualSumOfAllHouseholdDemandFor21Industries()[industry]);
			}
		}
		
		public static void dynamicEconomy(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds,
				boolean imposeLockdown, double feelingPoorSpending, int numberofPeopleToInterviewForEachRequiredCandidate, 
				double behaviourChangeThreshold, int selectedLockdownRecipe) throws Exception {
			
			// pay people
			
			// compute household demand
			
			// compute firm demand
			
			// fire and fire dynamics
			
			System.out.println("Dynamic behaviour");
			regionHouseholds.setHhldIncomeAnddisplay();
			regionHouseholds.determineCurrentHhldStatus();  // classify houses by employed, mixed, short term unemployed
//			// now all households incomes have been computed
//			
			if(imposeLockdown == true) {
				imposeLockdownMeasures(regionIndustries, regionHouseholds);
			}
			else {
				removeLockdownMeasures(regionIndustries, regionHouseholds);
			}
////
			// compute household demand and convert into 55 industry and 21 industry from
			regionHouseholds.computeOverallHouseholdDemand(feelingPoorSpending, behaviourChangeThreshold);
			regionHouseholds.map55IndustriesTo21Industries(industryNames);
			regionHouseholds.computeAnnualWeeklyMonthlyDemand();	// compute demand over different time horizons

			// determine required output
			//readProductionRecipes(regionIndustries);
			computeRequiredOutput(regionIndustries, regionHouseholds);
			
			//convert requied output in different scenarios from 1 column matrix to vector/ double[]
			convertMatricesToVector(regionIndustries);
			
			// maps required to map demand for 55 industires (derived from hhld demand, to 21 industry demand, so can be operated on
			map55IndustriesTo21Industries(regionIndustries.requiredOutputAnnualNormal55Array, 
					regionIndustries.requiredOutputAnnualNormal21Array);
			
			map55IndustriesTo21Industries(regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe55Array, 
					regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe21Array);
			
			map55IndustriesTo21Industries(regionIndustries.requiredLockdownAnnualMediumStressRecipe55Array, 
					regionIndustries.requiredLockdownAnnualMediumStressRecipe21Array);
			
			map55IndustriesTo21Industries(regionIndustries.requiredLockdownAnnualHighStressRecipe55Array, 
					regionIndustries.requiredLockdownAnnualHighStressRecipe21Array);
			
//			// select output level for industry based on external conditions
						selectOutputLevel(regionIndustries, selectedLockdownRecipe);
						
			// delete this!! regionIndustries.initialiseWorkersRequiredPerUnitOutput(); 
//						
			regionIndustries.findRequiredWorkforceAndOccupstionsToMeetOutput();
			regionIndustries.requiredChangeInOccupationsWorkforce();
			
			Statistics(regionIndustries, regionHouseholds);
			//regionHouseholds.sortWholePopulation();
			Statistics(regionIndustries, regionHouseholds);
			
			//printSetsAndPeople(regionHouseholds, regionIndustries);
			
			hireAndFire(regionIndustries, regionHouseholds, numberofPeopleToInterviewForEachRequiredCandidate);
			//regionHouseholds.updateAnnualIncomesToReflectChanges();
			Statistics(regionIndustries, regionHouseholds);

			
			
		}
		
		public static void initialiseStatics(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
			
			System.out.println("Initialising static employment data of all industries");
			regionIndustries.employedSet = regionHouseholds.getEmployedSet();
			regionIndustries.shortTermUnemployedList= regionHouseholds.getShortTermUnemployedList();
			
			
			System.out.println("Now have all employed people in region");
			
			System.out.println("Proceed to sort/ allocate to jobs");

		}
		
		
		public static void hireAndFire(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds, double multiple) throws Exception{
			// pick n* people at random from population
			
			System.out.println("Update household employed and unemployed set to reflect industry");

			regionHouseholds.setEmployedSet(regionIndustries.getEmployedSet());
			regionHouseholds.setShortTermUnemployedList(regionIndustries.getShortTermUnemployedList());
			

			
			for(Industry i: regionIndustries.regionIndustries) {
				i.hireFire(regionHouseholds, multiple);
				
				System.out.println("*******************************************************************");
				System.out.println("size of employed set: "+ regionHouseholds.getEmployedSet().size());
				System.out.println("size of population: "+ regionHouseholds.getPopulationSet().size());
				System.out.println("size of short term unemployed set: "+ regionHouseholds.getShortTermUnemployedList().size());
				System.out.println("size of long term unemployed set: "+ regionHouseholds.getLongTermUnemployedSet().size());
				System.out.println("size of retired set: "+ regionHouseholds.getRetiredSet().size());
			}
			//if unemployed AND have same occupation, hire
			
			// else, no worker found
			
			// update person employment status
			
			// update household status to reflect changes
		}

		public static void printSetsAndPeople(RegionHouseholds regionHouseholds, RegionIndustries regionIndustries) {
			
			ArrayList<Integer> temp;
			temp = regionHouseholds.getEmployedSet();
			//Collections.sort(temp);
			
			for(int i: temp) {
				System.out.println("Employed set person from hhlds: " + i);
			}
			
			temp = regionIndustries.getEmployedSet();
			//Collections.sort(temp);
			
			for(int j: temp) {
				System.out.println("Employed set person from indust: " + j);
			}
			
			temp = regionHouseholds.getRetiredSet();
			Collections.sort(temp);
			for(int x: temp) {
				System.out.println("Retired set person from hhlds: " + x);
			}
			
			temp = regionHouseholds.getShortTermUnemployedList();
			//Collections.sort(temp);
			for(int i: temp) {
				System.out.println("short term unemployed set person from hhlds: " + i);
			}
			
			temp = regionIndustries.getShortTermUnemployedList();
			//Collections.sort(temp);
			for(int i: temp) {
				System.out.println("short term unemployed set person from indus: " + i);
			}
			
			temp = regionHouseholds.getEmployedSet();
			//Collections.sort(temp);
			for(int i: temp) {
				System.out.println("Employed set person from hhlds: " + i);
			}
		}
		
		public void initialiseWorkersRequiredPerUnitOutput() {
			
			System.out.println("Update each industry with required output level");
			
			
			for(int i = 0; i < this.regionIndustries.size(); i++ ) {
				System.out.println(" selected ouput 21: "+ this.selectedOutput21[i]);
				this.regionIndustries.get(i).initialiseWorkforcePerUnitOutput(this.selectedOutput21[i]);
				// workforceOccupationProportionOfIndustry
			}
		}
		
		public void findRequiredWorkforceAndOccupstionsToMeetOutput() {
			
			System.out.println("Required workforce and occupations to meet output");
			for(int i = 0; i < this.regionIndustries.size(); i++ ) {
				this.regionIndustries.get(i).requiredWorkforceAndOccupationsToMeetOutput(false);
			}
		}
		
		public void requiredChangeInOccupationsWorkforce(){
			System.out.println("Required change in workforce and occupations to meet output");
			for(int i = 0; i < this.regionIndustries.size(); i++ ) {
				System.out.println("Industry: " + i + RegionIndustries.getIndustryNames()[i]);
				this.regionIndustries.get(i).requiredChangeInEmploymentToMeetOutput();
			}
		}
		
		public static void imposeLockdownMeasures(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
			regionIndustries.setLockdownImposed(true);
			//regionHouseholds.setLockdownImposed(true);
			regionHouseholds.getHouseholds().get(0).setLockdownImposed(true);
			
			System.out.println("Lockdown? "+ regionIndustries.isLockdownImposed());
		}
		
		public static void removeLockdownMeasures(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
			regionIndustries.setLockdownImposed(false);
			regionHouseholds.setLockdownImposed(false);
			regionHouseholds.getHouseholds().get(0).setLockdownImposed(false);
			
			System.out.println("Lockdown? "+ regionIndustries.isLockdownImposed());
		}
		
		public static void selectOutputLevel(RegionIndustries regionIndustries, int selectedLockdownRecipe) { 
			if(lockdownImposed == true) {
				//select lockdown recipe
				if(selectedLockdownRecipe == 1) {
				regionIndustries.selectedOutput21 = 
						regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe21Array;
				}
				else if(selectedLockdownRecipe == 2) {
					regionIndustries.selectedOutput21 = regionIndustries.requiredLockdownAnnualMediumStressRecipe21Array;
				}
				else {
					regionIndustries.selectedOutput21 = regionIndustries.requiredLockdownAnnualHighStressRecipe21Array;
				}
			}
			else {
				// select normal recipe
				regionIndustries.selectedOutput21 = regionIndustries.requiredOutputAnnualNormal21Array; 
			}
			// for each industry
			for(int i = 0; i < regionIndustries.regionIndustries.size(); i++ ) {
				System.out.println(" selected ouput 21: "+ regionIndustries.selectedOutput21[i]);
				regionIndustries.regionIndustries.get(i).setRequiredCurrentOutput(regionIndustries.selectedOutput21[i]);
				// workforceOccupationProportionOfIndustry
			}
		}
		
		public static void map55IndustriesTo21Industries(double[] array55, double[] array21) {
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
						sumDemand += array55[industry + count];
					}
					
					array21[industryGroup] = sumDemand;	
				count += numberOfIndustriesInGroup;
				industryGroup +=1;
				}
				else {
					// -1
					// store + run 2 loops
					// industries R and S
					//set R and set S
					// increment counter by 2
					
					
					double store = array55[count] * 0.5; 
					
					array21[industryGroup] = store;
					array21[industryGroup + 1] = store;
					
					count += 1;	// advance offset on large vector of 55 industries by 1 as past RS now
					industryGroup += 2; // advance index on vector of 21 industires by 2!!
				}
			}
			
			// Set final industry U to 0 --> ignoring extra-territorial!!
			array21[20] = 0;

			System.out.println("Mapping process to annual complete");
			for(int i = 0; i < 21; i++ ) {
				System.out.println("Industry " + i + " demand: "+ array21[i]);
			}
		}
		
		public double[] convertMatrixToVector(RealMatrix matrix) {
			
			double[] output = new double[55];
			for(int i = 0; i<55; i++) {
				output[i] = matrix.getEntry(i, 0);
			}			
			return output;		
		}
		
		public static void convertMatricesToVector(RegionIndustries regionIndustries) {
			// regionIndustries.requiredOutputAnnualNormal;
			regionIndustries.requiredOutputAnnualNormal55Array = regionIndustries.convertMatrixToVector(regionIndustries.requiredOutputAnnualNormal);
			regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe55Array = regionIndustries.convertMatrixToVector(regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe);
			regionIndustries.requiredLockdownAnnualMediumStressRecipe55Array = regionIndustries.convertMatrixToVector(regionIndustries.requiredLockdownAnnualMediumStressRecipe);
			regionIndustries.requiredLockdownAnnualHighStressRecipe55Array = regionIndustries.convertMatrixToVector(regionIndustries.requiredLockdownAnnualHighStressRecipe);

		}
	
		/**
		 * Use linear algebra to compute the required output for different production recipes
		 * @param regionIndustries
		 * @param regionHouseholds
		 */
		public static void computeRequiredOutput(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
		// get household demand vector 55
		double[] test = regionHouseholds.getAnnualSumOfAllHouseholdDemandFor55Industries();
		double[][] annualDemand = new double[55][1];
		
		for(int i = 0; i<55; i++) {
			annualDemand[i][0] = test[i];
		}
		
		// leontief
		//RealVector householdAnnualDemandTest = MatrixUtils.createRealVector(test);
		RealMatrix convertedVectorToMatrix  = MatrixUtils.createRealMatrix(annualDemand);
		System.out.println("columns of vector converted to matrix: " + convertedVectorToMatrix.getColumnDimension());
		System.out.println("rows of vector converted to matrix: " + convertedVectorToMatrix.getRowDimension());
		
		RealMatrix inputNormalRecipe = MatrixUtils.createRealMatrix(regionIndustries.inputOutputNoRecipeCutbacks);
		RealMatrix inputLockdownDefaultStressRecipe = MatrixUtils.createRealMatrix(regionIndustries.inputOutputRecipeCutbacksSlightlyStressed);
		RealMatrix inputLockdownMediumStressRecipe = MatrixUtils.createRealMatrix(regionIndustries.inputOutputRecipeCutbacksMediumStressed);
		RealMatrix inputLockdownHighStressRecipe = MatrixUtils.createRealMatrix(regionIndustries.inputOutputRecipeCutbacksMostStressed);
		
		RealMatrix identity55 = MatrixUtils.createRealIdentityMatrix(55);
		
		int rows = inputNormalRecipe.getRowDimension();
		int columns = inputNormalRecipe.getColumnDimension();
		
		RealMatrix b = identity55.subtract(inputNormalRecipe);
		RealMatrix bLockdownDefaultStress = identity55.subtract(inputNormalRecipe);
		RealMatrix bLockdownMediumStress  = identity55.subtract(inputNormalRecipe);
		RealMatrix bLockdownHighStress  = identity55.subtract(inputNormalRecipe);

		//RealMatrix b = identity55 - inputNormalRecipe;
		
		// using QR decomposition as it provides LEAST SQUARES SOLUTIONS, therefore can deal with 
				//there being multiple solutions --> it apporximates the best solution
		RealMatrix bInverse = new QRDecomposition(b).getSolver().getInverse(); 
		RealMatrix bLockdownDefaultStressInverse = new QRDecomposition(bLockdownDefaultStress).getSolver().getInverse();
		RealMatrix bLockdownMediumStressInverse = new QRDecomposition(bLockdownMediumStress).getSolver().getInverse(); 
		RealMatrix bLockdownHighStressInverse = new QRDecomposition(bLockdownHighStress).getSolver().getInverse(); 
		
		System.out.println("inverse column dimension " +bInverse.getColumnDimension());
		System.out.println("inverse row dimension " + bInverse.getRowDimension());
		
		RealMatrix requiredOutputNormal = bInverse.multiply(convertedVectorToMatrix);
		RealMatrix requiredOutputLockdownDefaultStressRecipe = bLockdownDefaultStress.multiply(convertedVectorToMatrix);
		RealMatrix requiredLockdownMediumStressRecipe = bLockdownMediumStress.multiply(convertedVectorToMatrix);
		RealMatrix requiredLockdownHighStressRecipe = bLockdownHighStress.multiply(convertedVectorToMatrix);

		System.out.println("requiredOutput column dimension " +requiredOutputNormal.getColumnDimension());
		System.out.println("requiredOutput row dimension" + requiredOutputNormal.getRowDimension());
		
		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " normal output: " + requiredOutputNormal.getEntry(i, 0));
		}
		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " defualt stress output: " + requiredOutputLockdownDefaultStressRecipe.getEntry(i, 0));
		}

		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " medium stress output: " + requiredLockdownMediumStressRecipe.getEntry(i, 0));
		}

		// remove -ve values. Set as 0
		for(int i = 0; i < 55; i++) {
			if(requiredOutputNormal.getEntry(i, 0)<0)
				requiredOutputNormal.setEntry(i, 0,0);
			
			if(requiredOutputLockdownDefaultStressRecipe.getEntry(i, 0)<0)
				requiredOutputLockdownDefaultStressRecipe.setEntry(i, 0,0);
			
			if(requiredLockdownMediumStressRecipe.getEntry(i, 0)<0)
				requiredLockdownMediumStressRecipe.setEntry(i, 0,0);
			
			if(requiredLockdownHighStressRecipe.getEntry(i, 0) < 0)
				requiredLockdownHighStressRecipe.setEntry(i, 0, 0);

		}
		
		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " normal output: " + requiredOutputNormal.getEntry(i, 0));
		}
		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " default stress output: " + requiredOutputLockdownDefaultStressRecipe.getEntry(i, 0));
		}

		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " medium stress output: " + requiredLockdownMediumStressRecipe.getEntry(i, 0));
		}

		regionIndustries.requiredOutputAnnualNormal = requiredOutputNormal;
		regionIndustries.requiredOutputAnnualLockdownDefaultStressRecipe = requiredOutputLockdownDefaultStressRecipe;
		regionIndustries.requiredLockdownAnnualMediumStressRecipe = requiredLockdownMediumStressRecipe;
		regionIndustries.requiredLockdownAnnualHighStressRecipe = requiredLockdownHighStressRecipe;
	
		
		/**
		 * regionIndustries.requiredMaterialInputsAnnualNormal = inputNormalRecipe.multiply(requiredOutputNormal);
		regionIndustries.requiredMaterialInputsAnnualLockdownDefaultStressRecipe = inputLockdownDefaultStressRecipe.multiply(requiredOutputLockdownDefaultStressRecipe);
		regionIndustries.requiredMaterialInputsLockdownAnnualMediumStressRecipe = inputLockdownMediumStressRecipe.multiply(requiredLockdownMediumStressRecipe);
		regionIndustries.requiredMaterialInputsLockdownAnnualHighStressRecipe = inputLockdownHighStressRecipe.multiply(requiredLockdownHighStressRecipe);

		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " raw materials: " + 
					regionIndustries.requiredMaterialInputsAnnualNormal.getEntry(i, 0));
		}
		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " default stress raw materials: " + 
					regionIndustries.requiredMaterialInputsAnnualLockdownDefaultStressRecipe.getEntry(i, 0));
		}

		for(int i = 0; i < 55; i++) {
			System.out.println("Industry " + i + " medium stress raw materials: " + 
					regionIndustries.requiredMaterialInputsLockdownAnnualMediumStressRecipe.getEntry(i, 0));
		}

		**/
				// new SingularValueDecomposition(p).getSolver().getInverse();
		// new QRDecomposition(B).getSolver().getInverse()
		// new LUDecomposition(B).getSolver().getInverse()
	}
		
		
		public static void computeWorkforce(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
			
			// count all employed people in region
			double employedWorkforceCount = regionIndustries.employedSet.size();
			double unemployedWorkforceCount = regionIndustries.shortTermUnemployedList.size();
			double estimatedWorkforce;
			double estimatedSum = 0;
			
			double sumPct = 0;
			
			System.out.println("All people in region: " + regionIndustries.employedSet.size() + regionIndustries.shortTermUnemployedList.size());
			System.out.println("employed people in region:" + regionIndustries.employedSet.size());
			System.out.println("short-term unemployed people in region:" + regionIndustries.shortTermUnemployedList.size());
			
			// approximate people in each industry
			//employedWorkforceCount= employedWorkforceCount * 100;
			 
			for(int i = 0; i< regionIndustries.workforceInIndustryCount.length; i++) {
				
				estimatedWorkforce = (int) (0.5 + employedWorkforceCount * regionIndustries.percentWorkforceInIndustry[i]);
				regionIndustries.workforceInIndustryCount[i] = estimatedWorkforce;
				
				estimatedSum += estimatedWorkforce;
				sumPct += regionIndustries.percentWorkforceInIndustry[i];
				
				System.out.println("industry " + i + " workforce: " + estimatedWorkforce);
				
			}
			if(sumPct != 1) {
				System.out.println(" sumPctCheck: " + sumPct);
				System.out.println(" sumPct does not equal 1");
			}
			
			System.out.println("total estimated workforce: " + estimatedSum);
			System.out.println("actual workforce: " + employedWorkforceCount);
			
			// if the two do not match
			
			int difference;
			difference = (int) (employedWorkforceCount - estimatedSum);
			System.out.println("difference is " + difference);
			// smaller
			if(difference > 0) {				
				// allocate each extra person across industries until none left
				System.out.println("loop1");

				for(int i = 0; i < difference; i++) {
					//mod needed for circular loop

					int divisor = (int) (difference % 20);		// neglecting industry 21
					regionIndustries.workforceInIndustryCount[divisor] = regionIndustries.workforceInIndustryCount[divisor] + 1;  
				}
				//while loop
			}
			// larger
			else if(difference < 0) {
				System.out.println("loop2");

				for(int i = 0; i < (-1*difference); i++) {
					//mod needed for circular loop
					int divisor = (int) (-1*difference % 20);		// neglecting industry 21
					regionIndustries.workforceInIndustryCount[divisor] = regionIndustries.workforceInIndustryCount[divisor] - 1;  
				}	
			}
		
			//check sum workforce estimate
			double sumCheck = 0;
			for(double i: regionIndustries.workforceInIndustryCount) {
				sumCheck += i;
			}
				
			
			System.out.println("Post modification");
			System.out.println("total estimated workforce: " + sumCheck);
			System.out.println("actual workforce: " + employedWorkforceCount);
			
		}
		
		public static void readCsvIndustryProportions(RegionIndustries regionIndustries) {
			
			// read in Occupation proportion of workforce data
			String fileName = "src\\industry\\IndustriesProportionOfOverallWorkforce2.csv";	        
			List<List<String>> csv= usefulMethods.CsvParser.readCSVtoStringList(fileName);
			
			
	       double[][] proportionOfWorkforce = usefulMethods.CsvParser.convertListtoArray(csv);
	       
	       System.out.println(proportionOfWorkforce.length);
	       regionIndustries.percentWorkforceInIndustry = proportionOfWorkforce[0];
	       
		}
	       
	    public static void readCsvOccupationsInIndustryProportions(RegionIndustries regionIndustries) {
			
	    	   String occupationsFile =  "src\\industry\\OccupationsPctPerGivenIndustry2.csv";
		       List<List<String>> occupationsList= usefulMethods.CsvParser.readCSVtoStringList(occupationsFile);
		       occupationsList = usefulMethods.CsvParser.removeLabelsFromList(occupationsList);
		       
		       double[][] occupationsArray= usefulMethods.CsvParser.convertListtoArray(occupationsList);
		       
		       System.out.println("now have array of occupations within each industry");
		       
		       regionIndustries.workforceOccupationProportionOfIndustry = occupationsArray;
		       
		       
		       System.out.println("dimensions: rows" + regionIndustries.workforceOccupationProportionOfIndustry.length);
		       System.out.println("dimensions: columns" + regionIndustries.workforceOccupationProportionOfIndustry[0].length);

		       
		       /**
		        *for(double[] i: occupationsArray) {
		        *
		    	*  for(double j: i)
		    	*	   System.out.println("value" + i);
		        * }
		       **/
		}
		
	    public static void computeOccupationsCountPerIndustry(RegionIndustries regionIndustries) {
	    	 
	    	// multiply by number working in each industry

	    	//get industries
	    	RegionIndustries industries = regionIndustries;
	    	
	    	double[][] workforceOccupations = industries.workforceOccupationProportionOfIndustry;
	    	industries.workforceOccupationOfIndustryCount = workforceOccupations;
	    	
	    	int industryCount= workforceOccupations[0].length;
	    	int occupationsCount = workforceOccupations.length;
	    	
	    	System.out.println("ind count" + industryCount);
	    	
	    	System.out.println("Enter");
	    	for(int i = 0; i< industryCount; i++)
	    	{
	    		for(int j = 0; j < occupationsCount; j++) {
	    			industries.workforceOccupationOfIndustryCount[j][i] = 
	    					industries.workforceInIndustryCount[i] * industries.workforceOccupationProportionOfIndustry[j][i];
	    					
	    			industries.workforceOccupationOfIndustryCount[j][i] = (int)(0.5 + industries.workforceOccupationProportionOfIndustry[j][i]);
	    			
	    		//System.out.println("test" + industries.workforceOccupationOfIndustryCountInteger[j][i]);
	    		}
	    	}
	    	
	    	
	    	// for each industry
	    		// for each occupation
	    	
	    	// adjust where necessary
	    }
	    
	    public static void adjustOccupationsPerIndustry(RegionIndustries regionIndustries) {
	    	
	    	// compare occupations per industry to estimates
	    	
	    	double[] actual = regionIndustries.workforceInIndustryCount;
	    	double[] estimated = new double[actual.length];
	    	double[][] allOccupations = regionIndustries.workforceOccupationOfIndustryCount;
	    	
	    	double sumEstimate;
	    	int industry= actual.length;
	    	int occupation = allOccupations.length;
	    	//double[] allOccupationsGivenIndustry = new double[occupation];

	    	
	    	for(int i = 0; i < industry; i++) {	    		
	    		sumEstimate = 0;
	    		int maxLocation = 0;
    			double max = 0;
    			double difference = 0;
    			
	    		for(int j = 0; j < occupation; j++) {
	    			sumEstimate += allOccupations[j][i];
	    			//allOccupationsGivenIndustry[j]= allOccupations[j][i];
	    		
	    			if(allOccupations[j][i] > max) {
    					maxLocation = j;
    					max = allOccupations[j][i];
    				}
	    		}
	    		System.out.println("Check1");
	    		System.out.println("estimate1: " + sumEstimate);
	    		System.out.println("actual1: " + actual[i]);
	    		System.out.println("difference1: " + (sumEstimate - actual[i]));
	    		
	    		// deal with differences
	    		difference = sumEstimate - actual[i];
	    		System.out.println("difference is " + difference);
	    	//	Random random = new Random();

	    
	    			// add difference to largest occupation
	    			if(difference > 0) {
	    				System.out.println("loop 1");
		    			// if estimate > than actual
	    				allOccupations[maxLocation][i] -= difference;
	    			}
	    			else if(difference < 0) {
	    				System.out.println("loop 2");
	    				difference = difference*-1;
	    				allOccupations[maxLocation][i] += difference;	
	    			}
	    			
	    			for(int k = 0; k < occupation; k++) {
	    				regionIndustries.workforceOccupationOfIndustryCount[k][i] = allOccupations[k][i];
	    			}
	    			 
	    		
	    				int sumEstimate2 = 0;
	    			for(int j = 0; j < occupation; j++) {
		    			sumEstimate2 += allOccupations[j][i];
		    			}
		    	
	    			System.out.println("Check 2");
		    		System.out.println("estimate2: " + sumEstimate2);
		    		System.out.println("actual2: " + actual[i]);
		    		System.out.println("difference2: " + (sumEstimate2 - actual[i]));
		    				
	    	}
	    }
	    
	    public static void allocateIncomeToEconomicallyInactive(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
	    	
	    }
	    
	    public static void allocateOccupationsAndIndustryToEmployedAndSetWages(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
		   
	    	// get set of people to be employed
	    	double[][] occupationsInIndustriesCount = regionIndustries.workforceOccupationOfIndustryCount;
	    	double[] workforceInIndustry = regionIndustries.workforceInIndustryCount;
	    	double[] occupationMedianWage = regionIndustries.medianWagesForOccupations;
	    	double[] occupationsRLI = regionIndustries.rliOccupations;
	    	
	    	int occupationsCount = occupationsInIndustriesCount.length; 
	    	int industriesCount =  20; 			//occupationsInIndustriesCount[0].length-1;	because ignoring EXTRATERRESTRIAL INDUSTRY	//20   	
	    	
	    	int personId;
	    	ArrayList<Integer> initiallyEmployedPeople = regionIndustries.employedSet;
	    	System.out.println("employed set size: " + initiallyEmployedPeople.size());
	    	Collections.shuffle(initiallyEmployedPeople);
	    	
	    	int cumulativeJobsAllocated = 0; 
	    	
	    	for(int industry = 0; industry < industriesCount; industry++) {
	    		
	    		// create new industry
	    		String industryName = regionIndustries.industryNames[industry];
	    		int industryNumber = industry;
	    		
	    		Industry newIndustry = new Industry();
	    		newIndustry.setId(industryNumber);
	    		newIndustry.setIndustryName(industryName);
	    		newIndustry.setCurrentNumberOfEmployeesPerOccupation(new int[occupationsCount]);

	    		//set occupation RLI and median wage	    		
	    		newIndustry.setOccupationRLI(occupationsRLI);
	    		newIndustry.setOccupationMedianWage(occupationMedianWage);
	    		
	    		// get the occupation tracker list
	    		ArrayList<ArrayList<Integer>> employeesByOccupation = newIndustry.getAllEmployeesByOccupation();
	    		ArrayList<Integer> occupationsArray;

	    		for(int occupation = 0; occupation < occupationsCount; occupation++) {
	    		
	    		// add row of new occupation to employeeByOccupation List
	    		occupationsArray = new ArrayList<Integer>();
	    		
		    	// get proportions for each occupation and get scaled numbers for each occupation
		    	// divide short term unemployed population by 20 and allocated each occupation to this number of citizens
			    	int jobsInOccupation = (int) occupationsInIndustriesCount[occupation][industry];
			    	
			    		//allocate the jobs in for loop
			
			    		// allocate this many jobs per occupation to short term unemployed until none left. Then allocate remaining
			    		
			    		for(int personAllocated = 0; personAllocated < jobsInOccupation; personAllocated++) {
			    			// get person from population set + set status
			    			personId = initiallyEmployedPeople.get(cumulativeJobsAllocated + personAllocated);
			    			System.out.println("person drawn: "+ (cumulativeJobsAllocated + personAllocated));
			    			System.out.println("cumu jobs allocated including 0: "+ (cumulativeJobsAllocated+1));
			    			System.out.println("Person id from employed set: " + personId);
			    			
			    			//update person status to reflect occupation they have, in spite of being unemployed
			    			Person person = regionHouseholds.getPopulationSet().get(personId);
			    			person.setOccupation(occupation);
			    			person.setIncome((int) occupationMedianWage[occupation]);
			    			person.setInitialEmploymentStatus(true);
			    			person.setIndustry(industry);
			    			person.setStatus(regionHouseholds.getLabels()[0]);	// ensure labelled as employed
			    			
			    			//System.out.println("Person id: " + personId);
			    			//System.out.println("remaining people to allocate: "+ initiallyEmployedPeople.size());
			    			
			    			// add person to employeeByOccupation List
			    			occupationsArray.add(personId);
			    		}
		    			cumulativeJobsAllocated += jobsInOccupation;

			    	// add list of all people in occupation to workforceInOccupationsArray
			    		employeesByOccupation.add(occupationsArray);
			    		newIndustry.getCurrentNumberOfEmployeesPerOccupation()[occupation] = jobsInOccupation;
			    }
	    		// update aggregate figures
	    		newIndustry.setOverallWorkforceCount(workforceInIndustry[industry]);
	    		// add new industry to colletion in regionIndustries
	    		regionIndustries.regionIndustries.add(newIndustry);
	    		System.out.println("Adding new initialised industry to set");
	    		
	    		System.out.println("Have not added stock levels yet");
		    }
	    	Collections.sort(initiallyEmployedPeople);

	    }
	    
	    public static void allocateOccupationsToShortTermUnemployed(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
	    
	    	// get proportions for each occupation and get scaled numbers for each occupation
	    	// divide short term unemployed popoulation by 20 and allocated each occuption to this number of citizens
	    	
	    	int numberOfOccupations = regionIndustries.workforceOccupationOfIndustryCount.length;
	    	System.out.println("number of Occupations is: " + numberOfOccupations);	
	    	
	    	int jobsPerOccupation = (int) ((double) (regionIndustries.shortTermUnemployedList.size())/numberOfOccupations);
	    	int remainingJobsToAllocate = regionIndustries.shortTermUnemployedList.size();
	    	ArrayList<Integer> shortTermUnemployed = regionIndustries.shortTermUnemployedList;    	
	    	System.out.println("JObs to allocate per occupation: " + jobsPerOccupation);
	    	
	    	
	    	int peopleToAllocate;
	    	int personId;
	    	ArrayList<Integer> tempShortTermUnemp = new ArrayList<Integer>();
			int cumulativeJobsAllocated = 0;

	    	Collections.shuffle(shortTermUnemployed);
	    	// allocate this many jobs per occupation to short term unemployed until none left. Then allocate remaining
	    	for(int occupation = 0; occupation< numberOfOccupations; occupation++ ) {
	    		System.out.println("Occupation count: " + occupation);
	    		System.out.println("Max occupation: " + numberOfOccupations);
	    		
	    		if(occupation < (numberOfOccupations -1)) {
	    			peopleToAllocate = jobsPerOccupation;
	    			System.out.println("Not final allocation. people to allocate to occupation: " + peopleToAllocate);
	    		}
	    		else { 
	    			//if final allocation, allocate remaining jobs to ensure that do not allocate more people than
	    			// there actually are
	    			peopleToAllocate= remainingJobsToAllocate;
	    			System.out.println("Final allocation. remaining people to allocate to occupation: " + peopleToAllocate);
	    		}
	    		
	    		System.out.println("people to allocate to occupation: " + peopleToAllocate);
	    		for(int personAllocated = 0; personAllocated < peopleToAllocate; personAllocated++) {
	    			// get person from population set + set status
	    			// allocating occupations to short-term unemployed, NOT removing them from the LIST
	    			personId = shortTermUnemployed.get(cumulativeJobsAllocated + personAllocated);
	    			System.out.println("person drawn: "+ (cumulativeJobsAllocated + personAllocated));
	    			System.out.println("cumu jobs allocated including 0: "+ (cumulativeJobsAllocated+ personAllocated + 1));
	    			
	    			//update person status to reflect occupation they have, in spite of being unemployed
	    			Person person = regionHouseholds.getPopulationSet().get(personId);
	    			person.setOccupation(occupation);
	    			person.setIncome(0);
	    			person.setInitialEmploymentStatus(false);	// set initial employment status 
	    			person.setIndustry(-1);
	    			person.setStatus(regionHouseholds.getLabels()[1]);	// ensure labelled as short term employed

	    			
	    			System.out.println("Person id: " + personId);
	    			
	    		}
	    		cumulativeJobsAllocated += peopleToAllocate;
	    		// Make an arrayList for each occupation
	    		// may have to use industry object to make these
	    		
	    		remainingJobsToAllocate = regionIndustries.shortTermUnemployedList.size() - cumulativeJobsAllocated;
	    		System.out.println("End of loop Remaining jobs to allocate: " + remainingJobsToAllocate);
	    		System.out.println("Jobs per occupation: " + jobsPerOccupation);
	    		//System.out.println("number of short term unemployed remaining: " + shortTermUnemployed.size());
    			//System.out.println("Actual short term unemp set in households size is: " + regionHouseholds.getShortTermUnemployedList().size());
    			//System.out.println("Actual short term unemp set in industries size is: " + regionIndustries.shortTermUnemployedList.size());
	    	}
	    	Collections.sort(shortTermUnemployed);

	    }
	    
	    public static void readCsvOccupationMedianWageNameRLI(RegionIndustries regionIndustries) {
	    	
	    	String wages = "src\\industry\\OccupationsMedianWageNoLabels.csv";	        
	    	String occupationLabels = "src\\industry\\OccupationsNames.csv";	        
	    	String occupationRLI = "src\\industry\\OccupationsRLINoLabels.csv";	        

	    	
	    	List<List<String>> csvWages= usefulMethods.CsvParser.readCSVtoStringList(wages);
			List<List<String>> csvOccupationNames= usefulMethods.CsvParser.readCSVtoStringList(occupationLabels);
			List<List<String>> csvOccupationRLI= usefulMethods.CsvParser.readCSVtoStringList(occupationRLI);
			
			String[] labels = usefulMethods.CsvParser.getColumnLabels(csvOccupationNames);
			regionIndustries.occupations = labels;
			
			double[][] OccupationsMedianWageGrid = usefulMethods.CsvParser.convertListtoArray(csvWages);
			double[] OccupationsMedianWage = OccupationsMedianWageGrid[0];
			regionIndustries.medianWagesForOccupations = OccupationsMedianWageGrid[0];
			
			double[][] OccupationsRLIGrid = usefulMethods.CsvParser.convertListtoArray(csvOccupationRLI);
			double[] OccupationsRLI = OccupationsRLIGrid[0];
			regionIndustries.rliOccupations = OccupationsRLIGrid[0];
			
			for(int i = 0; i < regionIndustries.medianWagesForOccupations.length; i++ ) {
				System.out.println(labels[i] + " median wage: " + OccupationsMedianWage[i] + " RLI: " + OccupationsRLI[i]);
			}
			// alternative could be to use getters and setters, but would still need to pass OBJECT into method as being passed in now. 
			// Superfluous if all operations on object take place within OBJECT/ CLASS itself!!
			
			// rather than passing whole object in, EASIER to use getters and setters!!
			// getters and setters allow object to be operated on remotely/ from OUTSIDE the object itself.
			
			// getter and setter used here would be necessary IF accessing from a REMOTE object/ different object of a different class
			// Allow parameters of OTHER objects to be set + modified!!!
	    }
	    public static void readCsvLabels(RegionIndustries regionIndustries) {
	    	
	    }
	    
	    public static void readProductionRecipes(RegionIndustries regionIndustries) {
	    	String noCutbacks = "src\\industry\\ProductionRecipeNoCutbacksNoLabels.csv";	
	    	String leastStressedCutbacks = "src\\industry\\ProductionRecipeLeastStressedCutbacksNoLabels.csv";
	    	String mediumStressedCutbacks = "src\\industry\\ProductionRecipeMediumCutbacksNoLabels.csv";	
	    	String mostStressedCutbacks = "src\\industry\\ProductionRecipeMostStressedCutbacksNoLabels.csv";	
	    	
	    	List<List<String>> noCutbacksList= usefulMethods.CsvParser.readCSVtoStringList(noCutbacks);
	    	List<List<String>> leastStressedCutbacksList= usefulMethods.CsvParser.readCSVtoStringList(leastStressedCutbacks);
	    	List<List<String>> mediumStressedCutbacksList= usefulMethods.CsvParser.readCSVtoStringList(mediumStressedCutbacks);
	    	List<List<String>> mostStressedCutbacksList= usefulMethods.CsvParser.readCSVtoStringList(mostStressedCutbacks);
	    	
	    	regionIndustries.inputOutputNoRecipeCutbacks = usefulMethods.CsvParser.convertListtoArray(noCutbacksList);
	    	regionIndustries.inputOutputRecipeCutbacksSlightlyStressed = usefulMethods.CsvParser.convertListtoArray(leastStressedCutbacksList);
	    	regionIndustries.inputOutputRecipeCutbacksMediumStressed = usefulMethods.CsvParser.convertListtoArray(mediumStressedCutbacksList);
	    	regionIndustries.inputOutputRecipeCutbacksMostStressed = usefulMethods.CsvParser.convertListtoArray(mostStressedCutbacksList);
	    }
	    // initialise wages
	    public static void initialiseWages(RegionIndustries regionIndustries) {
	    	
	    }
	    
	    public static void Statistics(RegionIndustries regionIndustries, RegionHouseholds regionHouseholds) {
	    	

	    	
	    	System.out.println("economically active population is " + regionHouseholds.getEconomicallyActivePopulation());
	    	System.out.println("employed population is " + regionHouseholds.getEmployedSet().size());
	    	System.out.println("short-term unemployed population " + regionHouseholds.getShortTermUnemployedList().size());
	    	System.out.println("retired population is " + regionHouseholds.getRetiredSet().size());
	    	System.out.println("long term unemployed population is " + regionHouseholds.getLongTermUnemployedSet().size());
	    	
	    	System.out.println("population excl children " + regionHouseholds.getPopulationExcludingChildren());
	    	
	    	//System.out.println("Household information");
	    	//System.out.println("Short term unemployed households" + regionHouseholds.hous);
	    	//System.out.println("Mixed employment households" + )
	    	//System.out.println("Fully employed households" + )
	    }

		public static String[] getIndustryNames() {
			return industryNames;
		}

		public static boolean isLockdownImposed() {
			return lockdownImposed;
		}

		public static void setLockdownImposed(boolean lockdownImposed) {
			RegionIndustries.lockdownImposed = lockdownImposed;
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

}
