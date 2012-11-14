package org.m3.hron

import groovy.io.LineColumnReader

class HronParser {
  private LineColumnReader reader

  /**
   * Parse a text representation of a HRON data structure
   *
   * @param text HRON text to parse
   * @return a data structure of lists and maps
   */
  public Object parseText(String text) {
    if (!text) throw new IllegalArgumentException("The HRON input text should neither be null nor empty.");

    return parse(new LineColumnReader(new StringReader(text)))
  }

  /**
   * Parse a text representation of a HRON data structure. By default HronParser
   * uses an internal visitor which builds up the Map<String, Object> result,
   * this method allows you to provide your own.
   *
   * @param text HRON text to parse
   * @param visitor a custom visitor which should be called for the detected elements in the text
   * @return a data structure of lists and maps
   */
  public void parseText(String text, HronVisitor visitor) {
    if (!text) throw new IllegalArgumentException("The HRON input text should neither be null nor empty.");

    parseWithVisitor(new LineColumnReader(new StringReader(text)), visitor)
  }


  /**
   * Parse a HRON data structure from content from a reader
   *
   * @param reader reader over a HRON content
   * @return a data structure of lists and maps
   */
  public Object parse(Reader reader) {
    Map<String, Object> result = [:]
    HronVisitor visitor = new MapBuildingHronVisitor(map: result)

    parseWithVisitor(reader, visitor)

    result
  }

  /**
   * Parse a HRON data structure from content from a reader. By default HronParser
   * uses an internal visitor which builds up the Map<String, Object> result,
   * this method allows you to provide your own.
   *
   * @param reader reader over a HRON content
   * @param visitor a custom visitor which should be called for the elements detected
   * @return a data structure of lists and maps
   */
  public void parse(Reader reader, HronVisitor visitor) {
    parseWithVisitor(reader, visitor)
  }


  /**
   * Parse a HRON data structure from content from a reader
   *
   * @param reader reader over a HRON content
   * @return a data structure of lists and maps
   */
  private void parseWithVisitor(Reader reader, HronVisitor visitor) {
    this.reader = (reader instanceof LineColumnReader) ? reader : new LineColumnReader(reader)

    HronParseState state = new HronParseState(visitor: visitor)
    state.open()

    reader.eachLine { String line ->
      parseLine(line, state)
    }

    state.close()
  }

  private void parseLine(String line, HronParseState state) {
    //ignore empty/null lines and lines starting with the comment character
    if (!line || line[0] == '#') return

    List<Character> whole = line.chars.collect { it as Character }
    List<Character> head = whole.take(state.currentIndent).takeWhile { it == '\t' }

    int indent = head.size()
    List<Character> tail = whole.drop(indent)

    //Ignore lines which are all tabs and too short to reach the current indent
    if (!tail) return

    Character pivot = tail.first()
    String rest = (tail.size() < 2) ? "" : line[(indent+1)..-1]
    def location = { "[${reader.line}, ${indent+1}]" }

    try {
      switch(pivot) {
        case '#':
          break

        case '=':
          if (indent > state.currentIndent) throw new HronParseException("Invalid indent $indent at line ${location()}")
          if (indent < state.currentIndent) state.popUntilIndent(indent)

          state.openString rest
          break

        case '@':
          if (indent > state.currentIndent) throw new HronParseException("Invalid indent $indent at ${location()}")
          if (indent < state.currentIndent) state.popUntilIndent(indent)

          state.openObject rest
          break

        case '\t':
          //for string data
          if (state.currentString == null) throw new HronParseException("String data encountered even though no string has been opened at ${location()}")
          if (indent != state.currentIndent) throw new HronParseException("Invalid indent $indent at ${location()}, expected ${state.currentIndent+1}")

          state.currentString << rest
          break

        default:
          throw new HronParseException("Invalid character '$pivot' encountered at ${location()}")
      }
    } catch (HronParseException e) {
      state.visitor.error(state.currentObject, reader.line, indent+1, e)
      throw e
    }
 }
}