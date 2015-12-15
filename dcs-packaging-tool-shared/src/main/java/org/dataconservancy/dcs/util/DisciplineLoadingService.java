/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.util;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DisciplineLoadingService {

    Map<String, List<String>> disciplineMap;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    public DisciplineLoadingService(String filePath) {
        parseDisciplineFile(filePath);

        //If the discipline map wasn't created initialize an empty one to fufill the contract of the API.
        if (disciplineMap == null) {
            disciplineMap = new HashMap<>();
        }
    }

    private void parseDisciplineFile(String filePath) {
        final QNameMap qnames = new QNameMap();

        final String defaultnsUri ="http://dataconservancy.org/schemas/bop/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        // The XStream Driver
        XStream x = new XStream(new StaxDriver(qnames));
        x.setMode(XStream.NO_REFERENCES);

        // XStream converter, alias, and QName registrations
        x.alias(AvailableDisciplineConverter.E_DISCIPLINE_GROUPS, Map.class);
        x.registerConverter(new AvailableDisciplineConverter());
        qnames.registerMapping(new QName(defaultnsUri, AvailableDisciplineConverter.E_DISCIPLINE_GROUPS), HashMap.class);

        try {
            InputStream mapFileStream = null;
            if(filePath.startsWith("classpath:"))
            {
                String path = filePath.substring("classpath:".length());
                if(!path.startsWith("/")){
                    path = "/" + path;
                }
                mapFileStream = DisciplineLoadingService.class.getResourceAsStream(path);
                if (mapFileStream == null) {
                    log.error("Error reading discipline file. Couldn't find classpath file: " + path);
                }
            }
            else
            {
                File mapFile = new File(filePath);
                if (mapFile.exists()) {
                    mapFileStream = new FileInputStream(mapFile);
                }
            }
            if (mapFileStream != null) {
                disciplineMap = (Map<String, List<String>>)x.fromXML(mapFileStream);
            }
        } catch (FileNotFoundException e) {
            log.error("Error reading discipline file: " + filePath + " : " + e.getMessage());
        }

    }

    public List<String> getDisciplinesForGroup(String groupName) {
        List<String> disciplines = new ArrayList<>();

        if (disciplineMap.containsKey(groupName)) {
            disciplines = disciplineMap.get(groupName);
        }

        return disciplines;
    }

    public Map<String, List<String>> getAllDisciplines() {
        return disciplineMap;
    }

    public Set<String> getAllDisciplineGroups() {
            return disciplineMap.keySet();
    }
}
