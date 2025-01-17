package com.greenart7c3.nostrsigner.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.events.EmojiUrl
import java.math.BigDecimal

@Immutable
interface AmberEventInterface {
    fun id(): HexKey

    fun pubKey(): HexKey

    fun createdAt(): Long

    fun kind(): Int

    fun description(): String

    fun tags(): Array<Array<String>>

    fun content(): String

    fun sig(): HexKey

    fun toJson(): String

    fun checkSignature()

    fun hasValidSignature(): Boolean

    fun isTaggedUser(idHex: String): Boolean

    fun isTaggedEvent(idHex: String): Boolean

    fun isTaggedAddressableNote(idHex: String): Boolean
    fun isTaggedAddressableNotes(idHexes: Set<String>): Boolean

    fun isTaggedHash(hashtag: String): Boolean
    fun isTaggedGeoHash(hashtag: String): Boolean

    fun isTaggedHashes(hashtags: Set<String>): Boolean
    fun isTaggedGeoHashes(hashtags: Set<String>): Boolean

    fun firstIsTaggedHashes(hashtags: Set<String>): String?
    fun firstIsTaggedAddressableNote(addressableNotes: Set<String>): String?

    fun isTaggedAddressableKind(kind: Int): Boolean
    fun getTagOfAddressableKind(kind: Int): ATag?

    fun hashtags(): List<String>
    fun geohashes(): List<String>

    fun getReward(): BigDecimal?
    fun getPoWRank(): Int
    fun getGeoHash(): String?

    fun zapAddress(): String?
    fun isSensitive(): Boolean
    fun zapraiserAmount(): Long?

    fun taggedAddresses(): List<ATag>
    fun taggedUsers(): List<HexKey>
    fun taggedEvents(): List<HexKey>
    fun taggedUrls(): List<String>

    fun taggedEmojis(): List<EmojiUrl>
    fun matchTag1With(text: String): Boolean
}
