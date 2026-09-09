package dev.twov9ru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.twov9ru.ui.theme.Ash
import dev.twov9ru.ui.theme.Graphite
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.Obsidian
import dev.twov9ru.ui.theme.Snow
import dev.twov9ru.ui.viewmodel.PlayerViewModel

private val EQ_BAND_LABELS = listOf("60Hz", "170Hz", "310Hz", "600Hz", "1kHz", "3kHz", "6kHz", "12kHz", "14kHz", "16kHz")
private val EQ_BAND_FREQ   = EQ_BAND_LABELS  // labels and freqs identical for display

/**
 * Hardware EQ + DSP bottom sheet — opened via Swipe-Up on the 2V9RU Nexus mark.
 *
 * Contains:
 * - 10-band EQ sliders (±15dB, hardware-offloaded via AudioEffects API in Phase 2)
 * - Bass Boost level
 * - Virtualizer level
 * - Crossfade duration (0–5 seconds)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DspSheet(
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val dsp by playerViewModel.dspState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest    = onDismiss,
        sheetState          = sheetState,
        containerColor      = Obsidian,
        dragHandle          = { BottomSheetDefaults.DragHandle(color = Ash) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Text("DSP & HARDWARE EQ", style = MaterialTheme.typography.headlineMedium, color = Snow)
            Spacer(Modifier.height(20.dp))

            // ── EQ Toggle ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equalizer", style = MaterialTheme.typography.titleLarge, color = Snow, modifier = Modifier.weight(1f))
                Switch(
                    checked  = dsp.eqEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled) playerViewModel.onSetEqBand(0, 0f) // toggle via first band as proxy
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Nexus, checkedTrackColor = Nexus.copy(alpha = 0.4f))
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 10-Band EQ Sliders ─────────────────────────────────────────
            // Show in 2-row grid: 5 bands per row
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..4) {
                        val band = row * 5 + col
                        Column(
                            modifier            = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text  = "%+.0f".format(dsp.bandLevels[band]),
                                style = MaterialTheme.typography.labelSmall,
                                color = Nexus
                            )
                            Slider(
                                value         = dsp.bandLevels[band],
                                onValueChange = { playerViewModel.onSetEqBand(band, it) },
                                valueRange    = -15f..15f,
                                colors        = SliderDefaults.colors(
                                    thumbColor       = Nexus,
                                    activeTrackColor = Nexus.copy(alpha = 0.7f),
                                    inactiveTrackColor = Ash.copy(alpha = 0.3f)
                                )
                            )
                            Text(
                                text  = EQ_BAND_LABELS[band],
                                style = MaterialTheme.typography.labelSmall,
                                color = Ash
                            )
                        }
                    }
                }
                if (row == 0) Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── Bass Boost ─────────────────────────────────────────────────
            DspSliderRow(
                label    = "Bass Boost",
                value    = dsp.bassBoostLevel.toFloat(),
                maxValue = 1000f,
                onValue  = { playerViewModel.onSetBassBoost(it.toInt()) }
            )

            Spacer(Modifier.height(12.dp))

            // ── Virtualizer ────────────────────────────────────────────────
            DspSliderRow(
                label    = "Virtualizer",
                value    = dsp.virtualizerLevel.toFloat(),
                maxValue = 1000f,
                onValue  = { playerViewModel.onSetVirtualizer(it.toInt()) }
            )

            Spacer(Modifier.height(20.dp))

            // ── Crossfade ──────────────────────────────────────────────────
            Text("Crossfade", style = MaterialTheme.typography.titleLarge, color = Snow)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (sec in 0..5) {
                    val selected = dsp.crossfadeSec == sec
                    val label = if (sec == 0) "Off" else "${sec}s"
                    androidx.compose.material3.FilterChip(
                        selected = selected,
                        onClick  = { playerViewModel.onSetCrossfade(sec) },
                        label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors   = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Nexus,
                            selectedLabelColor     = androidx.compose.ui.graphics.Color.Black,
                            containerColor         = Graphite,
                            labelColor             = Ash
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DspSliderRow(
    label:    String,
    value:    Float,
    maxValue: Float,
    onValue:  (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Snow, modifier = Modifier.weight(1f))
        Slider(
            value         = value,
            onValueChange = onValue,
            valueRange    = 0f..maxValue,
            modifier      = Modifier.weight(2f),
            colors        = SliderDefaults.colors(
                thumbColor        = Nexus,
                activeTrackColor  = Nexus.copy(alpha = 0.7f),
                inactiveTrackColor = Ash.copy(alpha = 0.3f)
            )
        )
    }
}
