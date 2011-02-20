package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Case expression for typeswitch.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TypeCase extends Single {
  /** Variable. */
  final Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   * @param r return expression
   */
  public TypeCase(final InputInfo ii, final Var v, final Expr r) {
    super(ii, r);
    var = v;
  }

  @Override
  public TypeCase comp(final QueryContext ctx) throws QueryException {
    return comp(ctx, null);
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param v value to be bound
   * @return resulting item
   * @throws QueryException query exception
   */
  TypeCase comp(final QueryContext ctx, final Value v) throws QueryException {
    if(var.name == null) {
      super.comp(ctx);
    } else {
      final int s = ctx.vars.size();
      ctx.vars.add(v == null ? var : var.bind(v, ctx).copy());
      super.comp(ctx);
      ctx.vars.reset(s);
    }
    type = expr.type();
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return var.name == null || !var.eq(v) ? super.count(v) : 0;
  }

  @Override
  public boolean removable(final Var v) {
    return var.name != null && var.eq(v) || super.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    return var.name != null && var.eq(v) ? this : super.remove(v);
  }

  /**
   * Evaluates the expression.
   * @param ctx query context
   * @param seq sequence to be checked
   * @return resulting item
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext ctx, final Iter seq) throws QueryException {
    if(var.type != null && !var.type.instance(seq)) return null;
    if(var.name == null) return ctx.iter(expr);

    final int s = ctx.vars.size();
    ctx.vars.add(var.bind(seq.finish(), ctx).copy());
    final ItemIter ir = ItemIter.get(ctx.iter(expr));
    ctx.vars.reset(s);
    return ir;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAR, var.name != null ? var.name.atom() :
      Token.EMPTY);
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(var.type == null ? DEFAULT : CASE);
    if(var.name != null) tb.add(' ');
    return tb.add(var + " " + RETURN + ' ' + expr).toString();
  }
}
