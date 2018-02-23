package FPGrowthImplimentation;

import java.util.List;

import weka.associations.FPGrowth;
import weka.associations.FPGrowth.AssociationRule;
import weka.core.Instances;

public class FPGWrapper {
	Instances forAssociationMining;
	List<String> MustContain;
	String MustContainCommas = "";
	FPGrowth fpg = new FPGrowth();
	List<AssociationRule> Rules;
	public FPGWrapper(Instances Input, List<String> MustContain){
		forAssociationMining = Input;
		this.MustContain = MustContain;
		for(String s:MustContain){
			MustContainCommas += s + ",";
		}
		if(!MustContainCommas.isEmpty())
			MustContainCommas = MustContainCommas.substring(0, MustContainCommas.length()-1);
		System.out.println(MustContainCommas);
	}
	
	
//	119539_dataset.arff
	public void setOptions(){
		fpg.setRulesMustContain(MustContainCommas);
//		fpg.setTransactionsMustContain(MustContainCommas);
		fpg.setUseORForMustContainList(true);
		fpg.setNumRulesToFind(50);
		fpg.setDelta(0.001);
		fpg.setLowerBoundMinSupport(0.01);
		fpg.setUpperBoundMinSupport(0.05);
		fpg.setMinMetric(0.04);
		
	}
	
	public void Execute(){
		try{
			fpg.buildAssociations(forAssociationMining);
			Rules = fpg.getAssociationRules();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public List<AssociationRule> getRules(){
		return Rules;
	}
	
}
