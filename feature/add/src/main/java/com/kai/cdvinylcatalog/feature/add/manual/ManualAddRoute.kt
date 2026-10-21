package com.kai.cdvinylcatalog.feature.add.manual

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kai.cdvinylcatalog.common.ui.message.asText
import com.kai.cdvinylcatalog.feature.add.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio

/**
 * Связывает экран с платформой: держит launcher'ы камеры, галереи и кропа
 * и запускает их по эффектам ViewModel.
 *
 * Это единственное место фичи, где Compose встречается с `Intent`.
 */
@Composable
fun ManualAddRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManualAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success))
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onIntent(ManualAddContract.Intent.OnPhotoPicked(it.toString())) }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onIntent(
            if (result.resultCode == Activity.RESULT_OK) {
                ManualAddContract.Intent.OnCropFinished
            } else {
                ManualAddContract.Intent.OnCropCancelled
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ManualAddContract.Effect.NavigateBack -> onNavigateBack()
                is ManualAddContract.Effect.NavigateToDetails -> {
                    onNavigateToDetails(effect.itemId)
                }
                is ManualAddContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message.asText(context), Toast.LENGTH_SHORT)
                        .show()
                }
                is ManualAddContract.Effect.LaunchCamera -> {
                    cameraLauncher.launch(effect.shareableUri.toUri())
                }
                is ManualAddContract.Effect.LaunchCrop -> {
                    cropLauncher.launch(
                        buildCropIntent(
                            context = context,
                            sourceUri = effect.sourceUri.toUri(),
                            destinationUri = effect.destinationUri.toUri()
                        )
                    )
                }
            }
        }
    }

    ManualAddScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        onOpenCamera = { viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked) },
        onOpenGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}

private fun buildCropIntent(
    context: Context,
    sourceUri: Uri,
    destinationUri: Uri
): Intent {
    val options = UCrop.Options().apply {
        setAspectRatioOptions(
            0,
            AspectRatio(context.getString(R.string.add_manual_aspect_ratio_free), 0f, 0f),
            AspectRatio(context.getString(R.string.add_manual_aspect_ratio_1_1), 1f, 1f),
            AspectRatio(context.getString(R.string.add_manual_aspect_ratio_4_3), 4f, 3f),
            AspectRatio(context.getString(R.string.add_manual_aspect_ratio_16_9), 16f, 9f)
        )
        setCompressionFormat(Bitmap.CompressFormat.JPEG)
        setCompressionQuality(90)
        setHideBottomControls(false)
        setFreeStyleCropEnabled(true)
    }

    return UCrop.of(sourceUri, destinationUri)
        .withOptions(options)
        .withMaxResultSize(1080, 1080)
        .getIntent(context)
}
