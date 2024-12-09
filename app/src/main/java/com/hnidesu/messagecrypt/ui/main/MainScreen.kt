package com.hnidesu.messagecrypt.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hnidesu.messagecrypt.R

@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel
) {
    Column {
        TopBar()
        MainContent(mainScreenViewModel)
    }
}

@Composable
private fun TopBar(){
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterVertically),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun MainContent(
    mainScreenViewModel: MainScreenViewModel
){
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        OutlinedTextField(
            value = mainScreenViewModel.inputText,
            onValueChange = { text -> mainScreenViewModel.inputText = text },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            placeholder = {
                Text("请输入要加密的数据")
            })
        Row {
            RadioButton(mainScreenViewModel.encrypt, {
                mainScreenViewModel.encrypt = true
            }, Modifier.align(Alignment.CenterVertically))
            Text("加密", Modifier.align(Alignment.CenterVertically))
            RadioButton(!mainScreenViewModel.encrypt, {
                mainScreenViewModel.encrypt = false
            }, Modifier.align(Alignment.CenterVertically))
            Text("解密", Modifier.align(Alignment.CenterVertically))
            Spacer(Modifier.width(20.dp))
            OutlinedTextField(
                value = mainScreenViewModel.password,
                onValueChange = { text -> mainScreenViewModel.password = text },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                placeholder = {
                    Text("请输入密码")
                },
                label = {
                    Text("密码")
                })
        }
        Button(
            onClick = {
                mainScreenViewModel.cryptText()
            },
            modifier = Modifier
                .fillMaxWidth()
        ) { Text("执行") }
        OutlinedTextField(
            value = mainScreenViewModel.outputText,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().height(200.dp),
            placeholder = {
                Text("结果将在这里显示")
            },
            readOnly = true
        )
        Button(
            onClick = {
                mainScreenViewModel.copyToClipboard()
            },
            modifier = Modifier
                .fillMaxWidth()
        ) { Text("复制结果") }
    }
}


@Composable
@Preview(showBackground = true)
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(MainScreenViewModel(null))
    }
}