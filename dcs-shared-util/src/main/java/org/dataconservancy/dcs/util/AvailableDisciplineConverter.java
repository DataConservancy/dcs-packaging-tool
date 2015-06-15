/* Copyright 2012 Johns Hopkins University
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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import java.lang.Class;import java.lang.Object;import java.lang.Override;import java.lang.String;import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.dcs.util.Util.isEmptyOrNull;

public class AvailableDisciplineConverter extends AbstractEntityConverter {

    /**
     * The group of disciplines.
     */
    public static final String E_DISCIPLINE_GROUPS = "disciplineGroups";

    /**
     * The collections element name.
     */
    public static final String E_DISCIPLINE_GROUP = "disciplineGroup";
    
    /**
     * The name of the discipline group.
     */
    public static final String A_DISCIPLINE_GROUP_NAME = "name";

    /**
     * The discipline element name.
     */
    public static final String E_DISCIPLINE = "discipline";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        final Map<String, List<String>> disciplines = (Map<String, List<String>>) source;
        
        for (String disciplineGroup : disciplines.keySet()) {
            writer.startNode(E_DISCIPLINE_GROUP);
            writer.addAttribute(A_DISCIPLINE_GROUP_NAME, disciplineGroup);
            for (String discipline : disciplines.get(disciplineGroup)) {
                writer.startNode(E_DISCIPLINE);
                writer.setValue(discipline);
                writer.endNode();
            }
            writer.endNode();
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        Map<String, List<String>> disciplines = new HashMap<String, List<String>>();        
        
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String containerElementName = getElementName(reader);

            if (containerElementName.equals(E_DISCIPLINE_GROUP)) {
                String groupName = reader.getAttribute(A_DISCIPLINE_GROUP_NAME);
                if (!isEmptyOrNull(groupName)) {
                    List<String> disciplineList = new ArrayList<String>();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        if (getElementName(reader).equals(E_DISCIPLINE)) {
                            final String value = reader.getValue();
                            if (!isEmptyOrNull(value)) {
                                disciplineList.add(value);
                            }
                        }
                        reader.moveUp();
                    }
                    disciplines.put(groupName, disciplineList);
                }                
            }
            reader.moveUp();
        }
        
        return disciplines;
    }

    @Override
    public boolean canConvert(Class type) {
        return HashMap.class == type;
    }
}