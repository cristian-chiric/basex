package org.basex.query.ft;

import java.util.ArrayList;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;

/**
 * Simple Thesaurus entry for full-text requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ThesQuery {
  /** Thesaurus root references. */
  private final ArrayList<Thesaurus> thes = new ArrayList<Thesaurus>(1);

  /**
   * Adds two thesaurus definitions.
   * @param th second thesaurus
   */
  public void add(final Thesaurus th) {
    thes.add(th);
  }

  /**
   * Merges two thesaurus definitions.
   * @param th second thesaurus
   */
  public void merge(final ThesQuery th) {
    for(final Thesaurus t : th.thes) {
      boolean f = false;
      for(final Thesaurus tt : thes) f |= tt.sameAs(t);
      if(!f) thes.add(t);
    }
  }

  /**
   * Finds a thesaurus term.
   * @param ii input info
   * @param ft token
   * @return result list
   * @throws QueryException query exception
   */
  byte[][] find(final InputInfo ii, final byte[] ft) throws QueryException {
    final TokenList tl = new TokenList();
    for(final Thesaurus th : thes) th.find(ii, tl, ft);
    return tl.toArray();
  }
}
