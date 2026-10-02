/*
 * Executado depois que o app está pronto.
 * Com -Dgriffon.smoke=true faz um smoke test do griffon-mini e encerra o app.
 */
if (System.getProperty('griffon.smoke') == 'true') {
    def controller = app.controllers.app
    println "SMOKE addons=${app.addonManager.addons.keySet()}"
    println "SMOKE controller is GriffonController: ${controller instanceof griffon.core.GriffonController}"
    println "SMOKE model is GriffonModel: ${app.models.app instanceof griffon.core.GriffonModel}"
    println "SMOKE controller.log: ${controller.log?.name}"
    println "SMOKE windows=${app.windowManager.windows.size()}"
    println "SMOKE OK"
    app.shutdown()
}
