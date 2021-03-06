package org.basex.test.build;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.junit.*;

/**
 * Tests for parsing XML documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XMLParserTest extends SandboxTest {
  /**
   * Tests the internal parser (Option {@link Prop#INTPARSE}).
   */
  @Test
  public void intParse() {
    context.prop.set(Prop.MAINMEM, true);
    context.prop.set(Prop.CHOP, false);

    final StringBuilder sb = new StringBuilder();

    final String[] docs = {
        "<x/>", " <x/> ", "<x></x>", "<x>A</x>", "<x><x>", "<x/><x/>", "<x></x><x/>",
        "<x>", "</x>", "<x></x></x>", "x<x>", "<x>x", "<x><![CDATA[ ]]></x>",
    };
    for(final String doc : docs) {
      // parse document with default parser (expected to yield correct result)
      context.prop.set(Prop.INTPARSE, false);
      boolean def = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        def = false;
      }

      // parse document with internal parser
      context.prop.set(Prop.INTPARSE, true);
      boolean cust = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        cust = false;
      }

      // compare results
      if(def != cust) {
        sb.append("\n").append(def ? "- not accepted: " : "- not rejected: ").append(doc);
      }
    }

    // list all errors
    if(sb.length() != 0) fail(sb.toString());
  }

  /**
   * Tests the namespace stripping option (Option {@link Prop#STRIPNS}).
   * @throws Exception exceptions
   */
  @Test
  public void parse() throws Exception {
    context.prop.set(Prop.STRIPNS, true);
    final String doc = "<e xmlns='A'><b:f xmlns:b='B'/></e>";
    for(final boolean b : new boolean[] { false, true }) {
      context.prop.set(Prop.INTPARSE, b);
      new CreateDB(NAME, doc).execute(context);
      String result = new XQuery(".").execute(context).replaceAll("[\\r\\n]+ *", "");
      assertEquals("<e><f/></e>", result);
      result = new XQuery("e/f").execute(context).replaceAll("[\\r\\n]+ *", "");
      assertEquals("<f/>", result);
    }
  }
}
