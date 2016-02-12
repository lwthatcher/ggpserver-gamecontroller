package tud.gamecontroller.game.ggpbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.factory.PropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;

public class PropNetStateMachine extends StateMachine {
	private long isTerminalTime=0;
	private long getGoalTime=0;
	private long getInitialStateTime=0;
	private long getLegalMovesTime=0;
	private long getNextStateTime=0;
	private long getOrderingTime=0;
	private long depthChargeTime=0;
	public void printTimes(){
		System.out.println("isTerminalTime:      "+isTerminalTime);
		System.out.println("getGoalTime:         "+getGoalTime);
		//System.out.println("getInitialStateTime: "+getInitialStateTime);
		System.out.println("getLegalMovesTime:   "+getLegalMovesTime);
		System.out.println("getNextStateTime:    "+getNextStateTime);
		//System.out.println("getOrderingTime:     "+getOrderingTime);
		System.out.println("depthChargeTime:     "+depthChargeTime);
	}
	/**
	 * Computes if the state is terminal.  Should return the value of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		//TODO compute if the MachineState is terminal
		long start = System.currentTimeMillis();
		/*System.out.print("isTerminal...");
		System.out.println("false");*/
		setBaseProps(state);
		propagateTruthValues(terminalOrdering);
		isTerminalTime+=(System.currentTimeMillis()-start);
		return terminal.getValue();
	}
	
	/**
	 * Computes the goal for a role in the current state. Should return the value of the goal proposition that is true for 
	 * role.  If the number of goals that are true for role != 1, then you should throw a GoalDefinitionException
	 */
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		//TODO compute the goal for role in state
		long start = System.currentTimeMillis();
		setBaseProps(state);
		propagateTruthValues(goalPropsOrderings.get(role));
		Set<Proposition> goalProps=goalPropositions.get(role);
		int val = -1;
		for(Proposition gp:goalProps){
			if(gp.getValue()){
				if(val != -1){
					throw new GoalDefinitionException(state, role);
				}
				val = getGoalValue(gp);	
			}
		}
		if(val == -1)
			throw new GoalDefinitionException(state, role);
		getGoalTime+=(System.currentTimeMillis()-start);
		return val;
		/*System.out.print("getGoal...");
		System.out.println(val);*/
	}
	
	/**
	 * Returns the initial state.  The initial state can be computed by only setting the truth value of the init 
	 * proposition to true, and then computing the resulting state.
	 */
	@Override
	public MachineState getInitialState() {
		//TODO compute the initial state
		long start = System.currentTimeMillis();
		for(Proposition p:pnet.getPropositions()){
			p.setValue(false);
		}
		init.setValue(true);
		MachineState state=getStateFromBase();
		init.setValue(false);
		System.out.println("Initial state: "+state.toString());
		getInitialStateTime+=(System.currentTimeMillis()-start);
		return state;
	}
	
	/**
	 * Computes the legal moves for role in state
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		//TODO compute legal moves
		long start = System.currentTimeMillis();
		//System.out.print("getLegalMoves...");
		Set<Proposition> legalProps = legalPropositions.get(role);
		List<Move> moves=new ArrayList<Move>();
		//set base props
		setBaseProps(state);
		propagateTruthValues(legalPropsOrderings.get(role));
		for(Proposition lp:legalProps) {
			if(lp.getValue()) {
				moves.add(getMoveFromProposition(lp));
			}
		}
		//System.out.println(role.toString()+": returning "+moves.size()+" moves.");
		getLegalMovesTime+=(System.currentTimeMillis()-start);
		return moves;
	}

	//set base props
	private void setBaseProps(MachineState state){
		for(Proposition bp:basePropositions.values()){
			// System.err.println("base prop: " + bp.getName().toString());
			bp.setValue(false);
		}
		for(GdlSentence g:state.getContents()){
			// System.err.println("state prop: " + g);
			Proposition bp = basePropositions.get(g);
			bp.setValue(true);
		}
	}
	private void propagateTruthValues(List<Proposition> order) {
		for(int i=0; i<order.size();i++){
			Proposition p = order.get(i);
			p.setValue(p.getSingleInput().getValue());
		}
	}
	//set input props
	private void setInputProps(List<Move> moves){
		for(Proposition bp:inputPropositions.values()){
			bp.setValue(false);
		}
		if(moves==null)return;
		List<Proposition> moveProps=toDoesProps(moves);
		for(Proposition p:moveProps){
			p.setValue(true);
		}
	}
	/**
	 * Computes the next state given state and the list of moves.
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		//TODO compute the next state
		long start = System.currentTimeMillis();
		//System.out.print("getNextState...");
		//set base props
		setBaseProps(state);
		//set input props
		setInputProps(moves);
		for(int i=0; i<ordering.size(); i++){
			Proposition p = ordering.get(i);
			p.setValue(p.getSingleInput().getValue());
		}
		MachineState s=getStateFromBase();
		getNextStateTime+=(System.currentTimeMillis()-start);		
		//System.out.println(state.toString()+" TO "+s.toString());
		return s;
	}
	
	/**
	 * This should compute the topological ordering of propositions.
	 * Each component is either a proposition, logical gate, or transition.
	 * Logical gates and transitions only have propositions as inputs.
	 * The base propositions and input propositions should always be exempt from this ordering
	 * The base propositions values are set from the MachineState that operations are performed on and the
	 * input propositions are set from the Moves that operations are performed on as well (if any).
	 * @return The order in which the truth values of propositions need to be set.
	 */
	private List<Proposition> getOrdering()
	{
		//TODO compute the topological ordering
		long start = System.currentTimeMillis();
		Set<Proposition> props=pnet.getPropositions();
		props.removeAll(basePropositions.values());
		props.removeAll(inputPropositions.values());
		getOrderingTime+=(System.currentTimeMillis()-start);
		return computeSubsetOrdering(props);
		//return order;
	}
	private List<Proposition> computeSubsetOrdering(Collection<Proposition> col){
		Set<Component> seen= new HashSet<Component>();
		//List<Component> components = new ArrayList<Component>(pnet.getComponents());
		seen.addAll(basePropositions.values());
		seen.addAll(inputPropositions.values());
		seen.add(init);
		List<Proposition> order = new LinkedList<Proposition>();
		for(Proposition p:col){
			//Component c = p.getSingleInput();
			recursiveOrder(order,seen,p);
		}
		return order;
	}
//	private List<Proposition> computeBasePropsOrdering(){
//		Set<Component> seen= new HashSet<Component>();
//		seen.addAll(inputPropositions.values());
//		seen.add(init);
//		List<Proposition> order = new LinkedList<Proposition>();
//		for(Proposition p:basePropositions.values()){
//			recursiveOrder(order,seen,p);
//		}
//		return order;
//	}
	private void recursiveOrder(List<Proposition> order,Set<Component> seen,Proposition prop){
		if(seen.contains(prop)){
			return;
		}
		seen.add(prop);
		if(prop.getInputs().size() == 1){
			Set<Component> inputs = prop.getSingleInput().getInputs();
			for(Component c:inputs) {
				recursiveOrder(order,seen,(Proposition) c);
			}
			order.add(prop);
		}
		else
			System.out.println("WTFFFFF");
	}
	private void computeManyOrderings(){
		Set<Proposition> allLegalsAndTerminal=new HashSet<Proposition>();
		allLegalsAndTerminal.add(terminal);
		terminalOrdering=computeSubsetOrdering(allLegalsAndTerminal);
		Set<Proposition> dontInclude=new HashSet<Proposition>(basePropositions.values());
		dontInclude.addAll(inputPropositions.values());
		dontInclude.add(init);
		legalPropsOrderings=new HashMap<Role,List<Proposition>>();
		goalPropsOrderings=new HashMap<Role,List<Proposition>>();
		for(Role r:roles){
			Set<Proposition> copy=new HashSet<Proposition>(legalPropositions.get(r));
			allLegalsAndTerminal.addAll(legalPropositions.get(r));
			copy.removeAll(dontInclude);
			legalPropsOrderings.put(r, computeSubsetOrdering(copy));
			copy.clear();
			copy.addAll(goalPropositions.get(r));
			copy.removeAll(dontInclude);
			goalPropsOrderings.put(r, computeSubsetOrdering(copy));
		}
		allLegalsAndTerminal.removeAll(dontInclude);
		legalAndTerminalFullOrdering=computeSubsetOrdering(allLegalsAndTerminal);
		//baseOrdering=computeBasePropsOrdering();
		//return orderings;
	}
	
	/** Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/** The underlying proposition network  */
	private PropNet pnet;
	/** An index from GdlTerms to Base Propositions.  The truth value of base propositions determines the state */
	private Map<GdlSentence, Proposition> basePropositions;
	/** An index from GdlTerms to Input Propositions.  Input propositions correspond to moves a player can take */
	private Map<GdlSentence, Proposition> inputPropositions;
	/** The terminal proposition.  If the terminal proposition's value is true, the game is over */
	private Proposition terminal;
	/** Maps roles to their legal propositions */
	private Map<Role, Set<Proposition>> legalPropositions;
	/** Maps roles to their goal propositions */
	private Map<Role, Set<Proposition>> goalPropositions;
	/** The topological ordering of the propositions */
	private List<Proposition> ordering;
	private Map<Role,List<Proposition>> legalPropsOrderings;
	private Map<Role,List<Proposition>> goalPropsOrderings;
	private List<Proposition> legalAndTerminalFullOrdering;
	private List<Proposition> terminalOrdering;
	//private List<Proposition> baseOrdering;
	/** Set to true and everything else false, then propagate the truth values to compute the initial state*/
	private Proposition init;
	/** The roles of different players in the game */
	private List<Role> roles;
	/** A map between legal and input propositions.  The map contains mappings in both directions*/
//	private Map<Proposition, Proposition> legalInputMap;
	
//	//doesnt combine legal,base
//	private boolean canRemoveRedundantPropositions(Proposition in, Proposition out){
//		if(inputPropositions.containsKey(in.getName()) || init.equals(in) || init.equals(out)
//				|| terminal.equals(out))
//			return false;
//		boolean inIsLegalProp = false;
//		boolean outIsLegalProp=false;
//		boolean inIsBaseProp = basePropositions.containsKey(in.getName());
//		boolean outIsBaseProp = basePropositions.containsKey(out.getName());
//		for(Role r:roles){
//			Set<Proposition> thisset = legalPropositions.get(r);
//			if(thisset.contains(in)) return false;//inIsLegalProp=true;
//			if(thisset.contains(out)) return false;//outIsLegalProp=true;
//			thisset = goalPropositions.get(r);
//			if(thisset.contains(in) || thisset.contains(out)){
//				return false;
//			}
//		}
//		/*if(outIsLegalProp && (inIsBaseProp || inIsLegalProp)) return false;
//		if(inIsLegalProp && outIsBaseProp) {
//			//System.out.println("inIsLegalProp: "+inIsLegalProp+" outIsLegalProp: "+outIsLegalProp
//			//		+" inIsBaseProp: "+inIsBaseProp+" outIsBaseProp: "+outIsBaseProp);
//			return false;
//		}
//		/*if(outIsLegalProp || inIsLegalProp ) {
//			System.out.println("inIsLegalProp: "+inIsLegalProp+" outIsLegalProp: "+outIsLegalProp
//					+" inIsBaseProp: "+inIsBaseProp+" outIsBaseProp: "+outIsBaseProp);
//			return false;
//		}*/
//		if(outIsLegalProp || inIsBaseProp || inIsLegalProp || outIsBaseProp) {
//			System.out.println("inIsLegalProp: "+inIsLegalProp+" outIsLegalProp: "+outIsLegalProp
//					+" inIsBaseProp: "+inIsBaseProp+" outIsBaseProp: "+outIsBaseProp);
//			System.out.println("in: "+in.getName()+" out: "+out.getName());
//			return false;
//		}/**/
//		return true;
//	}
//	private void removeRedundantPropositions(){
//		Set<Component> toremove = new HashSet<Component>();
//		Set<Component> comp = pnet.getComponents();
//		Iterator<Component> it = comp.iterator();
//		////////////////////////////
//		int size=pnet.getPropositions().size();
//		int nrem=0;
//		////////////////////////////
//		while(it.hasNext()){
//			Component c = it.next();
//			if((c instanceof And || c instanceof Or) && c.getInputs().size() == 1){
//				Proposition input = (Proposition)c.getSingleInput();
//				Proposition output = (Proposition)c.getSingleOutput();
//				if(canRemoveRedundantPropositions(input,output)){
//					nrem++;
//					//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//					input.getOutputs().remove(c);
//					input.getOutputs().addAll(output.getOutputs());
//					
//					Iterator<Component> outit = output.getOutputs().iterator();
//					while(outit.hasNext()){
//						Component o = outit.next();
//						o.getInputs().remove(output);
//						o.getInputs().add(input);
//					}
//					toremove.add(c);
//					toremove.add(output);
//					pnet.getPropositions().remove(output);
//					if(terminal.equals(output)){
//						terminal = input;
//					}
//					
//					Iterator<Role> roleit = goalPropositions.keySet().iterator();
//					while(roleit.hasNext()){
//						Role r = roleit.next();
//						Set<Proposition> thisset = goalPropositions.get(r);
//						if(thisset.contains(output)){
//							thisset.remove(output);
//							thisset.add(input);
//						}
//					}
//				}
//			}
//			
//			
//		}
//		comp.removeAll(toremove);
//		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX "+nrem+" of "+size+" props removed");
//	}
	
	/**
	 * Initializes the PropNetStateMachine.  You should compute the topological ordering here.
	 * Additionally you can compute the initial state here, if you want.
	 */
	@Override
	public void initialize(List<Gdl> description) {
		System.out.println("Initializing....");

//        List<GdlRule> flatDescription = new PropNetAnnotatedFlattener(description).flatten();
//        GamerLogger.log("StateMachine", "Converting...");
//        pnet = new PropNetConverter().convert(flatDescription);
		
		pnet = PropNetFactory.create(description);
		///////////////#######################
		
		///////////////////######################
		roles = computeRoles(description);
		basePropositions = pnet.getBasePropositions();
		inputPropositions = pnet.getInputPropositions();
		terminal = pnet.getTerminalProposition();
		legalPropositions = pnet.getLegalPropositions();
		init = pnet.getInitProposition();
		goalPropositions = pnet.getGoalPropositions();
		setGoalValuesInitially();
		
		//removeRedundantPropositions();
		
		// legalInputMap = pnet.getLegalInputMap();
		ordering = getOrdering();
		computeManyOrderings();
		System.out.println("terminal ordering size: "+terminalOrdering.size());
		//System.out.println("baseprops ordering size: "+baseOrdering.size());
		System.out.println("ordering size: "+ordering.size());
		System.out.println("legalAndTerminalFullOrdering size: "+legalAndTerminalFullOrdering.size());
		System.out.print("legalOrdering sizes: ");
		for(Role r:roles){
			System.out.print(legalPropsOrderings.get(r).size()+", ");
		}
		System.out.println("");
		System.out.println("...Initialization complete");
		//pnet.renderToFile("propnetPicture.dot");
	}

/*Helper methods*/
	/**
	 * The Input propositions are indexed by (does ?player ?action)
	 * This translates a List of Moves (backed by a sentence that is simply ?action)
	 * to GdlTerms that can be used to get Propositions from inputPropositions
	 *  and accordingly set their values etc.  This is a naive implementation when coupled with 
	 *  setting input values, feel free to change this for a more efficient implementation.
	 * 
	 * @param moves
	 * @return
	 */
//	private List<GdlTerm> toDoes(List<Move> moves)
//	{
//		List<GdlTerm> doeses = new ArrayList<GdlTerm>(moves.size());
//		Map<Role, Integer> roleIndices = getRoleIndices();
//		for (int i = 0; i < roles.size(); i++)
//		{
//			int index = roleIndices.get(roles.get(i));
//			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)).toTerm());
//		}
//		return doeses;
//	}
	//not from starter code
	private List<Proposition> toDoesProps(List<Move> moves)
	{
		List<Proposition> doeses = new ArrayList<Proposition>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();
		for (int i = 0; i < roles.size(); i++)
		{
			int index = roleIndices.get(roles.get(i));
			doeses.add(inputPropositions.get(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index))));
		}
		return doeses;
	}
	
	/**
	 * Takes in a Legal Proposition and returns the appropriate corresponding Move
	 * @param p
	 * @return a PropNetMove
	 */
	
	private static Move getMoveFromProposition(Proposition p)
	{
		return new Move(p.getName().get(1));
	}
	
	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */	
	private Map<Proposition,Integer> goalValues;
    private void setGoalValuesInitially(){
    	goalValues= new HashMap<Proposition,Integer>();
    	for(int i=0;i<roles.size();i++){
    		Set<Proposition> props=goalPropositions.get(roles.get(i));
    		for(Proposition p:props){
    			GdlRelation relation = (GdlRelation) p.getName();
    			GdlConstant constant = (GdlConstant) relation.get(1);
    			goalValues.put(p,Integer.parseInt(constant.toString()));
    		}
    	}
	}
    private int getGoalValue(Proposition goalProposition)
	{
		return goalValues.get(goalProposition);
	}
	
	/**
	 * A Naive implementation that computes a PropNetMachineState
	 * from the true BasePropositions.  This is correct but slower than more advanced implementations
	 * You need not use this method!
	 * @return PropNetMachineState
	 */	
	public MachineState getStateFromBase()
	{
		Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (Proposition p : basePropositions.values())
		{
			//p.setValue(p.getSingleInput().getValue());
			if (p.getSingleInput().getValue())
			{
				contents.add(p.getName());
			}

		}
		return new MachineState(contents);
	}

	/**
	 * Helper method, used to get compute roles.  You should only be using this
	 * for Role indexing (because of compatibility with the GameServer state machine's roles)
	 * @param description
	 * @return the list of roles for the current game.  Compatible with the GameServer's state machine.
	 */
	private List<Role> computeRoles(List<Gdl> description)
	{
		List<Role> roles = new ArrayList<Role>();
		for (Gdl gdl : description)
		{
			if (gdl instanceof GdlRelation)
			{
				GdlRelation relation = (GdlRelation) gdl;				
				if (relation.getName().getValue().equals("role"))
				{
					roles.add(new Role((GdlConstant)relation.get(0)));
				}
			}
		}
		return roles;
	}
//	//set base props
//	private void updateBaseProps(){
//		for (Proposition p : basePropositions.values())
//		{
//			p.setValue(p.getSingleInput().getValue());
//		}
//	}
	/*public MachineState performDepthCharge(MachineState state, int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
		//private MachineState WAT(MachineState state, int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
	    	//System.out.println("performing DepthCharge...on: "+state.toString());
		long start = System.currentTimeMillis();
    	setInputProps(null);//sets all to false ################################
    	setBaseProps(state);
		propagateTruthValues(legalAndTerminalFullOrdering);
        //updateBaseProps();
        int nDepth = 0;
        while(!terminal.getValue()){// && nDepth<20) {
            nDepth++;
            setInputProps(null);
            for(Role r:roles){
            	makeRandomMove(r);
            }
    		propagateTruthValues(ordering);
            //System.out.println(nDepth+": "+getStateFromBase().toString());
            updateBaseProps();
    		propagateTruthValues(legalAndTerminalFullOrdering);
            //updateBaseProps();
            //state = getNextStateDestructively(state, getRandomJointMove(state));
        }
        //while(nDepth==20);
        if(theDepth != null)
            theDepth[0] = nDepth;
        //System.out.println("returning at depth: "+nDepth+": "+getStateFromBase().toString());

		depthChargeTime+=(System.currentTimeMillis()-start);
        return getStateFromBase();
    }    
	private void makeRandomMove(Role r){
		Set<Proposition> legalProps = legalPropositions.get(r);
		List<Proposition> legal=new ArrayList<Proposition>();
		for(Proposition lp:legalProps) {
			if(lp.getValue()) {
				legal.add(lp);
			}
		}
		int m=(int)(Math.random()*legal.size());
		legalInputMap.get(legal.get(m)).setValue(true);
	}*/

}
