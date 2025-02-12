package ru.barinov.crypto_container_explorer

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.barinov.core.FileIndex

internal const val PAGE_SIZE = 20

class IndexesPagingSource(
    private val loader: suspend (Long, Int) -> List<FileIndex>,
) : PagingSource<Int, FileIndex>() {

    private var indexFilePointer = 0L

    override fun getRefreshKey(state: PagingState<Int, FileIndex>): Int? {
        val anchorPos = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPos) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FileIndex> {
        val pageIndex = params.key ?: 0
        runCatching {
            loader(indexFilePointer, params.loadSize).also { indexFilePointer = it.lastOrNull()?.indexStartPoint ?: 0L }
        }.fold(
            onFailure = { error -> return LoadResult.Error(error) },
            onSuccess = { content ->
                return LoadResult.Page(
                    data = content,
                    prevKey = if (pageIndex == 0) null else pageIndex - 1,
                    nextKey = if (content.size == params.loadSize) pageIndex + 1 else null
                )
            }
        )
    }
}
