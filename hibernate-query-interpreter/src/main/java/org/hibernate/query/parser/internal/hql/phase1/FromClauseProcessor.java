/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.phase1;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.sqm.query.SemanticException;
import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ImplicitAliasGenerator;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseListener;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolver;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.query.parser.internal.hql.path.JoinPredicatePathResolverImpl;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.Predicate;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The main piece of Phase 1 processing of an HQL/JPQL statement responsible for processing the from clauses
 * present in the query and building some in-flight indexes (symbol tables) to be used later.
 * <p>
 * This is needed because, just like SQL, the from clause defines the namespace for the query.  We need to
 * know that namespace before we can start processing the other clauses which work within that namespace.
 * <p>
 * E.g., in the HQL {@code select p.name from Person p} we cannot effectively process the {@code p.name}
 * reference in the select clause until after we have processed the from clause and can then recognize that
 * {@code p} is a (forward) reference to the alias {@code p} defined in the from clause.
 *
 * @author Steve Ebersole
 */
public class FromClauseProcessor extends HqlParserBaseListener {
	private static final Logger log = Logger.getLogger( FromClauseProcessor.class );

	private final ParsingContext parsingContext;
	private final FromElementBuilder fromElementBuilder;
	private FromClauseIndex fromClauseIndex;
	private FromClauseStackNode currentFromClauseStackNode;
	private FromElementSpace currentFromElementSpace;

	private Statement.Type statementType;

	// Using HqlParser.QuerySpecContext references directly did not work in my experience, as each walk
	// seems to build new instances.  So here use the context text as key.
	private final Map<String, FromClauseStackNode> fromClauseMap = new HashMap<String, FromClauseStackNode>();
	private final Map<String, FromElement> fromElementMap = new HashMap<String, FromElement>();

	private RootEntityFromElement dmlRoot;

	public FromClauseProcessor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;

		this.fromClauseIndex = new FromClauseIndex();
		this.fromElementBuilder = new FromElementBuilder( parsingContext );
	}

	public Statement.Type getStatementType() {
		return statementType;
	}

	public RootEntityFromElement getDmlRoot() {
		return dmlRoot;
	}

	public FromClauseIndex getFromClauseIndex() {
		return fromClauseIndex;
	}

	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	public FromClauseStackNode findFromClauseForQuerySpec(HqlParser.QuerySpecContext ctx) {
		return fromClauseMap.get( ctx.getText() );
	}

	@Override
	public void enterSelectStatement(HqlParser.SelectStatementContext ctx) {
		statementType = Statement.Type.SELECT;

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( ctx.querySpec().selectClause() == null ) {
				throw new StrictJpaComplianceViolation(
						"Encountered implicit select-clause, but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.IMPLICIT_SELECT
				);
			}
		}
	}

	@Override
	public void enterInsertStatement(HqlParser.InsertStatementContext ctx) {
		statementType = Statement.Type.INSERT;

		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference( ctx.insertSpec().intoSpec().dotIdentifierSequence() );
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		log.debugf(
				"Generated implicit alias [%s] for INSERT target [%s]",
				alias,
				entityTypeDescriptor.getTypeName()
		);

		dmlRoot = new RootEntityFromElement( null, alias, entityTypeDescriptor );
		fromClauseIndex.registerAlias( dmlRoot );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	@Override
	public void enterUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		statementType = Statement.Type.UPDATE;

		dmlRoot = visitDmlRootEntityReference( ctx.mainEntityPersisterReference() );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	@Override
	public void enterDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		statementType = Statement.Type.DELETE;

		dmlRoot = visitDmlRootEntityReference( ctx.mainEntityPersisterReference() );
		fromElementMap.put( ctx.getText(), dmlRoot );
	}

	protected RootEntityFromElement visitDmlRootEntityReference(HqlParser.MainEntityPersisterReferenceContext rootEntityContext) {
		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference( rootEntityContext.dotIdentifierSequence() );
		String alias = interpretAlias( rootEntityContext.IDENTIFIER() );
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for DML root entity reference [%s]",
					alias,
					entityTypeDescriptor.getTypeName()
			);
		}
		final RootEntityFromElement root = new RootEntityFromElement( null, alias, entityTypeDescriptor );
		fromClauseIndex.registerAlias( root );
		return root;
	}

	@Override
	public void enterQuerySpec(HqlParser.QuerySpecContext ctx) {
		super.enterQuerySpec( ctx );

		if ( currentFromClauseStackNode == null ) {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause() , fromClauseIndex);
			fromClauseIndex.registerRootFromClauseStackNode( currentFromClauseStackNode );
		}
		else {
			currentFromClauseStackNode = new FromClauseStackNode( new FromClause(), currentFromClauseStackNode );
			fromClauseIndex = currentFromClauseStackNode.getFromClauseIndex();
		}
	}

	@Override
	public void exitQuerySpec(HqlParser.QuerySpecContext ctx) {
		fromClauseMap.put( ctx.getText(), currentFromClauseStackNode );

		if ( currentFromClauseStackNode == null ) {
			throw new ParsingException( "Mismatch currentFromClause handling" );
		}
		currentFromClauseStackNode = currentFromClauseStackNode.getParentNode();
		if(fromClauseIndex.getParent() != null) {
			fromClauseIndex = fromClauseIndex.getParent();
		}
	}

	@Override
	public void enterFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = currentFromClauseStackNode.getFromClause().makeFromElementSpace();
	}

	@Override
	public void exitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = null;
	}

	@Override
	public void enterFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityTypeDescriptor.class.isInstance( entityTypeDescriptor ) ) {
			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityTypeDescriptor.getTypeName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		final RootEntityFromElement rootEntityFromElement = fromElementBuilder.makeRootEntityFromElement(
				currentFromElementSpace,
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				currentFromClauseStackNode.getFromClauseIndex(),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), rootEntityFromElement );
	}

	private EntityTypeDescriptor resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityTypeDescriptor entityTypeDescriptor = parsingContext.getConsumerContext().resolveEntityReference(
				entityName
		);
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		// todo : not sure I like asserts for this kind of thing.  They are generally disable in runtime environments.
		// either the thing is important to check or it isn't.
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;

		return aliasNode.getText();
	}

	@Override
	public void enterCrossJoin(HqlParser.CrossJoinContext ctx) {
		final EntityTypeDescriptor entityTypeDescriptor = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityTypeDescriptor.class.isInstance( entityTypeDescriptor ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as query root, not in cross join : " +
							entityTypeDescriptor.getTypeName()
			);
		}

		final CrossJoinedFromElement join = fromElementBuilder.makeCrossJoinedFromElement(
				currentFromElementSpace,
				entityTypeDescriptor,
				currentFromClauseStackNode.getFromClauseIndex(),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), join );
	}

	@Override
	public void enterJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				currentFromClauseStackNode,
				JoinType.INNER,
				interpretAlias( ctx.IDENTIFIER() ),
				false
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve JPA collection join path : " + ctx.getText() );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

	@Override
	public void enterQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final JoinType joinType;
		if ( ctx.outerKeyword() != null ) {
			// for outer joins, only left outer joins are currently supported
			joinType = JoinType.LEFT;
		}
		else {
			joinType = JoinType.INNER;
		}

		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				currentFromClauseStackNode,
				joinType,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.qualifiedJoinRhs().path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( !ImplicitAliasGenerator.isImplicitAlias( joinedPath.getAlias() ) ) {
				if ( QualifiedAttributeJoinFromElement.class.isInstance( joinedPath ) ) {
					if ( QualifiedAttributeJoinFromElement.class.cast( joinedPath ).isFetched() ) {
						throw new StrictJpaComplianceViolation(
								"Encountered aliased fetch join, but strict JPQL compliance was requested",
								StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN
						);
					}
				}
			}
		}

		if ( ctx.qualifiedJoinPredicate() != null ) {
			visitor.setCurrentJoinRhs( joinedPath );
			joinedPath.setOnClausePredicate( (Predicate) ctx.qualifiedJoinPredicate().accept( visitor ) );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

	private static class QualifiedJoinTreeVisitor extends AbstractHqlParseTreeVisitor {
		private final FromElementBuilder fromElementBuilder;
		private final FromClauseIndex fromClauseIndex;
		private final ParsingContext parsingContext;
		private final FromElementSpace fromElementSpace;
		private final FromClauseStackNode currentFromClauseNode;

		private QualifiedJoinedFromElement currentJoinRhs;

		public QualifiedJoinTreeVisitor(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				FromClauseStackNode fromClauseNode,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( parsingContext, fromElementBuilder );
			this.fromElementBuilder = fromElementBuilder;
			this.fromClauseIndex = fromClauseIndex;
			this.parsingContext = parsingContext;
			this.fromElementSpace = fromElementSpace;
			this.currentFromClauseNode = fromClauseNode;
			this.attributePathResolverStack.push(
					new JoinAttributePathResolver(
							fromElementBuilder,
							fromClauseIndex,
							currentFromClauseNode,
							parsingContext,
							fromElementSpace,
							joinType,
							alias,
							fetched
					)
			);
		}

		@Override
		public FromClause getCurrentFromClause() {
			return fromElementSpace.getFromClause();
		}

		@Override
		public FromClauseStackNode getCurrentFromClauseNode() {
			return currentFromClauseNode;
		}

		@Override
		public AttributePathResolver getCurrentAttributePathResolver() {
			return attributePathResolverStack.getCurrent();
		}

		public void setCurrentJoinRhs(QualifiedJoinedFromElement currentJoinRhs) {
			this.currentJoinRhs = currentJoinRhs;
		}

		@Override
		public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
			if ( currentJoinRhs == null ) {
				throw new ParsingException( "Expecting join RHS to be set" );
			}

			attributePathResolverStack.push(
					new JoinPredicatePathResolverImpl(
							fromElementBuilder,
							fromClauseIndex,
							parsingContext,
							getCurrentFromClauseNode(),
							currentJoinRhs
					)
			);
			try {
				return super.visitQualifiedJoinPredicate( ctx );
			}
			finally {
				attributePathResolverStack.pop();
			}
		}
	}

	private static class JoinAttributePathResolver extends BasicAttributePathResolverImpl {
		private final FromElementBuilder fromElementBuilder;
		private final FromElementSpace fromElementSpace;
		private final JoinType joinType;
		private final String alias;
		private final boolean fetched;
		private final FromClauseIndex fromClauseIndex;

		public JoinAttributePathResolver(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				FromClauseStackNode currentFromClauseNode,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( fromElementBuilder, fromClauseIndex, parsingContext, currentFromClauseNode );
			this.fromElementBuilder = fromElementBuilder;
			this.fromElementSpace = fromElementSpace;
			this.joinType = joinType;
			this.alias = alias;
			this.fetched = fetched;
			this.fromClauseIndex = currentFromClauseNode.getFromClauseIndex();
		}

		@Override
		protected JoinType getIntermediateJoinType() {
			return joinType;
		}

		protected boolean areIntermediateJoinsFetched() {
			return fetched;
		}

		@Override
		protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
			return fromElementBuilder.buildAttributeJoin(
					fromElementSpace,
					lhs,
					resolveAttributeDescriptor( lhs, terminalName ),
					fromClauseIndex,
					alias,
					joinType,
					fetched
			);
		}

		protected AttributeDescriptor resolveAttributeDescriptor(FromElement lhs, String attributeName) {
			final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor(
					attributeName
			);
			if ( attributeDescriptor == null ) {
				throw new SemanticException(
						"Name [" + attributeName + "] is not a valid attribute on from-element [" +
								lhs.getTypeDescriptor().getTypeName() + "]"
				);
			}

			return attributeDescriptor;
		}

		@Override
		protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
			return aliasedFromElement;
		}
	}
}
