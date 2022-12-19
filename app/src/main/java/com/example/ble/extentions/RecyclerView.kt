package com.example.ble.extentions

import androidx.recyclerview.widget.DiffUtil

fun <T> genericDiffUtil(
    item: (T, T) -> Boolean,
    content: (T, T) -> Boolean,
) = object : DiffUtil.ItemCallback<T>() {

    override fun areContentsTheSame(oldItem: T, newItem: T) = content(oldItem, newItem)

    override fun areItemsTheSame(oldItem: T, newItem: T) = item(oldItem, newItem)
}
