package org.aspic.inference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.aspic.inference.writers.KnowledgeWriter;

/**
 * <p>A KnowledgeBase collects and indexes Rules.
 * Facts and Rules (both strict and defeasible) are all
 * represented in a KnowledgeBase as Rule instances. Facts are simply Rules
 * with an empty antecedent.</p>
 *  
 * <p> A KnowledgeBase object manages a list of Rules that are 
 * indexed by rule_tag and arity where
 * rule_tag is a string that combines the rule consequent's functor 
 * and the Rule modifier (belief) and
 * arity is the number of terms in the antecedent.</p>
 * 
 * <p>When a user constructs a knowledge base they add their own rules 
 * and beliefs but in addition, the object add's
 * it's own autogenerated rules and beliefs.  These autogenerated rules
 * can be identified with the Rule.isAutoGenerated method and must be 
 * carefully managed by this class to maintain the integrity of it's
 * index.</p>
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class KnowledgeBase implements Cloneable, Serializable {
	//TODO?: Implement non-indexed KB.
	//TODO?: Implement db backed KB.
	//TODO: consolidate getRules and ruleIterator - do we need them both?
	//TODO: Implement KnowledgeBase.merge
	//TODO: Add in Knowledge listeners so that if the knowledge changes, then queries can know they are "stale" and need refreshing.
	// a map that stores all rules in the knowledge base, indexed by the consequent's functor and the arity of the antecedent.
    protected Map<String, Map<Integer, List<Rule>>> rulesMap = new HashMap<String, Map<Integer, List<Rule>>>();
	// A map that keeps a track of named rules, where the key is the ruleTag of the rule name's consequent and the value is the rule
    protected RuleNameBidiMap namedRules = new RuleNameBidiMap(); 
	// used to automatically generate unique rule names.
	protected int counter = 0;  
	// flag for whether the knowledge base needs to include automatically generated transposed strict rules
    protected boolean usingTransposition;
    protected Map<Rule, List<Rule>> transposedRules = null;
    // used for capturing known "predicates" for the getPredicates and getPredicateArity methods
    protected Map<String, Set<Integer>> predicates = new HashMap<String, Set<Integer>>(); 

    /** 
	 * default Constructor
	 */
	public KnowledgeBase() {
       this.usingTransposition = false;
	}
	
	/**
	 * Constructor for transposed knowledge base
	 * @param usingTransposition transposition adds additional transposed rules for every strict rule
	 **/
	public KnowledgeBase(Boolean usingTransposition) {
        this.usingTransposition = usingTransposition;
		if (isUsingTransposition()) {
			transposedRules = new HashMap<Rule, List<Rule>>();
		}
	}
    
    /**
     * Get list of non auto-generated rules.
     * @return list of non auto-generated rules.
     */
    public List<Rule> getRules(){        
        List<Rule> myrules = new Vector<Rule>();
        Iterator<Rule> ruleIterator = this.ruleIterator();        
        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();
            if (!(rule.isAutoGenerated())){
                myrules.add(rule);
            }
        }        
        return myrules;
    }
    
    /**
     * Add rules to the knowledge base.
     * @param rules list of rules.
     */
    public void addRules(List rules){
        for (Iterator<Rule> it = rules.iterator(); it.hasNext(); ){ 
            addRule(it.next());
        }
    }

    public boolean equals(Object obj){   
        if (!(obj instanceof KnowledgeBase)){
            return false;
        }
        KnowledgeBase copy = (KnowledgeBase)obj;

        if ((copy.rulesMap.equals(this.rulesMap)) &&    
        		
            (copy.namedRules.equals(this.namedRules)) &&
            (copy.isUsingTransposition() == this.isUsingTransposition()))
        {
            return true;
        }
        else{
            return false;
        }
    }    
    
    
	/**
	 * Inspects whether this KnowledgeBase is transposing strict rules.
	 * NB there is no setter because it's fairly difficult to jump
	 * between the two states (using and not using transposition).
	 * @return true if the KnowledgeBase is transposing strict (and proper) rules
	 */
	public boolean isUsingTransposition() {
		return usingTransposition;
	}
	
	public void setUsingTransposition(boolean usingTransposition) {
		if (this.isUsingTransposition() == false && usingTransposition == true) {
			transposedRules = new HashMap<Rule, List<Rule>>();
			ArrayList<Rule> rulesToBeTransposed = new ArrayList<Rule>();
			// iterate through the whole knowledgebase, and add rule transpositions where appropriate
			Iterator<Rule> ruleIterator = this.ruleIterator();
			while (ruleIterator.hasNext()) {
				Rule rule = ruleIterator.next();
				if(rule.isStrict() && !rule.isFact()) {
					rulesToBeTransposed.add(rule);
				}
			}
			// why two steps? because if we try in one we get a ConcurrentModificationException in KnowledgBase$RuleIterator
			ruleIterator = rulesToBeTransposed.iterator();
			while(ruleIterator.hasNext()) {
				addRuleTranspositions(ruleIterator.next());
			}
		} else if (this.isUsingTransposition() == true && usingTransposition==false) {
			// remove all transposed Rules from knowledgeBase and set transposedRules to null.
			Iterator<Rule> transposedRuleIterator = transposedRules.keySet().iterator();
			while (transposedRuleIterator.hasNext()) {
				removeRuleTranspositions(transposedRuleIterator.next());
			}
			transposedRules = null;
		}
	    this.usingTransposition = usingTransposition;
	}
	
	/** 
	 * When adding a Rule to a KnowledgeBase, the indexes must be correctly
	 * set and each Element within the Rule must have it's knowledgeBase 
	 * reference set to the containing knowledgebase, i.e. *this* object.	 * 
	 */
	public void addRule(Rule rule) {
		// check that rule doesnt exist already (throw exception if it does)
		if (!ruleExists(rule)) { // not sure if the exists check will be too expensive
			if (rule.getName()==null || !rule.getName().equals(rule.getConsequent())) {
	            // try to add ruleHook Rule for all proper Rules
				if (!rule.isFact()) {
					addRuleHook(rule);
				}
				addRuleLocal(rule);
				// now update the predicates map
				if (!rule.isAutoGenerated()) {
					for(Predicate predicate :  rule.getPredicates()) {
						String functor = predicate.getFunctor();
						if (!functor.equals("~")) {
							if (predicates.containsKey(functor)) {
								predicates.get(functor).add(new Integer(predicate.getArity()));
							} else {
								Set<Integer> set = new HashSet<Integer>();
								set.add(new Integer(predicate.getArity()));
								predicates.put(functor, set);
							}
						}
					}
				}
			} else {
				// Note that if this is too strong then as an alternative we could raise a LOG WARNING. or just return false.
				throw new RuntimeException("A rule's name cannot be the same as it's consequent.");				
			}
		} else {
			// Note that if this is too strong then as an alternative we could raise a LOG WARNING. or just return false.
			throw new RuntimeException("Rule already exists in knowledge Base");
		}

		if (isUsingTransposition() == true && rule.isFact() == false && rule.isStrict() == true) {
			addRuleTranspositions(rule);
		}
	}	
	
	/**
	 * Remove explicitly stated Rule and any associated autoGenerated
	 * rules from KnowledgeBase.  Note that there is no
	 * updateRule(Rule) method because the knowledgeBase might not be able
	 * to identify the old rule (and associated autogenerated rules)
	 * from the new rule, thus the consumer has to explicitly delete 
	 * the old rule and then add a new rule to make an update.  
	 * If this knowledgebase is transposed, then all transposed rules
	 * also need to be removed.
	 */
	public boolean removeRule(Rule rule) throws RuntimeException {
		Rule kbcopy = this.getRule(rule); 		
		if (kbcopy.isAutoGenerated()) {
			throw new RuntimeException("Cannot remove an automatically generated rule - try removing the rule that generated it");
		} else {
			if (rule.getName()==null) {              
				return localRemove(kbcopy);
			} else {
				boolean result = localRemove(kbcopy) && localRemoveName(kbcopy);                        
                
				if (isUsingTransposition() && rule.isStrict()) {
					removeRuleTranspositions(kbcopy);
				}
				return result;
			}
		}
	}	

	/**
	 * Get's a rule iterator.
	 * @return an iterator over the knowledge base's rules.
	 */	 
	public Iterator<Rule> ruleIterator() {
		return new RuleIterator();
	}

	/** 
	 * Checks whether a rule is a rule name.
	 * @param candidate rule to be checked
	 * @return true if candidate is a key in the named Rules collection
	 */
	public boolean isRuleName(Constant candidate) {
        
        //return (namedRules.containsKey(candidate.ruleTag()));                
        return (namedRules.nameExists(candidate));
	}
	
	/**
	 * Retrieves Rule from named rules cache by passing it's rule name.
	 * @param name rule name.
	 * @return Rule from the namedRules cache - i.e. the rule with the candidate name.
	 */
	public Rule getRuleFromName(Constant name) {
        //return namedRules.get(name.ruleTag());              
		return namedRules.getRule(name); 		
	}          

    /**
	 * Override the standard Object method with a view of the knowledgebase that shows the rule Index
	 * output of the form :
	 * ruleTag, arity, index : "rule" 
	 */
	public String toString() {		
		String ruleTag="";
		int arity=-1;
		int index=0;
		StringBuffer result = new StringBuffer();
        
        //Henrik: Added for empty knowledgebases
        if (rulesMap.isEmpty()){
            return "";
        }        
		
        Iterator<Rule> iterator = new RuleIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (rule.getConsequent().ruleTag().equals(ruleTag) && rule.getAntecedent().size()==arity) index++; else index=0;
			ruleTag = rule.getConsequent().ruleTag();
			arity = rule.getAntecedent().size();
			result.append(ruleTag + ", " + arity + ", " + index + ": \"" + rule.inspect() + "\"");
			if (iterator.hasNext()) result.append("\n");
		}
		return result.toString();		
	}

	/**
	 * Simplified version of inspect(boolean, boolean).  Show everything.
	 * @return view of knowledgebase
	 */
	public String inspect() {
		return inspect(true, true);
	}
	
	/**
	 * @param showAutoGeneratedRuleNames show/hide autogenerated names in proper rules
	 * @param showAutoGeneratedRules show/hide autogenerated Rules (i.e. rule hooks)
	 * @return view of knowledge base.
	 */
	public String inspect(boolean showAutoGeneratedRuleNames, boolean showAutoGeneratedRules) {
		StringBuffer result = new StringBuffer();
		boolean started=false;
		Iterator<Rule> iterator = new RuleIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			String temp = rule.inspect(showAutoGeneratedRuleNames, showAutoGeneratedRules);
			if (temp.length()>0) {
				if (started==true) result.append("\n");
				result.append(temp);
				result.append(".");
				started = true;
			}
		}
		return result.toString();		
	}
	
	/** 
	 * Using the Visitor pattern, this method allows the details
	 * of this KnowledgeBase to be written in different ways.
	 * @param writer
	 */
	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	public Set<String> getAllPredicates() {
		return predicates.keySet();
	}
	
	public Set<Integer> getPredicateArities(String predicate) {
		return predicates.get(predicate);
	}
	
	/**
	 * @deprecated Warning: leaks memory!
	 */
	@Deprecated
	public Object clone() {
		KnowledgeBase o = null;
		try {
			o = (KnowledgeBase) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone rulesMap
		o.rulesMap = (Map<String, Map<Integer, List<Rule>>>) ((HashMap<String, Map<Integer, List<Rule>>>) o.rulesMap).clone();
		Iterator<String> ruleTagIterator = o.rulesMap.keySet().iterator();
		while(ruleTagIterator.hasNext()) {
			String ruleTag = ruleTagIterator.next();
			HashMap<Integer, List<Rule>> arityMap = (HashMap<Integer, List<Rule>>) ((HashMap<Integer, List<Rule>>) o.rulesMap.get(ruleTag)).clone();
			Iterator<Integer> arityIterator = arityMap.keySet().iterator();
			while (arityIterator.hasNext()) {
				Integer arity = arityIterator.next();
				List<Rule> rules = (ArrayList<Rule>) ((ArrayList<Rule>) arityMap.get(arity)).clone();
				for (int i=0; i<rules.size(); i++) {
					Rule rule = (Rule) rules.get(i).clone();
					rule.setKnowledgeBase(o);
					rules.set(i, rule);
				}
				arityMap.put(arity, rules);
			}
			o.rulesMap.put(ruleTag, arityMap);
		}
		// clone namedRules
		o.namedRules = (RuleNameBidiMap) o.namedRules.clone();
		
		return o;
	}
	/**
	 * Check to see if an equivalent rules exists in the knowlewdge base.
	 * Could have simply checked for null in getRule, but I prefer a more
	 * explicit design.
	 * @param rule
	 * @return
	 */
	public boolean ruleExists(Rule rule) {
		if (rulesMap.containsKey(rule.getConsequent().ruleTag()) && rulesMap.get(rule.getConsequent().ruleTag()).containsKey(rule.getAntecedent().size())) {
			List<Rule> list = rulesMap.get(rule.getConsequent().ruleTag()).get(rule.getAntecedent().size());
			Iterator<Rule> iterator = list.iterator();
			// iterate through each rule and remove the match if there is one.
			while (iterator.hasNext()) {
				Rule testRule = iterator.next();
				if (testRule.isEqualModuloVariables(rule)) return true;
			}
			return false;	
		} else {
			return false;
		}				
	}
	
	/**
	 * Get the Knowledge Base's copy of a particular rule.
	 * EK: Made this public so we can know the name under which a rule was added...
	 * @param rule
	 * @return
	 */
	public Rule getRule(Rule rule) {
		if (rulesMap.containsKey(rule.getConsequent().ruleTag()) && rulesMap.get(rule.getConsequent().ruleTag()).containsKey(rule.getAntecedent().size())) {
			List<Rule> list = rulesMap.get(rule.getConsequent().ruleTag()).get(rule.getAntecedent().size());
			Iterator<Rule> iterator = list.iterator();
			// iterate through each rule and remove the match if there is one.
			while (iterator.hasNext()) {
				Rule testRule = iterator.next();
				if (testRule.isEqualModuloVariables(rule)) return testRule;
			}
			throw new RuntimeException("unable to locate rule - ruleTag and arity matched, but no rules were listed.");	
		} else {
			throw new RuntimeException("unable to locate rule - ruleTag and arity not matched.");
		}		
	}

	/**
	 *  used in Constant.argument 
	 */
	protected Map<Integer, List<Rule>> getArityMap(String ruleTag) {
		// TODO: in Ruby this map is sorted.  Needed?
		return rulesMap.get(ruleTag);
	}
	
	/*
	 * Get the Knowledge Base's copy of a particular rule, ignoring the name.
	 * NB This was once used in removeRule, but it's usage was replaced by the transposedRules map.
	 * But I think this could be useful so I've not yet deleted it.
	private Rule getRuleIgnoringName(Rule rule) {
		Rule local = (Rule) rule.clone();
		local.setArg(3, new Variable("X")); // bit of a hack this one.  Replace the name with a variable and use isUnifiable to locate the rule.
		if (rulesMap.containsKey(local.getConsequent().ruleTag()) && rulesMap.get(local.getConsequent().ruleTag()).containsKey(local.getAntecedent().size())) {
			List<Rule> list = rulesMap.get(local.getConsequent().ruleTag()).get(local.getAntecedent().size());
			Iterator<Rule> iterator = list.iterator();
			// iterate through each rule and remove the match if there is one.
			while (iterator.hasNext()) {
				Rule testRule = iterator.next();
				if (testRule.isUnifiable(local)) return testRule;
			}
			throw new RuntimeException("unable to locate rule - ruleTag and arity matched, but no rules were listed.");	
		} else {
			throw new RuntimeException("unable to locate rule - ruleTag and arity not matched.");
		}		
	}
	 */
	
	/**
	 * The internal index of Rules has a very specific structure:
	 * 			Map<String, Map<Integer, List<Rule>>>
	 * A HashMap whose values are more HashMaps whose values are ArrayLists or Rules.
	 * The outside Map's keys are rule consequent rule_tags
	 * The inside Map's keys are the rule antecedent arity's
	 * The inside Map's values are Lists of Rules (all rules with the same consequent rule_tag and antecedent arity).
	 * In order to insert a rule into this index, one must first
	 * check that there isnt already an item in the outside and 
	 * inside Map (and if not, add it) and then add the rule to the list.
	 * @param rule
	 */
	protected void addRuleLocal(Rule rule) {
		List<Rule> ruleArray;
		Map<Integer, List<Rule>> arityMap;
		String ruleTag = rule.getConsequent().ruleTag();
		if (rulesMap.containsKey(ruleTag)) {
			arityMap = rulesMap.get(ruleTag);
			Integer arity = Integer.valueOf(rule.getAntecedent().size());
			if (arityMap.containsKey(arity)) {
				arityMap.get(arity).add(rule);
			} else {
				ruleArray = new ArrayList<Rule>();
				ruleArray.add(rule);
				arityMap.put(arity, ruleArray);
			}
		} else {
			ruleArray = new ArrayList<Rule>();
			ruleArray.add(rule);
			arityMap = new HashMap<Integer, List<Rule>>();
			arityMap.put(Integer.valueOf(rule.getAntecedent().size()), ruleArray);
			rulesMap.put(rule.getConsequent().ruleTag(), arityMap);
		}
		// Having added the knowledge, set the callback
		rule.setKnowledgeBase(this);
	}
	
	// check that prescribed rulename doesnt conflict with autogenerated rulename, and if it does, change the automatically generated Rule name.	
	private void addRuleHook(Rule rule) {    
        
        if (rule.getName()==null) {
			Rule ruleHook = new Rule(new Constant(generateName()), rule.getDob());
			ruleHook.setAutoGenerated(true);
			rule.setAutoNamed(true);
			addRuleLocal(ruleHook);
			rule.setName(ruleHook.getConsequent());
            //namedRules.put(ruleHook.getConsequent().ruleTag(), rule);
			namedRules.add(ruleHook.getConsequent(), rule); 
		} else {
            
			// check for conflicts
            // if (namedRules.containsKey(rule.getName().ruleTag())) {
			if (namedRules.nameExists(rule.getName())) { 
                // Rule conflictingRule = namedRules.get(rule.getName().ruleTag());
				Rule conflictingRule = namedRules.getRule(rule.getName()); 
				if (conflictingRule.isAutoNamed()) {
					// if conflicting rule was automatically named then we can add it again
					this.removeRule(conflictingRule);
					conflictingRule.setName(null);
					this.addRule(conflictingRule);
				} else {
					// otherwise the conflict cannot be resolved and we bail
					throw new RuleNameException();
				}
			}
            // Rule ruleHook = new Rule(rule.getConsequent(), rule.getDob());
			Rule ruleHook = new Rule(rule.getName(), rule.getDob());
			ruleHook.setAutoGenerated(true);
			addRuleLocal(ruleHook);
            //namedRules.put(ruleHook.getConsequent().ruleTag(), rule);
			namedRules.add(ruleHook.getConsequent(), rule);
		}
	}
	
	/*
	 * Generate a new, previously unused, rule name, r[X].
	 */
	private String generateName() {
		String name;
		do {
			counter++;
			name = "r" + counter;
        //} while (namedRules.containsKey(name));
		} while (namedRules.nameExists(new Constant(name)));
		return name;
	}
	
	/*
	 * remove rule from rulesMap.
	 */
	private boolean localRemove(Rule rule) {
		List<Rule> list = rulesMap.get(rule.getConsequent().ruleTag()).get(rule.getAntecedent().size());
		return list.remove(rule);
	}
	
	/*
	 * remove rule name from rulesMap and namedRules
	 */
	private boolean localRemoveName(Rule rule) {
		Rule ruleHook = new Rule(rule.getName(), rule.getDob());
		Rule kbnamecopy = this.getRule(ruleHook);
		return localRemove(kbnamecopy) && namedRules.remove(rule.getName());
	}
	
	private void removeRuleTranspositions(Rule rule) {
		Iterator<Rule> ruleTranspositionIterator = transposedRules.get(rule).iterator();
		while (ruleTranspositionIterator.hasNext()) {
			Rule transposition = ruleTranspositionIterator.next();
			localRemoveName(transposition);
			localRemove(transposition);
		}
		transposedRules.remove(rule);
	}
	
	/*
	 * add Rule tranpositions to knowledge base.
	 */
	private void addRuleTranspositions(Rule rule) {
		transposedRules.put(rule, new ArrayList<Rule>());
		Rule[] rules = transposeRule(rule);
		for(int i=0; i<rules.length; i++) {
			addRuleHook(rules[i]);
			addRuleLocal(rules[i]);
			transposedRules.get(rule).add(rules[i]);
		}		
	}
	
	/* transposition: if you have a strict rule a <- b, c return new rules:
	 * ~b <- ~a, c.
	 * ~c <- a, ~b.
	 * TODO?: move this to Rule class
	 */
	private Rule[] transposeRule(Rule rule) {
		Rule[] rules = new Rule[rule.getAntecedent().size()];
		Constant negCon = rule.getConsequent().negation();
		for(int i=0; i<rule.getAntecedent().size(); i++) {
			Constant newCon = ((Constant) rule.getAntecedent().get(i)).negation();
			Rule newRule = (Rule) rule.clone();
			newRule.setName(null);
			newRule.setConsequent(newCon);
			newRule.getAntecedent().set(i, negCon);
			newRule.setAutoGenerated(true);
			rules[i] = newRule;
		}
		return rules;
	}

	/*		
	public Rule getRule(String ruleTag, int arity, int index) {
		return rulesMap.get(ruleTag).get(arity).get(index);
	}
	
	public List<Rule> getRules(String ruleTag, int arity) {
		return rulesMap.get(ruleTag).get(arity);
	}
	*/
	/**
	 * Exception that's raised if a user attempts to add a Rule
	 * with a user specified rule name that already exists in
	 * the database.
	 *
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	public class RuleNameException extends RuntimeException {
	}
	
	/**
	 * An iterator over the knowledge bases rules.  Allows us to hide 
	 * the rulesMap field.  Care has been taken to avoid the confusion 
	 * caused by empty lists in the rulesMap which are left when
	 * rules are removed.
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	private class RuleIterator implements Iterator<Rule> {
		Iterator<String> ruleTagIterator = null;
		String ruleTag = null;
		Iterator<Integer> arityIterator = null;
		Integer arity = 0;
		Iterator<Rule> ruleIterator = null;
		Rule nextRule = null;
		boolean queuedRule = false;
		
		public RuleIterator() {
			ruleTagIterator = KnowledgeBase.this.rulesMap.keySet().iterator();
			setArityIterator();
			setRuleIterator();
			hasNext();
		}
		
		public boolean hasNext() {
			if (queuedRule==true) {
				// in case hasNext() is called twice.
				return true;
			} else {
				if (ruleIterator!=null && ruleIterator.hasNext()) {
					nextRule = ruleIterator.next();
					queuedRule = true;
					return true;
				} else {
					if (arityIterator!=null && arityIterator.hasNext()) {
						setRuleIterator();
						return hasNext();
					} else {
						if (ruleTagIterator.hasNext()) {
							setArityIterator();
							return hasNext();
						} else {
							return false;
						}
					}
				}
			}
		}
		
		public Rule next() {
			if (queuedRule) {
				queuedRule=false;
				return nextRule;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
		private void setArityIterator() {
			if (ruleTagIterator.hasNext()) {
				ruleTag = ruleTagIterator.next();
				arityIterator = rulesMap.get(ruleTag).keySet().iterator();
			}
		}
		
		private void setRuleIterator() {
			if (arityIterator!=null && arityIterator.hasNext()) {
				arity = arityIterator.next();
				ruleIterator = rulesMap.get(ruleTag).get(arity).iterator();
			}
		}
	}
	
	/**
	 * Map that links Rule Names to Rules and whose name lookup uses isEqualModuloVariables.
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	private class RuleNameBidiMap implements Cloneable, Serializable {
		// TODO: Could use a performance boost in the lookup.
		private Map<Constant, Rule> namedRules;
		public RuleNameBidiMap() {
			namedRules = new HashMap<Constant, Rule>();
		}
        
        //Henrik
        public boolean equals(Object obj){
            if (!(obj instanceof RuleNameBidiMap)){
                return false;
            }
            RuleNameBidiMap copy = (RuleNameBidiMap)obj;
            return this.namedRules.equals(copy.namedRules);
        }
            
        
		public void add(Constant name, Rule rule) {
			if (!this.nameExists(name)) {
				namedRules.put(name, rule);
			} else {
				throw new RuntimeException("Cannot add this rule name, " + name.inspect() + " because an equivalent Name exists already: " + this.getName(name).inspect());
			}
		}
		public boolean remove(Constant name) {
			if (this.nameExists(name)) {
				namedRules.remove(name);
				return true;
			} else 
				return false;
		}
		public boolean nameExists(Constant name) {
			Iterator<Constant> nameIterator = namedRules.keySet().iterator();
			while (nameIterator.hasNext()) {
				if (name.isUnifiable(nameIterator.next())) return true;
			}
			return false;
		}
		public Rule getRule(Constant name) {
			Iterator<Constant> nameIterator = namedRules.keySet().iterator();
			while (nameIterator.hasNext()) {
				Constant candidate = nameIterator.next();
				if (name.isUnifiable(candidate)) return namedRules.get(candidate);
			}
			return null;
		}
		public boolean isNamed(Rule rule) {
			return namedRules.containsValue(rule);
		}
		/*
		public Constant getName(Rule rule) {
			return namedRules.
			return null;
		}
		*/
		public Constant getName(Constant name) {
			Iterator<Constant> nameIterator = namedRules.keySet().iterator();
			while (nameIterator.hasNext()) {
				Constant candidate = nameIterator.next();
				if (name.isUnifiable(candidate)) return candidate;
			}
			return null;
		}
		public Object clone() {
			RuleNameBidiMap o = null;
			try {
				o = (RuleNameBidiMap) super.clone();
			} catch(CloneNotSupportedException e) {
				e.printStackTrace();
			}
			o.namedRules = (Map<Constant, Rule>) ((HashMap<Constant, Rule>) o.namedRules).clone();
			Iterator<Constant> ruleNameIterator = o.namedRules.keySet().iterator();
			while (ruleNameIterator.hasNext()) {
				Constant ruleName = ruleNameIterator.next();
				Rule rule = (Rule) o.namedRules.get(ruleName).clone();
				o.namedRules.put(ruleName, rule);
			}
			return o;
		}
	}
}
