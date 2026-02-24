package com.pruhbootlooper.ocdisplaywen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pruhbootlooper.ocdisplaywen.ui.theme.OcdisplaywenTheme
import com.pruhbootlooper.ocdisplaywen.view.HomePage
import com.pruhbootlooper.ocdisplaywen.view.Ocdisplaywenview
import com.pruhbootlooper.ocdisplaywen.viewmodel.HomeViewModel
import com.pruhbootlooper.ocdisplaywen.viewmodel.OcDisplayWenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class]
        val ocDisplayWenViewModel = ViewModelProvider(this)[OcDisplayWenViewModel::class]
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            OcdisplaywenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Ocdisplaywenview(Modifier.padding(innerPadding), ocDisplayWenViewModel)
                }
            }
        }
    }
}