/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.criteria.tree.select;

import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.Selection;

import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelection;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractTupleElement;

/**
 * The Hibernate implementation of the JPA {@link Selection}
 * contract.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSimpleSelection<X>
		extends AbstractTupleElement<X>
		implements JpaSelection<X>, Serializable {
	public AbstractSimpleSelection(CriteriaBuilderImpl criteriaBuilder, Class<X> javaType) {
		super( criteriaBuilder, javaType );
	}

	public Selection<X> alias(String alias) {
		setAlias( alias );
		return this;
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}

	protected abstract SqmExpression visitExpression(CriteriaVisitor visitor);

	public boolean isCompoundSelection() {
		return false;
	}

	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException( "Not a compound selection" );
	}
}
