/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * API for parsing HQL/JPQL queries and JPA Criteria queries into Semantic Query Model (SQM) representation.  The
 * main entry point into the parsing is {@link org.hibernate.query.parser.SemanticQueryInterpreter}.
 * <p/>
 * For HQL/JPQL parsing, pass in the query string and a {@link org.hibernate.query.parser.ConsumerContext} and get
 * back the semantic query tree as a {@link org.hibernate.sqm.query.Statement}.
 * <p/>
 * For Criteria queries ...
 * <p/>
 * Generally, the parser will throw exceptions as one of 2 types:<ul>
 *     <li>
 *         {@link org.hibernate.sqm.query.QueryException} and derivatives represent problems with the
 *         query itself.
 *     </li>
 *     <li>
 *         {@link org.hibernate.query.parser.ParsingException} and derivatives represent errors (potential bugs)
 *         during parsing.
 *     </li>
 * </ul>
 */
package org.hibernate.query.parser;
