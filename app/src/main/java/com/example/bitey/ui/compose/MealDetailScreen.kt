package com.example.bitey.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitey.R
import com.example.bitey.data.model.Meal
import com.example.bitey.ui.viewmodel.MealDetailUiState

/*displays the selected recipe using Jetpack Compose*/
@Composable
fun MealDetailScreen(
    state: MealDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteClick: (Meal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        /*nav and fav action*/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.detail_back))
            }

            Spacer(modifier = Modifier.weight(1f))

            val meal = state.meal

            if (meal != null) {
                IconButton(
                    onClick = { onFavoriteClick(meal) }
                ) {
                    Image(
                        painter = painterResource(
                            if (state.isFavorite) {
                                R.drawable.ic_star_filled
                            } else {
                                R.drawable.ic_star_outline
                            }
                        ),
                        contentDescription = stringResource(
                            if (state.isFavorite) {
                                R.string.cd_remove_favorite
                            } else {
                                R.string.cd_add_favorite
                            }
                        ),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.detail_retry))
                    }
                }
            }

            state.meal != null -> {
                MealContent(meal = state.meal)
            }

            else -> {
                Text(
                    text = stringResource(R.string.detail_not_found),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/*scrollable recipe info*/
@Composable
private fun MealContent(meal: Meal) {
    val unknown = stringResource(R.string.detail_unknown)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = meal.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(
                R.string.detail_category,
                meal.category.ifBlank { unknown }
            ),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(
                R.string.detail_origin,
                meal.area.ifBlank { unknown }
            ),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Ingredient list.
        Text(
            text = stringResource(R.string.detail_ingredients),
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (meal.ingredients.isEmpty()) {
                    Text(stringResource(R.string.detail_no_ingredients))
                } else {
                    meal.ingredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "• ${ingredient.name}",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = ingredient.measure,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Preparation instructions.
        Text(
            text = stringResource(R.string.detail_instructions),
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        val steps = meal.instructions
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (steps.isEmpty()) {
                    Text(stringResource(R.string.detail_no_instructions))
                } else {
                    steps.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = instruction,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
