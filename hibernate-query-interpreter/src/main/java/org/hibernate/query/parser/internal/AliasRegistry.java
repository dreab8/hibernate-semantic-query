/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;

/**
 * @author Andrea Boriero
 */
public class AliasRegistry {

	private final Map<String, FromClauseIndex> identificationVariableAliasRegistry;
	private final Set<String> resultVariableAliasRegistry;

	private boolean strict = false;

	public AliasRegistry() {
		this.resultVariableAliasRegistry = new HashSet<String>();
		this.identificationVariableAliasRegistry = new HashMap<>();
	}

	public void registerResultVariableAlias(String alias) {
		if ( resultVariableAliasRegistry.contains( alias ) ) {
//			throw new AliasCollisionException( "Alias collision, alias " + alias + " is used in a different clause" );
		}
		resultVariableAliasRegistry.add( alias );
	}

	public void registerIdentificationVariableAlias(String context, FromClauseIndex alias) {
		identificationVariableAliasRegistry.put( context, alias );
	}
}
