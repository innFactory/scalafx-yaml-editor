package de.innfactory

import org.fxmisc.flowless.VirtualizedScrollPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.StackPane
import org.controlsfx.control.textfield.CustomTextField
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import scalafx.geometry.Insets;
import scalafx.geometry.Pos;
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.input.KeyCode
import javafx.css.PseudoClass
import java.util.regex.Pattern
import scalafx.scene.paint.Color._

class YamlPane() extends StackPane {


  val yamlArea = new YamlArea
  var searchFocusLastIndex: Int = -1
  // search text field
  val searchTextField = new CustomTextField
  searchTextField.setPromptText("Search")
  // handle key events
  searchTextField.setOnKeyReleased(new EventHandler[KeyEvent]() {
    override def handle(e: KeyEvent): Unit = {
      // if enter is pressed, search focus is moving
      if (e.isShiftDown && e.getCode == KeyCode.ENTER) {
        searchUp
        return
      }
      else if (e.getCode == KeyCode.ENTER) {
        searchDown
        return
      }
      else if (e.getCode == KeyCode.ESCAPE) {
        cancelSearch
        return
      } else if (e.getCode == KeyCode.SHIFT) {
        return
      }

      // clear focus and styles
      searchFocusLastIndex = -1;
      yamlArea.removeStyle("searchFocus")
      yamlArea.removeStyle("search")

      // search function if text is containing the string
      if (yamlArea.getText.toLowerCase.contains(searchTextField.getText.toLowerCase)) {
        // search
        yamlArea.addStyleRegex("search", Pattern.quote(searchTextField.getText), 1)
        // clear text field style
        searchTextField.pseudoClassStateChanged(PseudoClass.getPseudoClass("not-found"), false)
      } else {
        // not found: set text field style
        searchTextField.pseudoClassStateChanged(PseudoClass.getPseudoClass("not-found"), true)
      }

      // execute highlighting
      yamlArea.redraw

    }
  })



  // search icon
  val searchIcon = new FontAwesomeIconView
  searchIcon.setStyleClass("search-icon")
  searchTextField.setLeft(searchIcon)


  // up icon
  val upIcon = new FontAwesomeIconView
  upIcon.setStyleClass("chevron-up")
  upIcon.setOnMousePressed(e => searchUp)

  // down icon
  val downIcon = new FontAwesomeIconView
  downIcon.setStyleClass("chevron-down")
  downIcon.setOnMousePressed(e => searchDown)

  // cancel icon
  val cancelIcon = new FontAwesomeIconView
  cancelIcon.setStyleClass("cancel-icon")
  cancelIcon.setOnMousePressed(e => cancelSearch)


  // search pane
  val searchPane = new HBox {
    alignmentInParent = Pos.TopRight
  }
  searchPane.styleClass += "search-pane"
  StackPane.setMargin(searchPane, new Insets(new javafx.geometry.Insets(0, 20, 0, 0)))

  searchPane.children.add(searchTextField)
  searchPane.children.add(upIcon)
  searchPane.children.add(downIcon)
  searchPane.children.add(cancelIcon)
  // add sql area and wrap it in a richTextFX-specific-scrollPane
  children.add(new VirtualizedScrollPane(yamlArea))
  // add searchPane
  children.add(searchPane)


  /**
    * Moves search focus up
    */
  private def searchUp = {
    var searchFocusLastIndex_reverse = searchFocusLastIndex - searchTextField.getText.length

    if (searchFocusLastIndex_reverse <= 0) searchFocusLastIndex_reverse = yamlArea.getText.length

    val startIndex = yamlArea.getText.toLowerCase.lastIndexOf(searchTextField.getText.toLowerCase, searchFocusLastIndex_reverse)
    setSearchFocus(startIndex)
  }


  /**
    * Moves search focus down
    */
  private def searchDown = {
    val startIndex = yamlArea.getText.toLowerCase.indexOf(searchTextField.getText.toLowerCase, searchFocusLastIndex + 1)
    setSearchFocus(startIndex)
  }


  /**
    * focus the search string on startIndex.
    * - highlight it with css
    * - select it
    * - scroll to it
    *
    * @param startIndex
    */
  private def setSearchFocus(startIndex: Int) = {


    // initiate endIndex with startIndex
    var endIndex = startIndex

    if (startIndex > -1) {
      // the usual case: endIndex is the end of the word in search textField
      endIndex = startIndex + searchTextField.getText.length - 1
    }

    // remember the endIndex
    searchFocusLastIndex = endIndex

    // clear old styling
    yamlArea.removeStyle("searchFocus")
    // set new style
    yamlArea.addStylePos("searchFocus", startIndex, endIndex, 2)
    yamlArea.redraw

    // select text and scroll to it
    yamlArea.selectRange(startIndex, endIndex + 1)
    yamlArea.requestFollowCaret

  }


  /**
    * show searchPane
    */
  def openSearch = {
    searchPane.setVisible(true)
    searchTextField.requestFocus
  }


  /**
    * reset search and hide searchPane
    */
  private def cancelSearch = {
    searchTextField.clear
    // clear text field style
    searchTextField.pseudoClassStateChanged(PseudoClass.getPseudoClass("not-found"), false)
    searchFocusLastIndex = -1
    yamlArea.removeStyle("searchFocus")
    yamlArea.removeStyle("search")
    yamlArea.redraw
    searchPane.setVisible(false)
  }

}