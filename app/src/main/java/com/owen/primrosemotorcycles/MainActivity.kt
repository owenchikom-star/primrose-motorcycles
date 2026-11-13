package com.owen.primrosemotorcycles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.owen.primrosemotorcycles.ui.theme.PrimroseBackground
import com.owen.primrosemotorcycles.ui.theme.PrimroseMotorcyclesTheme
import com.owen.primrosemotorcycles.userInterface.auth.AuthScreen
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobCardSection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimroseMotorcyclesTheme {
                PrimroseBackground {
                    var isAuthenticated by remember {
                        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                    }
                    val windowSizeClass = calculateWindowSizeClass(this)

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        if (isAuthenticated) {
                            JobCardSection(
                                windowWidthSizeClass = windowSizeClass.widthSizeClass,
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            AuthScreen(
                                onAuthenticated = { isAuthenticated = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrimroseMotorcyclesTheme {
        Greeting("Android")
    }
}
