/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import java.util.Set;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;

/**
 * The basic SQM statement contract.
 *
 * @author Steve Ebersole
 */
public interface SqmStatement {
	Set<SqmParameter> getQueryParameters();

	<T> T accept(SemanticQueryWalker<T> walker);
}
