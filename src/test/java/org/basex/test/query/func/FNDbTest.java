package org.basex.test.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNDbTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";
  /** Test folder. */
  private static final String FLDR = "src/test/resources/dir/";
  /** Number of XML files for folder. */
  private static final int NFLDR;

  static {
    int fc = 0;
    for(final IOFile c : new IOFile(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
    NFLDR = fc;
  }

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(NAME, FILE).execute(context);
  }

  /**
   * Finishes the test.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void open() throws BaseXException {
    query(COUNT.args(_DB_OPEN.args(NAME)), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "")), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");

    // close database instance
    new Close().execute(context);
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");
    query(_DB_OPEN.args(NAME) + "//title/text()", "XML");

    // run function on non-existing database
    new DropDB(NAME).execute(context);
    error(_DB_OPEN.args(NAME), Err.BXDB_OPEN);
  }

  /** Test method. */
  @Test
  public void openPre() {
    query(_DB_OPEN_PRE.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_PRE.args(NAME, -1), Err.BXDB_RANGE);
  }

  /** Test method. */
  @Test
  public void openId() {
    query(_DB_OPEN_ID.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_ID.args(NAME, -1), Err.BXDB_RANGE);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void text() throws BaseXException {
    // run function without and with index
    new DropIndex(Commands.CmdIndex.TEXT).execute(context);
    query(_DB_TEXT.args(NAME, "XML"), "XML");
    new CreateIndex(Commands.CmdIndex.TEXT).execute(context);
    query(_DB_TEXT.args(NAME, "XML"), "XML");
    query(_DB_TEXT.args(NAME, "XXX"), "");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void textRange() throws BaseXException {
    // run function without and with index
    new DropIndex(Commands.CmdIndex.TEXT).execute(context);
    query(_DB_TEXT_RANGE.args(NAME, "Exercise", "Fun"), "Exercise 1Exercise 2");
    new CreateIndex(Commands.CmdIndex.TEXT).execute(context);
    query(_DB_TEXT_RANGE.args(NAME, "Exercise", "Fun"), "Exercise 1Exercise 2");
    query(_DB_TEXT_RANGE.args(NAME, "XXX", "XXX"), "");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void attribute() throws BaseXException {
    // run function without and with index
    new DropIndex(Commands.CmdIndex.ATTRIBUTE).execute(context);
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), "0");
    new CreateIndex(Commands.CmdIndex.ATTRIBUTE).execute(context);
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "id")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "XXX")), "");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "XXX")), "");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void attributeRange() throws BaseXException {
    // run function without and with index
    new CreateIndex(Commands.CmdIndex.ATTRIBUTE).execute(context);
    query(_DB_ATTRIBUTE_RANGE.args(NAME, "0", "9") + "/data()", "0 1");
    new CreateIndex(Commands.CmdIndex.ATTRIBUTE).execute(context);
    query(_DB_ATTRIBUTE_RANGE.args(NAME, "0", "9") + "/data()", "0 1");
    query(_DB_ATTRIBUTE_RANGE.args(NAME, "XXX", "XXX"), "");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void fulltext() throws BaseXException {
    // run function without and with index
    new DropIndex(Commands.CmdIndex.FULLTEXT).execute(context);
    error(_DB_FULLTEXT.args(NAME, "assignments"), Err.BXDB_INDEX);
    new CreateIndex(Commands.CmdIndex.FULLTEXT).execute(context);
    query(_DB_FULLTEXT.args(NAME, "assignments"), "Assignments");
    query(_DB_FULLTEXT.args(NAME, "XXX"), "");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void list() throws BaseXException {
    // add documents
    new Add("test/docs", FLDR).execute(context);
    contains(_DB_LIST.args(NAME), "test/");
    // create two other database and compare substring
    new CreateDB(NAME + 1).execute(context);
    new CreateDB(NAME + 2).execute(context);
    contains(_DB_LIST.args(), NAME + 1 + ' ' + NAME + 2);
    new DropDB(NAME + 1).execute(context);
    new DropDB(NAME + 2).execute(context);
  }

  /** Test method. */
  @Test
  public void listDetails() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));

    final String xmlCall = _DB_LIST_DETAILS.args(NAME, "xml");
    query(xmlCall + "/@raw/data()", "false");
    query(xmlCall + "/@content-type/data()", MimeTypes.APP_XML);
    query(xmlCall + "/@modified-date/xs:dateTime(.)");
    query(xmlCall + "/@size/data()", "");
    query(xmlCall + "/text()", "xml");

    final String rawCall = _DB_LIST_DETAILS.args(NAME, "raw");
    query(rawCall + "/@raw/data()", "true");
    query(rawCall + "/@content-type/data()", MimeTypes.APP_OCTET);
    query(rawCall + "/@modified-date/xs:dateTime(.) > " +
        "xs:dateTime('1971-01-01T00:00:01')", "true");
    query(rawCall + "/@size/data()", "3");
    query(rawCall + "/text()", "raw");

    query(_DB_LIST_DETAILS.args(NAME, "test"), "");
    error(_DB_LIST_DETAILS.args("mostProbablyNotAvailable"), Err.BXDB_OPEN);
  }

  /** Test method. */
  @Test
  public void system() {
    contains(_DB_SYSTEM.args(), Prop.VERSION);
  }

  /** Test method. */
  @Test
  public void info() {
    query("count(" + _DB_INFO.args(NAME) + "//" +
        SIZE.replaceAll(" |-", "").toLowerCase(Locale.ENGLISH) + ')', 1);
  }

  /** Test method. */
  @Test
  public void nodeID() {
    query(_DB_NODE_ID.args(" /html"), "1");
    query(_DB_NODE_ID.args(" / | /html"), "0 1");
  }

  /** Test method. */
  @Test
  public void nodePre() {
    query(_DB_NODE_PRE.args(" /html"), "1");
    query(_DB_NODE_PRE.args(" / | /html"), "0 1");
  }

  /** Test method. */
  @Test
  public void event() {
    error(_DB_EVENT.args("X", "Y"), Err.BXDB_EVENT);
  }

  /** Test method. */
  @Test
  public void output() {
    query(_DB_OUTPUT.args("x"), "x");
    query(_DB_OUTPUT.args("('x','y')"), "x y");
    query(_DB_OUTPUT.args("<a/>"), "<a/>");
    error(_DB_OUTPUT.args("x") + ",1", Err.UPNOT);
    error(_DB_OUTPUT.args(" count#1"), Err.FIVALUE);
  }

  /** Test method. */
  @Test
  public void add() {
    query(_DB_ADD.args(NAME, "\"<root/>\"", "t1.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t1.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " document { <root/> }", "t2.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t2.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " document { <root/> }", "test/t3.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t3.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, FILE, "in/"));
    query(COUNT.args(COLLECTION.args(NAME + "/in/input.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, FILE, "test/t4.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t4.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, FLDR, "test/dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/dir")), NFLDR);

    query("for $f in " + _FILE_LIST.args(FLDR, "true()", "*.xml") +
        " return " + _DB_ADD.args(NAME, " '" + FLDR + "' || $f", "dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/dir")), NFLDR);

    query("for $i in 1 to 3 return " +
        _DB_ADD.args(NAME, "\"<root/>\"", "\"doc\" || $i"));
    query(COUNT.args(" for $i in 1 to 3 return " +
        COLLECTION.args('"' + NAME + "/doc\" || $i")), 3);
  }

  /** Test method. */
  @Test
  public void addWithNS() {
    query(_DB_ADD.args(NAME, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void delete() throws BaseXException {
    new Add("test/docs", FLDR).execute(context);
    query(_DB_DELETE.args(NAME, "test"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void rename() throws BaseXException {
    new Add("test/docs", FLDR).execute(context);
    query(COUNT.args(COLLECTION.args(NAME + "/test")), NFLDR);

    // rename document
    query(_DB_RENAME.args(NAME, "test", "newtest"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
    query(COUNT.args(COLLECTION.args(NAME + "/newtest")), NFLDR);

    // rename binary file
    query(_DB_STORE.args(NAME, "one", ""));
    query(_DB_RENAME.args(NAME, "one", "two"));
    query(_DB_RETRIEVE.args(NAME, "two"));
    error(_DB_RETRIEVE.args(NAME, "one"), Err.WHICHRES);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void replace() throws BaseXException {
    new Add("test", FILE).execute(context);

    query(_DB_REPLACE.args(NAME, FILE, "\"<R1/>\""));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 1);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 0);

    query(_DB_REPLACE.args(NAME, FILE, " document { <R2/> }"));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 1);

    query(_DB_REPLACE.args(NAME, FILE, FILE));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/html"), 1);
  }

  /** Test method. */
  @Test
  public void optimize() {
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_OPTIMIZE.args(NAME, "true()"));
  }

  /** Test method. */
  @Test
  public void retrieve() {
    error(_DB_RETRIEVE.args(NAME, "raw"), Err.WHICHRES);
    query(_DB_STORE.args(NAME, "raw", "xs:hexBinary('41')"));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw") + ')', "41");
    query(_DB_DELETE.args(NAME, "raw"));
    error(_DB_RETRIEVE.args(NAME, "raw"), Err.WHICHRES);
  }

  /** Test method. */
  @Test
  public void store() {
    query(_DB_STORE.args(NAME, "raw1", "xs:hexBinary('41')"));
    query(_DB_STORE.args(NAME, "raw2", "b"));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw2") + ')', "62");
    query(_DB_STORE.args(NAME, "raw3", 123));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw3") + ')', "313233");
  }

  /** Test method. */
  @Test
  public void flush() {
    query(_DB_FLUSH.args(NAME));
    error(_DB_FLUSH.args(NAME + 'x'), Err.BXDB_OPEN);
  }

  /** Test method. */
  @Test
  public void isRaw() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_RAW.args(NAME, "xml"), "false");
    query(_DB_IS_RAW.args(NAME, "raw"), "true");
    query(_DB_IS_RAW.args(NAME, "xxx"), "false");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void exists() throws BaseXException {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "x/xml"));
    query(_DB_STORE.args(NAME, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(_DB_EXISTS.args(NAME), "true");
    query(_DB_EXISTS.args(NAME, "x/xml"), "true");
    query(_DB_EXISTS.args(NAME, "x/raw"), "true");
    query(_DB_EXISTS.args(NAME, "xxx"), "false");
    query(_DB_EXISTS.args(NAME, "x"), "false");
    query(_DB_EXISTS.args(NAME, ""), "false");
    // false expected for missing database
    new DropDB(NAME).execute(context);
    query(_DB_EXISTS.args(NAME), "false");
  }

  /** Test method. */
  @Test
  public void isXML() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_XML.args(NAME, "xml"), "true");
    query(_DB_IS_XML.args(NAME, "raw"), "false");
    query(_DB_IS_XML.args(NAME, "xxx"), "false");
  }

  /** Test method. */
  @Test
  public void contentType() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_CONTENT_TYPE.args(NAME, "xml"), MimeTypes.APP_XML);
    query(_DB_CONTENT_TYPE.args(NAME, "raw"), MimeTypes.APP_OCTET);
    error(_DB_CONTENT_TYPE.args(NAME, "test"), Err.WHICHRES);
  }
}
