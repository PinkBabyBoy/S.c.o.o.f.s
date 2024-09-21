package ru.barinov.file_browser

import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.file_browser.presentation.Sort
import java.util.SortedMap

fun  Collection<FileEntity>.sort(type: Sort.Type): List<FileEntity> {
   return this.sortedWith(
           when(type){
               Sort.Type.AS_IS -> compareByDescending { it.isDir }
               Sort.Type.NEW_FIRST -> compareByDescending { it.lastModifiedTimeStamp }
               Sort.Type.OLD_FIRST -> compareBy { it.lastModifiedTimeStamp }
               Sort.Type.BIG_FIRST -> compareByDescending { it.size.value }
               Sort.Type.SMALL_FIRST -> compareBy { it.size.value }
           }
   )
}