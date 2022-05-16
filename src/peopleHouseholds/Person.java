package peopleHouseholds;


public class Person implements Comparable<Person>{
	
	private int id;
	private String Status;
	private boolean initialEmploymentStatus;
	private boolean currentEmploymentStatus;
	private int unemployedIncome = 0; 	//8000;
	private int retiredIncome = 0;	//20000;
	private int longTermUnemployed = 0;	//12000;
	

	private int income = 0;
	private int initialIncome = -1;
	
	private int Industry = -1;
	private int Occupation;
	

	public Person(int id) {
		this.id = id;
	}
	public Person(int id, String Status, boolean initialEmploymentStatus) {
		this.id = id;
		this.Status  = Status;
		this.initialEmploymentStatus = initialEmploymentStatus;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getLongTermUnemployed() {
		return longTermUnemployed;
	}
	public void setLongTermUnemployed(int longTermUnemployed) {
		this.longTermUnemployed = longTermUnemployed;
	}
	public int getRetiredIncome() {
		return retiredIncome;
	}
	public void setRetiredIncome(int retiredIncome) {
		this.retiredIncome = retiredIncome;
	}

	
	public int getUnemployedIncome() {
		return unemployedIncome;
	}
	public void setUnemployedIncome(int unemployedIncome) {
		this.unemployedIncome = unemployedIncome;
	}
	
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public boolean isInitialiEmploymentStatus() {
		return initialEmploymentStatus;
	}
	public void setInitialiEmploymentStatus(boolean initialiEmploymentStatus) {
		this.initialEmploymentStatus = initialiEmploymentStatus;
	}
	public boolean isCurrentEmploymentStatus() {
		return currentEmploymentStatus;
	}
	public void setCurrentEmploymentStatus(boolean currentEmploymentStatus) {
		this.currentEmploymentStatus = currentEmploymentStatus;
	}
	public int getIncome() {
		return income;
	}
	public void setIncome(int income) {
		this.income = income;
	}
	public int getIndustry() {
		return Industry;
	}
	public void setIndustry(int industry) {
		Industry = industry;
	}
	public int getOccupation() {
		return Occupation;
	}
	public void setOccupation(int occupation) {
		Occupation = occupation;
	}
	
	public boolean isInitialEmploymentStatus() {
		return initialEmploymentStatus;
	}
	public void setInitialEmploymentStatus(boolean initialEmploymentStatus) {
		this.initialEmploymentStatus = initialEmploymentStatus;
	}
	
	@Override
	public int compareTo(Person o) {
		// TODO Auto-generated method stub
		return this.id - o.getId();
	}
	
}
