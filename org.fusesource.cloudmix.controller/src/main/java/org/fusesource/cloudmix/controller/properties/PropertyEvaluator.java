/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties;

import org.fusesource.cloudmix.common.dto.PropertyDefinition;
//import scala.tools.nsc.InterpreterLoop;

/**
 * @version $Revision: 1.1 $
 */
public class PropertyEvaluator {
    private final String expression;

    public PropertyEvaluator(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public String getValue() {
        //InterpreterLoop interpreter = new InterpreterLoop();

        // TODO
        return getExpression();
    }
}
