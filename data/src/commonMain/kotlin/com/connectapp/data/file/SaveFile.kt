package com.connectapp.data.file

interface SaveFile{
     fun saveAndExport(fileName: String, bytes: ByteArray): Boolean

     fun notifyOrShareFile(fileName: String)
}