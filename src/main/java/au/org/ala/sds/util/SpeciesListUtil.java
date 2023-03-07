/*
 * Copyright (C) 2013 Atlas of Living Australia
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

package au.org.ala.sds.util;

import au.org.ala.sds.model.SDSSpeciesListDTO;
import au.org.ala.sds.model.SDSSpeciesListItemDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Provides utility methods required to interface with the species list tool. Allows the SDS
 * list to be constructed from lists that are part of the list.ala.org.au tool.
 *
 * @author Natasha Carter (natasha.carter@csiro.au)
 */
public class SpeciesListUtil {

    final static Logger logger = Logger.getLogger(SpeciesListUtil.class);

    public static Map<String,SDSSpeciesListDTO> getSDSLists(){

        Map<String, SDSSpeciesListDTO> map = new java.util.HashMap<String, SDSSpeciesListDTO>();
        try {
            logger.info("Loading lists from " + Configuration.getInstance().getListToolUrl());
            //get the details about the species lists that are considered part of the SDS
            URL url = new URL(Configuration.getInstance().getListToolUrl() + "/ws/speciesList?isSDS=eq:true");
            //retrieve the lists from the json returned
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            URLConnection connection = url.openConnection();
            logger.debug("Looking up location using " + url);
            InputStream inStream = connection.getInputStream();
            java.util.Map values = mapper.readValue(inStream, java.util.Map.class);
            if(values.containsKey("lists")){
               List<Map<String, String>> lists = (List<Map<String, String>>) values.get("lists");
               for (Map<String, String> lmap : lists){
                   String dataResourceUid = lmap.get("dataResourceUid");
                   SDSSpeciesListDTO item = new SDSSpeciesListDTO(
                           dataResourceUid,
                           lmap.get("listName"),
                           lmap.get("region"),
                           lmap.get("authority"),
                           lmap.get("category"),
                           lmap.get("generalisation"),
                           lmap.get("sdsType"),
                           lmap.get("lastUpdated")
                   );
                   logger.debug(item);
                   map.put(dataResourceUid, item);
               }
               logger.debug(lists + " " + lists.getClass() + " " + lists.get(0).getClass());
            }

        } catch (Exception e){
            e.printStackTrace();
            logger.error("Unable to obtain the list details.", e);
        }
        return map;
    }

    /**
     * Retrieves the "isSDS" species list items ordering them by guid/scientific name
     * @return
     */
    public static List<SDSSpeciesListItemDTO> getSDSListItems(Collection<String> dataResourceUids, boolean hasMatch){
       try{
           String suffix = hasMatch ? "&sort=guid" : "&sort=rawScientificName";
           List<SDSSpeciesListItemDTO> values = new ArrayList();

           String drUids = StringUtils.join(dataResourceUids, ",");
           int offset = 0;
           int max = 400;
           boolean moreRecords = true;
           while(moreRecords) {
               URL url = new URL(Configuration.getInstance().getListToolUrl() + "/ws/speciesListItems?isSDS=eq:true" + suffix + "&druid=" + drUids + "&includeKVP=true&max=" + max + "&offset=" + offset);
               offset += max;

               ObjectMapper mapper = new ObjectMapper();
               mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
               URLConnection connection = url.openConnection();
               logger.error("Looking up location using " + url);
               InputStream inStream = connection.getInputStream();

               java.util.List<SDSSpeciesListItemDTO> drValues = mapper.readValue(
                       inStream,
                       new TypeReference<List<SDSSpeciesListItemDTO>>() {
                       }
               );

               if (!hasMatch) {
                   // include only records without an LSID
                   for (SDSSpeciesListItemDTO item : drValues) {
                       if (item.getGuid() == null) {
                           values.add(item);
                       }
                   }
               } else {
                   // include only records with an LSID
                   for (SDSSpeciesListItemDTO item : drValues) {
                       if (item.getGuid() != null) {
                           values.add(item);
                       }
                   }
               }

               moreRecords = drValues.size() == max;
           }
           logger.error(values);
           return values;
       } catch(Exception e){
           logger.error("Unable to get the list items. ", e);
       }
        return null;
    }

    public static void main(String[] args){
        Map<String, SDSSpeciesListDTO> sdsLists = getSDSLists();
        getSDSListItems(sdsLists.keySet(), true);
        getSDSListItems(sdsLists.keySet(), false);
    }
}
