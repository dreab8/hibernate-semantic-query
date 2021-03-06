/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class CountStarFunction extends AbstractAggregateFunction {
	public CountStarFunction(boolean distinct, BasicType resultType) {
		super( STAR, distinct, resultType );
	}

	@Override
	public BasicType getExpressionType() {
		return (BasicType) super.getExpressionType();
	}

	@Override
	public BasicType getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountStarFunction( this );
	}

	private static Expression STAR = new Expression() {
		@Override
		public Type getExpressionType() {
			return null;
		}

		@Override
		public Type getInferableType() {
			return null;
		}

		@Override
		public <T> T accept(SemanticQueryWalker<T> walker) {
			return null;
		}
	};
}
