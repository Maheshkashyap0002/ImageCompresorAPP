package com.example.imagecompressor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun PremiumScreen(navController: NavController) {

    val context = LocalContext.current
    var codeInput by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Enter Premium Code", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Code") }
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            if (codeInput.text == "Mahesh") {
                setPremium(context, true)
                Toast.makeText(context, "Premium Activated ✅", Toast.LENGTH_SHORT).show()

                navController.popBackStack() // 🔙 back to home
            } else {
                Toast.makeText(context, "Invalid Code ❌", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Activate")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back")
        }
    }
}