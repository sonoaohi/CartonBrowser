package com.example.cartonbrowser

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.mozilla.geckoview.WebExtension
import java.net.URI

class CustomWebExtensionMessageDelegate: WebExtension.MessageDelegate {
    override fun onConnect(port: WebExtension.Port) {
        Globals.mediaSwitchWebExtensionPort = port
        port.setDelegate(Globals.mediaSwitchWebExtensionPortDelegate)
        super.onConnect(port)

        port.postMessage( JSONObject().put("message",
            if (Globals.isAllowingImagesByDefault)
                "Deactivate images global blocking"
            else "Activate images global blocking")
        )
        port.postMessage( JSONObject().put("message",
            if (Globals.isAllowingMediaByDefault)
                "Deactivate media global blocking"
            else "Activate media global blocking")
        )
        port.postMessage( JSONObject().put("message",
            if (Globals.isAllowingWebfontsByDefault)
                "Deactivate webfonts global blocking"
            else "Activate webfonts global blocking")
        )
    }
}

class CustomWebExtensionPortDelegate: WebExtension.PortDelegate {
    override fun onPortMessage(message: Any, port: WebExtension.Port) {
        (message as JSONObject).let {
            EventBus.getDefault().post(
                MediaBlockingHostExceptionResultEvent(
                    it.getString("host"),
                    it.getBoolean("imageResult"),
                    it.getBoolean("mediaResult"),
                    it.getBoolean("fontResult"),
                    it.getString("imageListType").let {
                        if (it == "whitelist") {
                            MediaBlockingHostExceptionListType.WHITELIST
                        } else{
                            MediaBlockingHostExceptionListType.BLACKLIST
                        }
                    },
                    it.getString("mediaListType").let {
                        if (it == "whitelist") {
                            MediaBlockingHostExceptionListType.WHITELIST
                        } else{
                            MediaBlockingHostExceptionListType.BLACKLIST
                        }
                    },
                    it.getString("fontListType").let {
                        if (it == "whitelist") {
                            MediaBlockingHostExceptionListType.WHITELIST
                        } else{
                            MediaBlockingHostExceptionListType.BLACKLIST
                        }
                    }
                )
            )
        }
        super.onPortMessage(message, port)
    }
}

enum class MediaBlockingHostExceptionListType {
    WHITELIST,
    BLACKLIST
}
