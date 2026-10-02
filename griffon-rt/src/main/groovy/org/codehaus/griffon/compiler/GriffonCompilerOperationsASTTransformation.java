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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.griffon.compiler.GriffonCompilerContext.KEY_DISABLE_LOGGING_INJECTION;
import static org.codehaus.griffon.compiler.GriffonCompilerContext.KEY_DISABLE_THREADING_INJECTION;
import static org.codehaus.griffon.compiler.GriffonCompilerContext.getConfigOption;

/**
 * Global transformation that applies the compile unit operations that used to be
 * registered only by the CLI's {@code GriffonCompiler} (Ant based), so that builds
 * driven by Gradle produce the same bytecode:
 * <ul>
 * <li>{@link LoggingInjectionOperation}: wraps {@code log.debug(expr)} with {@code isDebugEnabled()}</li>
 * <li>{@link ThreadingInjectionOperation}: runs controller actions outside the UI thread</li>
 * </ul>
 * Each one can be disabled with the system properties
 * {@code griffon.disable.logging.injection} and {@code griffon.disable.threading.injection}.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GriffonCompilerOperationsASTTransformation implements ASTTransformation {
    private final LoggingInjectionOperation loggingInjection = new LoggingInjectionOperation();
    private final ThreadingInjectionOperation threadingInjection = new ThreadingInjectionOperation();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (GriffonCompilerContext.projectName == null) return;
        if (nodes == null || nodes.length == 0 || !(nodes[0] instanceof ModuleNode)) return;

        boolean logging = !getConfigOption(KEY_DISABLE_LOGGING_INJECTION);
        boolean threading = !getConfigOption(KEY_DISABLE_THREADING_INJECTION);
        if (!logging && !threading) return;

        List<ClassNode> classes = new ArrayList<ClassNode>(((ModuleNode) nodes[0]).getClasses());
        for (ClassNode classNode : classes) {
            if (logging) loggingInjection.call(source, null, classNode);
            if (threading) threadingInjection.call(source, null, classNode);
        }
    }
}
