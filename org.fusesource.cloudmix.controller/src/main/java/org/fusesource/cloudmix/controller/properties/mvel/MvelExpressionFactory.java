/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties.mvel;

import org.fusesource.cloudmix.controller.properties.Expression;
import org.fusesource.cloudmix.controller.properties.ExpressionFactory;

/**
 * A factory of expressions using <a href="http://mvel.codehaus.org/">MVEL</a>
 * @version $Revision: 1.1 $
 */
public class MvelExpressionFactory implements ExpressionFactory {
    public Expression createExpression(String expression) {
        return new MvelExpression(expression);
    }
}
