package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventEntity
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings

data class PhoneContactItem(
  val name: String,
  val phone: String,
  val category: String = "Family"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactsToEventScreen(
  event: EventEntity?,
  allContacts: List<ContactEntity>,
  existingEventGuests: List<EventContactCrossRef>,
  onBack: () -> Unit,
  onAddSelectedContacts: (Set<Long>) -> Unit,
  onAddNewContact: (String, String, String, String) -> Unit,
  onImportPhoneContacts: (List<Pair<String, String>>) -> Unit = {},
  language: String = "en",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  val alreadyAddedIds = remember(existingEventGuests) {
    existingEventGuests.map { it.contactId }.toSet()
  }

  // Selected contacts state (pre-filled with existing ones)
  var selectedContactIds by remember(alreadyAddedIds) {
    mutableStateOf(alreadyAddedIds)
  }

  var showNewContactDialog by remember { mutableStateOf(false) }
  var showPhoneContactsSheet by remember { mutableStateOf(false) }

  // Phone contacts state
  var phoneContactsList by remember { mutableStateOf<List<PhoneContactItem>>(emptyList()) }
  var phoneSearchQuery by remember { mutableStateOf("") }
  var selectedPhoneContacts by remember { mutableStateOf<Set<PhoneContactItem>>(emptySet()) }

  // Helper to load real phone contacts if permission is available
  fun loadDeviceContacts(ctx: Context): List<PhoneContactItem> {
    val list = mutableListOf<PhoneContactItem>()
    try {
      if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        val cursor: Cursor? = ctx.contentResolver.query(
          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
          arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
          ),
          null,
          null,
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
          val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
          val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
          val seenPhones = mutableSetOf<String>()
          while (it.moveToNext()) {
            val name = it.getString(nameIdx) ?: "Unknown"
            val phone = it.getString(phoneIdx)?.replace(" ", "") ?: ""
            if (phone.isNotBlank() && seenPhones.add(phone)) {
              list.add(PhoneContactItem(name = name, phone = phone, category = "Phone"))
            }
          }
        }
      }
    } catch (e: Exception) {
      // Graceful fallback
    }

    return list
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    phoneContactsList = loadDeviceContacts(context)
    showPhoneContactsSheet = true
  }

  // Native single contact picker
  val nativeContactPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickContact()
  ) { contactUri: Uri? ->
    if (contactUri != null) {
      try {
        val cursor = context.contentResolver.query(
          contactUri,
          arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
          null, null, null
        )
        cursor?.use {
          if (it.moveToFirst()) {
            val nameIdx = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val name = it.getString(nameIdx) ?: "New Contact"
            onAddNewContact(name, "+880 1700-000000", "Phone", "Picked from Android contacts")
          }
        }
      } catch (e: Exception) {
        // Fallback
      }
    }
  }

  val filteredList = remember(allContacts, searchQuery) {
    if (searchQuery.isBlank()) allContacts
    else allContacts.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
      it.phone.contains(searchQuery) ||
      it.relation.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar: Back Arrow + Title
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = PlumDark,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("add_guests_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = "Add Guests to Event",
              style = MaterialTheme.typography.titleMedium,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = event?.title ?: "Event",
              style = MaterialTheme.typography.labelSmall,
              color = AccentGold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar Below Top Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by name, relation, or phone...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentGold,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_guests_search_bar")
        )
      }
    }

    // Action Buttons: "Import from phone" and "Add new contact"
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Direct Import from phone CTA
          Button(
            onClick = {
              if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                phoneContactsList = loadDeviceContacts(context)
                showPhoneContactsSheet = true
              } else {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("add_guests_import_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentGold,
              contentColor = PlumDark
            )
          ) {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = null,
              tint = PlumDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = AppStrings.get("import_phone", language),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          // Add new contact
          OutlinedButton(
            onClick = { showNewContactDialog = true },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("add_guests_new_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PersonAdd,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = AppStrings.get("add_new_contact", language),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = DeepPlum
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Counter Line
        val selectedCount = selectedContactIds.size
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$selectedCount selected to invite",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DeepPlum
          )

          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Select All",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AccentGold,
              modifier = Modifier
                .clickable {
                  selectedContactIds = allContacts.map { it.id }.toSet()
                }
                .padding(4.dp)
            )
            Text(
              text = "Clear",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
              modifier = Modifier
                .clickable {
                  selectedContactIds = emptySet()
                }
                .padding(4.dp)
            )
          }
        }
      }
    }

    // Contacts List with Checkboxes
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("add_guests_list")
    ) {
      if (filteredList.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No contacts found",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Tap 'Import from phone' above to load phone contacts directly.",
                style = MaterialTheme.typography.bodySmall,
                color = AccentGold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(filteredList, key = { it.id }) { contact ->
          val isChecked = selectedContactIds.contains(contact.id)

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedContactIds = if (isChecked) {
                  selectedContactIds - contact.id
                } else {
                  selectedContactIds + contact.id
                }
              }
              .testTag("guest_contact_row_${contact.id}"),
            color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.background
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              InitialsAvatar(
                name = contact.name,
                backgroundColorHex = contact.avatarColorHex,
                size = 44.dp
              )

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = contact.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                    color = AccentGold.copy(alpha = 0.16f)
                  ) {
                    Text(
                      text = contact.relation,
                      style = MaterialTheme.typography.labelSmall,
                      color = PlumDark,
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
              }

              Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                  selectedContactIds = if (checked) {
                    selectedContactIds + contact.id
                  } else {
                    selectedContactIds - contact.id
                  }
                },
                colors = CheckboxDefaults.colors(
                  checkedColor = DeepPlum,
                  checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("guest_checkbox_${contact.id}")
              )
            }
          }
          HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderSubtle)
        }
      }
    }

    // Bottom Action Bar: Confirm Button
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Box(modifier = Modifier.padding(16.dp)) {
        Button(
          onClick = { onAddSelectedContacts(selectedContactIds) },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("add_guests_confirm_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = DeepPlum,
            contentColor = Color.White
          )
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Update Guest List (${selectedContactIds.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // BottomSheet: Phone Contact List
  if (showPhoneContactsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showPhoneContactsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
      ) {
        // Sheet Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Phone Contacts",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Text(
              text = "Select contacts to add to your event",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }

          IconButton(onClick = { showPhoneContactsSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone search bar
        OutlinedTextField(
          value = phoneSearchQuery,
          onValueChange = { phoneSearchQuery = it },
          placeholder = { Text("Search phone contacts...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        val displayedPhoneContacts = remember(phoneContactsList, phoneSearchQuery) {
          if (phoneSearchQuery.isBlank()) phoneContactsList
          else phoneContactsList.filter {
            it.name.contains(phoneSearchQuery, ignoreCase = true) ||
            it.phone.contains(phoneSearchQuery)
          }
        }

        // Quick select helper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${selectedPhoneContacts.size} selected",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
              selectedPhoneContacts = displayedPhoneContacts.toSet()
            }) {
              Text("Select All", fontSize = 12.sp, color = AccentGold, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { selectedPhoneContacts = emptySet() }) {
              Text("Clear", fontSize = 12.sp, color = TextMuted)
            }
          }
        }

        HorizontalDivider(color = BorderSubtle)

        // Scrollable list of phone contacts
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
        ) {
          items(displayedPhoneContacts, key = { it.phone }) { contact ->
            val isSelected = selectedPhoneContacts.contains(contact)

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedPhoneContacts = if (isSelected) {
                    selectedPhoneContacts - contact
                  } else {
                    selectedPhoneContacts + contact
                  }
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              InitialsAvatar(name = contact.name, size = 40.dp)

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = contact.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${contact.category} • ${contact.phone}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }

              Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                  selectedPhoneContacts = if (checked) {
                    selectedPhoneContacts + contact
                  } else {
                    selectedPhoneContacts - contact
                  }
                },
                colors = CheckboxDefaults.colors(checkedColor = DeepPlum)
              )
            }
            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Import Selected CTA Button
        Button(
          onClick = {
            val listToImport = selectedPhoneContacts.map { Pair(it.name, it.phone) }
            onImportPhoneContacts(listToImport)
            showPhoneContactsSheet = false
          },
          enabled = selectedPhoneContacts.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("import_phone_contacts_confirm_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark,
            disabledContainerColor = AccentGold.copy(alpha = 0.4f),
            disabledContentColor = PlumDark.copy(alpha = 0.4f)
          )
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add Selected to Event (${selectedPhoneContacts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alternative: open Android native system contact picker
        OutlinedButton(
          onClick = {
            showPhoneContactsSheet = false
            nativeContactPickerLauncher.launch(null)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(12.dp),
          border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
        ) {
          Text(
            text = "Or pick directly using Android System Picker",
            style = MaterialTheme.typography.labelMedium,
            color = DeepPlum
          )
        }
      }
    }
  }

  // Dialog for Adding New Contact Manually
  if (showNewContactDialog) {
    var newName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var newPhone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var newRelation by remember { mutableStateOf("Vendor") }
    var newNote by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showNewContactDialog = false },
      title = {
        Text(
          text = AppStrings.get("add_new_contact", language),
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = newName,
            onValueChange = {
              newName = it
              if (nameError) nameError = false
            },
            label = { Text("Full Name *") },
            placeholder = { Text("e.g. Royal Catering or Uncle Farhan") },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
              { Text("Name is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = newPhone,
            onValueChange = {
              newPhone = it
              if (phoneError) phoneError = false
            },
            label = { Text("Phone Number *") },
            placeholder = { Text("+880 1712-345678") },
            singleLine = true,
            isError = phoneError,
            supportingText = if (phoneError) {
              { Text("Phone number is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(14.dp))

          // Category Chips for quick selection
          Text(
            text = "Category *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Vendor", "Family", "Friend", "Other").forEach { cat ->
              val isSel = newRelation.equals(cat, ignoreCase = true)
              FilterChip(
                selected = isSel,
                onClick = { newRelation = cat },
                label = {
                  Text(
                    text = cat,
                    fontSize = 11.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = if (cat == "Vendor") DeepPlum else AccentGold,
                  selectedLabelColor = if (cat == "Vendor") Color.White else PlumDark
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = newNote,
            onValueChange = { newNote = it },
            label = { Text("Note / Role (optional)") },
            placeholder = { Text("e.g. Flower vendor, Photographer...") },
            maxLines = 2,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isBlank()) {
              nameError = true
              return@Button
            }
            if (newPhone.isBlank()) {
              phoneError = true
              return@Button
            }
            onAddNewContact(
              newName.trim(),
              newPhone.trim(),
              newRelation.trim(),
              newNote.trim()
            )
            showNewContactDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Add Contact")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewContactDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}
