/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Map;

/**
 * Binary operation expression binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryOperationExpressionBinder {
    
    /**
     * Bind binary operation expression with metadata.
     *
     * @param segment binary operation expression segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded binary operation expression segment
     */
    public static BinaryOperationExpression bind(final BinaryOperationExpression segment, final SQLStatementBinderContext statementBinderContext,
                                                 final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        ExpressionSegment boundedLeft = ExpressionSegmentBinder.bind(segment.getLeft(), statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        ExpressionSegment boundedRight = ExpressionSegmentBinder.bind(segment.getRight(), statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        return new BinaryOperationExpression(segment.getStartIndex(), segment.getStopIndex(), boundedLeft, boundedRight, segment.getOperator(), segment.getText());
    }
}
