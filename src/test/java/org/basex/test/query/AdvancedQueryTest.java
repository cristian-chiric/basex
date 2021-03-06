package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.test.*;
import org.basex.util.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /**
   * Runs the specified query.
   * @param query query string
   * @return result
   */
  protected static String query(final String query) {
    final QueryProcessor qp = new QueryProcessor(query, context);
    qp.ctx.sc.baseURI(".");
    try {
      return qp.execute().toString().replaceAll("(\\r|\\n)+ *", "");
    } catch(final QueryException ex) {
      fail("Query failed:\n" + query + "\nMessage: " + ex.getMessage());
      return null;
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param result query result
   */
  protected static void query(final String query, final Object result) {
    final String res = query(query);
    final String exp = result.toString();
    if(!res.equals(exp))
      fail("Wrong result:\n[Q] " + query + "\n[E] \u00bb" + result +
          "\u00ab\n[F] \u00bb" + res + '\u00ab');
  }

  /**
   * Checks if a query yields the specified result.
   * @param query query string
   * @param result query result
   */
  protected static void contains(final String query, final String result) {
    final String res = query(query);
    if(!res.contains(result))
      fail("Result does not contain \"" + result + "\":\n" + query + "\n[E] " +
          result + "\n[F] " + res);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error expected error
   */
  protected static void error(final String query, final Err... error) {
    final QueryProcessor qp = new QueryProcessor(query, context);
    qp.ctx.sc.baseURI(".");
    try {
      final String res = qp.execute().toString().replaceAll("(\\r|\\n) *", "");
      fail("Query did not fail:\n" + query + "\n[E] " +
          error[0] + "...\n[F] " + res);
    } catch(final QueryException ex) {
      check(ex, error);
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param ex exception
   * @param error expected errors
   */
  protected static void check(final QueryException ex, final Err... error) {
    if(error.length == 0) Util.notexpected("No error code specified");
    final byte[] msg = Token.token(ex.getMessage());
    boolean found = false;
    for(final Err e : error) found |= Token.contains(msg, e.qname().local());
    if(!found) fail('\'' + Token.string(error[0].qname().string()) +
        "' not contained in '" + Token.string(msg) + "'.");
  }

  /**
   * Returns serialization parameters.
   * @param arg serialization arguments
   * @return parameter string
   */
  protected static String serialParams(final String arg) {
    return "<serialization-parameters " +
      "xmlns='http://www.w3.org/2010/xslt-xquery-serialization'>" + arg +
      "</serialization-parameters>";
  }
}
