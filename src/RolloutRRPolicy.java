package rddl.policy;

/* This is a template to start your work */

import java.util.*;
import javax.naming.InitialContext;

import rddl.EvalException;
import rddl.RDDL;
import rddl.RDDL.*;
import rddl.State;
import rddl.RDDL.BOOL_EXPR;
import rddl.RDDL.CPF_DEF;
import rddl.RDDL.DOMAIN;
import rddl.RDDL.EXPR;
import rddl.RDDL.INSTANCE;
import rddl.RDDL.ENUM_TYPE_DEF;
import rddl.RDDL.LCONST;
import rddl.RDDL.LVAR;
import rddl.RDDL.OBJECTS_DEF;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.PVAR_NAME;
import rddl.RDDL.TYPE_DEF;
import rddl.RDDL.TYPE_NAME;
import util.Permutation;
import rddl.DeepCloneUtil;

public class RolloutRRPolicy extends Policy {
	
	public Random _random = new Random();
	DOMAIN _theDomain;
	String _instanceName;
	
	public RolloutRRPolicy () { 
		super();
	}
	
	public RolloutRRPolicy(String instance_name) {
		super(instance_name);
	}
	public boolean checkreasonable(State cs,ArrayList<PVAR_INST_DEF> factions ){
	    int output=0;
	    ArrayList<LCONST> elevators = cs._hmObject2Consts.get(new TYPE_NAME("elevator")); // [e0,e1]
		int numOfEle = elevators.size();
		ArrayList<LCONST> floors = cs._hmObject2Consts.get(new TYPE_NAME("floor")); //[f0,f1,…f9]
		int numOfFlo = floors.size();		
		for(int i=0;i<=numOfEle-1;i++){
		   ArrayList<LCONST> termsbottom = new ArrayList<LCONST>();
		   ArrayList<LCONST> termstop = new ArrayList<LCONST>();
						//if((Boolean)s.getPVariableAssign(new PVAR_NAME("elevator-at-floor"), terms)){
	       termsbottom.add(elevators.get(i));  //exp: terms: [e0,f1]
		   termsbottom.add(floors.get(0));
		   termstop.add(elevators.get(i));  //exp: terms: [e0,f1]
		   termstop.add(floors.get(numOfFlo-1));
		   ArrayList<LCONST> elevatorterms = new ArrayList<LCONST>();		  
		   elevatorterms.add(elevators.get(i));
		   if((Boolean)cs.getPVariableAssign(new PVAR_NAME("elevator-closed"), elevatorterms)
		     &(Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms)
			 &(Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms)
  			 ){
			 //System.out.println("check not passed");
		     return false;
			 
			
		   }
		   else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("elevator-at-floor"), termsbottom)
		   & (Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms)  
		  
		   ){ 
		     //System.out.println("check not passed");
		      return false;
			  
			
		   }
		   else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("elevator-at-floor"), termstop)
		   & (Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-up"), elevatorterms)  
		  
		   ){
		      //System.out.println("check not passed");
		      return false;
			  
			 
		   }
           else if(!(Boolean)cs.getPVariableAssign(new PVAR_NAME("elevator-closed"), elevatorterms)
		   & !(Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms)
		   ){
		     return false;
		   
		   }
           else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("elevator-closed"), elevatorterms)
		   & (Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms)
		   ){
		     return false;		   
		   }		   		   
		   else{
		       output=output+1;
			  
		   }
		}
	     if(output==numOfEle){
			   //System.out.println("total check  passed");
			   return true;
		       }
			  else{
				//System.out.println("total check not passed");
				return false;
				}
	}
	public ArrayList<PVAR_INST_DEF> RR2Policy(State s) throws EvalException {
		        ArrayList<PVAR_INST_DEF> actions = new ArrayList<PVAR_INST_DEF>();
                //System.out.println("s="+s); // print all previous states and actions
		        ArrayList<LCONST> elevators = s._hmObject2Consts.get(new TYPE_NAME("elevator"));
                ArrayList<LCONST> floors = s._hmObject2Consts.get(new TYPE_NAME("floor"));
                int numOfFlo = floors.size();
				int numOfEle = elevators.size();
                int[] position = new int[numOfEle];
		        //traverse each elevator   
                //find the initial states of those  elevators (for each state it is computed the same e0=0,e1=9)
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
	
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {
		//get the damin and parameters
		INSTANCE instance = rddl._tmInstanceNodes.get(_sInstanceName);
		DOMAIN domain = rddl._tmDomainNodes.get(instance._sDomain);
		int NUM_CONCURRENT_ACTIONS = instance._nNonDefActions;
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
     
        ArrayList<LCONST> elevators = s._hmObject2Consts.get(new TYPE_NAME("elevator")); // [e0,e1]
		int numOfEle = elevators.size();

		
		ArrayList<LCONST> floors = s._hmObject2Consts.get(new TYPE_NAME("floor")); //[f0,f1,…f9]
		int numOfFlo = floors.size();

		int W=150;
		int h=5;
		//int mybi[numOfEle][4]={0};
		int mybi[][]=new int[4][4];
		//System.out.println("******mybi[0][0]=" + mybi[0][0]);
		//System.out.println("******mybi[1][0]=" + mybi[1][0]);
		int currentactione0=0;
		int currentactione1=0;
		double allreward[][]=new double[4][4];//for all elevators
		for(int j=1;j<=W;j++){
		        RandomConcurrentPolicy _actionGenerator = new RandomConcurrentPolicy(); //randomly choose an action				
				State cs = new State();	//tempState					
				cs = (State) DeepCloneUtil.deepClone(s); // this step will give cs all of the possible states and actions TF values
				//System.out.println("****** cs0=" + cs);
				ArrayList<PVAR_INST_DEF> _SampleAction = new ArrayList<PVAR_INST_DEF>();// a variable capturing a concurrent action	
				// find a legal random action under the random policy
                while(true){
				    ArrayList<PVAR_INST_DEF> _SampleActiontmp = new ArrayList<PVAR_INST_DEF>();
				    State csnexttmp = new State();
					csnexttmp=cs;
					_SampleActiontmp = _actionGenerator.getActions(cs);// sample an action from another policy
					csnexttmp.computeNextState(_SampleActiontmp, _random);
					if(checkreasonable(csnexttmp,_SampleActiontmp)==true){
					_SampleAction = _SampleActiontmp;// sample an action from another policy
					break;
					}					
				}	
				cs.computeNextState(_SampleAction, _random);//compute next state //this will store the next state and current state at the same time
				//System.out.println("****** cs1=" + cs);
				double reward = ((Number)domain._exprReward.sample(new HashMap<LVAR,LCONST>(), cs, _random)).doubleValue();//compute reward //the reward is computed based on the "current state"	
				//System.out.println("====nowreward=" + reward);
				ArrayList<LCONST> elevatorterms = new ArrayList<LCONST>();
		        elevatorterms.add(elevators.get(0));
				ArrayList<LCONST> elevatorterms2 = new ArrayList<LCONST>();
		        elevatorterms2.add(elevators.get(1));
				if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms)){//for e0's action is a0   
						if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms2)){						
							mybi[0][0]=mybi[0][0]+1;
							currentactione0=0;currentactione1=0;						
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms2)){						
							mybi[0][1]=mybi[0][1]+1;
							currentactione0=0;currentactione1=1;								
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms2)){
						
						  mybi[0][2]=mybi[0][2]+1;
							currentactione0=0;currentactione1=2;								
						}
						else{						
						   mybi[0][3]=mybi[0][3]+1;
							currentactione0=0;currentactione1=3;											   				
						}					
				}
				else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms)){
			            if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms2)){						
							mybi[1][0]=mybi[1][0]+1;
							currentactione0=1;currentactione1=0;						
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms2)){						
							mybi[1][1]=mybi[1][1]+1;
							currentactione0=1;currentactione1=1;								
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms2)){
						
						  mybi[1][2]=mybi[1][2]+1;
							currentactione0=1;currentactione1=2;								
						}
						else{						
						   mybi[1][3]=mybi[1][3]+1;
							currentactione0=1;currentactione1=3;											   				
						}					
				}
				else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms)){
                        if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms2)){						
							mybi[2][0]=mybi[2][0]+1;
							currentactione0=2;currentactione1=0;						
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms2)){						
							mybi[2][1]=mybi[2][1]+1;
							currentactione0=2;currentactione1=1;								
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms2)){
						
						  mybi[2][2]=mybi[2][2]+1;
							currentactione0=2;currentactione1=2;								
						}
						else{						
						   mybi[2][3]=mybi[2][3]+1;
							currentactione0=2;currentactione1=3;											   				
						}									
				}
				else{
				        if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-current-dir"), elevatorterms2)){						
							mybi[3][0]=mybi[3][0]+1;
							currentactione0=3;currentactione1=0;						
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("close-door"), elevatorterms2)){						
							mybi[3][1]=mybi[3][1]+1;
							currentactione0=3;currentactione1=1;								
						}
						else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("open-door-going-down"), elevatorterms2)){
						
						  mybi[3][2]=mybi[3][2]+1;
							currentactione0=3;currentactione1=2;								
						}
						else{						
						   mybi[3][3]=mybi[3][3]+1;
							currentactione0=3;currentactione1=3;											   				
						}									   				
				}
				//System.out.println("====_SampleAction=" + _SampleAction);
				//System.out.println("====currentaction=" + currentaction);
				
				cs.advanceNextState();//entering the next state
				//System.out.println("****** cs2=" + cs);
			    ArrayList<PVAR_NAME> pvarnames = s._alActionNames;			
			    //System.out.println("****** pvarnames=" + pvarnames);
			   //System.out.println("****** _SampleAction.get(0)=" + _SampleAction.get(0));
			   //System.out.println("****** currentaction=" + currentaction);
			   allreward[currentactione0][currentactione1]=allreward[currentactione0][currentactione1]+reward;
			   ArrayList<PVAR_INST_DEF> _SampleAction2 = new ArrayList<PVAR_INST_DEF>();// a variable capturing a concurrent action		
			   for(int k=1;k<=h;k++){
			       //_SampleAction2 = _actionGenerator.getActions(cs);
				   /*while(true){
				        State csnexttmp = new State();
					    csnexttmp=cs;
						ArrayList<PVAR_INST_DEF> _SampleActiontmp = new ArrayList<PVAR_INST_DEF>();
						_SampleActiontmp = _actionGenerator.getActions(cs);// s
						csnexttmp.computeNextState(_SampleActiontmp, _random);//sample an action from another policy
						if(checkreasonable(csnexttmp,_SampleActiontmp)==true){
						_SampleAction2 = _SampleActiontmp;// sample an action from another policy
						break;
						}						
					}	*/

					_SampleAction2=RR2Policy(cs);			
		            cs.computeNextState(_SampleAction2, _random);//compute next state //this will store the next state and current state at the same time
				    //System.out.println("****** cs1=" + cs);
				    reward = ((Number)domain._exprReward.sample(new HashMap<LVAR,LCONST>(), cs, _random)).doubleValue();//compute reward //the reward is computed based on the "current state"				
				    allreward[currentactione0][currentactione1]=allreward[currentactione0][currentactione1]+reward;
				    cs.advanceNextState();//entering the next state
		       }
		}
		//find the best
	    for(int i=0;i<=3;i++){
		  for(int j=0;j<=3;j++){
			   if (mybi[i][j]==0){
				 allreward[i][j]=-10000000;
			  }
			  else{
				 allreward[i][j]=allreward[i][j]/mybi[i][j];
			  }
		  }		 
		}
		//System.out.println("Arrays.deepToString(array)"+Arrays.deepToString(allreward));
		
		int maxarme0=-1;
		int maxarme1=-1;
		double maxreward=-10000;
		for(int i=0;i<=3;i++){
			for(int j=0;j<=3;j++){
				if(allreward[i][j]>=maxreward){
				 maxreward=allreward[i][j];
				 maxarme0=i;
				 maxarme1=j;
			   }			
			}
		}
		//System.out.println(" maxarme0="+ maxarme0);
		//System.out.println("maxarme1="+maxarme1);
		ArrayList<PVAR_INST_DEF> finalAct = new ArrayList<PVAR_INST_DEF>();
		   //finalAct=_SampleAction;
		   ArrayList<PVAR_NAME> pvarnames = s._alActionNames;
		   ArrayList<LCONST> actionterms = new ArrayList<LCONST>();
		   actionterms.add(elevators.get(0));
		   PVAR_INST_DEF newActionBit;		   
		   newActionBit = new PVAR_INST_DEF( (pvarnames.get(maxarme0))._sPVarName, (Object)true, actionterms);		   
		   finalAct.add(newActionBit);		   
		   ArrayList<LCONST> actionterms2 = new ArrayList<LCONST>();
		   actionterms2.add(elevators.get(1));
		   PVAR_INST_DEF newActionBit2;		   
		   newActionBit2 = new PVAR_INST_DEF( (pvarnames.get(maxarme1))._sPVarName, (Object)true, actionterms2);		   
		   finalAct.add(newActionBit2);		   
		return finalAct;
	}
	
}

