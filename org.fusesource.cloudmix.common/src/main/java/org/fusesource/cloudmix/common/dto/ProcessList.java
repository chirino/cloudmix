/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision: 47094 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessList {

    @XmlElement(name = "process", required = false)
    private List<Process> processes = new ArrayList<Process>();

    public ProcessList() {
    }

    @Override
    public String toString() {
        return "ProcessList" + processes;
    }

    /**
     * Populates the history based on this list of processes
     */
    public void populateHistory(ProvisioningHistory history) {
        for (Process process : processes) {
            process.populateHistory(history);
        }
    }

    // Fluent API
    //-------------------------------------------------------------------------

    public Process process() {
        Process answer = new Process();
        processes.add(answer);
        return answer;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

}
