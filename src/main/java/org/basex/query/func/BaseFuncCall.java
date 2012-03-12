package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseFuncCall extends UserFuncCall {
  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   */
  public BaseFuncCall(final InputInfo ii, final QNm nm, final Expr[] arg) {
    super(ii, nm, arg);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    UserFunc fun = func;
    Value[] args = args(ctx);
    do {
      // cache arguments, evaluate function and reset variable scope
      final Value[] sf = addArgs(ctx, ii, fun.scope, fun.args, args);
      ctx.tailCalls = 0;
      try {
        return fun.item(ctx, ii);
      } catch(final Continuation c) {
        fun = c.getFunc();
        args = c.getArgs();
      } finally {
        fun.scope.exit(ctx, sf);
      }
    } while(true);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    UserFunc fun = func;
    Value[] args = args(ctx);
    do {
      // cache arguments, evaluate function and reset variable scope
      final Value[] sf = addArgs(ctx, input, fun.scope, fun.args, args);
      ctx.tailCalls = 0;
      try {
        return ctx.value(fun);
      } catch(final Continuation c) {
        fun = c.getFunc();
        args = c.getArgs();
      } finally {
        fun.scope.exit(ctx, sf);
      }
    } while(true);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // [LW] make result streamable
    return value(ctx).iter();
  }

  @Override
  public Expr markTailCalls() {
    return new TailFuncCall(input, name, func, expr);
  }
}
