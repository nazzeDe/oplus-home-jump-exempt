package com.nazze.oplusjumpallowlist.ui

import android.graphics.drawable.Drawable

data class AppListItem(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystem: Boolean,
)
