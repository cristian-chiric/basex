package org.deepfs.fsml.parsers;

import java.io.IOException;
import java.util.TreeMap;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.IOContent;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.FileType;
import org.deepfs.fsml.util.MimeType;
import org.deepfs.fsml.util.ParserException;
import org.deepfs.fsml.util.ParserRegistry;

/**
 * Parser for XML files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class XMLParser implements IFileParser {

  /** Suffixes of all file formats, this parser is able to parse. */
  private static final TreeMap<String, MimeType> SUFFIXES =
      new TreeMap<String, MimeType>();

  static {
    SUFFIXES.put("xml", MimeType.XML);
    SUFFIXES.put("kml", MimeType.KML);
    SUFFIXES.put("rng", MimeType.XML);
    SUFFIXES.put("webloc", MimeType.XML);
    SUFFIXES.put("mailtoloc", MimeType.XML);
    for(final String suf : SUFFIXES.keySet())
      ParserRegistry.register(suf, XMLParser.class);
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    return parse(f) != null ? true : false;
  }

  /**
   * Checks if the document is well-formed and returns the corresponding main
   * memory database.
   * @param f the {@link BufferedFileChannel} to read the xml document from
   * @return the main memory database or <code>null</code> if the document is
   *         not wellformed.
   * @throws IOException if any error occurs
   */
  public Data parse(final BufferedFileChannel f) throws IOException {
    if(f.size() > Integer.MAX_VALUE) throw new IOException(
        "Input file too big.");
    final byte[] data = new byte[(int) f.size()];
    f.get(data);
    try {
      final Parser p = Parser.xmlParser(new IOContent(data), new Prop());
      return new MemBuilder(p).build();
    } catch(final IOException ex) {
      // XML parsing exception...
      return null;
    }
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();
    final int maxSize = deepFile.fstextmax;
    if(bfc.size() <= maxSize) {
      final Data data = parse(bfc);
      if(data != null) {
        if(deepFile.fsmeta) {
          deepFile.setFileType(FileType.XML);
          final String name = bfc.getFileName();
          final String suf = name.substring(
              name.lastIndexOf('.') + 1).toLowerCase();
          final MimeType mime = SUFFIXES.get(suf);
          if(mime == null) Main.notexpected();
          deepFile.setFileFormat(mime);
        }
        if(deepFile.fsxml) {
          deepFile.addXML(bfc.getOffset(), (int) bfc.size(), data);
        }
        return; // successfully parsed xml content
      }
    }
    if(deepFile.fscont) { // if file too big or not wellformed
      try {
        deepFile.fallback();
      } catch(final ParserException e) {
        Main.debug("Failed to read text content from xml file with " +
            "fallback parser (%).", e);
      }
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}