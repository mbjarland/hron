package org.m3.hron

/**
 * Created with IntelliJ IDEA.
 * User: mbjarland
 * Date: 11/13/12
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 */
class HronParseState {
  Stack<HronObject> objects = []
  HronString currentString
  HronVisitor visitor

  void open() {
    objects << new HronObject(indent: -1)
  }

  void close() {
    closeUntilIndent(0)
  }

  boolean arrayIsOk() {
    currentObject.hasChildren
  }

  HronObject getCurrentObject() {
    objects.peek()
  }

  int getCurrentIndent() {
    currentObject.indent + 1
  }

  void openString(String propertyName) {
    closeString()

    Appendable data = visitor.stringPropertyVisitStarted(currentObject.object, propertyName)

    currentObject.hasChildren = true
    currentString = new HronString(parent: currentObject.object, propertyName: propertyName, indent: currentIndent, data: data)
  }

  void closeString() {
    if (currentString == null) return

    visitor.stringPropertyVisitEnded(currentObject.object, currentString.propertyName, currentString.data)
    currentString = null
  }

  void openObject(String objectName) {
    closeString()
    Object child = visitor.objectPropertyVisitStarted(currentObject.object, objectName)

    currentObject.hasChildren = true
    objects << new HronObject(parent: currentObject.object, propertyName: objectName, indent: currentIndent, object: child)
  }

  void closeUntilIndent(int indent) {
    closeString()
    while (indent < currentIndent) {
      visitor.objectPropertyVisitEnded(currentObject.parent, currentObject.propertyName, currentObject.object)
      objects.pop()
    }
  }

}
