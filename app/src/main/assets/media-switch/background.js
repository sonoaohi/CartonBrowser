
var globalImageBlockingActivated = true;
var globalMediaBlockingActivated = true;
var globalFontBlockingActivated = true;
var imageWhitelistedHosts = new Set();
var imageBlacklistedHosts = new Set();
var mediaWhitelistedHosts = new Set();
var mediaBlacklistedHosts = new Set();
var fontWhitelistedHosts = new Set();
var fontBlacklistedHosts = new Set();

function filter(details) {
    
    let currentHost;
    if ((typeof details.frameAncestors[details.frameAncestors.length - 1] !== 'undefined')
        && (typeof details.frameAncestors[details.frameAncestors.length - 1].url !== 'undefined')) {
        currentHost = new URL(details.frameAncestors[details.frameAncestors.length - 1].url).origin;
    } else {
        currentHost = new URL(details.documentUrl).origin;
    }

    if (details.type == 'image') {
        if (globalImageBlockingActivated) {
            if (!imageWhitelistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        } else {
            if (imageBlacklistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        }
    }

    if (details.type == 'media') {
        if (globalMediaBlockingActivated) {
            if (!mediaWhitelistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        } else {
            if (mediaBlacklistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        }
    }

    if (details.type == 'font') {
        if (globalFontBlockingActivated) {
            if (!fontWhitelistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        } else {
            if (fontBlacklistedHosts.has(currentHost)) {
                return {cancel: true};
            }
        }
    }

    if (['ping','speculative','beacon','csp_report'].includes(details.type)) {
        return {cancel: true};
    }
    
    return {};
}

function initialize() {

    let port = browser.runtime.connectNative("browser");
    port.onMessage.addListener(messageJSON => {
        if (messageJSON.message == "Activate images global blocking") {
            globalImageBlockingActivated = true;
        } else if (messageJSON.message == "Deactivate images global blocking") {
            globalImageBlockingActivated = false;
        } else if (messageJSON.message == "Activate media global blocking") {
            globalMediaBlockingActivated = true;
        } else if (messageJSON.message == "Deactivate media global blocking") {
            globalMediaBlockingActivated = false;
        } else if (messageJSON.message == "Activate webfonts global blocking") {
            globalFontBlockingActivated = true;
        } else if (messageJSON.message == "Deactivate webfonts global blocking") {
            globalFontBlockingActivated = false;
        } else if (messageJSON.message == "Add this host to temporary images blocking exceptions") {
            if (globalImageBlockingActivated) {
                imageWhitelistedHosts.add(messageJSON.hostException);
            } else {
                imageBlacklistedHosts.add(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Remove this host from temporary images blocking exceptions") {
            if (globalImageBlockingActivated) {
                imageWhitelistedHosts.delete(messageJSON.hostException);
            } else {
                imageBlacklistedHosts.delete(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Add this host to temporary media blocking exceptions") {
            if (globalMediaBlockingActivated) {
                mediaWhitelistedHosts.add(messageJSON.hostException);
            } else {
                mediaBlacklistedHosts.add(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Remove this host from temporary media blocking exceptions") {
            if (globalMediaBlockingActivated) {
                mediaWhitelistedHosts.delete(messageJSON.hostException);
            } else {
                mediaBlacklistedHosts.delete(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Add this host to temporary webfonts blocking exceptions") {
            if (globalFontBlockingActivated) {
                fontWhitelistedHosts.add(messageJSON.hostException);
            } else {
                fontBlacklistedHosts.add(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Remove this host from temporary webfonts blocking exceptions") {
            if (globalFontBlockingActivated) {
                fontWhitelistedHosts.delete(messageJSON.hostException);
            } else {
                fontBlacklistedHosts.delete(messageJSON.hostException);
            }
        } else if (messageJSON.message == "Request host exception check") {
            let _imageResult;
            let _mediaResult;
            let _fontResult;
            let _imageListType;
            let _mediaListType;
            let _fontListType;

            if (globalImageBlockingActivated) {
                _imageListType = "whitelist";
                _imageResult = imageWhitelistedHosts.has(messageJSON.hostException);
            } else {
                _imageListType = "blacklist";
                _imageResult = imageBlacklistedHosts.has(messageJSON.hostException);
            }
            if (globalMediaBlockingActivated) {
                _mediaListType = "whitelist";
                _mediaResult = mediaWhitelistedHosts.has(messageJSON.hostException);
            } else {
                _mediaListType = "blacklist";
                _mediaResult = mediaBlacklistedHosts.has(messageJSON.hostException);
            }
            if (globalFontBlockingActivated) {
                _fontListType = "whitelist";
                _fontResult = fontWhitelistedHosts.has(messageJSON.hostException);
            } else {
                _fontListType = "blacklist";
                _fontResult = fontBlacklistedHosts.has(messageJSON.hostException);
            }

            port.postMessage({
                imageResult: _imageResult,
                mediaResult: _mediaResult,
                fontResult: _fontResult,
                imageListType: _imageListType,
                mediaListType: _mediaListType,
                fontListType: _fontListType,
                host: messageJSON.hostException
            });
        }
    });

    browser.webRequest.onBeforeRequest.addListener(
        filter,
        {urls: ['<all_urls>'], types: ['image','media','font','ping','speculative','beacon','csp_report']},
        ['blocking']
    );
}

initialize();
