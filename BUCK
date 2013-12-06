include_defs('//tools/build.defs')

gerrit_war(name = 'gerrit')
gerrit_war(name = 'chrome',   ui = 'ui_chrome')
gerrit_war(name = 'firefox',  ui = 'ui_firefox')
gerrit_war(name = 'withdocs', docs = True)
gerrit_war(name = 'release',  docs = True, context = ['//plugins:core.zip'])

API_DEPS = [
<<<<<<< HEAD   (aba0f4 Merge "SideBySide2: Scroll to first diff chunk when opening )
  '//gerrit-extension-api:extension-api',
  '//gerrit-extension-api:extension-api-src',
  '//gerrit-extension-api:extension-api-javadoc',
  '//gerrit-plugin-api:plugin-api',
  '//gerrit-plugin-api:plugin-api-src',
  '//gerrit-plugin-api:plugin-api-javadoc',
  '//gerrit-plugin-gwtui:gwtui-api',
  '//gerrit-plugin-gwtui:gwtui-api-src',
  '//gerrit-plugin-gwtui:gwtui-api-javadoc',
=======
  ':extension-api',
  ':extension-api-src',
  ':plugin-api',
  ':plugin-api-src',
  ':plugin-gwtui',
  ':plugin-gwtui-src',
>>>>>>> BRANCH (acc959 Buck: add build for gerrit-plugin-gwtui)
]

genrule(
  name = 'api',
  cmd = ';'.join(
    ['cd $TMP'] +
    ['ln -s $(location %s) .' % n for n in API_DEPS] +
    ['zip -q0 $OUT *']),
  deps = API_DEPS,
  out = 'api.zip',
)

genrule(
  name = 'plugin-gwtui',
  cmd = 'ln -s $(location //gerrit-plugin-gwtui:client) $OUT',
  deps = ['//gerrit-plugin-gwtui:client'],
  out = 'plugin-gwtui.jar',
  visibility = ['//tools/maven:'],
)

genrule(
  name = 'plugin-gwtui-src',
  cmd = 'ln -s $(location //gerrit-plugin-gwtui:src) $OUT',
  deps = ['//gerrit-plugin-gwtui:src'],
  out = 'plugin-gwtui-src.jar',
  visibility = ['//tools/maven:'],
)
