/*
 * Copyright (C) 2012 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.sds.model;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * DTO to represent a species list item.  It includes all the unique information necessary for a sensitive instance.
 *
 * @author Natasha Carter (natasha.carter@csiro.au)
 */
public class SDSSpeciesListItemDTO {
    private String guid;
    private String name;
    private String family;
    private String dataResourceUid;
    private List<Map<String, String>> kvpValues;
    private Map<String, Object> classification;
    public static final List<String> commonNameLabels= Lists.newArrayList("commonname","vernacularname");
    private String commonName;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    // alias of commonName
    public void setVernacularName(String vernacularName) {
        this.commonName = vernacularName;
    }

    // Added for the change data structure returned by lists
    public void setLsid(String lsid) {
        this.guid = lsid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // alias of name
    public void setScientificName(String scientificName) {
        // prevent overwriting name when list.tool.version == 1
        if (this.name == null) {
            this.name = scientificName;
        }
    }

    public String getDataResourceUid() {
        return dataResourceUid;
    }

    public void setDataResourceUid(String dataResourceUid) {
        this.dataResourceUid = dataResourceUid;
    }

    public List<Map<String, String>> getKvpValues() {
        return kvpValues;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setKvpValues(List<Map<String, String>> kvpValues) {
        this.kvpValues = kvpValues;

        // family has moved to kvpValues
        for(Map<String, String> pair: kvpValues){
            if("family".equalsIgnoreCase(pair.get("key"))){
                setFamily(pair.get("value"));
            }
        }
    }
    // alias of kvpValues
    public void setProperties(List<Map<String, String>> properties) {
        setKvpValues(properties);
    }
    public String getKVPValue(String key){
        for(Map<String, String> pair: kvpValues){
            if(key.equalsIgnoreCase(pair.get("key").replaceAll("[^a-zA-Z]", ""))){
                return pair.get("value");
            }
        }
        return null;
    }
    public String getKVPValueCommonName(){
        for(Map<String, String> pair: kvpValues){
            if(commonNameLabels.contains(pair.get("key").toLowerCase().replaceAll(" ", ""))){
                return pair.get("value");
            }
        }
        return commonName;
    }

    public Map<String, Object> getClassification() {
        return classification;
    }
    public void setClassification(Map<String, Object> classification) {
        this.classification = classification;
    }

    @Override
    public String toString() {
        return "SDSSpeciesListItemDTO{" +
                "lsid='" + guid + '\'' +
                ", name='" + name + '\'' +
                ", dataResourceUid='" + dataResourceUid + '\'' +
                ", kvpValues=" + kvpValues +
                ", family=" + family +
                '}';
    }
}
