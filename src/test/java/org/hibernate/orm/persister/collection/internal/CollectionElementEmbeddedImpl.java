/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.internal;

import java.util.List;

import org.hibernate.orm.persister.collection.spi.AbstractCollectionElement;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.collection.spi.CollectionElementEmbedded;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.query.sqm.domain.SqmNavigable;

/**
 * @author Steve Ebersole
 */
public class CollectionElementEmbeddedImpl
		extends AbstractCollectionElement<EmbeddedType>
		implements CollectionElementEmbedded {
	public CollectionElementEmbeddedImpl(
			CollectionPersister persister,
			EmbeddedType ormType,
			List<Column> columns) {
		super( persister, ormType, columns );
	}

	@Override
	public SqmNavigable findNavigable(String navigableName) {
		return getEmbeddablePersister().findNavigable( navigableName );
	}

	@Override
	public String asLoggableText() {
		return "{embedded-element}";
	}

	@Override
	public EmbeddableMapper getEmbeddablePersister() {
		return getOrmType().getEmbeddableMapper();
	}
}
