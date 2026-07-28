package cl.jfheimpinacap.appwebcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppWebcamTheme {
                AppWebcamScreen()
            }
        }
    }
}

@Composable
fun AppWebcamScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(24.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.initial_status),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.next_stages_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            Card {
                Text(
                    text = stringResource(R.string.camera_not_configured),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppWebcamScreenPreview() {
    AppWebcamTheme(dynamicColor = false) {
        AppWebcamScreen()
    }
}
