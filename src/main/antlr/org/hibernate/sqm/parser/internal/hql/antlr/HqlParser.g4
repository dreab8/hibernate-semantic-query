parser grammar HqlParser;

options {
	tokenVocab=HqlLexer;
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.parser.internal.hql.antlr;
}

@members {
	/**
	 */
	protected void logUseOfReservedWordAsIdentifier(Token token) {
	}

	/**
	 * Determine if the text of the new upcoming token LT(1), if one, matches
	 * the passed argument.  Internally calls doesUpcomingTokenMatchAny( 1, checks )
	 */
	protected boolean doesUpcomingTokenMatchAny(String... checks) {
		return doesUpcomingTokenMatchAny( 1, checks );
	}

	/**
	 * Determine if the text of the new upcoming token LT(offset), if one, matches
	 * the passed argument.
	 */
	protected boolean doesUpcomingTokenMatchAny(int offset, String... checks) {
		final Token token = retrieveUpcomingToken( offset );
		if ( token != null ) {
			if ( token.getType() == IDENTIFIER ) {
				// todo : is this really a check we want?

				final String textToValidate = token.getText();
				if ( textToValidate != null ) {
					for ( String check : checks ) {
						if ( textToValidate.equalsIgnoreCase( check ) ) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	protected Token retrieveUpcomingToken(int offset) {
		if ( null == _input ) {
			return null;
		}
		return _input.LT( offset );
	}

	protected String retrieveUpcomingTokenText(int offset) {
		Token token = retrieveUpcomingToken( offset );
		return token == null ? null : token.getText();
	}
}

statement
	: ( selectStatement | updateStatement | deleteStatement | insertStatement ) EOF
	;

selectStatement
	: querySpec orderByClause?
	;

updateStatement
	: UPDATE FROM? mainEntityPersisterReference setClause whereClause
	;

setClause
	: SET assignment+
	;

assignment
	: dotIdentifierSequence EQUAL expression
	;

deleteStatement
	: DELETE FROM? mainEntityPersisterReference whereClause
	;

insertStatement
// todo : VERSIONED
	: INSERT insertSpec querySpec
	;

insertSpec
	: intoSpec targetFieldsSpec
	;

intoSpec
	: INTO dotIdentifierSequence
	;

targetFieldsSpec
	: LEFT_PAREN dotIdentifierSequence (COMMA dotIdentifierSequence)* RIGHT_PAREN

	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: ORDER BY sortSpecification (COMMA sortSpecification)*
	;

sortSpecification
	: expression collationSpecification? orderingSpecification?
	;

collationSpecification
	:	COLLATE collateName
	;

collateName
	:	dotIdentifierSequence
	;

orderingSpecification
	:	ASC
	|	DESC
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	:	selectClause? fromClause whereClause? ( groupByClause havingClause? )?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	SELECT DISTINCT? selectionList
	;

selectionList
	: selection (COMMA selection)*
	;

selection
	// I have noticed that without this predicate, Antlr will sometimes
	// interpret `select a.b from Something ...` as `from` being the
	// select-expression alias
	: selectExpression (resultIdentifier)?
	;

resultIdentifier
	: (AS identifier)
	| IDENTIFIER
	;

selectExpression
	:	dynamicInstantiation
	|	jpaSelectObjectSyntax
	|	expression
	;

dynamicInstantiation
	: NEW dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
	;

dynamicInstantiationTarget
	: LIST
	| MAP
	| dotIdentifierSequence
	;

dotIdentifierSequence
	: identifier (DOT identifier)*
	;

//path
//	: dotIdentifierSequence																# SimplePath
//	| TREAT LEFT_PAREN dotIdentifierSequence AS dotIdentifierSequence RIGHT_PAREN		# TreatedPath
//	| path LEFT_BRACKET expression RIGHT_BRACKET (DOT path)?							# IndexedPath
//	| INDEX LEFT_PAREN identifier RIGHT_PAREN											# CollectionIndexPath
//	| KEY LEFT_PAREN mapReference RIGHT_PAREN											# MapKeyPath
//	| VALUE LEFT_PAREN collectionReference RIGHT_PAREN				   					# CollectionValuePath
//	| ENTRY LEFT_PAREN mapReference RIGHT_PAREN			   								# MapEntryPath
//	;


path
	// a SimplePath may be any number of things like:
	//		* Class FQN
	//		* Java constant (enum/static)
	//		* an identification variable
	//		* an unqualified attribute name
	: dotIdentifierSequence												# SimplePath
	// a Map.Entry cannot be further dereferenced
	| ENTRY LEFT_PAREN mapReference RIGHT_PAREN							# MapEntryPath
	// only one index-access is allowed per path
	| path LEFT_BRACKET expression RIGHT_BRACKET (pathTerminal)?		# IndexedPath
	// most path expressions fall into this bucket
	| pathRoot (pathTerminal)?											# CompoundPath
	;

pathRoot
	: identifier																			# SimplePathRoot
	| TREAT LEFT_PAREN dotIdentifierSequence AS dotIdentifierSequence RIGHT_PAREN			# TreatedPathRoot
	| KEY LEFT_PAREN mapReference RIGHT_PAREN												# MapKeyPathRoot
	| VALUE LEFT_PAREN collectionReference RIGHT_PAREN				   						# CollectionValuePathRoot
	;

pathTerminal
	: (DOT identifier)+
	;

collectionReference
// having as a separate rule allows us to validate that the path indeed resolves to a Collection attribute
	: path
	;

mapReference
// having as a separate rule allows us to validate that the path indeed resolves to a Map attribute
	: path
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	dynamicInstantiationArgExpression (AS? identifier)?
	;

dynamicInstantiationArgExpression
	:	expression
	|	dynamicInstantiation
	;

jpaSelectObjectSyntax
	:	OBJECT LEFT_PAREN identifier RIGHT_PAREN
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: FROM fromElementSpace (COMMA fromElementSpace)*
	;

fromElementSpace
	:	fromElementSpaceRoot ( crossJoin | jpaCollectionJoin | qualifiedJoin )*
	;

fromElementSpaceRoot
	: mainEntityPersisterReference
	;

mainEntityPersisterReference
	: dotIdentifierSequence (identificationVariableDef)?
	;

identificationVariableDef
//	: AS? {!doesUpcomingTokenMatchAny("where","join")}? identificationVariable
//	: AS identificationVariable
//	| {!doesUpcomingTokenMatchAny("where","join")}? identificationVariable
	: (AS identificationVariable)
	| IDENTIFIER
	;

identificationVariable
	: identifier
	;

crossJoin
	: CROSS JOIN mainEntityPersisterReference
	;

jpaCollectionJoin
	:	COMMA IN LEFT_PAREN path RIGHT_PAREN (identificationVariableDef)?
	;

qualifiedJoin
	: ( INNER | ((LEFT|RIGHT|FULL)? OUTER) )? JOIN FETCH? qualifiedJoinRhs (qualifiedJoinPredicate)?
	;

qualifiedJoinRhs
	: path (identificationVariableDef)?
	;

qualifiedJoinPredicate
	: (ON | WITH) predicate
	;



// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GROUP BY clause

groupByClause
	:	GROUP BY groupingSpecification
	;

groupingSpecification
	:	groupingValue ( COMMA groupingValue )*
	;

groupingValue
	:	expression collationSpecification?
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//HAVING clause

havingClause
	:	HAVING predicate
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	WHERE predicate
	;

predicate
	: LEFT_PAREN predicate RIGHT_PAREN					# GroupedPredicate
	| predicate OR predicate							# OrPredicate
	| predicate AND predicate							# AndPredicate
	| NOT predicate										# NegatedPredicate
	| expression IS (NOT)? NULL							# IsNullPredicate
	| expression IS (NOT)? EMPTY						# IsEmptyPredicate
	| expression EQUAL expression						# EqualityPredicate
	| expression NOT_EQUAL expression					# InequalityPredicate
	| expression GREATER expression						# GreaterThanPredicate
	| expression GREATER_EQUAL expression				# GreaterThanOrEqualPredicate
	| expression LESS expression						# LessThanPredicate
	| expression LESS_EQUAL expression					# LessThanOrEqualPredicate
	| expression IN inList								# InPredicate
	| expression BETWEEN expression AND expression		# BetweenPredicate
	| expression LIKE expression (likeEscape)?			# LikePredicate
	| MEMBER OF path									# MemberOfPredicate
	;

inList
	: ELEMENTS? LEFT_PAREN dotIdentifierSequence RIGHT_PAREN		# PersistentCollectionReferenceInList
	| LEFT_PAREN expression (COMMA expression)*	RIGHT_PAREN			# ExplicitTupleInList
	| expression													# SubQueryInList
	;

likeEscape
	: ESCAPE expression
	;

expression
	: expression DOUBLE_PIPE expression			# ConcatenationExpression
	| expression PLUS expression				# AdditionExpression
	| expression MINUS expression				# SubtractionExpression
	| expression ASTERISK expression			# MultiplicationExpression
	| expression SLASH expression				# DivisionExpression
	| expression PERCENT expression				# ModuloExpression
	| MINUS expression							# UnaryMinusExpression
	| PLUS expression							# UnaryPlusExpression
	| caseStatement								# CaseExpression
	| coalesce									# CoalesceExpression
	| nullIf									# NullIfExpression
	| literal									# LiteralExpression
	| parameter									# ParameterExpression
	| path										# PathExpression
	| function									# FunctionExpression
	| LEFT_PAREN querySpec RIGHT_PAREN			# SubQueryExpression
	;

caseStatement
	: simpleCaseStatement
	| searchedCaseStatement
	;

simpleCaseStatement
	: CASE expression (simpleCaseWhen)+ (caseOtherwise)? END
	;

simpleCaseWhen
	: WHEN expression THEN expression
	;

caseOtherwise
	: ELSE expression
	;

searchedCaseStatement
	: CASE (searchedCaseWhen)+ (caseOtherwise)? END
	;

searchedCaseWhen
	: WHEN predicate THEN expression
	;

coalesce
	: COALESCE LEFT_PAREN expression (COMMA expression)+ RIGHT_PAREN
	;

nullIf
	: NULLIF LEFT_PAREN expression COMMA expression RIGHT_PAREN
	;

literal
// todo : date/time literals (following JDBC escape syntax)
	: STRING_LITERAL
	| CHARACTER_LITERAL
	| INTEGER_LITERAL
	| LONG_LITERAL
	| BIG_INTEGER_LITERAL
	| FLOAT_LITERAL
	| DOUBLE_LITERAL
	| BIG_DECIMAL_LITERAL
	| HEX_LITERAL
	| OCTAL_LITERAL
	| NULL
	| TRUE
	| FALSE
	| timestampLiteral
	| dateLiteral
	| timeLiteral
	;

timestampLiteral
	: TIMESTAMP_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateLiteral
	: DATE_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

timeLiteral
	: TIME_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateTimeLiteralText
	: STRING_LITERAL | CHARACTER_LITERAL
	;

parameter
	: COLON identifier					# NamedParameter
	| QUESTION_MARK INTEGER_LITERAL		# PositionalParameter
	;

function
	: standardFunction
	| aggregateFunction
	| jpaCollectionFunction
	| hqlCollectionFunction
	| jpaNonStandardFunction
	| nonStandardFunction
	;

jpaNonStandardFunction
	: FUNCTION LEFT_PAREN nonStandardFunctionName (COMMA nonStandardFunctionArguments)? RIGHT_PAREN
	;

nonStandardFunctionName
	: dotIdentifierSequence
	;

nonStandardFunctionArguments
	: expression (COMMA expression)*
	;

nonStandardFunction
	: nonStandardFunctionName LEFT_PAREN nonStandardFunctionArguments? RIGHT_PAREN
	;

jpaCollectionFunction
	: SIZE LEFT_PAREN path RIGHT_PAREN					# CollectionSizeFunction
	| INDEX LEFT_PAREN identifier RIGHT_PAREN			# CollectionIndexFunction
	;

hqlCollectionFunction
	: MAXINDEX LEFT_PAREN path RIGHT_PAREN				# MaxIndexFunction
	| MAXELEMENT LEFT_PAREN path RIGHT_PAREN			# MaxElementFunction
	| MININDEX LEFT_PAREN path RIGHT_PAREN				# MinIndexFunction
	| MINELEMENT LEFT_PAREN path RIGHT_PAREN			# MinElementFunction
	;

aggregateFunction
	: avgFunction
	| sumFunction
	| minFunction
	| maxFunction
	| countFunction
	;

avgFunction
	: AVG LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

sumFunction
	: SUM LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

minFunction
	: MIN LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

maxFunction
	: MAX LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

countFunction
	: COUNT LEFT_PAREN DISTINCT? (expression | ASTERISK) RIGHT_PAREN
	;

standardFunction
	:	castFunction
	|	concatFunction
	|	substringFunction
	|	trimFunction
	|	upperFunction
	|	lowerFunction
	|	lengthFunction
	|	locateFunction
	|	absFunction
	|	sqrtFunction
	|	modFunction
	|	currentDateFunction
	|	currentTimeFunction
	|	currentTimestampFunction
	|	extractFunction
	|	positionFunction
	|	charLengthFunction
	|	octetLengthFunction
	|	bitLengthFunction
	;


castFunction
	: CAST LEFT_PAREN expression AS dataType RIGHT_PAREN
	;

dataType
	: IDENTIFIER
	;

concatFunction
	: CONCAT LEFT_PAREN expression (COMMA expression)+ RIGHT_PAREN
	;

substringFunction
	: SUBSTRING LEFT_PAREN expression COMMA substringFunctionStartArgument (COMMA substringFunctionLengthArgument)? RIGHT_PAREN
	;

substringFunctionStartArgument
	: expression
	;

substringFunctionLengthArgument
	: expression
	;

trimFunction
	: TRIM LEFT_PAREN trimSpecification? trimCharacter? FROM? expression RIGHT_PAREN
	;

trimSpecification
	: LEADING
	| TRAILING
	| BOTH
	;

trimCharacter
	: CHARACTER_LITERAL | STRING_LITERAL
	;

upperFunction
	: UPPER LEFT_PAREN expression RIGHT_PAREN
	;

lowerFunction
	: LOWER LEFT_PAREN expression RIGHT_PAREN
	;

lengthFunction
	: LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

locateFunction
	: LOCATE LEFT_PAREN locateFunctionSubstrArgument COMMA locateFunctionStringArgument (COMMA locateFunctionStartArgument)? RIGHT_PAREN
	;

locateFunctionSubstrArgument
	: expression
	;

locateFunctionStringArgument
	: expression
	;

locateFunctionStartArgument
	: expression
	;

absFunction
	:	ABS LEFT_PAREN expression RIGHT_PAREN
	;

sqrtFunction
	:	SQRT LEFT_PAREN expression RIGHT_PAREN
	;

modFunction
	:	MOD LEFT_PAREN modDividendArgument COMMA modDivisorArgument RIGHT_PAREN
	;

modDividendArgument
	: expression
	;

modDivisorArgument
	: expression
	;

currentDateFunction
	: CURRENT_DATE (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimeFunction
	: CURRENT_TIME (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimestampFunction
	: CURRENT_TIMESTAMP (LEFT_PAREN RIGHT_PAREN)?
	;

extractFunction
	: EXTRACT LEFT_PAREN extractField FROM expression RIGHT_PAREN
	;

extractField
	: datetimeField
	| timeZoneField
	;

datetimeField
	: nonSecondDatetimeField
	| SECOND
	;

nonSecondDatetimeField
	: YEAR
	| MONTH
	| DAY
	| HOUR
	| MINUTE
	;

timeZoneField
	: TIMEZONE_HOUR
	| TIMEZONE_MINUTE
	;

positionFunction
	: POSITION LEFT_PAREN positionSubstrArgument IN positionStringArgument RIGHT_PAREN
	;

positionSubstrArgument
	: expression
	;

positionStringArgument
	: expression
	;

charLengthFunction
	: CAST LEFT_PAREN expression RIGHT_PAREN
	;

octetLengthFunction
	: OCTET_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

bitLengthFunction
	: BIT_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

/**
 * The `identifier` is used to provide "keyword as identifier" handling.
 *
 * The lexer hands us recognized keywords using their specific tokens.  This is important
 * for the recognition of query structure, especially in terms of performance!
 *
 * However we want to continue to allow users to use mopst keywords as identifiers (e.g., attribute names).
 * This parser rule helps with that.  Here we expect that the caller already understands their
 * context enough to know that keywords-as-identifiers are allowed.
 */
identifier
	: IDENTIFIER
	| (ABS
	| ALL
	| AND
	| ANY
	| AS
	| ASC
	| AVG
	| BY
	| BETWEEN
	| BIT_LENGTH
	| BOTH
	| CAST
	| COALESCE
	| COLLATE
	| CONCAT
	| COUNT
	| CROSS
	| DAY
	| DELETE
	| DESC
	| DISTINCT
	| ELEMENTS
	| ENTRY
	| FROM
	| FULL
	| FUNCTION
	| GROUP
	| HOUR
	| IN
	| INDEX
	| INNER
	| INSERT
	| JOIN
	| KEY
	| LEADING
	| LEFT
	| LENGTH
	| LIKE
	| LIST
	| LOWER
	| MAP
	| MAX
	| MIN
	| MINUTE
	| MEMBER
	| MONTH
	| OBJECT
	| ON
	| OR
	| ORDER
	| OUTER
	| POSITION
	| RIGHT
	| SELECT
	| SECOND
	| SET
	| SQRT
	| SUBSTRING
	| SUM
	| TRAILING
	| TREAT
	| UPDATE
	| UPPER
	| VALUE
	| WHERE
	| WITH
	| YEAR) {
	    logUseOfReservedWordAsIdentifier(getCurrentToken());
	}
	;

