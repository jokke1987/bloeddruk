package com.joeri.bloeddruk.ui

/** Vaste labels om snel aan te tikken bij een meting. */
val PresetTags = listOf(
    "Na het sporten",
    "Rustmoment",
    "Medicatie ingenomen",
    "Gestrest",
    "Slecht geslapen"
)

/** Labels worden als "a,b,c" opgeslagen. */
fun String.tagList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }
