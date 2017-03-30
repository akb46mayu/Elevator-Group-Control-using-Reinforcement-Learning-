package rddl.policy;
import java.util.ArrayList;
import java.util.Map;
import rddl.EvalException;
import rddl.RDDL.TYPE_NAME;
import rddl.State;
import rddl.RDDL.LCONST;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.PVAR_NAME;
import util.Permutation;
// Written by Bo Fan
// My policy is based on the Round Robin Policy.
// I add several constraints to different elevators to make them perform differently in the same floor such as open door-going-down or going up
// I also add two positions around the middle floors, where the elevator can change its direction except for the top and the bottom floor.
public class BoFanPolicy extends Policy {
	public BoFanPolicy () {
		
	}
	
	public BoFanPolicy(String instance_name) {
		super(instance_name);
	}
	// this function computes the total number of "person-waiting-up" from the upper half of the floors
	public int costwaitup(int posupper, ArrayList<LCONST> floors,int numOfFlo,State s){			   
				   int totalsum=0;				   
				   for(int j = posupper; j < numOfFlo; j ++){
				      ArrayList<LCONST> floorterms2 = new ArrayList<LCONST>();	
				      floorterms2.add(floors.get(j));					  
				      if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-up"), floorterms2)) 					  
					     totalsum=totalsum+1;
				   }				
				   return totalsum;
	}
	// this function computes the total number of "person-waiting-down" from the lower half of the floors
    public int costwaitdown(int poslower, ArrayList<LCONST> floors,int numOfFlo,State s){			   
				   int totalsum=0;				   
				   for(int j = 0; j <= poslower; j ++){
				      ArrayList<LCONST> floorterms2 = new ArrayList<LCONST>();	
				      floorterms2.add(floors.get(j));					  
				      if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-down"), floorterms2)) 					  
					     totalsum=totalsum+1;
				   }				
				   return totalsum;
	}
	
	@Override
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {
		        ArrayList<PVAR_INST_DEF> actions = new ArrayList<PVAR_INST_DEF>();
                //System.out.println("s="+s); // print all previous states and actions
		        ArrayList<LCONST> elevators = s._hmObject2Consts.get(new TYPE_NAME("elevator"));
                ArrayList<LCONST> floors = s._hmObject2Consts.get(new TYPE_NAME("floor"));
                int numOfFlo = floors.size();
				int numOfEle = elevators.size();
                int[] position = new int[numOfEle];
				int posupper=numOfFlo/2;
				int poslower=numOfFlo/2-1;
		        //traverse each elevator   
                //my: find the intitial states of those two elevators (for each state it is computed the same e0=0, e1=9)
				for(int i = 0; i < numOfEle; i ++){
					//which floor is this elevator in
					for(int j = 0; j < numOfFlo; j ++){              
						ArrayList<LCONST> terms = new ArrayList<LCONST>();
						terms.add(elevators.get(i));  //exp: terms: [e0,f1]
						terms.add(floors.get(j));
						if((Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-at-floor"), terms)){
							position[i] = j;
							break;
						}
					}
				}
			
		        PVAR_INST_DEF checkActionBit_opendoor_goup;
				PVAR_INST_DEF checkActionBit_opendoor_godown;
				ArrayList<LCONST> tempterms = new ArrayList<LCONST>();
				tempterms.add(elevators.get(0));
				checkActionBit_opendoor_goup = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, tempterms);
				checkActionBit_opendoor_godown = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, tempterms);
				ArrayList<LCONST> upperfloorterms = new ArrayList<LCONST>();
				upperfloorterms.add(floors.get(posupper));
				ArrayList<LCONST> lowerfloorterms = new ArrayList<LCONST>();
				lowerfloorterms.add(floors.get(poslower));
				int k1=0;
				k1=costwaitup(posupper, floors,numOfFlo,s);
				//System.out.println(">>>>>>>>k1="+k1);
				int k2=0;
				k2=costwaitdown(poslower, floors,numOfFlo,s);
				//System.out.println(">>>>>>>>k2="+k2);
				for(int i = 0; i < numOfEle; i ++){  //for each elevator
				       //System.out.println("position[i]="+position[i]);
				       ArrayList<LCONST> actionterms = new ArrayList<LCONST>();
					   actionterms.add(elevators.get(i));
					   ArrayList<LCONST> floorterms = new ArrayList<LCONST>();
					   floorterms.add(floors.get(position[i]));
					   PVAR_INST_DEF newActionBit;
					   // state when the elevator is at the top floor
						if(position[i]==numOfFlo-1){
						   if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)& (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-down");  
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, actionterms);							  
						   }
						   else if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						   }
						   else {
							  //System.out.println("action=move-current-dir");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							 
						   } 
						}
						// when the elevator is at the bottom floor 
						else if (position[i]==0){
						//System.out.println("******e0 at floor f0");
						Boolean btmp=(Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms);
						//System.out.println("eledirup-check="+btmp);
						   if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-up");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						   }
						   else if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						   }
						   else{
							  //System.out.println("action=move-current-dir");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						   }								
						}
						// the elevator is not from the top and bottom, but person is waiting up on the floor
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-up"), floorterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-up");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						}
						// the elevator is not from the top and bottom, but person is waiting down on the floor
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-down"), floorterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-down");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						}	  
						// opened door needs to be closed
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
												
						}
                        // when the person is at the lower position f4 (if 10floors), no people waiting up at f4, and etc. 						
						else if(position[i]==poslower & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-up"), lowerfloorterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("person-in-elevator-going-up"), actionterms)==false){ 
						       // if the num of person-waiting up from the upper half floors<num of person-waiting down from the upper half floors
							   if(costwaitup(posupper, floors,numOfFlo,s)<costwaitdown(poslower, floors,numOfFlo,s)){
							     newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, actionterms);
								 //System.exit(1);
							   }
							   else{
							   
							     newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							   }

						}
						// when the person is at the upper position f5 (if ten floors), no people waiting down at f5, and etc.
						else if(position[i]==posupper & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms) & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-down"), upperfloorterms)==false & (Boolean)s.getPVariableAssign(new PVAR_NAME("person-in-elevator-going-down"), actionterms)==false){ 
						       // if the num of person-waiting down from the lower half floors<num of person-waiting up from the upper half floors
							   if(costwaitup(posupper, floors,numOfFlo,s)>costwaitdown(poslower, floors,numOfFlo,s)){
							     newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, actionterms);
								 //System.exit(1);
							   }
							   else{							   
							     newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							   }

						}
						else{
						      //System.out.println("action=move-current-dir");
							  //PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							  //actions.add(newActionBit);
						}
					
						       // action combination, delete the case when the same elevator will open door and go to the same direction 
						       int sameactionbit=0;
						       int elevator_index=0; //start from 0
							   for(PVAR_INST_DEF eachbit: actions){
							        
									//eachbit._sPredName;
									//System.out.println(">>>>>>eachbit._sPredName="+eachbit._sPredName);
									
									if(eachbit._sPredName.equals(newActionBit._sPredName) & eachbit._sPredName.equals(checkActionBit_opendoor_goup._sPredName) & position[i]==position[elevator_index] & position[i]!=0 & position[i]!=numOfFlo-1  ){ 
										  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);								
										  //System.out.println(">>>>>same action is found here<<<<<<<<");
										  actions.add(newActionBit);
										  sameactionbit=1;
										  //System.exit(1);
									      break;
									}
									else if(eachbit._sPredName.equals(newActionBit._sPredName) & eachbit._sPredName.equals(checkActionBit_opendoor_godown._sPredName) & position[i]==position[elevator_index] & position[i]!=0 & position[i]!=numOfFlo-1  ){
										 newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);								
										  //System.out.println(">>>>>same action is found here<<<<<<<<");
										  actions.add(newActionBit);
										  sameactionbit=1;
										  //System.exit(1);
										  break;
									}
									else {
									  //actions.add(newActionBit);
									  
									
									}
									elevator_index=elevator_index+1;
							  } 		
                              if(sameactionbit==1){
							  }
                              else{
							        actions.add(newActionBit);
							  }	
				}

	return actions;
	}

}
