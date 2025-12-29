function __initApp() {  // init app
    globalThis.__wxRequire = require;  // FIX: require cannot work in separate engine 
    require('./web-adapter');
    const firstScreen = require('./first-screen');


    // Polyfills bundle.
    require("src/polyfills.bundle.43263.js");

    // SystemJS support.
    require("src/system.bundle.f45da.js");


    // Adapt for IOS, swap if opposite
    const info = wx.getSystemInfoSync();
    if (canvas) {
        var _w = canvas.width;
        var _h = canvas.height;
        if (info.screenWidth < info.screenHeight) {
            if (canvas.width > canvas.height) {
                _w = canvas.height;
                _h = canvas.width;
            }
        } else {
            if (canvas.width < canvas.height) {
                _w = canvas.height;
                _h = canvas.width;
            }
        }
        canvas.width = _w;
        canvas.height = _h;
    }
    // Adjust initial canvas size
    if (canvas && window.devicePixelRatio >= 2) { canvas.width *= info.devicePixelRatio; canvas.height *= info.devicePixelRatio; }

    const importMap = require("src/import-map.230ae.js").default;
    System.warmup({
        importMap,
        importMapUrl: 'src/import-map.230ae.js',
        defaultHandler: (urlNoSchema) => {
            require('.' + urlNoSchema);
        },
        handlers: {
            'plugin:': (urlNoSchema) => {
                requirePlugin(urlNoSchema);
            },
            'project:': (urlNoSchema) => {
                require(urlNoSchema);
            },
        },
    });

    System.import('./application.6cd00.js').then((module) => {
        return Promise.resolve(module);
    }).then(({ Application }) => {
        return new Application();
    }).then((application) => {
        return Promise.resolve(application);
    }).then((application) => {
        return onApplicationCreated(application);
    })

    function onApplicationCreated(application) {
        return System.import('cc').then((module) => {
            return Promise.resolve(module);
        }).then((cc) => {
            require('./engine-adapter');
            return application.init(cc);
        }).then(() => {
            return application.start();
        });
    }

}  // init app

// NOTE: on WeChat Android end, we can only get the correct screen size at the second tick of game.
var sysInfo = wx.getSystemInfoSync();
if (sysInfo.platform.toLocaleLowerCase() === 'android') {
    GameGlobal.requestAnimationFrame(__initApp);
} else {
    __initApp();
}
