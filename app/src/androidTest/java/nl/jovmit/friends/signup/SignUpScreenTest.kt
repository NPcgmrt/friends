package nl.jovmit.friends.signup

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import nl.jovmit.friends.MainActivity
import nl.jovmit.friends.domain.exceptions.BackendException
import nl.jovmit.friends.domain.exceptions.ConnectionUnavailableException
import nl.jovmit.friends.domain.user.InMemoryUserCatalog
import nl.jovmit.friends.domain.user.User
import nl.jovmit.friends.domain.user.UserCatalog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class SignUpScreenTest {

  @get:Rule
  val signUpTestRule = createAndroidComposeRule<MainActivity>()

  private val userCatalog = InMemoryUserCatalog()
  private val signUpModule = module {
    factory<UserCatalog>(override = true) { userCatalog }
  }

  @Before
  fun setUp() {
    loadKoinModules(signUpModule)
  }

  @Test
  fun performSignUp() {
    launchSignUpScreen(signUpTestRule) {
      typeEmail("jovmit@friends.app")
      typePassword("PassW0rd!")
      submit()
    } verify {
      timelineScreenIsPresent()
    }
  }

  @Test
  fun displayBadEmailError() {
    launchSignUpScreen(signUpTestRule) {
      typeEmail("email")
      submit()
    } verify {
      badEmailErrorIsShown()
    }
  }

  @Test
  fun displayDuplicateAccountError() {
    val signedUpUserEmail = "alice@friends.com"
    val signedUpUserPassword = "@l1cePass"
    createUserWith(signedUpUserEmail, signedUpUserPassword)

    launchSignUpScreen(signUpTestRule) {
      typeEmail(signedUpUserEmail)
      typePassword(signedUpUserPassword)
      submit()
    } verify {
      duplicateAccountErrorIsShown()
    }
  }

  @Test
  fun displayBackendError() {
    replaceUserCatalogWith(UnavailableUserCatalog())

    launchSignUpScreen(signUpTestRule) {
      typeEmail("joe@friends.com")
      typePassword("Jo3PassWord#@")
      submit()
    } verify {
      backendErrorIsShown()
    }
  }

  @Test
  fun displayOfflineError() {
    replaceUserCatalogWith(OfflineUserCatalog())

    launchSignUpScreen(signUpTestRule) {
      typeEmail("joe@friends.com")
      typePassword("Jo3PassWord#@")
      submit()
    } verify {
      offlineErrorIsShown()
    }
  }

  private fun replaceUserCatalogWith(offlineUserCatalog: UserCatalog) {
    val replaceModule = module {
      factory(override = true) { offlineUserCatalog }
    }
    loadKoinModules(replaceModule)
  }

  @After
  fun tearDown() {
    val resetModule = module {
      single(override = true) { InMemoryUserCatalog() }
    }
    loadKoinModules(resetModule)
  }

  class UnavailableUserCatalog : UserCatalog {

    override fun createUser(email: String, password: String, about: String): User {
      throw BackendException()
    }
  }

  class OfflineUserCatalog : UserCatalog {

    override fun createUser(email: String, password: String, about: String): User {
      throw ConnectionUnavailableException()
    }
  }

  private fun createUserWith(
    signedUpUserEmail: String,
    signedUpUserPassword: String
  ) {
    userCatalog.createUser(signedUpUserEmail, signedUpUserPassword, "")
  }
}