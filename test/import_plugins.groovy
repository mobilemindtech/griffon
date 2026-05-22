import java.nio.file.Paths
import java.util.regex.Matcher

// 1. Definição de Caminhos e Variáveis
def userHome = System.getProperty("user.home")
// Altere o caminho se sua pasta .griffon estiver em outro diretório
def griffonPluginsDir = Paths.get(userHome, ".griffon").toFile()
def groupId = "org.codehaus.griffon.plugins"

if (!griffonPluginsDir.exists()) {
    println "❌ Pasta de plugins do Griffon não encontrada em: ${griffonPluginsDir.absolutePath}"
    return
}

println "🔍 Varrendo plugins e indexando Binários, Sources e Javadocs..."
println "📁 Origem: ${griffonPluginsDir.absolutePath}\n"

// Mapa para agrupar os componentes de cada artefato antes de rodar o Maven
// Estrutura: [ "griffon-pluginname-version" : [ main: File, sources: File, javadoc: File ] ]
def artefatosAgrupados = [:]

// 2. Primeira Passada: Escanear e Agrupar os arquivos relacionados
griffonPluginsDir.eachFileRecurse { File file ->
    if (file.isFile() && file.name.endsWith(".jar")) {

        // Foca nos JARs oficiais de distribuição/plugins, ignorando pastas temporárias de build interno
        if (file.absolutePath.contains("dist") || file.absolutePath.contains("plugin") || file.absolutePath.contains("addon") || file.absolutePath.contains("doc")) {

            // Regex para capturar o padrão: nome-do-plugin e versão, identificando se é -sources ou -javadoc
            // Casos: griffon-swing-1.4.0.jar, griffon-swing-1.4.0-sources.jar, griffon-swing-1.4.0-javadoc.jar
            def matcher = file.name =~ /^griffon-(.+?)-(\d+\.\d+.*?)(?:-(sources|javadoc))?\.jar$/

            if (matcher.matches()) {
                def pluginName = "griffon-" + matcher[0][1]
                def version = matcher[0][2]
                def classifier = matcher[0][3] ?: 'none' // Se for nulo, é o jar binário principal

                def chaveUnica = "${pluginName}:${version}:${classifier}"

                if (!artefatosAgrupados[chaveUnica]) {
                    artefatosAgrupados[chaveUnica] = [
                        artifactId: pluginName,
                        version: version,
                        classifier: classifier,
                        absolutePath: file.absolutePath,
                        groupId: groupId
                    ]
                }

            } else {
                println "ignore jar: $file.absolutePath"
            }
        }
    }
}

// 3. Segunda Passada: Processar o mapa e rodar o Maven unificado
int sucessos = 0
int falhas = 0

new File("deps.txt").withWriter {writer ->

    artefatosAgrupados.each { k, artifact ->
        // Só prosseguimos se houver pelo menos o JAR principal.
        // Se houver apenas o source isolado por algum motivo, ignoramos ou usamos como principal.


        // Monta o comando básico do Maven
        def cmd = [
            "mvn install:install-file",
            "-Dfile=${artifact.absolutePath.replace("\\", "/")}",
            "-DgroupId=${artifact.groupId}",
            "-DartifactId=${artifact.artifactId}",
            "-Dversion=${artifact.version}",
            "-Dpackaging=jar",
            "-DgeneratePom=true",
            "-DlocalRepositoryPath=C:/Users/dev/.m2/repository"

        ]

        if(artifact.classifier != 'none'){
            cmd << "-Dclassifier=${artifact.classifier}"
        }

        writer.write(cmd.join(" \\\n"))
        writer.write("\n\n")

    }

    artefatosAgrupados.each {k , artifact ->
        if(artifact.classifier == 'none'){
            if(artifact.artifactId.contains('compile')) {
                writer.write("compileOnly '${artifact.groupId}:${artifact.artifactId}:${artifact.version}'\n")
            } else {
                writer.write("implementation '${artifact.groupId}:${artifact.artifactId}:${artifact.version}'\n")
            }
        }
    }
}

println "\n🏁 Processo de Arqueologia de Plugins Concluído!"
println "🎉 Total de Artefatos Instalados com Sucesso: $sucessos | ⚠️ Falhas: $falhas"