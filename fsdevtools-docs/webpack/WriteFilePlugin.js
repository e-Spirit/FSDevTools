const pluginName = 'WriteManifestPlugin';

const write = require('write');
const fs = require('fs');
const path = require('path');

class WriteFilePlugin {

  constructor(options) {
    this.options = options;
  }

  apply(compiler) {
    compiler.hooks.environment.tap(pluginName, (env) => {
      const fullPath = path.join(this.options.path, this.options.fileName);
      const filePath = this.options.path;
      const fileName = this.options.fileName;
      const content = this.options.content;
      const contentData = typeof content === 'function' ? content({
        filePath,
        fileName,
        env
      }) : content;
      if (!fs.existsSync(fullPath)) {
        write.sync(fullPath, contentData);
      }
    });
  }
}

module.exports = WriteFilePlugin;
