/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEntity;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmIndexedElementBindingEntity
		extends AbstractSqmIndexedElementBinding
		implements SqmRestrictedCollectionElementBindingEntity {
	private static final Logger log = Logger.getLogger( SqmIndexedElementBindingEntity.class );

	private SqmFrom exportedFromElement;

	public SqmIndexedElementBindingEntity(
			SqmPluralAttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		super( pluralAttributeBinding, indexSelectionExpression );
	}

	@Override
	public SqmPluralAttributeBinding getSourceBinding() {
		return super.getSourceBinding();
	}

	@Override
	public SqmExpressableTypeEntity getBoundNavigable() {
		return (SqmExpressableTypeEntity) super.getBoundNavigable();
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return (SqmExpressableTypeEntity) getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmDomainTypeEntity getExportedDomainType() {
		return (SqmDomainTypeEntity) getExpressionType().getExportedDomainType();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into CollectionElementBindingEntity [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}
}
