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

public class RolloutTrafficPolicy2 extends Policy {	
	public Random _random = new Random();
	DOMAIN _theDomain;
	String _instanceName;	
	public RolloutTrafficPolicy2 () { 
		super();
	}	
	public RolloutTrafficPolicy2(String instance_name) {
		super(instance_name);
	}
	public boolean checkreasonable(State cs,ArrayList<PVAR_INST_DEF> factions ){	
	   return true;
	}
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {
		//get the domain and parameters
		INSTANCE instance = rddl._tmInstanceNodes.get(_sInstanceName);
		DOMAIN domain = rddl._tmDomainNodes.get(instance._sDomain);		
		int NUM_CONCURRENT_ACTIONS = instance._nNonDefActions;		
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		// get the current position of the robot
		State cs0 = new State();	//tempState			
		cs0 = (State) DeepCloneUtil.deepClone(s); 
		ArrayList<LCONST> rxpos = cs0._hmObject2Consts.get(new TYPE_NAME("xpos")); // [x0,x1]	
		ArrayList<LCONST> rypos = cs0._hmObject2Consts.get(new TYPE_NAME("ypos")); //[y0,y1]
		int xcpos=0;
		int ycpos=0;
		int breakoutloop=0;
        for(int i=0;i<=3;i++){
		  for(int j=0;j<=3;j++){
		     ArrayList<LCONST> terms = new ArrayList<LCONST>();
		     terms.add(rxpos.get(i));  //exp: terms: [e0,f1]
		     terms.add(rypos.get(j));
			 if((Boolean)cs0.getPVariableAssign(new PVAR_NAME("robot-at"), terms)){
			   xcpos=i;
			   ycpos=j;
			   breakoutloop=1;
			   System.out.println("--0---find pos");
			   break;
			 }
		  }
		  if(breakoutloop==1){
		  break;
		  }		
		}
		int W=10;
		int h=5;
		//int mybi[numOfEle][4]={0};
		int mybi[]=new int[4];
		int currentaction=0;
		double allreward[]=new double[4];//for all elevators
		// rollout part
		for(int j=1;j<=W;j++){
			System.out.println("xcpos=" + xcpos);
			System.out.println("ycpos=" + ycpos);
			 //randomly choose an action		
			RandomConcurrentPolicy _actionGenerator = new RandomConcurrentPolicy();		
			State cs = new State();	//tempState	
			// this step will give cs all of the possible states and actions TF values
			cs = (State) DeepCloneUtil.deepClone(s); 		
			// a variable capturing a concurrent action	
			ArrayList<PVAR_INST_DEF> _SampleAction = new ArrayList<PVAR_INST_DEF>();
			// find a legal random action under the random policy
		    while(true){
				    ArrayList<PVAR_INST_DEF> _SampleActiontmp = new ArrayList<PVAR_INST_DEF>();
				    State csnexttmp = new State();
					csnexttmp=cs;
					// sample an action from another policy
					_SampleActiontmp = _actionGenerator.getActions(cs);
					csnexttmp.computeNextState(_SampleActiontmp, _random);
					if(checkreasonable(csnexttmp,_SampleActiontmp)==true){
					_SampleAction = _SampleActiontmp;// sample an action from another policy
					break;
					}					
				}	
				//compute next state //this will store the next state and current state at the same time
		        cs.computeNextState(_SampleAction, _random);
				//System.out.println("****** cs1=" + cs);
				//compute reward //the reward is computed based on the "current state"
				double reward = ((Number)domain._exprReward.sample(new HashMap<LVAR,LCONST>(), cs, _random)).doubleValue();	
				//System.out.println("====nowreward=" + reward);
				ArrayList<LCONST> posterms = new ArrayList<LCONST>();
		        posterms.add(rxpos.get(xcpos));
				posterms.add(rypos.get(ycpos));
				// compute the total number of each action from W samples
				if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-east"), posterms)){
				  mybi[0]=mybi[0]+1;
				}
				else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-west"), posterms)){
				 mybi[1]=mybi[1]+1;
				}
				else if((Boolean)cs.getPVariableAssign(new PVAR_NAME("move-south"), posterms)){
				 mybi[2]=mybi[2]+1;
				}
				else{
				 mybi[3]=mybi[3]+1;
				}
				cs.advanceNextState();//entering the next state
				//System.out.println("****** cs2=" + cs);
			    ArrayList<PVAR_NAME> pvarnames = s._alActionNames;						    
			   allreward[currentaction]=allreward[currentaction]+reward;
			   // a variable capturing a concurrent action	
			   ArrayList<PVAR_INST_DEF> _SampleAction2 = new ArrayList<PVAR_INST_DEF>();	
			   for(int k=1;k<=h;k++){
			       _SampleAction2 = _actionGenerator.getActions(cs);
				   //compute next state //this will store the next state and current state at the same time
		            cs.computeNextState(_SampleAction2, _random);
				    //System.out.println("****** cs1=" + cs);
					//compute reward //the reward is computed based on the "current state"
				    reward = ((Number)domain._exprReward.sample(new HashMap<LVAR,LCONST>(), cs, _random)).doubleValue();				
				    allreward[currentaction]=allreward[currentaction]+reward;
				    cs.advanceNextState();//entering the next state
		       }
		  }
				//average 
	    for(int i=0;i<=3;i++){
		  
			   if (mybi[i]==0){
				 allreward[i]=0;
			  }
			  else{
				 allreward[i]=allreward[i]/mybi[i];
			  }		  	 
		}
		// find the best arm
		int maxarm=-1;		
		double maxreward=-10000;
		for(int i=0;i<=3;i++){		
				if(allreward[i]>maxreward){
				 maxreward=allreward[i];
				 maxarm=i;				
			   }						
		}
        ArrayList<PVAR_INST_DEF> finalAct = new ArrayList<PVAR_INST_DEF>();
		System.out.println("****** s=" + s);
		State cs = new State();	//tempState	
		cs = (State) DeepCloneUtil.deepClone(s); 
        ArrayList<PVAR_NAME> pvarnames = cs._alActionNames;
		PVAR_INST_DEF newActionBit;	         							
		newActionBit = new PVAR_INST_DEF( pvarnames.get(maxarm)._sPVarName, (Object)true, null);		
		ArrayList<PVAR_INST_DEF> _SampleAction3 = new ArrayList<PVAR_INST_DEF>();// a
		_SampleAction3.add(newActionBit);
		finalAct.add(newActionBit );
		return finalAct;
	}
	
}

