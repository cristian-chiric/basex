package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Simple date item, used for {@code xs:gYearMonth}, {@code xs:gYear},
 * {@code xs:gMonthDay}, {@code xs:gDay} and {@code xs:gMonth}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GDt extends ADate {
  /** Date pattern. */
  private static final Type[] TYPES = {
    AtomType.YEA, AtomType.YMO, AtomType.MON, AtomType.MDA, AtomType.DAY,
  };
  /** Date patterns. */
  private static final Pattern[] PATTERNS = {
    Pattern.compile("(-?)([0-9]{4})" + ZONE),
    Pattern.compile("(-?)([0-9]{4})-([0-9]{2})" + ZONE),
    Pattern.compile("--([0-9]{2})" + ZONE),
    Pattern.compile("--([0-9]{2})-([0-9]{2})" + ZONE),
    Pattern.compile("---([0-9]{2})" + ZONE)
  };
  /** Date pattern. */
  private static final String[] EXAMPLES = { XYEA, XYMO, XMON, XMDA, XDAY };
  /** Date zones. */
  private static final int[] ZONES = { 3, 4, 2, 3, 2 };

  /**
   * Constructor.
   * @param d date
   * @param t data type
   */
  public GDt(final ADate d, final Type t) {
    super(t, d);
    if(t != AtomType.YEA && t != AtomType.YMO) xc.setYear(UNDEF);
    if(t != AtomType.MON && t != AtomType.YMO && t != AtomType.MDA) xc.setMonth(UNDEF);
    if(t != AtomType.DAY && t != AtomType.MDA) xc.setDay(UNDEF);
    xc.setTime(UNDEF, UNDEF, UNDEF);
    xc.setMillisecond(UNDEF);
  }

  /**
   * Constructor.
   * @param d date
   * @param t data type
   * @param ii input info
   * @throws QueryException query exception
   */
  public GDt(final byte[] d, final Type t, final InputInfo ii) throws QueryException {
    super(t, d, EXAMPLES[type(t)], ii);

    final int i = type(t);
    final Matcher mt = PATTERNS[i].matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, EXAMPLES[i], ii);
    zone(mt, ZONES[i], d, ii);

    if(t == AtomType.MDA) {
      final int m = xc.getMonth() - 1;
      if(xc.getDay() > DAYS[m] + (m == 1 ? 1 : 0)) TIMERANGE.thrw(ii, type, d);
    }
  }

  /**
   * Returns the offset for the specified type.
   * @param type type
   * @return offset
   */
  private static int type(final Type type) {
    for(int t = 0; t < TYPES.length; ++t) if(TYPES[t] == type) return t;
    throw Util.notexpected();
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
  }
}
