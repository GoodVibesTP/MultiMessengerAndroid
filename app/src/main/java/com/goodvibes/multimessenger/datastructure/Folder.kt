package com.goodvibes.multimessenger.datastructure
import java.io.Serializable

const val idAllFolder = 0
const val idNewFolder = -1
const val idTGFolder = -2
const val idVKFolder = -3

data class Folder(
    var folderId: Int,
    var name: String,
    ) : Serializable
