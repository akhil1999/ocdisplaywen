package com.pruhbootlooper.ocdisplaywen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pruhbootlooper.ocdisplaywen.viewmodel.HomeViewModel

@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel : HomeViewModel) {
    val userData = viewModel.userData.observeAsState()
    val isLoading = viewModel.isLoading.observeAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Button(
            onClick = {
                viewModel.getUserData()
            }
        ){
            Text(text = "GetData!")
        }
        if(isLoading.value==true){
            CircularProgressIndicator()
        }else {
            userData.value?.name?.let {
                Text(text = it)
            }
        }
    }
}