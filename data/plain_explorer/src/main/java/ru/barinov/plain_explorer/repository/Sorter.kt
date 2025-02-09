package ru.barinov.plain_explorer.repository

import ru.barinov.core.FileEntity
import ru.barinov.core.SortType

fun Collection<FileEntity>.sort(type: SortType): List<FileEntity> {
   return this.sortedWith(
           when(type){
               SortType.AS_IS -> compareByDescending { it.isDir }
               SortType.NEW_FIRST -> compareByDescending { it.lastModifiedTimeStamp }
               SortType.OLD_FIRST -> compareBy { it.lastModifiedTimeStamp }
               SortType.BIG_FIRST -> compareByDescending { it.size.value }
               SortType.SMALL_FIRST -> compareBy { it.size.value }
           }
   )
}