/*
 * Copyright 2008-2013 the original author or authors.
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
package org.codehaus.griffon.runtime.swing;

import griffon.core.GriffonController;
import griffon.core.UIThreadManager;
import griffon.core.controller.GriffonControllerActionManager;
import griffon.swing.SwingAction;
import griffon.util.RunnableWithArgs;
import org.codehaus.griffon.runtime.core.controller.AbstractGriffonControllerAction;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class SwingGriffonControllerAction extends AbstractGriffonControllerAction {
    private final SwingAction toolkitAction;

    public SwingGriffonControllerAction(final GriffonControllerActionManager actionManager, final GriffonController controller, final String actionName) {
        super(actionManager, controller, actionName);
        final SwingGriffonControllerAction self = this;
        toolkitAction = new SwingAction(new RunnableWithArgs() {
            public void run(Object[] args) {
                actionManager.invokeAction(self.getController(), actionName, args);
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                UIThreadManager.getInstance().executeAsync(new Runnable() {
                    public void run() {
                        if (KEY_NAME.equals(evt.getPropertyName())) {
                            toolkitAction.putValue(Action.NAME, evt.getNewValue());
                        } else if (KEY_SHORT_DESCRIPTION.equals(evt.getPropertyName())) {
                            toolkitAction.putValue(Action.SHORT_DESCRIPTION, evt.getNewValue());
                        } else if (KEY_LONG_DESCRIPTION.equals(evt.getPropertyName())) {
                            toolkitAction.putValue(Action.LONG_DESCRIPTION, evt.getNewValue());
                        } else if (KEY_ENABLED.equals(evt.getPropertyName())) {
                            toolkitAction.setEnabled((Boolean) evt.getNewValue());
                        } else if (KEY_SELECTED.equals(evt.getPropertyName())) {
                            toolkitAction.putValue(Action.SELECTED_KEY, evt.getNewValue());
                        } else if (KEY_MNEMONIC.equals(evt.getPropertyName())) {
                            String mnemonic = (String) evt.getNewValue();
                            if (!isBlank(mnemonic)) {
                                toolkitAction.putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
                            }
                        } else if (KEY_ACCELERATOR.equals(evt.getPropertyName())) {
                            String accelerator = (String) evt.getNewValue();
                            if (!isBlank(accelerator)) {
                                toolkitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
                            }
                        } else if (KEY_SMALL_ICON.equals(evt.getPropertyName())) {
                            String smallIcon = (String) evt.getNewValue();
                            if (!isBlank(smallIcon)) {
                                toolkitAction.putValue(Action.SMALL_ICON, new ImageIcon(smallIcon));
                            }
                        } else if (KEY_LARGE_ICON.equals(evt.getPropertyName())) {
                            String largeIcon = (String) evt.getNewValue();
                            if (!isBlank(largeIcon)) {
                                toolkitAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon(largeIcon));
                            }
                        }
                    }
                });
            }
        });
    }

    protected void doInitialize() {
        toolkitAction.putValue(Action.NAME, getName());
        toolkitAction.putValue(Action.SHORT_DESCRIPTION, getShortDescription());
        toolkitAction.putValue(Action.LONG_DESCRIPTION, getLongDescription());
        toolkitAction.setEnabled(isEnabled());
        toolkitAction.putValue(Action.SELECTED_KEY, isSelected());
        String mnemonic = getMnemonic();
        if (!isBlank(mnemonic)) {
            toolkitAction.putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        }
        String accelerator = getAccelerator();
        if (!isBlank(accelerator)) {
            toolkitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
        }
        String smallIcon = getSmallIcon();
        if (!isBlank(smallIcon)) {
            toolkitAction.putValue(Action.SMALL_ICON, new ImageIcon(smallIcon));
        }
        String largeIcon = getLargeIcon();
        if (!isBlank(largeIcon)) {
            toolkitAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon(largeIcon));
        }
    }

    public Object getToolkitAction() {
        return toolkitAction;
    }

    protected void doExecute(Object... args) {
        ActionEvent event = null;
        if (args != null && args.length == 1 && args[0] instanceof ActionEvent) {
            event = (ActionEvent) args[0];
        }
        toolkitAction.actionPerformed(event);
    }
}
