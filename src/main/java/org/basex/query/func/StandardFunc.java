package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.nio.charset.*;
import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Standard (built-in) functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function signature. */
  Function sig;

  /**
   * Constructor.
   * @param ii input info
   * @param s function definition
   * @param args arguments
   */
  StandardFunc(final InputInfo ii, final Function s, final Expr... args) {
    super(ii, args);
    sig = s;
    type = sig.ret;
  }

  @Override
  public final Expr compile(final QueryContext ctx) throws QueryException {
    // compile all arguments
    super.compile(ctx);
    // skip context-based or non-deterministic functions, and non-values
    return optPre(uses(Use.CTX) || uses(Use.NDT) || !allAreValues() ? comp(ctx) :
      sig.ret.zeroOrOne() ? item(ctx, info) : value(ctx), ctx);
  }

  /**
   * Performs function specific compilations.
   * @param ctx query context
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  Expr comp(final QueryContext ctx) throws QueryException {
    return this;
  }

  /**
   * Atomizes the specified item.
   * @param it input item
   * @param ii input info
   * @return atomized item
   * @throws QueryException query exception
   */
  public static final Item atom(final Item it, final InputInfo ii) throws QueryException {
    final Type ip = it.type;
    return it instanceof ANode ? ip == NodeType.PI || ip == NodeType.COM ?
        Str.get(it.string(ii)) : new Atm(it.string(ii)) : it;
  }

  @Override
  public final boolean isFunction(final Function f) {
    return sig == f;
  }

  @Override
  public final boolean isVacuous() {
    return !uses(Use.UPD) && type == SeqType.EMP;
  }

  @Override
  public final String description() {
    return sig.toString();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, sig.desc), expr);
  }

  @Override
  public final String toString() {
    final String desc = sig.toString();
    return new TokenBuilder(desc.substring(0,
        desc.indexOf('(') + 1)).addSep(expr, SEP).add(PAR2).toString();
  }

  /**
   * Returns a data instance for the specified argument.
   * @param i index of argument
   * @param ctx query context
   * @return data instance
   * @throws QueryException query exception
   */
  Data data(final int i, final QueryContext ctx) throws QueryException {
    final Item it = checkNoEmpty(expr[i].item(ctx, info));
    final Type ip = it.type;
    if(it instanceof ANode) return checkDBNode(it).data;
    if(it instanceof AStr)  {
      final String name = string(it.string(info));
      if(!MetaData.validName(name, false)) INVDB.thrw(info, name);
      return ctx.resource.data(name, info);
    }
    throw STRNODTYPE.thrw(info, this, ip);
  }

  /**
   * Returns a normalized encoding representation.
   * @param i index of encoding argument
   * @param err error for invalid encoding
   * @param ctx query context
   * @return text entry
   * @throws QueryException query exception
   */
  public String encoding(final int i, final Err err, final QueryContext ctx)
      throws QueryException {
    if(i >= expr.length) return null;
    final String enc = string(checkStr(expr[i], ctx));
    if(!Charset.isSupported(enc)) err.thrw(info, enc);
    return normEncoding(enc);
  }

  /**
   * Returns all keys and values of the specified binding argument.
   * @param i index of argument
   * @param ctx query context
   * @return resulting map
   * @throws QueryException query exception
   */
  HashMap<String, Value> bindings(final int i, final QueryContext ctx)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<String, Value>();
    if(i < expr.length) {
      final Map map = checkMap(expr[i].item(ctx, info));
      for(final Item it : map.keys()) {
        byte[] key;
        if(it instanceof Str) {
          key = it.string(null);
        } else {
          final QNm qnm = (QNm) checkType(it, AtomType.QNM);
          final TokenBuilder tb = new TokenBuilder();
          if(qnm.uri() != null) tb.add('{').add(qnm.uri()).add('}');
          key = tb.add(qnm.local()).finish();
        }
        hm.put(string(key), map.get(it, info));
      }
    }
    return hm;
  }

  /**
   * Compares several signatures for equality.
   * @param sig signature to be found
   * @param sigs signatures to be compared
   * @return result of check
   */
  protected static boolean oneOf(final Function sig, final Function... sigs) {
    for(final Function s : sigs) if(sig == s) return true;
    return false;
  }
}
