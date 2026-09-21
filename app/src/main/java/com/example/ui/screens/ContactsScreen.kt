package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.FloatingActionButton
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
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
  contacts: List<ContactEntity>,
  allEventContacts: List<EventContactCrossRef>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedFilter: String,
  onFilterChange: (String) -> Unit,
  onAddContact: (name: String, phone: String, relation: String, note: String) -> Unit,
  onUpdateContact: (ContactEntity) -> Unit = {},
  onDeleteContact: (ContactEntity) -> Unit,
  onImportPhoneContacts: (List<Pair<String, String>>) -> Unit = {},
  language: String = "en",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAddDialog by remember { mutableStateOf(false) }
  var contactToEdit by remember { mutableStateOf<ContactEntity?>(null) }
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
  ) {
    phoneContactsList = loadDeviceContacts(context)
    showPhoneContactsSheet = true
  }

  val filterOptions = listOf("All", "Family", "Friend", "Vendor", "Other")

  val groupedContacts = remember(contacts) {
    contacts
      .sortedBy { it.name }
      .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
  }

  val eventCountMap = remember(allEventContacts) {
    allEventContacts.groupBy { it.contactId }.mapValues { it.value.size }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
    // 1. Top Section (Title + Add Button + Import Button + Search + Filters)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = AppStrings.get("contacts", language),
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Import from Phone Button
            Surface(
              modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    phoneContactsList = loadDeviceContacts(context)
                    showPhoneContactsSheet = true
                  } else {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                  }
                }
                .testTag("contacts_import_phone_button"),
              color = AccentGold.copy(alpha = 0.2f),
              border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.PhoneAndroid,
                  contentDescription = null,
                  tint = PlumDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Import",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = PlumDark
                )
              }
            }

            // "+" Button
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepPlum)
                .clickable { showAddDialog = true }
                .testTag("contacts_add_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Contact",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search contacts by name, phone...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("contacts_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chip Row
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filterOptions) { filter ->
            val isSelected = filter.equals(selectedFilter, ignoreCase = true)
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(
                  1.dp,
                  if (isSelected) DeepPlum else BorderSubtle,
                  RoundedCornerShape(20.dp)
                )
                .clickable { onFilterChange(filter) }
                .testTag("contacts_filter_$filter"),
              color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
            ) {
              Text(
                text = filter,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
              )
            }
          }
        }
      }
    }

    // 2. Alphabetically Grouped List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
      if (contacts.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No contacts found",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
              )
              Spacer(modifier = Modifier.height(8.dp))
              TextButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                  phoneContactsList = loadDeviceContacts(context)
                  showPhoneContactsSheet = true
                } else {
                  permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
              }) {
                Text("Tap here to import from phone", color = DeepPlum, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        groupedContacts.forEach { (initial, contactsInGroup) ->
          item(key = "header_$initial") {
            Text(
              text = initial.toString(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = DeepPlum,
              modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )
          }

          items(contactsInGroup, key = { it.id }) { contact ->
            val eventsCount = eventCountMap[contact.id] ?: 0

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .testTag("contact_item_${contact.id}"),
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 1.dp
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                InitialsAvatar(
                  name = contact.name,
                  backgroundColorHex = contact.avatarColorHex,
                  size = 46.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )

                  Spacer(modifier = Modifier.height(2.dp))

                  Spacer(modifier = Modifier.height(3.dp))

                  Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    val catLower = contact.relation.trim().lowercase()
                    val badgeBg = when (catLower) {
                      "vendor" -> DeepPlum.copy(alpha = 0.14f)
                      "friend" -> AccentGold.copy(alpha = 0.28f)
                      "family" -> Emerald.copy(alpha = 0.15f)
                      else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val badgeTextColor = when (catLower) {
                      "vendor" -> DeepPlum
                      "friend" -> PlumDark
                      "family" -> Emerald
                      else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                      modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                      color = badgeBg
                    ) {
                      Text(
                        text = contact.relation,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }

                    if (eventsCount > 0) {
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "• Invited to $eventsCount event(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepPlum,
                        fontSize = 11.sp
                      )
                    }
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Call button
                  IconButton(
                    onClick = {
                      val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phone}")
                      }
                      try {
                        context.startActivity(intent)
                      } catch (e: Exception) {
                        // Handle no dialer app
                      }
                    },
                    modifier = Modifier.size(38.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Call,
                      contentDescription = "Call ${contact.name}",
                      tint = DeepPlum,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  // Edit button
                  IconButton(
                    onClick = { contactToEdit = contact },
                    modifier = Modifier.size(38.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit ${contact.name}",
                      tint = DeepPlum,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  // Delete button
                  IconButton(
                    onClick = { onDeleteContact(contact) },
                    modifier = Modifier.size(38.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = TextMuted,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(96.dp))
      }
    }
  }

      // Add Contact Floating Action Button
      FloatingActionButton(
        onClick = { showAddDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 20.dp)
          .testTag("contacts_fab_add"),
        containerColor = AccentGold,
        contentColor = PlumDark,
        shape = CircleShape
      ) {
        Icon(
          imageVector = Icons.Default.PersonAdd,
          contentDescription = "Add Contact",
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }

  // BottomSheet for importing phone contacts
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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Import Phone Contacts",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Text(
              text = "Select contacts to save to your address book",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }

          IconButton(onClick = { showPhoneContactsSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = phoneSearchQuery,
          onValueChange = { phoneSearchQuery = it },
          placeholder = { Text("Search phone contacts...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
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

        Button(
          onClick = {
            val listToImport = selectedPhoneContacts.map { Pair(it.name, it.phone) }
            onImportPhoneContacts(listToImport)
            showPhoneContactsSheet = false
          },
          enabled = selectedPhoneContacts.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark
          )
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Import to App Contacts (${selectedPhoneContacts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    }
  }

  // Dialog for Adding New Contact Manually
  if (showAddDialog) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var relation by remember { mutableStateOf("Vendor") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
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
            value = name,
            onValueChange = {
              name = it
              if (nameError) nameError = false
            },
            label = { Text("Full Name *") },
            placeholder = { Text("e.g. Royal Catering or Uncle Farhan") },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
              { Text("Full name is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_contact_name_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = phone,
            onValueChange = {
              phone = it
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
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_contact_phone_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Category Chips for quick, unambiguous selection (Vendor, Family, Friend, Other)
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
              val isSel = relation.equals(cat, ignoreCase = true)
              FilterChip(
                selected = isSel,
                onClick = { relation = cat },
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
                ),
                modifier = Modifier.testTag("add_contact_chip_${cat.lowercase()}")
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note / Role (optional)") },
            placeholder = { Text("e.g. Stage decorator, Photographer...") },
            maxLines = 2,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isBlank()) {
              nameError = true
              return@Button
            }
            if (phone.isBlank()) {
              phoneError = true
              return@Button
            }
            onAddContact(
              name.trim(),
              phone.trim(),
              relation.trim(),
              note.trim()
            )
            showAddDialog = false
          },
          modifier = Modifier.testTag("add_contact_save_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Save Contact")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  // Dialog for Editing an Existing Contact or Vendor
  if (contactToEdit != null) {
    val target = contactToEdit!!
    var editName by remember(target) { mutableStateOf(target.name) }
    var editPhone by remember(target) { mutableStateOf(target.phone) }
    var editRelation by remember(target) { mutableStateOf(target.relation) }
    var editNote by remember(target) { mutableStateOf(target.note) }
    var editNameError by remember { mutableStateOf(false) }
    var editPhoneError by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { contactToEdit = null },
      title = {
        Text(
          text = if (language == "bn") "তথ্য পরিবর্তন করুন" else "Edit Contact / Vendor",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = editName,
            onValueChange = {
              editName = it
              if (editNameError) editNameError = false
            },
            label = { Text("Full Name *") },
            singleLine = true,
            isError = editNameError,
            supportingText = if (editNameError) {
              { Text("Full name is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_contact_name_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editPhone,
            onValueChange = {
              editPhone = it
              if (editPhoneError) editPhoneError = false
            },
            label = { Text("Phone Number *") },
            singleLine = true,
            isError = editPhoneError,
            supportingText = if (editPhoneError) {
              { Text("Phone number is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_contact_phone_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

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
              val isCatSelected = editRelation.equals(cat, ignoreCase = true)
              FilterChip(
                selected = isCatSelected,
                onClick = { editRelation = cat },
                label = { Text(cat, fontSize = 11.sp) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = DeepPlum,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            label = { Text(if (editRelation.equals("Vendor", ignoreCase = true)) "Vendor Service / Note" else "Note (Optional)") },
            placeholder = { Text(if (editRelation.equals("Vendor", ignoreCase = true)) "e.g. Photography, Catering, Stage Decor" else "e.g. Bride's Cousin") },
            maxLines = 2,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_contact_note_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isBlank()) {
              editNameError = true
              return@Button
            }
            if (editPhone.isBlank()) {
              editPhoneError = true
              return@Button
            }
            onUpdateContact(
              target.copy(
                name = editName.trim(),
                phone = editPhone.trim(),
                relation = editRelation.trim(),
                note = editNote.trim()
              )
            )
            contactToEdit = null
          },
          modifier = Modifier.testTag("edit_contact_save_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Update")
        }
      },
      dismissButton = {
        TextButton(onClick = { contactToEdit = null }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}
