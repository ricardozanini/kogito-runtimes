/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.workflow.core.node;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.process.core.ParameterDefinition;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.process.core.impl.ParameterDefinitionImpl;
import org.jbpm.process.core.impl.WorkImpl;

public class HumanTaskNode extends WorkItemNode {

    private static final long serialVersionUID = 510l;

    private String swimlane;

    public static final Set<String> TASK_PARAMETERS = Set.of(
            Work.PARAMETER_UNIQUE_TASK_ID,
            "TaskName",
            "NodeName",
            "NotStartedNotify",
            "NotStartedReassign",
            "NotCompletedNotify",
            "NotCompletedReassign",
            "Description",
            "Comment",
            "ActorId",
            "GroupId",
            "Priority",
            "Skippable",
            "Content",
            "Locale",
            "ExcludedOwnerId",
            "BusinessAdministratorId",
            "BusinessAdministratorGroupId");

    public HumanTaskNode() {
        Work work = new WorkImpl();
        work.setName("Human Task");
        Set<ParameterDefinition> parameterDefinitions = new HashSet<>();
        parameterDefinitions.add(new ParameterDefinitionImpl("TaskName", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("ActorId", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Priority", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Comment", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Skippable", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Content", new StringDataType()));
        // TODO: initiator
        // TODO: attachments
        // TODO: deadlines
        // TODO: delegates
        // TODO: recipients
        // TODO: ...
        work.setParameterDefinitions(parameterDefinitions);
        setWork(work);
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

}
