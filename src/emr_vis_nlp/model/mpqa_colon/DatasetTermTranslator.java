package emr_vis_nlp.model.mpqa_colon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for translating from the back-end dataset names to more
 * informative names to be displayed to the user.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DatasetTermTranslator {
    
    // mapping 
    private static Map<String, String> valNameMap;
    private static Map<String, String> attrNameMap;
    private static Map<String, String> revAttrNameMap;
    private static List<String> defaultValList;
    
    public static void buildValNameMap() {
        valNameMap = new HashMap<>();
        valNameMap.put("-1", "N/A");
        valNameMap.put("", "N/A");
        valNameMap.put("0", "Fail");
        valNameMap.put("1", "Pass");
    }
    
    public static void buildDefaultValList() {
        defaultValList = new ArrayList<>();
        defaultValList.add("N/A");
        defaultValList.add("Fail");
        defaultValList.add("Pass");
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
    
    public static void buildRevAttrNameMap() {
        revAttrNameMap = new HashMap<>();
        revAttrNameMap.put("25.1. Withdrawal time 6min", "Indicator_25");
        revAttrNameMap.put("24.1 Rate of procedures where prep adequate", "Indicator_24");
        revAttrNameMap.put("23.1 Rate of detection of large adenomas", "Indicator_23");
        revAttrNameMap.put("22.1 If indication is screening, was previous colonoscopy noted?", "Indicator_22");
//        revAttrNameMap.put("", "Indicator_26");
        revAttrNameMap.put("9.1 If negative study, no family history, and UC/Crohns, follow-up time for next procedure recommended should be 10 years", "Indicator_9");
        revAttrNameMap.put("2.1. ASA classification rate noted", "Indicator_2");
        revAttrNameMap.put("1.1. Fraction of procedures where a standard indication provided (list from Rex et al.)", "Indicator_1");
        revAttrNameMap.put("21.1. Was informed consent obtained?", "Indicator_21");
//        revAttrNameMap.put("", "Indicator_19B");
        revAttrNameMap.put("19.1. Cecal landmark rate", "Indicator_19");
        revAttrNameMap.put("16.1 Document quality of preparation", "Indicator_16");
        revAttrNameMap.put("17.1 Document withdrawal time", "Indicator_17");
        revAttrNameMap.put("18.1. Cecal intubation rate", "Indicator_18");
        revAttrNameMap.put("11.1 Rate of detection of any adenoma", "Indicator_11");
        revAttrNameMap.put("12.1 Rate of detection of advanced adenomas", "Indicator_12");
        revAttrNameMap.put("13.1. Rate of any polyps", "Indicator_13");
        revAttrNameMap.put("14.1. Rate of any polyps >9mm", "Indicator_14");
        revAttrNameMap.put("3.1 If indication is chronic diarrhea, then biopsy should be obtained", "Indicator_3.1");
//        revAttrNameMap.put("", "Indicator_4";
        
        // note: missing indicators:  5.1, 6.1, 7.1, 
    }
    
    public static String getValTranslation(String val) {
        if (valNameMap == null) buildValNameMap();
        if (valNameMap.containsKey(val)) return valNameMap.get(val);
        return val;
    }
    
    public static List<String> getDefaultValList() {
        if (defaultValList == null) buildDefaultValList();
        return defaultValList;
    }
    
    public static String getAttrTranslation(String attrName) {
        if (attrNameMap == null) buildAttrNameMap();
        if (attrNameMap.containsKey(attrName)) return attrNameMap.get(attrName);
        return attrName;
    }
    
    public static String getRevAttrTranslation(String attrName) {
        if (revAttrNameMap == null) buildRevAttrNameMap();
        if (revAttrNameMap.containsKey(attrName)) return revAttrNameMap.get(attrName);
        return attrName;
    }
    
}
