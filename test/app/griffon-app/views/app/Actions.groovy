package app

actions {
   action( id: 'newAction',
      name: 'New',
      mnemonic: 'N',
      accelerator: shortcut('N'),
      shortDescription: 'New',
      action: doAction
   )
   action( id: 'openAction',
      name: 'Open...',
      mnemonic: 'O',
      accelerator: shortcut('O'),
      shortDescription: 'Open',
       action: doAction
   )
   action( id: 'quitAction',
      name: 'Quit',
      mnemonic: 'Q',
      accelerator: shortcut('Q'),
       action: doAction
   )
   action( id: 'aboutAction',
      name: 'About',
      mnemonic: 'B',
      accelerator: shortcut('B'),
       action: doAction
   )

   action( id: 'saveAction',
      name: 'Save',
      mnemonic: 'S',
      accelerator: shortcut('S'),
      shortDescription: 'Save',
       action: doAction
   )
   action( id: 'saveAsAction',
      name: 'Save as...',
      accelerator: shortcut('shift S'),
       action: doAction
   )

   action(id: 'cutAction',
      name: 'Cut',
      mnemonic: 'T',
      accelerator: shortcut('X'),
      shortDescription: 'Cut',
       action: doAction
   )
   action(id: 'copyAction',
      name: 'Copy',
      mnemonic: 'C',
      accelerator: shortcut('C'),
      shortDescription: 'Copy',
       action: doAction
   )
   action(id: 'pasteAction',
      name: 'Paste',
      mnemonic: 'P',
      accelerator: shortcut('V'),
      shortDescription: 'Paste',
       action: doAction
   )
}