/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.persister.common.spi.Navigable;
import org.hibernate.query.sqm.SemanticException;

/**
 * Indicates that a "navigable" referenced in the query could not be resolved
 * to a {@link org.hibernate.persister.common.spi.Navigable} reference in a
 * situation where we expect such a navigable to exists and it is an error for
 * it to not.
 *
 * @see SqmNavigableSource#findNavigable
 *
 * @author Steve Ebersole
 *
 * @deprecated move to {@link org.hibernate.persister}
 */
@Deprecated
public class NavigableResolutionException extends SemanticException {
	public NavigableResolutionException(String message) {
		super( message );
	}
}
