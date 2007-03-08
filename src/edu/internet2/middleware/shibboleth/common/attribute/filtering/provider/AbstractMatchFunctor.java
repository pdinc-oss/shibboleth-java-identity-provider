/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.FilterContext;

/**
 * Base class for {@link MatchFunctor}s that delegate the evaluation and negate the result if necessary.
 */
public abstract class AbstractMatchFunctor implements MatchFunctor {

    /** Whether evaluation results should be negated. */
    private boolean negateResult;

    /**
     * Gets whether evaluation results should be negated.
     * 
     * @return whether evaluation results should be negated
     */
    public boolean getNegateResult() {
        return negateResult;
    }

    /**
     * Sets whether evaluation results should be negated.
     * 
     * @param negate whether evaluation results should be negated
     */
    public void setNegateResult(boolean negate) {
        negateResult = negate;
    }

    /** {@inheritDoc} */
    public boolean evaluate(FilterContext filterContext) throws FilterProcessingException {
        boolean result = doEvaluate(filterContext);
        if (negateResult) {
            return !result;
        }

        return result;
    }

    /** {@inheritDoc} */
    public boolean evaluate(FilterContext filterContext, String attributeId, Object attributeValue)
            throws FilterProcessingException {
        boolean result = doEvaluate(filterContext, attributeId, attributeValue);
        if (negateResult) {
            return !result;
        }

        return result;
    }

    /**
     * Evaluates this matching criteria. This evaluation is used while the filtering engine determiens policy
     * applicability.
     * 
     * @param filterContext current filtering context
     * 
     * @return true if the criteria for this matching function are meant
     * 
     * @throws FilterProcessingException thrown if the function can not be evaluated
     */
    protected abstract boolean doEvaluate(FilterContext filterContext) throws FilterProcessingException;

    /**
     * Evaluates this matching criteria. This evaluation is used while the filtering engine is filtering attribute
     * values.
     * 
     * @param filterContext the current filtering context
     * @param attributeId ID of the attribute being evaluated
     * @param attributeValue value of the attribute being evalauted
     * 
     * @return true if the criteria for this matching function are meant
     * 
     * @throws FilterProcessingException thrown if the function can not be evaluated
     */
    protected abstract boolean doEvaluate(FilterContext filterContext, String attributeId, Object attributeValue)
            throws FilterProcessingException;
}