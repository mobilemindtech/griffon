/*
 * Copyright 2010-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.compiler;

import org.codehaus.griffon.cli.CommandLineConstants;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Global transformation that applies, in Gradle builds, the compile unit operations that the CLI's
 * {@link GriffonCompiler} registers: {@link LoggingInjectionOperation} and
 * {@link ThreadingInjectionOperation} (controller methods/closures run outside the UI thread).
 * Without it, apps built with Gradle behave differently from apps built with the Griffon CLI.
 * <p>
 * Only active when {@code griffon.compiler.gradle=true} (set by the Griffon Gradle plugin), so CLI builds,
 * which already register the operations, are not processed twice. Each one can be disabled with
 * {@code griffon.disable.logging.injection} / {@code griffon.disable.threading.injection}.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GriffonCompilerOperationsASTTransformation implements ASTTransformation {
    private final LoggingInjectionOperation loggingInjection = new LoggingInjectionOperation();
    private final ThreadingInjectionOperation threadingInjection = new ThreadingInjectionOperation();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!"true".equals(System.getProperty("griffon.compiler.gradle"))) return;
        if (GriffonCompilerContext.projectName == null) return;
        if (nodes == null || nodes.length == 0 || !(nodes[0] instanceof ModuleNode)) return;

        boolean logging = !GriffonCompilerContext.getConfigOption(CommandLineConstants.KEY_DISABLE_LOGGING_INJECTION);
        boolean threading = !GriffonCompilerContext.getConfigOption(CommandLineConstants.KEY_DISABLE_THREADING_INJECTION);
        if (!logging && !threading) return;

        List<ClassNode> classes = new ArrayList<ClassNode>(((ModuleNode) nodes[0]).getClasses());
        for (ClassNode classNode : classes) {
            if (logging) loggingInjection.call(source, null, classNode);
            if (threading) threadingInjection.call(source, null, classNode);
        }
    }
}
