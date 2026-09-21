package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddContactsToEventScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsListScreen
import com.example.ui.screens.NewEventScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.EventViewModel

object Routes {
  const val DASHBOARD = "dashboard"
  const val EVENTS = "events"
  const val EVENT_DETAIL = "event_detail"
  const val NEW_EVENT = "new_event"
  const val EDIT_EVENT = "edit_event"
  const val ADD_GUESTS = "add_guests"
  const val BUDGET = "budget"
  const val CONTACTS = "contacts"
  const val SETTINGS = "settings"
  const val SIGN_IN = "sign_in"
  const val SIGN_UP = "sign_up"
}

data class BottomNavItem(
  val route: String,
  val titleEn: String,
  val titleBn: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val eventViewModel: EventViewModel = viewModel()
      val settings by eventViewModel.settings.collectAsStateWithLifecycle()

      MyApplicationTheme(darkTheme = settings.isDarkMode) {
        EventManagerApp(viewModel = eventViewModel)
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun EventManagerApp(viewModel: EventViewModel) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val events by viewModel.allEvents.collectAsStateWithLifecycle()
  val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
  val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
  val allEventContacts by viewModel.allEventContacts.collectAsStateWithLifecycle()
  val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()

  val selectedEvent by viewModel.selectedEvent.collectAsStateWithLifecycle()
  val selectedEventChecklist by viewModel.selectedEventChecklist.collectAsStateWithLifecycle()
  val selectedEventExpenses by viewModel.selectedEventExpenses.collectAsStateWithLifecycle()
  val selectedEventGuests by viewModel.selectedEventGuests.collectAsStateWithLifecycle()
  val selectedEventBudgetSummary by viewModel.selectedEventBudgetSummary.collectAsStateWithLifecycle()
  val selectedEventGuestSummary by viewModel.selectedEventGuestSummary.collectAsStateWithLifecycle()
  val selectedEventDays by viewModel.selectedEventDays.collectAsStateWithLifecycle()
  val selectedCateringPlan by viewModel.selectedCateringPlan.collectAsStateWithLifecycle()
  val cateringEstimate by viewModel.cateringEstimate.collectAsStateWithLifecycle()

  val contactSearchQuery by viewModel.contactSearchQuery.collectAsStateWithLifecycle()
  val contactFilter by viewModel.contactFilter.collectAsStateWithLifecycle()

  val navItems = listOf(
    BottomNavItem(
      route = Routes.DASHBOARD,
      titleEn = "Dashboard",
      titleBn = "ড্যাশবোর্ড",
      selectedIcon = Icons.Filled.Dashboard,
      unselectedIcon = Icons.Outlined.Dashboard,
      testTag = "nav_item_dashboard"
    ),
    BottomNavItem(
      route = Routes.EVENTS,
      titleEn = "Events",
      titleBn = "ইভেন্টসমূহ",
      selectedIcon = Icons.Filled.Celebration,
      unselectedIcon = Icons.Outlined.Celebration,
      testTag = "nav_item_events"
    ),
    BottomNavItem(
      route = Routes.CONTACTS,
      titleEn = "Contacts",
      titleBn = "যোগাযোগ",
      selectedIcon = Icons.Filled.Contacts,
      unselectedIcon = Icons.Outlined.Contacts,
      testTag = "nav_item_contacts"
    ),
    BottomNavItem(
      route = Routes.SETTINGS,
      titleEn = "Settings",
      titleBn = "সেটিংস",
      selectedIcon = Icons.Filled.Settings,
      unselectedIcon = Icons.Outlined.Settings,
      testTag = "nav_item_settings"
    )
  )

  val showBottomBar = currentRoute in listOf(
    Routes.DASHBOARD,
    Routes.EVENTS,
    Routes.CONTACTS,
    Routes.SETTINGS
  )

  LaunchedEffect(currentUser?.isLoggedIn, currentRoute) {
    if (currentUser?.isLoggedIn == true && (currentRoute == Routes.SIGN_IN || currentRoute == Routes.SIGN_UP)) {
      navController.navigate(Routes.DASHBOARD) {
        popUpTo(Routes.SIGN_IN) { inclusive = true }
        launchSingleTop = true
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (showBottomBar) {
        NavigationBar(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("app_bottom_navigation_bar"),
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp,
          windowInsets = NavigationBarDefaults.windowInsets
        ) {
          navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
              selected = isSelected,
              onClick = {
                navController.navigate(item.route) {
                  popUpTo(Routes.DASHBOARD) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = {
                Icon(
                  imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                  contentDescription = item.titleEn,
                  modifier = Modifier.size(24.dp)
                )
              },
              label = {
                Text(
                  text = if (settings.language == "bn") item.titleBn else item.titleEn,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  maxLines = 1
                )
              },
              alwaysShowLabel = true,
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGold,
                selectedTextColor = DeepPlum,
                indicatorColor = DeepPlum,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
              ),
              modifier = Modifier.testTag(item.testTag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Routes.SIGN_IN,
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Sign In
      composable(Routes.SIGN_IN) {
        SignInScreen(
          onSignInWithCredentials = { email, password, onSuccess, onError ->
            viewModel.signInWithCredentials(
              email = email,
              password = password,
              onSuccess = {
                onSuccess()
                navController.navigate(Routes.DASHBOARD) {
                  popUpTo(Routes.SIGN_IN) { inclusive = true }
                  launchSingleTop = true
                }
              },
              onError = { errorMsg ->
                onError(errorMsg)
              }
            )
          },
          onGoogleSignInConfirmed = { name, email, photoUrl ->
            viewModel.signInWithGoogle(name, email, photoUrl) {
              navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
                launchSingleTop = true
              }
            }
          },
          onRequestPasswordReset = { email, onSuccess, onError ->
            viewModel.verifyEmailForReset(
              email = email,
              onSuccess = onSuccess,
              onError = onError
            )
          },
          onResetPassword = { email, newPassword, onSuccess, onError ->
            viewModel.resetPassword(
              email = email,
              newPassword = newPassword,
              onSuccess = onSuccess,
              onError = onError
            )
          },
          onNavigateToSignUp = {
            navController.navigate(Routes.SIGN_UP)
          },
          googleWebClientId = settings.googleWebClientId,
          autoLoginEnabled = settings.autoLoginWithGoogleEnabled,
          onSaveGoogleWebClientId = { newId ->
            viewModel.updateGoogleWebClientId(newId)
          },
          language = settings.language
        )
      }

      // 2. Sign Up
      composable(Routes.SIGN_UP) {
        SignUpScreen(
          onSignUpWithCredentials = { name, email, password, onSuccess, onError ->
            viewModel.signUpWithCredentials(
              name = name,
              email = email,
              password = password,
              onSuccess = {
                onSuccess()
                navController.navigate(Routes.DASHBOARD) {
                  popUpTo(Routes.SIGN_IN) { inclusive = true }
                  launchSingleTop = true
                }
              },
              onError = { errorMsg ->
                onError(errorMsg)
              }
            )
          },
          onGoogleSignInConfirmed = { name, email, photoUrl ->
            viewModel.signInWithGoogle(name, email, photoUrl) {
              navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
                launchSingleTop = true
              }
            }
          },
          onNavigateToSignIn = {
            navController.navigate(Routes.SIGN_IN) {
              popUpTo(Routes.SIGN_UP) { inclusive = true }
            }
          },
          googleWebClientId = settings.googleWebClientId,
          onSaveGoogleWebClientId = { newId ->
            viewModel.updateGoogleWebClientId(newId)
          },
          language = settings.language
        )
      }

      // 3. Dashboard
      composable(Routes.DASHBOARD) {
        DashboardScreen(
          currentUser = currentUser,
          events = events,
          expenses = allExpenses,
          allEventContacts = allEventContacts,
          language = settings.language,
          onEventClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.EVENT_DETAIL)
          },
          onNewEventClick = {
            navController.navigate(Routes.NEW_EVENT)
          },
          onAddContactClick = {
            navController.navigate(Routes.CONTACTS)
          },
          onAddExpenseClick = { eventId ->
            if (eventId != null) {
              viewModel.selectEvent(eventId)
            }
            navController.navigate(Routes.BUDGET)
          },
          onAddGuestClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.ADD_GUESTS)
          },
          onChecklistClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.EVENT_DETAIL)
          },
          onSeeAllEventsClick = {
            navController.navigate(Routes.EVENTS)
          },
          onProfileClick = {
            navController.navigate(Routes.SETTINGS)
          },
          onDeleteEvent = { event ->
            viewModel.deleteEvent(event) {}
          }
        )
      }

      // 4. Events List Tab
      composable(Routes.EVENTS) {
        EventsListScreen(
          events = events,
          expenses = allExpenses,
          language = settings.language,
          onEventClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.EVENT_DETAIL)
          },
          onNewEventClick = {
            navController.navigate(Routes.NEW_EVENT)
          },
          onAddGuestClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.ADD_GUESTS)
          },
          onChecklistClick = { eventId ->
            viewModel.selectEvent(eventId)
            navController.navigate(Routes.EVENT_DETAIL)
          },
          onDeleteEvent = { event ->
            viewModel.deleteEvent(event) {}
          }
        )
      }

      // 5. Event Detail Screen
      composable(Routes.EVENT_DETAIL) {
        EventDetailScreen(
          event = selectedEvent,
          checklist = selectedEventChecklist,
          allContacts = allContacts,
          eventGuests = selectedEventGuests,
          budgetSummary = selectedEventBudgetSummary,
          guestSummary = selectedEventGuestSummary,
          currencySymbol = settings.defaultCurrency,
          language = settings.language,
          onBack = { navController.popBackStack() },
          onToggleChecklistItem = { item ->
            viewModel.toggleChecklistItem(item)
          },
          onAddChecklistItem = { title, dueDate ->
            viewModel.addChecklistItem(title, dueDate)
          },
          onDeleteChecklistItem = { item ->
            viewModel.deleteChecklistItem(item)
          },
          onUpdateGuestStatus = { contactId, newStatus ->
            val eventId = selectedEvent?.id ?: return@EventDetailScreen
            viewModel.updateGuestStatus(eventId, contactId, newStatus)
          },
          onNavigateToAddGuests = {
            navController.navigate(Routes.ADD_GUESTS)
          },
          onNavigateToBudget = {
            navController.navigate(Routes.BUDGET)
          },
          onEditEvent = {
            navController.navigate(Routes.EDIT_EVENT)
          },
          onUpdateContact = { contact ->
            viewModel.updateContact(contact)
          },
          onAddVendorToEvent = { name, phone, note ->
            val eventId = selectedEvent?.id ?: return@EventDetailScreen
            viewModel.addContact(name, phone, "Vendor", note, eventId)
          },
          onRemoveVendorFromEvent = { contactId ->
            val eventId = selectedEvent?.id ?: return@EventDetailScreen
            viewModel.removeGuestFromEvent(eventId, contactId)
          },
          onLinkContactToEvent = { contactId ->
            val eventId = selectedEvent?.id ?: return@EventDetailScreen
            viewModel.toggleGuestForEvent(eventId, contactId, false)
          },
          eventDays = selectedEventDays,
          onAddEventDay = { dayTitle, dateFormatted, timeFormatted, dateTimeMillis, location, notes ->
            viewModel.addEventDay(dayTitle, dateFormatted, timeFormatted, dateTimeMillis, location, notes)
          },
          onUpdateEventDay = { day ->
            viewModel.updateEventDay(day)
          },
          onDeleteEventDay = { day ->
            viewModel.deleteEventDay(day)
          },
          cateringPlan = selectedCateringPlan,
          cateringEstimate = cateringEstimate,
          onSaveCateringPlan = { perPlate, buffer ->
            viewModel.saveCateringPlanForSelectedEvent(perPlate, buffer)
          },
          onAddCateringToBudget = { estimatedCost, recommendedPlates, perPlateCost, bufferPercent ->
            val eventId = selectedEvent?.id ?: return@EventDetailScreen
            viewModel.addOrUpdateCateringExpense(eventId, estimatedCost, recommendedPlates, perPlateCost, bufferPercent)
          }
        )
      }

      // 6. New Event Screen
      composable(Routes.NEW_EVENT) {
        NewEventScreen(
          onClose = { navController.popBackStack() },
          onCreateEvent = { title, category, colorHex, coverUri, date, time, location, budget, desc, dateTimeMillis ->
            viewModel.createEvent(
              title = title,
              category = category,
              colorHex = colorHex,
              coverPhotoUri = coverUri,
              dateFormatted = date,
              timeFormatted = time,
              location = location,
              budget = budget,
              currency = settings.defaultCurrency,
              description = desc,
              dateTimeMillis = dateTimeMillis,
              onSuccess = { newId ->
                navController.popBackStack()
              }
            )
          },
          defaultCurrency = settings.defaultCurrency,
          language = settings.language
        )
      }

      // Edit Event Screen
      composable(Routes.EDIT_EVENT) {
        val currentEvent = selectedEvent
        NewEventScreen(
          initialEvent = currentEvent,
          onClose = { navController.popBackStack() },
          onCreateEvent = { title, category, colorHex, coverUri, date, time, location, budget, desc, dateTimeMillis ->
            if (currentEvent != null) {
              viewModel.updateEvent(
                eventId = currentEvent.id,
                title = title,
                category = category,
                colorHex = colorHex,
                coverPhotoUri = coverUri,
                dateFormatted = date,
                timeFormatted = time,
                location = location,
                budget = budget,
                currency = settings.defaultCurrency,
                description = desc,
                dateTimeMillis = dateTimeMillis,
                onSuccess = {
                  navController.popBackStack()
                }
              )
            } else {
              navController.popBackStack()
            }
          },
          defaultCurrency = settings.defaultCurrency,
          language = settings.language
        )
      }

      // 7. Add Guests to Event Screen
      composable(Routes.ADD_GUESTS) {
        val currentEvent = selectedEvent
        AddContactsToEventScreen(
          event = currentEvent,
          allContacts = allContacts,
          existingEventGuests = selectedEventGuests,
          onBack = { navController.popBackStack() },
          onAddSelectedContacts = { contactIds ->
            if (currentEvent != null) {
              viewModel.addGuestsToEvent(currentEvent.id, contactIds) {
                navController.popBackStack()
              }
            } else {
              navController.popBackStack()
            }
          },
          onAddNewContact = { name, phone, rel, note ->
            viewModel.addContact(name, phone, rel, note, currentEvent?.id)
          },
          onImportPhoneContacts = { list ->
            viewModel.importPhoneContacts(list, currentEvent?.id)
          },
          language = settings.language
        )
      }

      // 8. Budget Screen
      composable(Routes.BUDGET) {
        BudgetScreen(
          event = selectedEvent ?: events.firstOrNull(),
          allEvents = events,
          onSelectEvent = { id -> viewModel.selectEvent(id) },
          budgetSummary = selectedEventBudgetSummary,
          expenses = selectedEventExpenses,
          currencySymbol = settings.defaultCurrency,
          language = settings.language,
          onBack = { navController.popBackStack() },
          onAddExpense = { name, cat, amount, status, due, advance, dueDate ->
            viewModel.addExpense(
              name = name,
              category = cat,
              amount = amount,
              paymentStatus = status,
              dueAmount = due,
              advancePaid = advance,
              dueDate = dueDate
            )
          },
          onDeleteExpense = { exp ->
            viewModel.deleteExpense(exp)
          },
          onUpdateExpense = { exp ->
            viewModel.updateExpense(exp)
          }
        )
      }

      // 9. Contacts Screen
      composable(Routes.CONTACTS) {
        ContactsScreen(
          contacts = filteredContacts,
          allEventContacts = allEventContacts,
          searchQuery = contactSearchQuery,
          onSearchQueryChange = { q -> viewModel.setContactSearchQuery(q) },
          selectedFilter = contactFilter,
          onFilterChange = { f -> viewModel.setContactFilter(f) },
          onAddContact = { name, phone, relation, note ->
            viewModel.addContact(name, phone, relation, note)
          },
          onUpdateContact = { contact ->
            viewModel.updateContact(contact)
          },
          onDeleteContact = { contact ->
            viewModel.deleteContact(contact)
          },
          onImportPhoneContacts = { list ->
            viewModel.importPhoneContacts(list, null)
          },
          language = settings.language
        )
      }

      // 10. Settings Screen
      composable(Routes.SETTINGS) {
        SettingsScreen(
          currentUser = currentUser,
          settings = settings,
          onUpdateLanguage = { lang -> viewModel.updateLanguage(lang) },
          onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
          onUpdateCurrency = { curr -> viewModel.updateCurrency(curr) },
          onUpdateAccentColor = { colorHex -> viewModel.updateAccentColor(colorHex) },
          onToggleNotifications = { enabled -> viewModel.toggleNotifications(enabled) },
          onUpdateGoogleWebClientId = { newId -> viewModel.updateGoogleWebClientId(newId) },
          onToggleAutoLoginWithGoogle = { enabled -> viewModel.toggleAutoLoginWithGoogle(enabled) },
          onLogOut = {
            viewModel.logOut {
              navController.navigate(Routes.SIGN_IN) {
                popUpTo(0) { inclusive = true }
              }
            }
          }
        )
      }
    }
  }
}

