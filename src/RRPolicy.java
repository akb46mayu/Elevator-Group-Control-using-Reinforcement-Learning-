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
// Round Robin Policy
public class RR2Policy extends Policy {
	public RR2Policy () {
		
	}
	
	public RR2Policy(String instance_name) {
		super(instance_name);
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
		        //traverse each elevator   
                //find the initial states of those elevators (for each state it is computed the same e0=0,e1=9)
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
		        
				for(int i = 0; i < numOfEle; i ++){  //for each elevator
				       //System.out.println("position[i]="+position[i]);
				       ArrayList<LCONST> actionterms = new ArrayList<LCONST>();
					   actionterms.add(elevators.get(i));
					   ArrayList<LCONST> floorterms = new ArrayList<LCONST>();
					   floorterms.add(floors.get(position[i]));
						   // state when the elevator is at the top floor
						if(position[i]==numOfFlo-1){
						   if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)
						   & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-down");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   }
						   else if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)==false 
						   & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   }
						   else {
							  //System.out.println("action=move-current-dir");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   } 
						}
						// when the elevator is at the bottom floor 
						else if (position[i]==0){
						//System.out.println("******e0 at floor f0");
						Boolean btmp=(Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms);
						//System.out.println("eledirup-check="+btmp);
						   if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms)==false 
						   & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-up");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   }
						   else if( (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"), actionterms) 
						   & (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   }
						   else{
							 // System.out.println("action=move-current-dir");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						   }								
						}
						// the elevator is not from the top and bottom, but person is waiting up on the floor
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-up"), floorterms) 
						& (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms) 
						& (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-up");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-up")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						}
						// the elevator is not from the top and bottom, but person is waiting down on the floor
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("person-waiting-down"), floorterms) 
						& (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-dir-up"),actionterms)==false 
						& (Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)){
							  //System.out.println("action=open-door-going-down");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("open-door-going-down")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						}	
						// opened door needs to be closed
						else if((Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-closed"), actionterms)==false){
							  //System.out.println("action=close door");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("close-door")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
												
						}
						else{
						     // System.out.println("action=move-current-dir");
							  PVAR_INST_DEF newActionBit;
							  newActionBit = new PVAR_INST_DEF(new PVAR_NAME("move-current-dir")._sPVarName, (Object)true, actionterms);
							  actions.add(newActionBit);
						}
					
				}

			
	return actions;
	}

}
