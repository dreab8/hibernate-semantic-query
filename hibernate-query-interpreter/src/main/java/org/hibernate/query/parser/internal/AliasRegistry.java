/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrea Boriero
 */
public class AliasRegistry {

	private final Set<String> identificationVariableAliasRegistry;
	private final Set<String> resultVariableAliasRegistry;

	private String lastIdentificationAlias;

	private boolean strict = false;
	public AliasRegistry() {
		this.resultVariableAliasRegistry = new HashSet<String>();
		this.identificationVariableAliasRegistry = new HashSet<String>();
	}

	public void registerResultVariableAlias(String alias) {
		if ( resultVariableAliasRegistry.contains( alias ) ) {
//			throw new AliasCollisionException( "Alias collision, alias " + alias + " is used in a different clause" );
		}
		resultVariableAliasRegistry.add( alias );
		lastIdentificationAlias = null;
	}

	public void registerIdentificationVariableAlias(String alias) {
		lastIdentificationAlias = alias;
		identificationVariableAliasRegistry.add( alias );
	}
}
