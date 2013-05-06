package emr_vis_nlp.ml.deprecated;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Java adaptation of Phuong's SimpleSQ for pattern matching for the simpler 
 * variables. 
 * 
 * @deprecated 
 * @author Phuong Pham, alexander.p.conrad@gmail.com
 *
 */
public class SimpleSQMatcher {
	
	private static Map<String, String> varNameToPatternMap = null;
	
	public static Map<String, String> getVarNameToPatternMap() {
		
		if (varNameToPatternMap != null) {
			return varNameToPatternMap;
		}
		
		varNameToPatternMap = new HashMap<>();
		
		varNameToPatternMap.put("VAR_Any_adenoma", "(adenoma)|(serrated)");
		varNameToPatternMap.put("VAR_Appendiceal_orifice", "(cecal landmarks)|(typical landmarks)|(landmarks of cecum)|(characteristic anatomy)|(orifice)|(ileum)|(ileocolonic anastomoses)|(cecal pouch)");
		varNameToPatternMap.put("VAR_ASA", "(\\Wasa )");
		varNameToPatternMap.put("VAR_Biopsy", "(biopsy) then remove from this set all reports contain (cold biopsy)");
		varNameToPatternMap.put("VAR_Cecum_(reached_it)", "(cecum)|(ileocolic anastmoses)|(blind cecal pouch)");
		varNameToPatternMap.put("VAR_Ileo-cecal_valve", "(valve)|(ileum)|(cecal landmarks)|(typical landmarks)|(landmarks of cecum)|(characteristic anatomy)|(ileocolonic anastomoses)|(cecal pouch)");
		varNameToPatternMap.put("VAR_Informed_consent", "(risk)|(benefit)|(alternative)");
		varNameToPatternMap.put("VAR_Nursing_Reports", "(please see nurses*\'s* note)|(History and physical are documented on the intake form)|(see H&P)|(hospital note)|(nurs.+proc.+record)|(primary team note)");
		varNameToPatternMap.put("VAR_Prep_adequate", "(washing)|(well\\-prepared)|([345]/5)|(5 % obscured)|(slightly suboptimal)|(fairly clean)|prep.+((good)|((?<!in)adequate)|(excellent)|(fair))|(visualization)|(large amount)|(earlier)|(20 % obscured)|(suboptimally cleansed)|(some areas were not seen because of presence of stool)(less than adequate)|(somewhat suboptimal)|(mediocre)|(visualization is somewhat suboptimal)");  // note that this has RE patters for both YES and NO !
		varNameToPatternMap.put("VAR_Procedure_aborted", "(proc.+terminated\\.)");
		varNameToPatternMap.put("VAR_Indication_type", "diarrhea");
		
		return varNameToPatternMap;
		
	}
	
	
}
