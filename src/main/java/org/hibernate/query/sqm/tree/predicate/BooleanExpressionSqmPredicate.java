/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * Represents an expression whose type is boolean, and can therefore be used as a predicate.
 *
 * @author Steve Ebersole
 */
public class BooleanExpressionSqmPredicate implements SqmPredicate {
	private final SqmExpression booleanExpression;

	public BooleanExpressionSqmPredicate(SqmExpression booleanExpression) {
		assert booleanExpression.getExpressionType() != null;
		assert booleanExpression.getExpressionType().getExportedDomainType() instanceof SqmDomainTypeBasic;
		final Class expressionJavaType = ( (SqmDomainTypeBasic) booleanExpression.getExpressionType() ).getJavaType();
		assert boolean.class.equals( expressionJavaType ) || Boolean.class.equals( expressionJavaType );

		this.booleanExpression = booleanExpression;
	}

	public SqmExpression getBooleanExpression() {
		return booleanExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBooleanExpressionPredicate( this );
	}
}
