package com.pruhbootlooper.ocdisplaywen.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pruhbootlooper.ocdisplaywen.R
import com.pruhbootlooper.ocdisplaywen.Utils
import com.pruhbootlooper.ocdisplaywen.viewmodel.OcDisplayWenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Ocdisplaywenview(modifier: Modifier = Modifier, viewModel: OcDisplayWenViewModel){
//    val userData = viewModel.userData.observeAsState()
//    val isLoading = viewModel.isLoading.observeAsState() //val userData = viewModel.userData.observeAsState()
    var shouldShowDialog = viewModel.shouldShowDialog.observeAsState()
    var dialogTitle = viewModel.dialogTitle.observeAsState()
    var dialogText = viewModel.dialogText.observeAsState()
    var P = viewModel.P.observeAsState()
    var M = viewModel.M.observeAsState()
    var S = viewModel.S.observeAsState()
    var freq = viewModel.freq.observeAsState()
    var refreshRate = viewModel.refreshRate.observeAsState()

    init(viewModel)

//    println("P changed:${P.value}")
//    println("M changed:${M.value}")
//    println("S changed:${S.value}")

    if (shouldShowDialog.value==true && dialogTitle.value!=null && dialogText.value!=null) {
        MyAlertDialog(
            shouldShowDialog = shouldShowDialog.value!!,
            dialogTitle.value!!,
            dialogText.value!!,
            viewModel
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            CustomSlider("P", 1, 10, P.value!!, 10, viewModel)
            CustomSlider("M", 1, 255, M.value!!, 254, viewModel)
            CustomSlider("S", 0, 10, S.value!!, 10, viewModel)
        CustomDropDown(viewModel)
        Row(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(text = "PLL Frequency: ")
            if (freq.value!! > 1499){
                Text(text = "${freq.value} MHz", color = Color.Red)
            }else{
                Text(text = "${freq.value} MHz", color = Color.White)
            }
        }
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = "Estimated Refresh Rate: ")
            Text(text = "${refreshRate.value} Hz")
        }
        HorizontalDivider(modifier = Modifier.padding(5.dp))
        CustomButton(
            "Step 1:",
            "Backup stock boot image"
        ){
            viewModel.backupBootImage()
        }
        CustomButton("Step 2:", "Unpack DTS") {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.unpackDTS()
            }
        }
        CustomButton("Step 3:", "Modify DTS") {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.modifyDTS()
            }
        }
        CustomButton("Step 4:", "Reboot") {
            Utils.reboot()
        }
        HorizontalDivider(modifier = Modifier.padding(5.dp))
        Text(text = stringResource(R.string.description),
            modifier = Modifier.padding(5.dp),
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            lineHeight = 14.sp,
            color = Color.White
        )
        Text(text = "Credits: akhil1999, VDavid003, libxzr",
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            color = Color.White
            )
    }
}

suspend fun processSomething() : Boolean{
    delay(2000)
    return true
}

fun init(viewModel: OcDisplayWenViewModel){
    CoroutineScope(Dispatchers.IO).launch {
        viewModel.checkRoot()
    }
}

@Composable
fun CustomButton(step : String, label : String, onClick : () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = step,
            Modifier.padding(10.dp),
            color = Color.White
        )
        Spacer(Modifier.size(20.dp))
        Button(
            onClick = {
                onClick()
            },
            modifier = Modifier.padding(5.dp)
        ){
            Text(text = label)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropDown(viewModel: OcDisplayWenViewModel){
    val list = listOf("stock_profile", "oc_profile", "current_profile")
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var selectedText by remember {
        mutableStateOf(list[0])
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Selected Profile:",
            Modifier.padding(5.dp),
            color = Color.White
            )
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {
                isExpanded = !isExpanded
            }) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    value = selectedText,
                    onValueChange = {
                    },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    }
                )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = {isExpanded = false}, modifier = Modifier.background(Color.DarkGray)) {
                list.forEachIndexed { index, s ->
                    DropdownMenuItem(
                        text = { Text(text = list[index], color = Color.White) },
                        onClick = {
                            selectedText = list[index]
                            isExpanded = false
                            println("Current selection : $selectedText")
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.setPMSFromDB(selectedText)
                            }
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}


@Composable
fun CustomSlider(sliderText: String, lowerLimit: Int, upperLimit: Int, defaultValue: Int, steps: Int, viewModel: OcDisplayWenViewModel){
//    println("default values for $sliderText is $defaultValue")
    val temp = defaultValue.toFloat()
//    println("temp is $temp")
    var sliderPosition = defaultValue.toFloat()
//    println("sliderposition is ${sliderPosition}")
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = sliderText,
            modifier = Modifier
                .width(40.dp),
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Text(
            text = sliderPosition.toString(),
            modifier = Modifier
                .padding(5.dp),
            color = Color.White
        )
        Spacer(Modifier.weight(1f))
        Button (
            onClick = {
                if(sliderPosition != lowerLimit.toFloat()) {
                    sliderPosition--
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.decrement(sliderText)
                    }
                }
            },
            content = {
                Image(painter = painterResource(R.drawable.baseline_remove_24), contentDescription = null)
            },
            modifier = Modifier
                .width(50.dp)
                .height(30.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 4.dp,
                top = 4.dp,
                end = 4.dp,
                bottom = 4.dp,
            )
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it.roundToInt().toFloat()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.setValue(sliderText, it.roundToInt())
                }
            },
            modifier = Modifier
                .width(200.dp)
                .padding(5.dp),
            valueRange = lowerLimit.toFloat()..upperLimit.toFloat(),
            steps = steps,
        )
        Button (
            onClick = {
                if(sliderPosition != upperLimit.toFloat()) {
                    sliderPosition++
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.increment(sliderText)
                    }
                }
            },
            content = {
                Image(painter = painterResource(R.drawable.baseline_add_24), contentDescription = null)

            },
            modifier = Modifier
                .width(50.dp)
                .height(30.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 4.dp,
                top = 4.dp,
                end = 4.dp,
                bottom = 4.dp,
            )
        )
    }
}

@Composable
fun MyAlertDialog(shouldShowDialog: Boolean, dialogTitle : String, dialogText : String, viewModel: OcDisplayWenViewModel) {
    if (shouldShowDialog) { // 2
        AlertDialog( // 3
            onDismissRequest = {},
            // 5
            title = {
                    if(dialogTitle!="Success :D"){
                        Text(text = dialogTitle, color = Color.Red)
                    }else{
                        Text(text = dialogTitle, color = Color.Green)
                    }
                    },
            text = { Text(text = dialogText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissDialog()
                    }
                ) {
                    Text("Confirm")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}