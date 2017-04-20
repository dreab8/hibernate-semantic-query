/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.type.descriptor.java.spi.EmbeddableJavaTypeDescriptor;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEmbeddable;

/**
 * @author Steve Ebersole
 */
public interface EmbeddedType extends ManagedType, SqmDomainTypeEmbeddable, TypeConfigurationAware {
	String getRoleName();

	<T> EmbeddableMapper<T> getEmbeddableMapper();

	@Override
	EmbeddableJavaTypeDescriptor getJavaTypeDescriptor();
}
