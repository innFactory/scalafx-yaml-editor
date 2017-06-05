package de.innfactory

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.layout.StackPane
import scalafx.scene.image._
import scalafx.scene.control._
import scalafx.scene.input._


object ScalaFXYamlEditor extends JFXApp {


  val yamlPane = new YamlPane

  // Menu
  val menuBar = new MenuBar
  val editMenu = new Menu("Edit")
  val editItem = new MenuItem("Search")
  editItem.onAction = e => yamlPane.openSearch
  editItem.accelerator = new KeyCodeCombination(KeyCode.F, KeyCombination.ControlDown)
  editMenu.items = List(editItem)
  menuBar.menus = List(editMenu)
  // hide menubar: just add it for shortkey
  menuBar.visible = false




  val primaryStage = new PrimaryStage {
    title = "YAML Editor"
    scene = new Scene(800,600) {
      fill = Color.rgb(38, 38, 38)
      opacity = 0.94
      content = new StackPane {
        children = Seq(
          menuBar,
          yamlPane
        )
      }
      val css1 = scene.getClass.getClassLoader.getResource("main.css").toExternalForm
      val css2 = scene.getClass.getClassLoader.getResource("syntax.css").toExternalForm
      stylesheets.add(css1);
      stylesheets.add(css2);
    }
  }




  // make yamlPane resizeable
  yamlPane.prefWidthProperty.bind(primaryStage.scene.get.widthProperty)
  yamlPane.prefHeightProperty.bind(primaryStage.scene.get.heightProperty)


  stage = primaryStage


}
