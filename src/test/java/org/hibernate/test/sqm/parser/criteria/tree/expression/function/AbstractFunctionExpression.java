/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.io.Serializable;

import org.hibernate.sqm.parser.criteria.tree.JpaExpression;

import org.hibernate.test.sqm.domain.BasicType;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractJpaExpressionImpl;

/**
 * Models the basic concept of a SQL function.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionExpression<X>
		extends AbstractJpaExpressionImpl<X>
		implements JpaExpression<X>, Serializable {

	private final String functionName;

	public AbstractFunctionExpression(
			String functionName,
			BasicType sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		super( criteriaBuilder, sqmType, javaType );
		this.functionName = functionName;
	}

	protected  static int properSize(int number) {
		return number + (int)( number*.75 ) + 1;
	}

	public String getFunctionName() {
		return functionName;
	}

	public BasicType<X> getFunctionResultType() {
		return getExpressionSqmType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicType<X> getExpressionSqmType() {
		return (BasicType<X>) super.getExpressionSqmType();
	}
}
