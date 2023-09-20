package com.greenart7c3.nostrsigner.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.greenart7c3.nostrsigner.R
import com.greenart7c3.nostrsigner.models.Account
import com.greenart7c3.nostrsigner.models.IntentData
import com.greenart7c3.nostrsigner.models.SignerType
import com.greenart7c3.nostrsigner.service.AmberUtils
import com.greenart7c3.nostrsigner.service.IntentUtils
import com.greenart7c3.nostrsigner.service.getAppCompatActivity
import com.greenart7c3.nostrsigner.ui.components.EncryptDecryptData
import com.greenart7c3.nostrsigner.ui.components.EventData
import com.greenart7c3.nostrsigner.ui.components.LoginWithPubKey
import com.vitorpamplona.quartz.encoders.toHexKey
import com.vitorpamplona.quartz.encoders.toNpub
import com.vitorpamplona.quartz.events.Event
import com.vitorpamplona.quartz.events.LnZapRequestEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier,
    json: IntentData?,
    packageName: String?,
    account: Account
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier
    ) {
        if (json == null) {
            Column(
                Modifier.fillMaxSize(),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Text("No event to sign")
            }
        } else {
            json.let {
                var key = "$packageName-${it.type}"
                val appName = packageName ?: it.name
                when (it.type) {
                    SignerType.GET_PUBLIC_KEY -> {
                        val remember = remember {
                            mutableStateOf(account.savedApps[key] ?: false)
                        }
                        val shouldRunOnAccept = account.savedApps[key] ?: false
                        LoginWithPubKey(
                            shouldRunOnAccept,
                            remember,
                            packageName,
                            appName,
                            {
                                val sig = account.keyPair.pubKey.toNpub()
                                coroutineScope.launch {
                                    sendResult(
                                        context,
                                        packageName,
                                        account,
                                        key,
                                        remember.value,
                                        clipboardManager,
                                        "",
                                        "",
                                        sig
                                    )
                                }
                                return@LoginWithPubKey
                            },
                            {
                                context.getAppCompatActivity()?.finish()
                            }
                        )
                    }

                    SignerType.NIP04_DECRYPT, SignerType.NIP04_ENCRYPT, SignerType.NIP44_ENCRYPT, SignerType.NIP44_DECRYPT, SignerType.DECRYPT_ZAP_EVENT -> {
                        val remember = remember {
                            mutableStateOf(account.savedApps[key] ?: false)
                        }
                        val shouldRunOnAccept = account.savedApps[key] ?: false
                        EncryptDecryptData(
                            shouldRunOnAccept,
                            remember,
                            packageName,
                            appName,
                            it.type,
                            {
                                try {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val sig = try {
                                            AmberUtils.encryptOrDecryptData(
                                                it.data,
                                                it.type,
                                                account,
                                                it.pubKey
                                            )
                                                ?: context.getString(R.string.could_not_decrypt_the_message)
                                        } catch (e: Exception) {
                                            context.getString(R.string.could_not_decrypt_the_message)
                                        }

                                        val result =
                                            if (sig == context.getString(R.string.could_not_decrypt_the_message) && (it.type == SignerType.DECRYPT_ZAP_EVENT)) {
                                                ""
                                            } else {
                                                sig
                                            }

                                        sendResult(
                                            context,
                                            packageName,
                                            account,
                                            key,
                                            remember.value,
                                            clipboardManager,
                                            "",
                                            it.id,
                                            result
                                        )
                                    }

                                    return@EncryptDecryptData
                                } catch (e: Exception) {
                                    val message = if (it.type.toString().contains("ENCRYPT", true)) {
                                        "encrypt"
                                    } else {
                                        "decrypt"
                                    }
                                    coroutineScope.launch {
                                        Toast.makeText(
                                            context,
                                            "Error to $message data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@EncryptDecryptData
                                }
                            },
                            {
                                context.getAppCompatActivity()?.finish()
                            }
                        )
                    }

                    else -> {
                        val event =
                            IntentUtils.getIntent(it.data, account.keyPair)
                        key = "$packageName-${it.type}-${event.kind}"
                        val remember = remember {
                            mutableStateOf(account.savedApps[key] ?: false)
                        }
                        val shouldRunOnAccept = account.savedApps[key] ?: false
                        EventData(
                            shouldRunOnAccept,
                            remember,
                            packageName,
                            appName,
                            event,
                            event.toJson(),
                            {
                                if (event.pubKey != account.keyPair.pubKey.toHexKey()) {
                                    coroutineScope.launch {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.event_pubkey_is_not_equal_to_current_logged_in_user),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@EventData
                                }

                                val localEvent =
                                    Event.fromJson(
                                        it.data
                                    )
                                if (localEvent is LnZapRequestEvent && localEvent.tags.any { tag -> tag.any { t -> t == "anon" } }) {
                                    val resultEvent =
                                        AmberUtils.getZapRequestEvent(
                                            localEvent,
                                            account.keyPair.privKey
                                        )
                                    coroutineScope.launch {
                                        sendResult(
                                            context,
                                            packageName,
                                            account,
                                            key,
                                            remember.value,
                                            clipboardManager,
                                            resultEvent.toJson(),
                                            it.id,
                                            resultEvent.toJson()
                                        )
                                    }
                                } else {
                                    val signedEvent = AmberUtils.getSignedEvent(
                                        event,
                                        account.keyPair.privKey
                                    )

                                    coroutineScope.launch {
                                        sendResult(
                                            context,
                                            packageName,
                                            account,
                                            key,
                                            remember.value,
                                            clipboardManager,
                                            signedEvent.toJson(),
                                            it.id,
                                            signedEvent.sig
                                        )
                                    }
                                }
                            },
                            {
                                context.getAppCompatActivity()?.finish()
                            }
                        )
                    }
                }
            }
        }
    }
}