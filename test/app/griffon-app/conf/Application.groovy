application {
    title = 'App'
    startupGroups = ['app']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "app"
    'app' {
        model      = 'app.AppModel'
        view       = 'app.AppView'
        controller = 'app.AppController'
    }

}
