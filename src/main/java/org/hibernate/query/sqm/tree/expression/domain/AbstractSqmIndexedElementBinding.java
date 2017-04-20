/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.NotYetImplementedException;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.domain.SqmNavigable;
import org.hibernate.query.sqm.tree.SqmPropertyPath;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmIndexedElementBinding
		extends AbstractSpecificSqmElementBinding
		implements SqmRestrictedCollectionElementBinding {
	private final SqmExpression indexSelectionExpression;
	private final SqmPropertyPath propertyPath;

	public AbstractSqmIndexedElementBinding(
			SqmPluralAttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		super( pluralAttributeBinding );
		this.indexSelectionExpression = indexSelectionExpression;
		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{indexes}" );
	}

	@Override
	public SqmPluralAttributeBinding getSourceBinding() {
		return getPluralAttributeBinding();
	}

	@Override
	public SqmNavigable getBoundNavigable() {
		return getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public String asLoggableText() {
		return propertyPath.getFullPath();
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new NotYetImplementedException(  );
	}
}
