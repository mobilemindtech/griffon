package app

import app.ContentView


build(Actions)
application(
    title: 'MyApp',
    preferredSize: [320, 240],
    pack: true,
    //location: [50,50],
    locationByPlatform: true,
    iconImage:   imageIcon('/griffon-icon-48x48.png').image,
    iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                 imageIcon('/griffon-icon-32x32.png').image,
                 imageIcon('/griffon-icon-16x16.png').image]) {
    //migLayout(layoutConstraints: 'fill')
    def w  = build(ContentView)
    menuBar(build(MenuBar))
    //menuBar(menuBar {
    //  menu(text: 'File', mnemonic: 'F'){}
    //})
    widget(w)

    // Acesso direto pelo ID em qualquer lugar do script da View
    doLater {
        //meuBotaoMagico.enabled = false
    }
}
