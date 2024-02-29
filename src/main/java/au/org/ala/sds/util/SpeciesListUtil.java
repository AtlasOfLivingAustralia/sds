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
import java.text.SimpleDateFormat;
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
            URL url;
            if (Configuration.getInstance().getListToolVersion() == 2) {
                url = new URL(Configuration.getInstance().getListToolUrl() + "/speciesList/?isSDS=true&isAuthoritative=true&pageSize=1000");
            } else {
                url = new URL(Configuration.getInstance().getListToolUrl() + "/ws/speciesList?isSDS=eq:true");
            }
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
                   String dataResourceUid = lmap.getOrDefault("id", lmap.get("dataResourceUid"));
//                   if ("65d2a740ac5fc44c91cce89d".equals(dataResourceUid)) {
                       SDSSpeciesListDTO item = new SDSSpeciesListDTO(
                               dataResourceUid,
                               lmap.getOrDefault("title", lmap.get("listName")),
                               lmap.get("region"),
                               lmap.get("authority"),
                               lmap.get("category"),
                               lmap.get("generalisation"),
                               lmap.getOrDefault("sdsType", "CONSERVATION"),
                               formatDate(lmap.get("lastUpdated"))
                       );
                       logger.debug(item);
                       map.put(dataResourceUid, item);
//                   }
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

           for (String id : dataResourceUids) {
               int offset = 0;
               int max = 10000;
               int page = 1;
               boolean moreRecords = true;
               while (moreRecords) {
                   URL url;
                   if (Configuration.getInstance().getListToolVersion() == 2) {
                       url = new URL(Configuration.getInstance().getListToolUrl() + "/speciesListItems/" + id + "?pageSize=" + max + "&page=" + page);
                       page++;
                   } else {
                       url = new URL(Configuration.getInstance().getListToolUrl() + "/ws/speciesListItems?isSDS=eq:true" + suffix + "&druid=" + id + "&includeKVP=true&max=" + max + "&offset=" + offset);
                   }
                   offset += max;

                   ObjectMapper mapper = new ObjectMapper();
                   mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                   URLConnection connection = url.openConnection();

                   InputStream inStream = connection.getInputStream();

                   java.util.List<SDSSpeciesListItemDTO> drValues = mapper.readValue(
                           inStream,
                           new TypeReference<List<SDSSpeciesListItemDTO>>() {
                           }
                   );

                   // additional work to make v2 response compatible with v1
                   if (Configuration.getInstance().getListToolVersion() == 2) {
                       for (SDSSpeciesListItemDTO item : drValues) {
                           if (item.getClassification() != null && item.getClassification().get("taxonConceptID") != null) {
                               item.setGuid(item.getClassification().get("taxonConceptID").toString());
                           }
                           if (item.getClassification() != null && item.getClassification().get("vernacularName") != null) {
                               item.setCommonName(item.getClassification().get("vernacularName").toString());
                           }
                           // This change (matched family instead of uploaded family) should impact nothing as it is already
                           // an issue if the uploaded family is not the same as the matched family. This is a problem,
                           // for different reasons, in both the old and new lists.
                           if (item.getClassification() != null && item.getClassification().get("family") != null) {
                               item.setFamily(item.getClassification().get("family").toString());
                           }
                           item.setDataResourceUid(id);
                       }
                   }

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
           }

           // manually sort for listToolVersion==2
           if (Configuration.getInstance().getListToolVersion() == 2) {
               if (hasMatch) {
                   values.sort(new Comparator<SDSSpeciesListItemDTO>() {
                       @Override
                       public int compare(SDSSpeciesListItemDTO o1, SDSSpeciesListItemDTO o2) {
                           if (o1.getGuid() == null && o2.getGuid() == null) {
                               return 0;
                           } else if (o1.getGuid() == null) {
                               return -1;
                           } else if (o2.getGuid() == null) {
                               return 1;
                           } else {
                               return o1.getGuid().compareTo(o2.getGuid());
                           }
                       }
                   });
               } else {
                   values.sort(new Comparator<SDSSpeciesListItemDTO>() {
                       @Override
                       public int compare(SDSSpeciesListItemDTO o1, SDSSpeciesListItemDTO o2) {
                           if (o1.getName() == null && o2.getName() == null) {
                               return 0;
                           } else if (o1.getName() == null) {
                               return -1;
                           } else if (o2.getName() == null) {
                               return 1;
                           } else {
                               return o1.getName().compareTo(o2.getName());
                           }
                       }
                   });
               }
           }

           return values;
       } catch(Exception e){
           logger.error("Unable to get the list items. ", e);
       }
        return null;
    }

    private static String formatDate(Object date) {
        if (date instanceof Long) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return sdf.format(date);
        }
        return date.toString();
    }
}
