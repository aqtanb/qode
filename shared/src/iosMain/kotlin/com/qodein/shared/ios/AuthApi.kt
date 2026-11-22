package com.qodein.shared.ios

import com.qodein.shared.common.Result
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.auth.SendEmailCodeUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithEmailCodeUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.legal.GetLegalDocumentUseCase
import com.qodein.shared.domain.usecase.user.AcceptConsentAndCreateUserUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.UserResolutionResult
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountItem(val id: String, val displayName: String?, val photoUrl: String?)

enum class SignInOutcome { SIGNED_IN, NEEDS_CONSENT }

/** Email-code sign-in and the signed-in account for Swift. One instance per sign-in attempt. */
class AuthApi : KoinComponent {
    private val sendEmailCodeUseCase: SendEmailCodeUseCase by inject()
    private val signInWithEmailCodeUseCase: SignInWithEmailCodeUseCase by inject()
    private val acceptConsentUseCase: AcceptConsentAndCreateUserUseCase by inject()
    private val signOutUseCase: SignOutUseCase by inject()
    private val getLegalDocumentUseCase: GetLegalDocumentUseCase by inject()
    private val getUserById: GetUserByIdUseCase by inject()
    private val authRepository: AuthRepository by inject()

    private var pendingUser: GoogleAuthResult? = null

    @Throws(Exception::class)
    suspend fun sendEmailCode(email: String) = call { sendEmailCodeUseCase(email) }

    @Throws(Exception::class)
    suspend fun signInWithEmailCode(
        email: String,
        code: String
    ): SignInOutcome =
        when (val resolution = call { signInWithEmailCodeUseCase(email, code) }) {
            is UserResolutionResult.ExistingUser -> SignInOutcome.SIGNED_IN
            is UserResolutionResult.NewUserNeedsConsent -> {
                pendingUser = resolution.authUser
                SignInOutcome.NEEDS_CONSENT
            }
        }

    /** Creates the user row for the account that [signInWithEmailCode] left waiting for consent. */
    @Throws(Exception::class)
    suspend fun acceptConsent() {
        val user = checkNotNull(pendingUser) { "No sign-in is waiting for consent" }
        call { acceptConsentUseCase(user) }
        pendingUser = null
    }

    @Throws(Exception::class)
    suspend fun signOut() {
        pendingUser = null
        signOutUseCase()
    }

    @Throws(Exception::class)
    suspend fun legalDocument(privacyPolicy: Boolean): String =
        call {
            getLegalDocumentUseCase(if (privacyPolicy) DocumentType.PrivacyPolicy else DocumentType.TermsOfService)
        }.content

    /**
     * The signed-in account, or null when signed out or still waiting for consent.
     * Reads the auth session directly: the shared auth-state flows replay a cached value, which can be stale here.
     */
    @Throws(Exception::class)
    suspend fun currentAccount(): AccountItem? {
        val authUser = authRepository.observeAuthState().first() ?: return null
        return when (val result = getUserById(authUser.uid)) {
            is Result.Success -> AccountItem(result.data.id.value, result.data.displayName ?: authUser.email, result.data.profile.photoUrl)
            is Result.Error -> null
        }
    }
}
