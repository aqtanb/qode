package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldState
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Data class for form submission data
 */
data class PromoCodeSubmission(
    val store: Store? = null,
    val code: String = "",
    val title: String = "",
    val description: String = "",
    val category: Category? = null,
    val minimumOrderAmount: String = "",
    val discountAmount: String = "",
    val discountPercentage: String = "",
    val isFirstOrderOnly: Boolean = false,
    val isSingleUse: Boolean = false,
    val expiryDate: LocalDate? = null
)

/**
 * Form validation state
 */
data class FormValidation(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)

/**
 * PromoCode submission form component
 *
 * @param onSubmit Called when form is submitted with valid data
 * @param stores List of available stores
 * @param categories List of available categories
 * @param modifier Modifier to be applied to the form
 * @param isLoading Whether the form is in loading state
 * @param initialData Initial form data for editing
 */
@Composable
fun SubmissionForm(
    onSubmit: (PromoCodeSubmission) -> Unit,
    stores: List<Store>,
    categories: List<Category>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    initialData: PromoCodeSubmission = PromoCodeSubmission()
) {
    var formData by remember { mutableStateOf(initialData) }
    var validation by remember { mutableStateOf(FormValidation(false)) }
    var showPreview by remember { mutableStateOf(false) }

    // Update validation when form data changes
    validation = validateForm(formData)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QodeSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
    ) {
        // Form header
        Text(
            text = "Submit Promo Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Share a promo code with the community",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Store selection
        StoreSelector(
            selectedStore = formData.store,
            stores = stores,
            onStoreSelected = { formData = formData.copy(store = it) },
            error = validation.errors["store"],
        )

        // Basic info section
        BasicInfoSection(
            code = formData.code,
            title = formData.title,
            description = formData.description,
            onCodeChange = { formData = formData.copy(code = it) },
            onTitleChange = { formData = formData.copy(title = it) },
            onDescriptionChange = { formData = formData.copy(description = it) },
            validation = validation,
        )

        // Category selection
        CategorySelector(
            selectedCategory = formData.category,
            categories = categories,
            onCategorySelected = { formData = formData.copy(category = it) },
            error = validation.errors["category"],
        )

        // Discount details section
        DiscountDetailsSection(
            minimumOrderAmount = formData.minimumOrderAmount,
            discountAmount = formData.discountAmount,
            discountPercentage = formData.discountPercentage,
            onMinimumOrderChange = { formData = formData.copy(minimumOrderAmount = it) },
            onDiscountAmountChange = { formData = formData.copy(discountAmount = it) },
            onDiscountPercentageChange = { formData = formData.copy(discountPercentage = it) },
            validation = validation,
        )

        // Options section
        OptionsSection(
            isFirstOrderOnly = formData.isFirstOrderOnly,
            isSingleUse = formData.isSingleUse,
            expiryDate = formData.expiryDate,
            onFirstOrderOnlyChange = { formData = formData.copy(isFirstOrderOnly = it) },
            onSingleUseChange = { formData = formData.copy(isSingleUse = it) },
            onExpiryDateChange = { formData = formData.copy(expiryDate = it) },
        )

        // Preview section
        AnimatedVisibility(
            visible = validation.isValid,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            PreviewSection(
                formData = formData,
                showPreview = showPreview,
                onTogglePreview = { showPreview = !showPreview },
            )
        }

        Spacer(modifier = Modifier.height(QodeSpacing.md))

        // Submit button
        QodeButton(
            onClick = { onSubmit(formData) },
            text = "Submit Promo Code",
            variant = QodeButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = validation.isValid && !isLoading,
            loading = isLoading,
        )

        // Disclaimer
        Text(
            text = "By submitting",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = QodeSpacing.sm),
        )
    }
}

/**
 * Store selector component
 */
@Composable
private fun StoreSelector(
    selectedStore: Store?,
    stores: List<Store>,
    onStoreSelected: (Store) -> Unit,
    error: String?
) {
    var showDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStores = stores.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column {
        Text(
            text = "Store *",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(QodeSpacing.xs))

        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDropdown = true },
                colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                    containerColor = if (error != null) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedStore?.name ?: "Select a store",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedStore != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select store",
                    )
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Search field
                QodeTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search stores...",
                    variant = QodeTextFieldVariant.Search,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.sm),
                )

                // Store list
                filteredStores.take(10).forEach { store ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = store.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = store.category.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onStoreSelected(store)
                            showDropdown = false
                            searchQuery = ""
                        },
                        leadingIcon = {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(QodeCorners.xs),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = QodeCommerceIcons.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = QodeSpacing.xs),
            )
        }
    }
}

/**
 * Basic info section with code, title, and description
 */
@Composable
private fun BasicInfoSection(
    code: String,
    title: String,
    description: String,
    onCodeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    validation: FormValidation
) {
    QodeCard(variant = QodeCardVariant.Outlined) {
        Column(
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            Text(
                text = "Promo Code Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            QodeTextField(
                value = code,
                onValueChange = onCodeChange,
                label = "Promo Code",
                placeholder = "e.g., SAVE20, WELCOME50",
                required = true,
                state = if (validation.errors.containsKey("code")) {
                    QodeTextFieldState.Error(validation.errors["code"] ?: "")
                } else {
                    QodeTextFieldState.Default
                },
                helperText = "Enter the exact code as provided by the store",
            )

            QodeTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Title",
                placeholder = "e.g., 20% off electronics",
                required = true,
                state = if (validation.errors.containsKey("title")) {
                    QodeTextFieldState.Error(validation.errors["title"] ?: "")
                } else {
                    QodeTextFieldState.Default
                },
                helperText = "Brief, descriptive title for the offer",
            )

            QodeTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Description",
                placeholder = "Provide more details about the offer, terms, and conditions",
                variant = QodeTextFieldVariant.Multiline,
                helperText = "Help others understand what this code offers",
            )
        }
    }
}

/**
 * Category selector
 */
@Composable
private fun CategorySelector(
    selectedCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    error: String?
) {
    var showDropdown by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Category *",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(QodeSpacing.xs))

        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDropdown = true },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        selectedCategory?.let { category ->
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(QodeSpacing.sm))
                        }

                        Text(
                            text = selectedCategory?.name ?: "Select a category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedCategory != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select category",
                    )
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            showDropdown = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.color,
                            )
                        },
                    )
                }
            }
        }

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = QodeSpacing.xs),
            )
        }
    }
}

/**
 * Discount details section
 */
@Composable
private fun DiscountDetailsSection(
    minimumOrderAmount: String,
    discountAmount: String,
    discountPercentage: String,
    onMinimumOrderChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    onDiscountPercentageChange: (String) -> Unit,
    validation: FormValidation
) {
    QodeCard(variant = QodeCardVariant.Outlined) {
        Column(
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            Text(
                text = "Discount Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Provide either discount amount OR percentage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                QodeTextField(
                    value = discountAmount,
                    onValueChange = onDiscountAmountChange,
                    label = "Discount Amount (₸)",
                    placeholder = "e.g., 5000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = discountPercentage.isEmpty(),
                )

                Text(
                    text = "OR",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )

                QodeTextField(
                    value = discountPercentage,
                    onValueChange = onDiscountPercentageChange,
                    label = "Discount Percentage (%)",
                    placeholder = "e.g., 20",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = discountAmount.isEmpty(),
                )
            }

            QodeTextField(
                value = minimumOrderAmount,
                onValueChange = onMinimumOrderChange,
                label = "Minimum Order Amount (₸)",
                placeholder = "e.g., 10000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                helperText = "Leave empty if no minimum order required",
            )
        }
    }
}

/**
 * Options section with checkboxes and date picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsSection(
    isFirstOrderOnly: Boolean,
    isSingleUse: Boolean,
    expiryDate: LocalDate?,
    onFirstOrderOnlyChange: (Boolean) -> Unit,
    onSingleUseChange: (Boolean) -> Unit,
    onExpiryDateChange: (LocalDate?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    QodeCard(variant = QodeCardVariant.Outlined) {
        Column(
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            Text(
                text = "Additional Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            // First order only checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onFirstOrderOnlyChange(!isFirstOrderOnly) },
            ) {
                Checkbox(
                    checked = isFirstOrderOnly,
                    onCheckedChange = onFirstOrderOnlyChange,
                )
                Spacer(modifier = Modifier.width(QodeSpacing.sm))
                Column {
                    Text(
                        text = "First order only",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "This code can only be used by new customers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Single use checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSingleUseChange(!isSingleUse) },
            ) {
                Checkbox(
                    checked = isSingleUse,
                    onCheckedChange = onSingleUseChange,
                )
                Spacer(modifier = Modifier.width(QodeSpacing.sm))
                Column {
                    Text(
                        text = "Single use per person",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Each person can only use this code once",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Expiry date picker
            Column {
                Text(
                    text = "Expiry Date (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(QodeSpacing.xs))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(QodeSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = expiryDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                    ?: "Select date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (expiryDate != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )

                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select date",
                            )
                        }
                    }

                    if (expiryDate != null) {
                        IconButton(onClick = { onExpiryDateChange(null) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear date",
                            )
                        }
                    }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onExpiryDateChange(date)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Preview section
 */
@Composable
private fun PreviewSection(
    formData: PromoCodeSubmission,
    showPreview: Boolean,
    onTogglePreview: () -> Unit
) {
    QodeCard(variant = QodeCardVariant.Filled) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePreview() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Icon(
                    imageVector = if (showPreview) Icons.Default.ExpandMore else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showPreview) "Hide preview" else "Show preview",
                )
            }

            AnimatedVisibility(
                visible = showPreview,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = QodeSpacing.md),
                ) {
                    Text(
                        text = "How your promo code will appear:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(QodeSpacing.sm))

                    // Mock promo code card
                    // TODO: Use actual PromoCodeCard component with the form data
                    OutlinedCard {
                        Column(
                            modifier = Modifier.padding(QodeSpacing.md),
                        ) {
                            Text(
                                text = formData.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = formData.code,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (formData.description.isNotBlank()) {
                                Text(
                                    text = formData.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Form validation logic
 */
private fun validateForm(formData: PromoCodeSubmission): FormValidation {
    val errors = mutableMapOf<String, String>()

    // Required fields
    if (formData.store == null) {
        errors["store"] = "Please select a store"
    }

    if (formData.code.isBlank()) {
        errors["code"] = "Promo code is required"
    } else if (formData.code.length < 2) {
        errors["code"] = "Promo code must be at least 2 characters"
    }

    if (formData.title.isBlank()) {
        errors["title"] = "Title is required"
    } else if (formData.title.length < 5) {
        errors["title"] = "Title must be at least 5 characters"
    }

    if (formData.category == null) {
        errors["category"] = "Please select a category"
    }

    // Discount validation
    val hasDiscountAmount = formData.discountAmount.isNotBlank()
    val hasDiscountPercentage = formData.discountPercentage.isNotBlank()

    if (!hasDiscountAmount && !hasDiscountPercentage) {
        errors["discount"] = "Please provide either discount amount or percentage"
    }

    if (hasDiscountAmount && hasDiscountPercentage) {
        errors["discount"] = "Please provide either amount OR percentage, not both"
    }

    return FormValidation(
        isValid = errors.isEmpty(),
        errors = errors,
    )
}

// Sample data for preview
private fun getSampleStores(): List<Store> {
    return listOf(
        Store(
            id = "kaspi",
            name = "Kaspi Bank",
            category = StoreCategory.Electronics,
            followersCount = 15420,
        ),
        Store(
            id = "arbuz",
            name = "Arbuz.kz",
            category = StoreCategory.Food,
            followersCount = 8630,
        ),
        Store(
            id = "magnum",
            name = "Magnum",
            category = StoreCategory.Food,
            followersCount = 12100,
        ),
    )
}

private fun getSampleCategories(): List<Category> {
    return listOf(
        Category(
            id = "electronics",
            name = "Electronics",
            icon = Icons.Default.Category,
            followersCount = 5420,
        ),
        Category(
            id = "food",
            name = "Food & Drinks",
            icon = Icons.Default.Category,
            followersCount = 3210,
        ),
        Category(
            id = "fashion",
            name = "Fashion",
            icon = Icons.Default.Category,
            followersCount = 2840,
        ),
    )
}

// Preview
@Preview(name = "SubmissionForm", showBackground = true)
@Composable
private fun SubmissionFormPreview() {
    QodeTheme {
        SubmissionForm(
            onSubmit = {},
            stores = getSampleStores(),
            categories = getSampleCategories(),
        )
    }
}
