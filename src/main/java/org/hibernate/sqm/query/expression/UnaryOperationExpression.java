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
public class UnaryOperationExpression implements ImpliedTypeExpression {
	public enum Operation {
		PLUS,
		MINUS
	}

	private final Operation operation;
	private final Expression operand;
	private Type typeDescriptor;

	public UnaryOperationExpression(Operation operation, Expression operand) {
		this( operation, operand, operand.getExpressionType() );
	}

	public UnaryOperationExpression(Operation operation, Expression operand, Type typeDescriptor) {
		this.operation = operation;
		this.operand = operand;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public Type getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Type getInferableType() {
		return operand.getExpressionType();
	}

	@Override
	public void impliedType(Type type) {
		if ( type != null ) {
			this.typeDescriptor = type;
			if ( operand instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) operand ).impliedType( type );
			}
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUnaryOperationExpression( this );
	}

	public Expression getOperand() {
		return operand;
	}

	public Operation getOperation() {
		return operation;
	}
}
