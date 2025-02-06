package com.dergoogler.liquidwars.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapSelectBottomSheet(
    maps: List<String>,
    onClose: () -> Unit,
    onMapSelect: (Int) -> Unit,
    enabled: Boolean = true,
) {
    BottomSheet(
        onDismissRequest = onClose,
        enabledNavigationSpacer = true
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = maps,
                key = { index, map -> "$index$map" }
            ) { index, map ->
                val newIndex = index - 1
                val interactionSource: MutableInteractionSource =
                    remember { MutableInteractionSource() }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                enabled = enabled,
                                interactionSource = interactionSource,
                                indication = ripple(),
                                onClick = {
                                    onMapSelect(newIndex)
                                }
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapImage(
                            modifier = Modifier.size(100.dp),
                            selection = newIndex
                        )
                        Text(
                            text = map,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}