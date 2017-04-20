/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.hibernate.query.sqm.ParsingException;
import org.hibernate.query.sqm.produce.spi.criteria.CriteriaVisitor;
import org.hibernate.query.sqm.produce.spi.criteria.JpaPredicate;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.tree.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractJpaExpressionImpl;

/**
 * Basic template support for {@link Predicate} implementors providing
 * expression handling, negation and conjunction/disjunction handling.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPredicateImpl
		extends AbstractJpaExpressionImpl<Boolean>
		implements JpaPredicate, Serializable {

	protected AbstractPredicateImpl(CriteriaBuilderImpl criteriaBuilder, SqmExpressableType sqmType) {
		super( criteriaBuilder, sqmType, Boolean.class );
	}

	public boolean isNegated() {
		return false;
	}

	public JpaPredicate not() {
		return new NegatedPredicateWrapper( this );
	}


	// Selection ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public final boolean isCompoundSelection() {
		// Should always be false for predicates
		return super.isCompoundSelection();
	}

	@Override
	public final List<Selection<?>> getCompoundSelectionItems() {
		// Should never have sub selection items for predicates
		return super.getCompoundSelectionItems();
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		throw new ParsingException( "Unexpected call to visitExpression on JpaPredicate" );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		throw new ParsingException( "Unexpected call to visitSelections on JpaPredicate" );
	}
}
