
File Structure of Code

The source code can be found in the following git repository: 
https://github.com/yeCodes/UK_Economy_ABM_Final

The code in the git repository is contained in the folder entitled, ‘UK_Economy_ABM_Final’. The source code can be found within the ‘src’ folder. There are three important folders within this, the ‘peopleHouseholds’ folder, which contains the classes defining person agents, household agents and regional household agents.  The industry package contains the industry class, which defines industry agents and the class which defines regional industry agents. It is this ‘RegionIndustries’ class that contains the main method, which initialises and runs the simulation. 
There are numerous CSV files within the ‘industry’ and ‘peopleHouseholds’ packages. These are used for model initialisation.

Running the Model

To run the model, go to the main method found in ‘~/industry/RegionIndustries’ and enter the model parameters, which are to be found between lines 125 and 140 of the class. Lastly, to vary the duration of lockdown, go to line 224 within the main method and vary the number of ‘imposeLockdown’ for loops. Do the same on 232, this time within the ‘liftLockdown’ loop, to set the number of simulations cycles to run after lockdown has been removed. 
The ‘Results’ folder at the top of the file directory contains the results of the 13 model scenarios that have been commented on in this project. 
The model outputs all the results of a simulation to CSV files at the highest level of the file directory (the level of the ‘src’ and ‘Results’ folders). Before running subsequent scenarios, the results should be manually transferred to the ‘Results’ folder and given column and row headers that are consistent with the labelling conventions used in the other scenario within the folder.

Abstract

This paper analyses the effect of lockdown and the closure of non-essential industries on the UK economy in response to the COVID-19 pandemic. The paper makes a case for the use of agent-based modelling for policy-making decisions. 
Owing to the broad scope of the project, it aims to provide a proof of concept that establishes a methodology for creating an agent-based model, which expands upon the existing literature by modelling households and employment dynamics in greater detail. 
The model gives insights into five key areas: 
1.	The ways in which lockdown affects key parts of the UK economy
2.	How people  in the UK are affected by multiple rounds of lockdown
3.	How the effect on people propagates through the economy and affects households
4.	How the adjustments impacted households make to their spending affect industries 
5.	How different industries adjust to lockdown conditions
These insights form a useful starting point for further exploration of what an optimal economic policy response to the crisis should look like. 
Thirteen different economic scenarios are presented in the paper. The response of the UK economy to the imposition and removal of lockdown conditions, as well as the closure of non-essential industries is examined across the thirteen scenarios. 
The model scenarios show that the economy is most sensitive to the amount by which people cut their household expenditure when they feel poorer and that industries will cut workforces aggressively in response to lockdown and the closure of non-essential industries. The model scenarios also show that all industries are not affected equally by the lockdown. 
