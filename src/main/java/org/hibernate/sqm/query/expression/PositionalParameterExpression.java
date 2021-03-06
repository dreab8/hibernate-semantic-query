/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PositionalParameterExpression implements ParameterExpression {
	private final int position;
	private Type typeDescriptor;

	public PositionalParameterExpression(int position) {
		this.position = position;
	}

	public PositionalParameterExpression(int position, Type typeDescriptor) {
		this.position = position;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public Type getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Type getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPositionalParameterExpression( this );
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public void impliedType(Type type) {
		if ( type != null ) {
			this.typeDescriptor = type;
		}
	}
}
