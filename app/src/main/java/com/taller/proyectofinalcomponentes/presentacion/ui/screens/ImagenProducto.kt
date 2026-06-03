package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.taller.proyectofinalcomponentes.core.utils.ImageUtils

@Composable
fun ImagenProducto(
    imageUrl: String?,
    nombre: String,
    categoria: String,
    size: Dp = 70.dp,
    cornerRadius: Dp = 12.dp
) {
    val context        = LocalContext.current
    val urlNormalizada = ImageUtils.getFullUrl(imageUrl)

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (urlNormalizada != null) {
            val request = ImageRequest.Builder(context)
                .data(urlNormalizada)
                .crossfade(true)
                .crossfade(300)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .allowHardware(false)
                .build()

            SubcomposeAsyncImage(
                model              = request,
                contentDescription = nombre,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(size / 3),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    PlaceholderTexto(categoria)
                }
            )
        } else {
            PlaceholderTexto(categoria)
        }
    }
}

@Composable
private fun PlaceholderTexto(categoria: String) {
    Text(
        text       = categoria.take(2).uppercase(),
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}