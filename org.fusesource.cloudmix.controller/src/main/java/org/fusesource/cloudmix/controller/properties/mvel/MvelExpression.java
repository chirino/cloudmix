/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties.mvel;

import org.fusesource.cloudmix.common.util.Strings;
import org.fusesource.cloudmix.controller.properties.Expression;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.util.Map;

/**
 * An expression implementation using <a href="http://mvel.codehaus.org/">MVEL</a>
 *
 * @version $Revision: 1.1 $
 */
public class MvelExpression implements Expression {
    private final String expression;

    public MvelExpression(String expression) {
        this.expression = expression;
    }

    public Object evaluate(Map<String, Object> variables) {
        ParserContext context = new ParserContext();
        context.addImport("Strings", Strings.class);

        Serializable compiled = MVEL.compileExpression(expression, context);

        return MVEL.executeExpression(compiled, variables);
    }

    @Override
    public String toString() {
        return "Mvel[" + expression + "]";
    }
}
