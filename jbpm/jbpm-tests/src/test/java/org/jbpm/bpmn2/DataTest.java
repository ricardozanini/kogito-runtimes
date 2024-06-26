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
package org.jbpm.bpmn2;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jbpm.bpmn2.activity.XPathProcessModel;
import org.jbpm.bpmn2.activity.XPathProcessProcess;
import org.jbpm.bpmn2.core.Association;
import org.jbpm.bpmn2.core.DataStore;
import org.jbpm.bpmn2.core.Definitions;
import org.jbpm.bpmn2.xml.ProcessHandler;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.kie.kogito.process.ProcessInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTest extends JbpmBpmn2TestCase {

    @Test
    public void testImport() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-Import.bpmn2");
        KogitoProcessInstance processInstance = kruntime.startProcess("Import");
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testDataObject() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataObject.bpmn2");
        Map<String, Object> params = new HashMap<>();
        params.put("employee", "UserId-12345");
        KogitoProcessInstance processInstance = kruntime.startProcess("DataObject",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testDataStore() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataStore.bpmn2");
        KogitoProcessInstance processInstance = kruntime.startProcess("DataStore");
        Definitions def = (Definitions) processInstance.getProcess()
                .getMetaData().get("Definitions");
        assertThat(def.getDataStores()).isNotNull().hasSize(1);

        DataStore dataStore = def.getDataStores().get(0);
        assertThat(dataStore.getId()).isEqualTo("employee");
        assertThat(dataStore.getName()).isEqualTo("employeeStore");
        assertThat(((ObjectDataType) dataStore.getType()).getClassName()).isEqualTo(String.class.getCanonicalName());

    }

    @Test
    public void testAssociation() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-Association.bpmn2");
        KogitoProcessInstance processInstance = kruntime.startProcess("Association");
        List<Association> associations = (List<Association>) processInstance.getProcess().getMetaData().get(ProcessHandler.ASSOCIATIONS);
        assertThat(associations).isNotNull().hasSize(1);

        Association assoc = associations.get(0);
        assertThat(assoc.getId()).isEqualTo("_1234");
        assertThat(assoc.getSourceRef()).isEqualTo("_1");
        assertThat(assoc.getTargetRef()).isEqualTo("_2");

    }

    @Test
    public void testEvaluationProcess() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-Evaluation.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        params.put("employee", "UserId-12345");
        KogitoProcessInstance processInstance = kruntime.startProcess("Evaluation",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testEvaluationProcess2() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-Evaluation2.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        params.put("employee", "UserId-12345");
        KogitoProcessInstance processInstance = kruntime.startProcess("Evaluation2", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testEvaluationProcess3() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-Evaluation3.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<>();
        params.put("employee", "john2");
        KogitoProcessInstance processInstance = kruntime.startProcess("Evaluation3",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testXpathExpression() throws Exception {
        Application app = ProcessTestHelper.newApplication();
        org.kie.kogito.process.Process<XPathProcessModel> process = XPathProcessProcess.newProcess(app);
        XPathProcessModel model = process.createModel();

        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<instanceMetadata><user approved=\"false\" /></instanceMetadata>"
                                .getBytes()));

        model.setInstanceMetadata(document);
        ProcessInstance<XPathProcessModel> processInstance = process.createInstance(model);
        processInstance.start();
        assertThat(processInstance).extracting(ProcessInstance::status).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    @Test
    public void testDataInputAssociations() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociations.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {
                    @Override
                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    @Override
                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(workItem.getParameter("coId")).isEqualTo("hello world");
                    }
                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<>();
        params.put("instanceMetadata", document.getFirstChild());
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociations", params);

    }

    @Test
    public void testDataInputAssociationsWithStringObject() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociationsStringObject.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    @Override
                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    @Override
                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(workItem.getParameter("coId")).isEqualTo("hello");
                    }

                });
        Map<String, Object> params = new HashMap<>();
        params.put("instanceMetadata", "hello");
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociationsStringObject", params);

    }

    /**
     * TODO testDataInputAssociationsWithLazyLoading
     */
    @Test
    @Disabled
    public void testDataInputAssociationsWithLazyLoading()
            throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociationsLazyCreating.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        Element coIdParamObj = (Element) workItem.getParameter("coId");
                        assertThat(coIdParamObj.getNodeName()).isEqualTo("mydoc");
                        assertThat(coIdParamObj.getFirstChild().getNodeName()).isEqualTo("mynode");
                        assertThat(coIdParamObj.getFirstChild().getFirstChild().getNodeName()).isEqualTo("user");
                        assertThat(coIdParamObj.getFirstChild().getFirstChild().getAttributes().getNamedItem("hello").getNodeValue()).isEqualTo("hello world");
                    }

                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<>();
        params.put("instanceMetadata", document.getFirstChild());
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociationsLazyCreating", params);

    }

    @Test
    public void testDataInputAssociationsWithString() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociationsString.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(workItem.getParameter("coId")).isEqualTo("hello");
                    }

                });
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociationsString");

    }

    @Test
    public void testDataInputAssociationsWithStringWithoutQuotes()
            throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociationsStringNoQuotes.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(workItem.getParameter("coId")).isEqualTo("hello");
                    }

                });
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociationsStringNoQuotes");

    }

    @Test
    public void testDataInputAssociationsWithXMLLiteral() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataInputAssociationsXmlLiteral.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(((org.w3c.dom.Node) workItem.getParameter("coId")).getNodeName()).isEqualTo("id");
                        assertThat(((org.w3c.dom.Node) workItem.getParameter("coId")).getFirstChild().getTextContent()).isEqualTo("some text");
                    }

                });
        KogitoProcessInstance processInstance = kruntime.startProcess("DataInputAssociationsXmlLiteral");

    }

    /**
     * TODO testDataInputAssociationsWithTwoAssigns
     */
    @Test
    @Disabled
    public void testDataInputAssociationsWithTwoAssigns() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-DataInputAssociations-two-assigns.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        assertThat(((Element) workItem.getParameter("Comment")).getNodeName()).isEqualTo("foo");
                        // assertEquals("mynode", ((Element)
                        // workItem.getParameter("Comment")).getFirstChild().getNodeName());
                        // assertEquals("user", ((Element)
                        // workItem.getParameter("Comment")).getFirstChild().getFirstChild().getNodeName());
                        // assertEquals("hello world", ((Element)
                        // workItem.getParameter("coId")).getFirstChild().getFirstChild().getAttributes().getNamedItem("hello").getNodeValue());
                    }

                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<>();
        params.put("instanceMetadata", document.getFirstChild());
        KogitoProcessInstance processInstance = kruntime.startProcess("process",
                params);

    }

    @Test
    public void testDataOutputAssociationsforHumanTask() throws Exception {
        kruntime = createKogitoProcessRuntime("BPMN2-DataOutputAssociations-HumanTask.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        DocumentBuilderFactory factory = DocumentBuilderFactory
                                .newInstance();
                        DocumentBuilder builder;
                        try {
                            builder = factory.newDocumentBuilder();
                        } catch (ParserConfigurationException e) {
                            throw new RuntimeException(e);
                        }
                        final Map<String, Object> results = new HashMap<>();

                        // process metadata
                        org.w3c.dom.Document processMetadaDoc = builder
                                .newDocument();
                        org.w3c.dom.Element processMetadata = processMetadaDoc
                                .createElement("previoustasksowner");
                        processMetadaDoc.appendChild(processMetadata);
                        // org.w3c.dom.Element procElement =
                        // processMetadaDoc.createElement("previoustasksowner");
                        processMetadata
                                .setAttribute("primaryname", "my_result");
                        // processMetadata.appendChild(procElement);
                        results.put("output", processMetadata);

                        mgr.completeWorkItem(workItem.getStringId(), results);
                    }

                });
        Map<String, Object> params = new HashMap<>();
        KogitoProcessInstance processInstance = kruntime.startProcess("process",
                params);

    }

    @Test
    public void testDataOutputAssociations() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataOutputAssociations.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        try {
                            Document document = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(
                                            "<user hello='hello world' />"
                                                    .getBytes()));
                            Map<String, Object> params = new HashMap<>();
                            params.put("output", document.getFirstChild());
                            mgr.completeWorkItem(workItem.getStringId(), params);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }

                    }

                });
        KogitoProcessInstance processInstance = kruntime.startProcess("DataOutputAssociations");

    }

    @Test
    public void testDataOutputAssociationsXmlNode() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-DataOutputAssociationsXmlNode.bpmn2");
        kruntime.getKogitoWorkItemManager().registerWorkItemHandler("Human Task",
                new KogitoWorkItemHandler() {

                    public void abortWorkItem(KogitoWorkItem manager,
                            KogitoWorkItemManager mgr) {

                    }

                    public void executeWorkItem(KogitoWorkItem workItem,
                            KogitoWorkItemManager mgr) {
                        try {
                            Document document = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(
                                            "<user hello='hello world' />"
                                                    .getBytes()));
                            Map<String, Object> params = new HashMap<>();
                            params.put("output", document.getFirstChild());
                            mgr.completeWorkItem(workItem.getStringId(), params);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }

                    }

                });
        KogitoProcessInstance processInstance = kruntime.startProcess("DataOutputAssociationsXmlNode");

    }

    @Test
    public void testDefaultProcessVariableValue() throws Exception {

        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/data/BPMN2-CorrelationKey.bpmn2");

        Map<String, Object> parameters = new HashMap<String, Object>();

        KogitoWorkflowProcessInstance processInstance = (KogitoWorkflowProcessInstance) kruntime.startProcess("CorrelationKey", parameters);

        assertThat(processInstance.getVariable("procVar")).isEqualTo("defaultProc");
        assertThat(processInstance.getVariable("intVar")).isEqualTo(1);

    }

}
