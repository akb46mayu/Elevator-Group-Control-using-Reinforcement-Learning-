/**
 *
 *
 * RDDL: Implements a random policy for a domain with concurrent actions
 *       (allows mixed action types)
 * 
 * @author Tom Walsh (thomasjwalsh@gmail.com)
 * @author Scott Saner (ssanner@gmail.com)
 * @version 11/7/10
 *
 **/

package rddl.policy;

import java.util.*;

import rddl.*;
import rddl.RDDL.*;
import util.Permutation;

public class RandomConcurrentPolicy extends Policy {
		
	public int NUM_CONCURRENT_ACTIONS = 3; // Max number of non-default concurrent actions
	public int MAX_INT_VALUE = 5; // Max int value to use when selecting random action
	public double MAX_REAL_VALUE = 5.0d; // Max real value to use when selecting random action
	
	public RandomConcurrentPolicy () { 
		super();
	}
	
	public RandomConcurrentPolicy(String instance_name) {
		super(instance_name);
	}

	public void setNumConcurrentActions(int num_concurrent) {
		NUM_CONCURRENT_ACTIONS = num_concurrent;
	}
	
	public void setActionMaxIntValue(int max_int_value) {
		MAX_INT_VALUE = max_int_value;
	}
	
	public void setActionMaxRealValue(double max_real_value) {
		MAX_REAL_VALUE = max_real_value; 
	}
	
	boolean CheckSameTerm(ArrayList<LCONST> termA, ArrayList<LCONST> termB){
		boolean ifSame = true;
		
		for(LCONST aterm: termA){
			boolean ifExist = false;
			for(LCONST bterm: termB){
				if(aterm.equals(bterm)){
					ifExist = true;
					break;
				}
			}
			if(!ifExist){
				ifSame = false;
				break;
			}
		}
		return ifSame;
	}
	
	public ArrayList<PVAR_INST_DEF> getActions(State s) throws EvalException {
		
		ArrayList<PVAR_INST_DEF> actions = new ArrayList<PVAR_INST_DEF>();
		//get concurrency
		NUM_CONCURRENT_ACTIONS = s._nMaxNondefActions;
		//check constraits
		boolean passed_constraints = false;
		while(true){
			// Get a random action
			//s._alActionNames can give you all predicates
			PVAR_NAME p = s._alActionNames.get(_random.nextInt(s._alActionNames.size()));
			//get definition of p
			PVARIABLE_DEF pvar_def = s._hmPVariables.get(p);
			//get the term list
			//s.generateAtoms gives you all possible parameters for predicate p
			ArrayList<ArrayList<LCONST>> inst = s.generateAtoms(p);
			//randomly pick up one
			ArrayList<LCONST> terms = inst.get(_random.nextInt(inst.size()));
			//build an action
			PVAR_INST_DEF newAction = new PVAR_INST_DEF(p._sPVarName, true, terms);
			//check redandency
			boolean ifExists = false;
			for(PVAR_INST_DEF eachbit: actions){
				if(newAction._sPredName.equals(eachbit._sPredName) && 
						CheckSameTerm(newAction._alTerms, eachbit._alTerms) && 
								newAction._oValue.equals(eachbit._oValue)){
					ifExists = true;
					break;
				}
			}
			// Now set the action
			if(!ifExists){
				actions.add(newAction);
				passed_constraints = true;
				//interface of checking a=constraints
				try {
					s.checkStateActionConstraints(actions);
				} catch (EvalException e) { 
					// Got an eval exception, constraint violated
					passed_constraints = false;
					//System.out.println(actions + " : " + e);
					//System.out.println(s);
					//System.exit(1);
				} catch (Exception e) { 
					// Got a real exception, something is wrong
					System.out.println("\nERROR evaluating constraint on action set: " + 
							actions + /*"\nConstraint: " +*/ e + "\n");
					e.printStackTrace();
					throw new EvalException(e.toString());
				}
				if(!passed_constraints)
					actions.remove(actions.size()-1); 
			}
			
			//if we get what want then quit
			if (actions.size() == NUM_CONCURRENT_ACTIONS)
				break;
		}
				
		// Return the action list
		//System.out.println("**Action: " + actions);
		return actions;
	}

}
