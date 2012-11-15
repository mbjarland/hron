package org.m3.hron

/**
 * Created with IntelliJ IDEA.
 * User: mbjarland
 * Date: 11/13/12
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
class MapBuildingHronVisitor implements HronVisitor {
  Map<String, Object> map = [:]
  Map<Object, Object> lastInserted = [:]

  private insertOrArrayify(parent, child, String propertyName) {
    Map parentMap = (parent == null) ? map : parent as Map

    if (propertyName == '') {
      String lastKey = lastInserted[parent]
      def last = parentMap[lastKey]
      if (last instanceof List) {
        last << child
      } else {
        parentMap[lastKey] = [last, child]
      }
    } else {
      parentMap[propertyName] = child
      lastInserted[parent] = propertyName
    }
  }

  @Override
  Object objectPropertyVisitStarted(Object parent, String propertyName) {
    Map<String, Object> child = [:]

    insertOrArrayify(parent, child, propertyName)

    child
  }


  @Override
  void objectPropertyVisitEnded(Object parent, String propertyName, Object child) {
    //do nothing for now
  }

  @Override
  Appendable stringPropertyVisitStarted(Object parent, String propertyName) {
    new StringBuilder()
  }

  @Override
  void stringPropertyVisitEnded(Object parent, String propertyName, Appendable propertyValue) {
    String child = propertyValue.toString()

    insertOrArrayify(parent, child, propertyName)
  }

  @Override
  void error(Object parent, long line, int column, HronParseException error) {
    //do nothing for now
  }
}
