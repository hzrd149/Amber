package com.greenart7c3.nostrsigner.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.LiveData
import com.vitorpamplona.quartz.crypto.KeyPair
import com.vitorpamplona.quartz.signers.NostrSigner
import com.vitorpamplona.quartz.signers.NostrSignerInternal

class History(
    val appName: String,
    val type: String,
    val time: Long,
    val kind: Int?
)

@Stable
class Account(
    val keyPair: KeyPair,
    val signer: NostrSigner = NostrSignerInternal(keyPair),
    var name: String,
    var savedApps: MutableMap<String, Boolean>
) {
    val saveable: AccountLiveData = AccountLiveData(this)
}

class AccountLiveData(account: Account) : LiveData<AccountState>(AccountState(account))

@Immutable
class AccountState(val account: Account)
