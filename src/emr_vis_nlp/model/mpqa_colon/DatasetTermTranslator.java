package emr_vis_nlp.model.mpqa_colon;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for translating from the back-end dataset names to more
 * informative names to be displayed to the user.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DatasetTermTranslator {
    
    private static Map<String, String> valNameMap;
    private static Map<String, String> attrNameMap;
    
    public static void buildValNameMap() {
        valNameMap = new HashMap<>();
        valNameMap.put("-1", "N/A");
        valNameMap.put("", "N/A");
        valNameMap.put("0", "Fail");
        valNameMap.put("1", "Pass");
    }
    
    public static void buildAttrNameMap() {
        attrNameMap = new HashMap<>();
        attrNameMap.put("Indicator_25", "25.1. Withdrawal time 6min");
        attrNameMap.put("Indicator_24", "24.1 Rate of procedures where prep adequate");
        attrNameMap.put("Indicator_23", "23.1 Rate of detection of large adenomas");
        attrNameMap.put("Indicator_22", "22.1 If indication is screening, was previous colonoscopy noted?");
//        attrNameMap.put("Indicator_26", "");
        attrNameMap.put("Indicator_9", "9.1 If negative study, no family history, and UC/Crohns, follow-up time for next procedure recommended should be 10 years");
        attrNameMap.put("Indicator_2", "2.1. ASA classification rate noted");
        attrNameMap.put("Indicator_1", "1.1. Fraction of procedures where a standard indication provided (list from Rex et al.)");
        attrNameMap.put("Indicator_21", "21.1. Was informed consent obtained?");
//        attrNameMap.put("Indicator_19B", "");
        attrNameMap.put("Indicator_19", "19.1. Cecal landmark rate");
        attrNameMap.put("Indicator_16", "16.1 Document quality of preparation");
        attrNameMap.put("Indicator_17", "17.1 Document withdrawal time");
        attrNameMap.put("Indicator_18", "18.1. Cecal intubation rate");
        attrNameMap.put("Indicator_11", "11.1 Rate of detection of any adenoma");
        attrNameMap.put("Indicator_12", "12.1 Rate of detection of advanced adenomas");
        attrNameMap.put("Indicator_13", "13.1. Rate of any polyps");
        attrNameMap.put("Indicator_14", "14.1. Rate of any polyps >9mm");
        attrNameMap.put("Indicator_3.1", "3.1 If indication is chronic diarrhea, then biopsy should be obtained");
//        attrNameMap.put("Indicator_4", "";
        
        // note: missing indicators:  5.1, 6.1, 7.1, 
    }
    
    public static String getValTranslation(String val) {
        if (valNameMap == null) buildValNameMap();
        if (valNameMap.containsKey(val)) return valNameMap.get(val);
        return val;
    }
    
    public static String getAttrTranslation(String attrName) {
        if (attrNameMap == null) buildAttrNameMap();
        if (attrNameMap.containsKey(attrName)) return attrNameMap.get(attrName);
        return attrName;
    }
    
}
