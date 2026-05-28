package org.codehaus.griffon.gradle.plugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.GroovyExtensionModuleTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.plugins.JavaApplication

import java.nio.file.FileSystems
import java.nio.file.Files

class GriffonPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def extension = project.extensions.create('griffonConfig', GriffonPluginExtension)

        // Carrega o arquivo de propriedades embutido no JAR do plugin em tempo de compilação
        def defaultProps = new Properties()
        try(def resourceStream = getClass().getResourceAsStream('/griffon-plugin-defaults.properties')) {
            defaultProps.load(resourceStream)
        }

        // Tenta ler do gradle.properties do projeto cliente (app).
        // Se o cliente NÃO definir, usa o valor padrão embutido no JAR do plugin!
        def propgriffonVersion = project.providers.gradleProperty('griffonVersion')
            .orElse(defaultProps.getProperty('griffonVersion', '1.6.0-SNAPSHOT')).get()

        def propgroovyVersion = project.providers.gradleProperty('groovyVersion')
            .orElse(defaultProps.getProperty('groovyVersion', '6.0.0-alpha-1')).get()

        def proplog4jVersion = project.providers.gradleProperty('log4jVersion')
            .orElse(defaultProps.getProperty('log4jVersion', '1.2.17')).get()

        def propslf4jVersion = project.providers.gradleProperty('slf4jVersion')
            .orElse(defaultProps.getProperty('slf4jVersion', '2.0.13')).get()

        def propslf4jReload4jVersion = project.providers.gradleProperty('slf4jReload4jVersion')
            .orElse(defaultProps.getProperty('slf4jReload4jVersion', '2.0.13')).get()

        def propreload4jVersion = project.providers.gradleProperty('reload4jVersion')
            .orElse(defaultProps.getProperty('reload4jVersion', '1.2.25')).get()

        // Define as convenções na extensão
        extension.groovyVersion.convention(propgroovyVersion)
        extension.griffonVersion.convention(propgriffonVersion)
        extension.log4jVersion.convention(proplog4jVersion)
        extension.slf4jVersion.convention(propslf4jVersion)
        extension.slf4jReload4jVersion.convention(propslf4jReload4jVersion)
        extension.reload4jVersion.convention(propreload4jVersion)
        extension.javaVersion.convention(26)


        // Aplica os plugins bases necessários no projeto destino
        project.pluginManager.apply('java')
        project.pluginManager.apply('groovy')
        project.pluginManager.apply('idea')
        project.pluginManager.apply('application')
        project.pluginManager.apply('com.gradleup.shadow')

        project.afterEvaluate {

            def groovyVersion = extension.groovyVersion.get()
            def griffonVersion = extension.griffonVersion.get()
            def log4jVersion = extension.log4jVersion.get()
            def slf4jVersion = extension.slf4jVersion.get()
            def slf4jReload4jVersion = extension.slf4jReload4jVersion.get()
            def reload4jVersion = extension.reload4jVersion.get()
            def javaTargetVersion = extension.javaVersion.get()

            // 2. Configura Repositórios do projeto destino
            project.repositories {
                mavenLocal()
                mavenCentral()
                maven { url 'https://raw.githubusercontent.com/mobilemindtech/m2/master' }
                maven { url 'https://repo1.maven.org/maven2' }
            }

            // 3. Injeta dependências padrões fixas do ecossistema
            project.dependencies {
                implementation "org.apache.groovy:groovy-all:${groovyVersion}"
                implementation "org.codehaus.griffon:griffon-rt:${griffonVersion}"

                compileOnly "org.codehaus.griffon:griffon-cli:${griffonVersion}"

                implementation "log4j:log4j:${log4jVersion}"
                implementation "org.slf4j:slf4j-api:${slf4jVersion}"
                implementation "org.slf4j:slf4j-reload4j:${slf4jReload4jVersion}"
                implementation "ch.qos.reload4j:reload4j:${reload4jVersion}"
                implementation "org.slf4j:jcl-over-slf4j:${slf4jVersion}"

                implementation 'commons-lang:commons-lang:2.6'

                implementation 'org.codehaus.griffon.plugins:griffon-swing-runtime:1.4.0'

                compileOnly 'org.codehaus.griffon.plugins:griffon-swing-compile:1.4.0'
                compileOnly 'org.ow2.asm:asm:9.7'
            }

            // 4. Configura compilação Java (Release 25)
            project.tasks.withType(JavaCompile).configureEach { task ->
                task.options.release.set(javaTargetVersion)
                task.options.compilerArgs << '--add-opens=java.base/java.lang=ALL-UNNAMED'
            }

            // 5. Configura Estrutura de Pastas do Griffon (SourceSets)
            project.sourceSets {
                main {
                    groovy {
                        srcDirs = [
                            'griffon-app/conf',
                            'griffon-app/controllers',
                            'griffon-app/models',
                            'griffon-app/views',
                            'griffon-app/services',
                            'griffon-app/lifecycle',
                            'src/main/java',
                            'src/main/groovy'
                        ]
                    }
                    java {
                        // Limpamos o sourceSet do Java puro para o Gradle não tentar
                        // compilar o Java antes do Groovy de forma isolada.
                        srcDirs = []
                    }
                    resources {
                        srcDirs = [
                            'griffon-app/resources',
                            'griffon-app/i18n',
                            'src/main/resources'
                        ]
                    }
                }
            }

            // 6. Configura Opções do Groovy Compiler
            project.tasks.withType(GroovyCompile).configureEach { task ->
                task.options.incremental = true
                task.groovyOptions.optimizationOptions.indy = true
                task.groovyOptions.fork = true
                task.groovyOptions.forkOptions.with {
                    memoryInitialSize = '512m'
                    memoryMaximumSize = '2g'
                    jvmArgs << '--add-opens=java.base/java.lang=ALL-UNNAMED'
                    jvmArgs << '--add-opens=java.base/java.util=ALL-UNNAMED'
                    jvmArgs << "-Dgriffon.compiler.project.name=${project.name}".toString()
                    jvmArgs << "-Dgriffon.compiler.basedir=${project.projectDir.absolutePath}".toString()
                    jvmArgs << "-Dgriffon.compiler.verbose=false"
                    jvmArgs << "-Dgriffon.compiler.gradle=true"
                }
            }

            def genMetadata = project.tasks.register('generateGriffonMetadata') { task ->
                task.description = "Generates application.properties for griffon.util.Metadata"
                task.group = "build"

                // Define onde o arquivo será injetado dentro das classes finais do build
                def buildDirFile = project.layout.buildDirectory.get().asFile

                def outputDir = new File(buildDirFile, "resources/main")
                def metadataFile = new File(outputDir, "application.properties")
                def griffonProps = new File(project.layout.projectDirectory.getAsFile(), "application.properties")

                // Define as entradas e saídas para o Gradle saber se precisa rodar de novo (Incremental Build)
                task.inputs.property("appVersion", project.version.toString())
                task.inputs.property("appName", project.name)
                task.outputs.file(metadataFile)

                task.doLast {
                    outputDir.mkdirs()
                    metadataFile.text = griffonProps.text
                    println "[GriffonGradlePlugin] 'application.properties' generated for Metadata framework!"
                }
            }

            // 7. Registra a task de propriedades de artefatos
            def genArtifacts = project.tasks.register('generateGriffonArtifactsProperties') { task ->
                task.description = "Generate file META-INF/griffon-artifacts.properties"
                task.group = "build"

                def griffonAppDir = project.file("griffon-app")
                def buildDirFile = project.layout.buildDirectory.get().asFile
                def outputDir = new File(buildDirFile, "/resources/main/META-INF")

                task.inputs.dir griffonAppDir
                task.outputs.dir outputDir

                task.doLast {
                    def artifactPaths = [
                        [type: "model", path: "models", suffix: "Model"],
                        [type: "view", path: "views", suffix: "View"],
                        [type: "controller", path: "controllers", suffix: "Controller"],
                        [type: "service", path: "services", suffix: "Service"]
                    ]
                    def artifacts = [:]
                    if (griffonAppDir.exists()) {
                        griffonAppDir.eachFileRecurse { file ->
                            if (file.isFile() && file.name.endsWith(".groovy")) {
                                def relativePath = file.absolutePath.substring(griffonAppDir.absolutePath.length())
                                def normalizedPath = relativePath.replace(File.separator, "/")
                                artifactPaths.each { entry ->
                                    if (normalizedPath.contains("/${entry.path}/")) {
                                        def idx = normalizedPath.indexOf("/${entry.path}/") + entry.path.length() + 2
                                        def classPath = normalizedPath.substring(idx)
                                        def klass = classPath.replace("/", ".").replaceAll(/\.groovy$/, "")
                                        if (entry.suffix ? klass.endsWith(entry.suffix) : true) {
                                            if (!artifacts[entry.type]) artifacts[entry.type] = []
                                            artifacts[entry.type] << klass
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (artifacts) {
                        outputDir.mkdirs()
                        new File(outputDir, "griffon-artifacts.properties").withPrintWriter("UTF-8") { writer ->
                            artifacts.each { type, list -> writer.println("${type} = '${list.join(',')}'") }
                        }
                        println "[GriffonGradlePlugin] 'griffon-artifacts.properties' generated!"
                    }
                }
            }

            // 8. Registra a task de propriedades de addons (Tradução fiel do collectAddonMetadata)
            def genAddons = project.tasks.register('generateGriffonAddonsProperties') { task ->
                task.description = "Generate file META-INF/griffon-addons.properties"
                task.group = "build"

                def buildDirFile = project.layout.buildDirectory.get().asFile
                def projectDirFile = project.layout.projectDirectory.getAsFile()
                def outputDir = new File(buildDirFile, "resources/main/META-INF")
                task.outputs.dir outputDir

                // 1. Captura dependências de compilação de forma segura para o cache
                def compileArtifactsSignal = project.provider {
                    project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect { artifact ->
                        [
                            name   : artifact.moduleVersion.id.name.toString(),
                            version: artifact.moduleVersion.id.version.toString()
                        ]
                    }
                }

                // 2. Captura dependências de runtime de forma segura para o cache
                def runtimeArtifactsSignal = project.provider {
                    project.configurations.runtimeClasspath.resolvedConfiguration.resolvedArtifacts.collect { artifact ->
                        [
                            name   : artifact.moduleVersion.id.name.toString(),
                            version: artifact.moduleVersion.id.version.toString()
                        ]
                    }
                }

                task.inputs.property("runtimeArtifacts", runtimeArtifactsSignal)
                task.inputs.property("compileArtifacts", compileArtifactsSignal)

                task.doLast {
                    Map addons = [:]

                    // Tradução da lógica antiga de detecção de JARs para o padrão de artefatos do Maven
                    def processArtifact = { artifact ->
                        def name = artifact.name
                        def version = artifact.version

                        // O script original procurava por: griffon-${name}-runtime-*.jar
                        if (name.startsWith("griffon-") && name.endsWith("-runtime")) {
                            def cleanName = name.replaceAll(/^griffon-/, "").replaceAll(/-runtime$/, "")
                            addons[cleanName] = version
                        }
                        // O script original procurava por: griffon-${name}-addon-*.jar
                        else if (name.startsWith("griffon-") && name.contains("-addon")) {
                            def cleanName = name.replaceAll(/^griffon-/, "").replaceAll(/-addon.*$/, "")
                            addons[cleanName] = version
                        }
                    }

                    // Alimenta o mapa processando ambos os classpaths
                    compileArtifactsSignal.get().each { processArtifact(it) }
                    runtimeArtifactsSignal.get().each { processArtifact(it) }

                    if (addons) {
                        // toolkit plugin always goes first (Igual ao código original)
                        def metadataProps = new Properties()
                        def appPropertiesFile = new File(projectDirFile, "application.properties")

                        if (appPropertiesFile.exists()) {
                            appPropertiesFile.withInputStream { stream ->
                                metadataProps.load(stream)
                            }

                            // No código original: Metadata.current.getApplicationToolkit()
                            // que lê a chave 'app.toolkit' do application.properties
                            def toolkit = metadataProps.getProperty("app.toolkit")

                            if (toolkit && addons.containsKey(toolkit)) {
                                def toolkitVersion = addons.remove(toolkit)
                                // Coloca o toolkit na primeira posição do mapa
                                addons = [(toolkit): toolkitVersion] + addons
                            }
                        }
                    }
                    outputDir.mkdirs()
                    // Escrita do arquivo respeitando a saída exata original: "$name = $version"
                    new File(outputDir, "griffon-addons.properties").withPrintWriter("UTF-8") { writer ->
                        addons.each { name, version ->
                            writer.println("${name} = ${version}")
                        }
                    }
                    println "[GriffonGradlePlugin] 'griffon-addons.properties' generated successfully (Matched legacy behavior)!"
                }
            }

            // Amarração do ciclo de recursos
            project.tasks.named('processResources').configure { task ->
                task.dependsOn genMetadata, genArtifacts, genAddons
            }

            // 9. Configura Application MainClass padrão do Griffon
            project.extensions.configure(JavaApplication) { app ->
                app.mainClass.set('griffon.swing.SwingApplication')
            }

            // 10. Configura o comportamento padrão da Task 'run'
            project.tasks.named('run').configure { task ->
                task.dependsOn genMetadata, genArtifacts, genAddons
                task.systemProperty 'griffon.env', 'dev'
                task.workingDir = project.rootProject.projectDir
                task.standardInput = System.in
                task.standardOutput = System.out
                task.errorOutput = System.err
                task.jvmArgs = [
                    '--add-opens=java.base/java.lang=ALL-UNNAMED',
                    '--add-opens=java.base/java.util=ALL-UNNAMED',
                    '-Djava.awt.headless=false',
                    "-Dgriffon.application.name=${project.name}".toString()
                ]
            }

            // 11. Ajustes do ShadowJar para extensões do Groovy (100% Compatível com Configuration Cache)
            project.tasks.withType(ShadowJar).configureEach { task ->
                task.archiveClassifier.set('dist')

                // 1. Mescla os arquivos normais de serviços do Java (SPI)
                task.mergeServiceFiles()

                // Mapas locais para acumular as extensões durante a cópia (Fase de Configuração)
                def extensionClasses = new LinkedHashMap<String, Boolean>()
                def staticExtensionClasses = new LinkedHashMap<String, Boolean>()

                // 2. Intercepta o arquivo ExtensionModule durante o pipeline de arquivos
                task.filesMatching("META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule") { fileDetails ->
                    def props = new Properties()
                    fileDetails.file.withInputStream { props.load(it) }

                    props.getProperty("extensionClasses")?.split(",")?.each { extensionClasses[it.trim()] = true }
                    props.getProperty("staticExtensionClasses")?.split(",")?.each { staticExtensionClasses[it.trim()] = true }

                    // Exclui o arquivo individual para evitar duplicidade
                    fileDetails.exclude()
                }

                // 3. Execução: Injeta o arquivo mesclado usando Java puro (Sem usar o objeto 'project')
                task.doLast {
                    if (!extensionClasses.isEmpty() || !staticExtensionClasses.isEmpty()) {
                        // Obtém o arquivo JAR final gerado pelo Shadow
                        def jarFile = task.archiveFile.get().asFile
                        if (!jarFile.exists()) return

                        // Cria o conteúdo do ExtensionModule em memória
                        def output = new ByteArrayOutputStream()
                        output.withPrintWriter { writer ->
                            writer.println("moduleName=griffon-merged-module")
                            writer.println("moduleVersion=1.0.0")
                            if (!extensionClasses.isEmpty()) {
                                writer.println("extensionClasses=${extensionClasses.keySet().join(',')}")
                            }
                            if (!staticExtensionClasses.isEmpty()) {
                                writer.println("staticExtensionClasses=${staticExtensionClasses.keySet().join(',')}")
                            }
                        }
                        def fileBytes = output.toByteArray()

                        // Injeta o arquivo cirurgicamente dentro do ZIP/JAR usando o FileSystem do Java
                        def jarUri = URI.create("jar:" + jarFile.toURI().toString())
                        def env = [create: "false"]

                        try {
                            FileSystems.newFileSystem(jarUri, env).withCloseable { fs ->
                                def internalPath = fs.getPath("META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule")

                                // Garante que as pastas pai existam dentro do JAR
                                Files.createDirectories(internalPath.getParent())

                                // Escreve os bytes do arquivo unificado na raiz do JAR
                                Files.write(internalPath, fileBytes)
                            }
                            println "[GriffonGradlePlugin] 'ExtensionModule' injected to jar with successfully"
                        } catch (Exception e) {
                            println "[GriffonGradlePlugin] Error to inject ExtensionModule to JAR: ${e.message}"
                            throw e
                        }
                    }
                }
            }
        }
    }
}