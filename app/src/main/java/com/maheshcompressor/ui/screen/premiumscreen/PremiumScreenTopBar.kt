package com.maheshcompressor.ui.screen.premiumscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maheshcompressor.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreenTopBar(navController: NavController){



    TopAppBar(

        title = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center

            ){
                Text("Premium Upgrade",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black ,)
            }



        },
        navigationIcon = {




            IconButton(onClick = { navController.popBackStack() }) {

                Icon(
                    painter = painterResource(id = R.drawable.regular_outline_arrow_left),
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(23.dp)
                )
            }


        }
    )
}