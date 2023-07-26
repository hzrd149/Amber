package com.greenart7c3.nostrsigner.service.model

import androidx.compose.runtime.Immutable
import com.greenart7c3.nostrsigner.models.HexKey
import com.greenart7c3.nostrsigner.models.TimeUtils
import com.greenart7c3.nostrsigner.models.toHexKey
import com.greenart7c3.nostrsigner.service.CryptoUtils

@Immutable
class RelayAuthEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : Event(id, pubKey, createdAt, kind, tags, content, sig) {
    fun relay() = tags.firstOrNull() { it.size > 1 && it[0] == "relay" }?.get(1)
    fun challenge() = tags.firstOrNull() { it.size > 1 && it[0] == "challenge" }?.get(1)

    companion object {
        const val kind = 22242

        fun create(relay: String, challenge: String, privateKey: ByteArray, createdAt: Long = TimeUtils.now()): RelayAuthEvent {
            val content = ""
            val pubKey = CryptoUtils.pubkeyCreate(privateKey).toHexKey()
            val tags = listOf(
                listOf("relay", relay),
                listOf("challenge", challenge)
            )
            val id = generateId(pubKey, createdAt, kind, tags, content)
            val sig = CryptoUtils.sign(id, privateKey)
            return RelayAuthEvent(id.toHexKey(), pubKey, createdAt, tags, content, sig.toHexKey())
        }
    }
}
