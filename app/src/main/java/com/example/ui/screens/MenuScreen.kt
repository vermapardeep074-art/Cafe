package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.LuxuryMenuItemCard
import com.example.ui.theme.*

@Composable
fun MenuScreen(
  menuItems: List<MenuItem>,
  selectedCategory: MenuCategory?,
  searchQuery: String,
  selectedDietaryTag: DietaryTag?,
  activeCurrency: Currency,
  favorites: Set<String>,
  onSelectCategory: (MenuCategory?) -> Unit,
  onUpdateSearch: (String) -> Unit,
  onSelectDietaryTag: (DietaryTag?) -> Unit,
  onItemClick: (MenuItem) -> Unit,
  onQuickAdd: (MenuItem) -> Unit,
  onToggleFavorite: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  // Filter items
  val filteredItems = menuItems.filter { item ->
    val matchesCategory = selectedCategory == null || item.category == selectedCategory
    val matchesSearch = searchQuery.isBlank() ||
        item.name.contains(searchQuery, ignoreCase = true) ||
        item.description.contains(searchQuery, ignoreCase = true)
    val matchesDietary = selectedDietaryTag == null || item.dietaryTags.contains(selectedDietaryTag)
    matchesCategory && matchesSearch && matchesDietary
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Search Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onUpdateSearch,
          placeholder = { Text("Search coffee, pastries, sandwiches...") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onUpdateSearch("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = DividerGold,
            focusedBorderColor = WarmCaramel
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pills (KFC / McDonald's rapid tabs)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          item {
            FilterChip(
              selected = selectedCategory == null,
              onClick = { onSelectCategory(null) },
              label = { Text("All (${menuItems.size})", fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("category_all")
            )
          }

          items(MenuCategory.values()) { cat ->
            val count = menuItems.count { it.category == cat }
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { onSelectCategory(if (selectedCategory == cat) null else cat) },
              label = {
                Text(
                  text = "${cat.title} ($count)",
                  fontWeight = FontWeight.SemiBold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("category_${cat.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dietary Tag Pills
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(DietaryTag.values()) { tag ->
            val isSelected = selectedDietaryTag == tag
            SuggestionChip(
              onClick = { onSelectDietaryTag(if (isSelected) null else tag) },
              label = {
                Text(
                  text = tag.label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (isSelected) LeafGreenDark else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface
              ),
              border = null
            )
          }
        }
      }
    }

    // 2. Schedule alert banner if viewing Morning Menu
    if (selectedCategory == MenuCategory.MORNING) {
      Surface(
        color = WarmCaramel.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = WarmCaramel,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Morning Menu is served daily 8:00 AM – 11:00 AM (Orders placed outside these hours will be scheduled for morning preparation).",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // 3. Items Grid
    if (filteredItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No culinary creations match your search",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try clearing search filters or selecting another category.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredItems, key = { it.id }) { item ->
          LuxuryMenuItemCard(
            item = item,
            activeCurrency = activeCurrency,
            isFavorite = favorites.contains(item.id),
            onItemClick = { onItemClick(item) },
            onQuickAdd = { onQuickAdd(item) },
            onToggleFavorite = { onToggleFavorite(item.id) }
          )
        }
      }
    }
  }
}
