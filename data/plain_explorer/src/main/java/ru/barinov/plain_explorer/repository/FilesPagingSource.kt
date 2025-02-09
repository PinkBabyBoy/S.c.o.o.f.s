package ru.barinov.plain_explorer.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.barinov.core.FileEntity

const val PAGE_SIZE = 20

class FilesPagingSource(private val folderContent: List<FileEntity>?): PagingSource<Int, FileEntity>() {

    override fun getRefreshKey(state: PagingState<Int, FileEntity>): Int? {
        val anchorPos = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPos) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FileEntity> {
        val pageIndex = params.key ?: 0
        folderContent ?: return LoadResult.Page(emptyList(), null, null)
        return try {
            val pageContent = when{
                folderContent.size < PAGE_SIZE -> folderContent
                else -> folderContent.subList(
                    params.loadSize * pageIndex,
                    safeBounds(params.loadSize * (pageIndex + 1), folderContent.size)
                )
            }
            LoadResult.Page(
                data = pageContent,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (pageContent.size == params.loadSize) pageIndex + 1 else null
            )
        } catch (e: Exception) {
             LoadResult.Error(throwable = e)
        }
    }

    private fun safeBounds(calculated: Int, listLastIndex: Int): Int =
        if(calculated > listLastIndex) listLastIndex else calculated
}
