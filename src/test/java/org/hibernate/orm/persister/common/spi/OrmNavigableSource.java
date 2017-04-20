/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.common.spi;

import java.util.List;

import org.hibernate.query.sqm.domain.SqmNavigableSource;

/**
 * @author Steve Ebersole
 */
public interface OrmNavigableSource<T> extends OrmNavigable<T>, SqmNavigableSource<T> {
	@Override
	OrmNavigable findNavigable(String navigableName);

	// todo : overload this for entity- and collection-valued attributes
	//		but that requires splitting SingularAttributeEntity into interface/impl
	//		and moving the interface into SPI
	List<JoinColumnMapping> resolveJoinColumnMappings(OrmAttribute attribute);
}
