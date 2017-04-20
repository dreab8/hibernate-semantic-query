/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.domain.SqmPluralAttributeIndex;
import org.hibernate.query.sqm.domain.SqmPluralAttribute;
import org.hibernate.query.sqm.tree.SqmPropertyPath;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmCollectionIndexBinding extends AbstractSqmNavigableBinding implements
		SqmCollectionIndexBinding {
	private final SqmPluralAttributeBinding attributeBinding;
	private final SqmPluralAttribute pluralAttributeReference;
	private final SqmPropertyPath propertyPath;

	public AbstractSqmCollectionIndexBinding(SqmPluralAttributeBinding pluralAttributeBinding) {
		this.attributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = pluralAttributeBinding.getBoundNavigable();

		assert pluralAttributeReference.getCollectionClassification() == SqmPluralAttribute.CollectionClassification.MAP
				|| pluralAttributeReference.getCollectionClassification() == SqmPluralAttribute.CollectionClassification.LIST;

		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{keys}" );
	}

	public SqmPluralAttributeBinding getPluralAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getPluralAttributeBinding().getBoundNavigable().getIndexReference();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmPluralAttributeBinding getSourceBinding() {
		return attributeBinding;
	}

	@Override
	public SqmPluralAttributeIndex getBoundNavigable() {
		return pluralAttributeReference.getIndexReference();
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapKeyBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "KEY(" + attributeBinding.asLoggableText() + ")";
	}
}
