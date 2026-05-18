package app

import static griffon.util.GriffonApplicationUtils.*

menuBar {
    menu(text: 'File', mnemonic: 'F') {
        menuItem(newAction)
        menuItem(openAction)
        separator()
        menuItem(saveAction)
        menuItem(saveAsAction)
        if( !isMacOSX ) {
            separator()
            menuItem(quitAction)
        }
    }

    menu(text: 'Edit', mnemonic: 'E') {
        menuItem(cutAction)
        menuItem(copyAction)
        menuItem(pasteAction)
    }

    if(!isMacOSX) {
        glue()
        menu(text: 'Help', mnemonic: 'H') {
            menuItem(aboutAction)
        }
    }
}
