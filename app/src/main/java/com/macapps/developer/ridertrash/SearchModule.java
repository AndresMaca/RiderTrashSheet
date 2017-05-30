package com.macapps.developer.ridertrash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Developer on 30/5/2017.
 */

public class SearchModule {



    public SearchModule(){

    }

    public ArrayList<String> searchModule(HashMap<String, Double> shortestStart){
        LinkedHashMap<String,Double> linkedHashMap=sortHashMapByValues(shortestStart);
        ArrayList<String> strings=new ArrayList<>();

        for(String key:linkedHashMap.keySet()){
            strings.add(key);
        }
        return strings;
        //Toast.makeText(this, shortestStart.toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, strings.toString(), Toast.LENGTH_LONG).show();



    }
    public LinkedHashMap<String, Double> sortHashMapByValues(
            HashMap<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Double> sortedMap =
                new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
