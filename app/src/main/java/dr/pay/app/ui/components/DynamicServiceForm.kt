package dr.pay.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dr.pay.app.data.EMisrService
import dr.pay.app.data.EMisrInput
import dr.pay.app.data.EMisrSelection

@Composable
fun DynamicServiceForm(
    service: EMisrService,
    onValueChange: (Map<String, String>) -> Unit
) {
    val formValues = remember { mutableStateMapOf<String, String>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = service.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Render input fields
        service.inp?.forEach { input ->
            OutlinedTextField(
                value = formValues[input.name] ?: "",
                onValueChange = { newValue ->
                    formValues[input.name] = newValue
                    onValueChange(formValues.toMap())
                },
                label = { Text(input.title ?: input.name) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = when (input.type) {
                    "number" -> KeyboardOptions(keyboardType = KeyboardType.Number)
                    "phone" -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                    else -> KeyboardOptions.Default
                }
            )
        }

        // Render selection if available
        if (service.sel != null) {
            var expanded by remember { mutableStateOf(false) }
            var selectedOption by remember { mutableStateOf<EMisrSelection?>(null) }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedOption?.title ?: "Select Option")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    service.sel.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.title) },
                            onClick = {
                                selectedOption = option
                                formValues["selection"] = option.value
                                onValueChange(formValues.toMap())
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
