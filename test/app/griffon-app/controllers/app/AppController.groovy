package app

import java.awt.Color

class AppController {
    // these will be injected by Griffon
    def model
    def view

     void mvcGroupInit(Map args) {
        // this method is called after model and view are injected
         println "app init"
     }

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

    /*
        Remember that actions will be called outside of the UI thread
        by default. You can change this setting of course.
        Please read chapter 9 of the Griffon Guide to know more.

     */

    def doAction = { evt = null ->
        view.meuBotaoMagico.setForeground(Color.red)
        view.meuBotaoMagico.setBackground(Color.black)
        println "do action"
    }


}
