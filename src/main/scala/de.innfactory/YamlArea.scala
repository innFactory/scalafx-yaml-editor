package de.innfactory

import java.util

import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

import org.fxmisc.richtext.model.StyleSpan
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import java.util.{Collection, Collections, List}
import java.lang.Integer
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import scala.util.control.Breaks._
import scala.collection.mutable.TreeMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import java.util.stream.Collectors


class YamlArea extends CodeArea {

  setParagraphGraphicFactory(LineNumberFactory.get(this))
  private var styleDataList = Array[StyleData]()
  private var zIndexMap = new HashMap[String, Int]()
  private val delimiters = "\n ,;-/!%&()?\\\\'*+><|:#=[{]}~\""


  // set style rules
  addStyleRegex("keyword", "\\s[-|_|a-z|A-Z|0-9]*:", 0)
  addStyleRegex("keyword2", "&[-|_|a-z|A-Z|0-9]*|\\*[-|_|a-z|A-Z|0-9]*", 0)
  addStyleRegex("comment", "#[^\n]*", 0)
  addStyleRegex("number", "\\s[0-9-|0-9.|0-9,|0-9,.]*", 0)


  /*
   * Mark all words as these on caret position.
   */
  setOnMouseReleased(e=>{
    val caretPos = getCaretPosition

    // get word on caret pos
    val text = getText
    var word = ""
    var hasRightSide = true

    if (caretPos < text.length - 1) {
      word = text.substring(caretPos, caretPos +1)
      if (delimiters.indexOf(word.charAt(0)) != -1) {
        hasRightSide = false
        word = ""
      }
    }

    // left side of word
    breakable {for (i <- caretPos-1 to 0 by -1) {
      val s = text.substring(i, i+1)
      if (delimiters.indexOf(s.charAt(0)) == -1) {
        word = s + word
      } else {break}
    }}


    // right side of word
    if (hasRightSide) {
      breakable {for (i <- caretPos + 1 to text.length -1) {
        val s = text.substring(i, i+1)
        if (delimiters.indexOf(s.charAt(0)) == -1) {
          word = word +s
        } else {break}
      }}
    }

    println(word)

    // set style for all words
    removeStyle("caretword")
    addStyleRegex("default", "(" + word + ")", 0)
    addStyleRegex("caretword", "(" + word + ")", 0)
    redraw
    setShowCaret(org.fxmisc.richtext.GenericStyledArea.CaretVisibility.ON)

  })



  textProperty.addListener(new ChangeListener[String]() {
    override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      redraw
    }
  });

  /**
    * Add a regex expression with a style.
    *
    * @param cssClass style class name (without a leading dot)
    * @param regex the expression which will have the style
    * @param zIndex the visual hierachy of the style
    */
  def addStyleRegex(cssClass: String, string: String, zIndex: Int): Unit = {
    styleDataList = styleDataList :+ new StyleData(cssClass, Pattern.compile(string, Pattern.CASE_INSENSITIVE))
    zIndexMap = zIndexMap += (cssClass -> zIndex)
  }


  /**
    * Add a style for the position. If no endIndex is given, the position will be market until the word end.
    *
    * @param cssClass style class name (without a leading dot)
    * @param startIndex
    * @param endIndex
    * @param zIndex the visual hierachy of the style
    */
  def addStylePos(cssClass: String, startIndex: Int, endIndex: Int, zIndex: Int): Unit = {
    // add style
    for (i <- startIndex to endIndex) {
      styleDataList = styleDataList :+ new StyleData(cssClass, null, i)
    }
    zIndexMap = zIndexMap += (cssClass -> zIndex)
    redraw
  }

  def removeStyle(cssClass: String): Unit = {
    styleDataList = styleDataList.filter(!_.cssClass.equals(cssClass))
    zIndexMap = zIndexMap -= cssClass
  }



  private def computeHighlighting(text: String): StyleSpans[util.Collection[String]] = {

    var styleSpanMap: TreeMap[Int, StyleSpan[util.Collection[String]]] = TreeMap()

    // generate style spans for style data
    styleDataList.foreach((sd: StyleData) => {

      // check if regex pattern is set
      if (sd.pattern != null) {

        // apply regex pattern
        val matcher = sd.pattern.matcher(text)

        // find regex matches
        while (matcher.find) {

          // generate a span for each letter of found word
          for (i <- matcher.start to matcher.end - 1) {
            addNewSpanToMap(styleSpanMap, i, sd.cssClass)
          }

        }

      } else if (sd.pos > -1) {
        // add a span if style data pos is set
        addNewSpanToMap(styleSpanMap, sd.pos, sd.cssClass)

      }
    })


    var spansBuilder: StyleSpansBuilder[Collection[String]] = new StyleSpansBuilder[Collection[String]]

    // add empty span to avoid => java.lang.IllegalStateException: No spans have been added
    spansBuilder.add(util.Collections.emptyList(), 0)

    // compute Highlighting
    var lastKwEnd = 0
    styleSpanMap.keySet.foreach((startIndex: Int) => {
      spansBuilder.add(Collections.singleton("default"), startIndex - lastKwEnd)
      spansBuilder.add(styleSpanMap.get(startIndex).get)
      lastKwEnd = startIndex + styleSpanMap.get(startIndex).get.getLength
    })

    // clear highlighting rest of text
    spansBuilder.add(Collections.singleton("default"), text.length - lastKwEnd)


    spansBuilder.create
  }

  def setText(text: String): Unit = {
    replaceText(text)
    redraw
  }

  /**
    * Compute Highlighting again with all styles.
    */
  def redraw: Unit = {

    setStyleSpans(0, computeHighlighting(getText))
  }

  private def addNewSpanToMap(styleSpanMap: TreeMap[Int, StyleSpan[util.Collection[String]]], pos: Int, cssClass: String): Unit = {

    // check if span has already a style
    if (styleSpanMap.contains(pos)) {

      // apply zIndex hierarchy
      // get styles which are already on that span
      val oldStyles: java.util.List[String] = styleSpanMap.get(pos).get.getStyle.stream().collect(Collectors.toList());

      // get zIndex of these styles
      val oldZIndex = zIndexMap.get(oldStyles.get(0))
      // get zIndex of the new style
      val newZIndex = zIndexMap.get(cssClass)

      /*
       * Compare new and old zIndex
       * 1. if new one is higher: apply new style
       * 2. if both are equal: apply both
       * 3. if old one is higher: apply old style and ignore new one
       */
      if (newZIndex.get > oldZIndex.get) {
        // 1. if new one is higher: apply new style
        styleSpanMap += (pos -> new StyleSpan[util.Collection[String]](Collections.singleton(cssClass), 1))
      } else if (newZIndex.get == oldZIndex.get) {
        // 2. if both are equal: apply both
        // add new style class to the old styles and add them to the span
        oldStyles.add(cssClass)
        styleSpanMap += (pos -> new StyleSpan[util.Collection[String]](oldStyles, 1))
      } else if (newZIndex.get < oldZIndex.get) {
        // 3. if old one is higher: apply old style and ignore new one
        styleSpanMap += (pos -> new StyleSpan[util.Collection[String]](oldStyles, 1))
      }

    } else {
      // add the style class
      styleSpanMap += (pos -> new StyleSpan[util.Collection[String]](Collections.singleton(cssClass), 1))
    }
  }


  class StyleData(val cssClass: String, val pattern: Pattern = null, val pos: Int = -1) {}

}

