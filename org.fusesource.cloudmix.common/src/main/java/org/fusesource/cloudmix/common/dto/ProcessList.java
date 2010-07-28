/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$
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
