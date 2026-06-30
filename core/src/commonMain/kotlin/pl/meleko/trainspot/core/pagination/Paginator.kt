package pl.meleko.trainspot.core.pagination

import pl.meleko.trainspot.core.Error
import pl.meleko.trainspot.core.Result

class Paginator<Key, Item, E : Error>(
    private val initialKey: Key,
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (nextKey: Key) -> Result<Item, E>,
    private val getNextKey: suspend (currentKey: Key, result: Item) -> Key,
    private val onError: suspend (E) -> Unit,
    private val onSuccess: suspend (result: Item, newKey: Key) -> Unit,
    private val endReached: (currentKey: Key, result: Item) -> Boolean
) {
    private var currentKey = initialKey
    private var isMakingRequest = false
    private var isEndReached = false

    suspend fun loadNextItems() {
        if (isMakingRequest || isEndReached) return

        isMakingRequest = true
        onLoadUpdated(true)

        when (val result = onRequest(currentKey)) {
            is Result.Error -> {
                onError(result.error)
                onLoadUpdated(false)
            }
            is Result.Success -> {
                val item = result.data
                currentKey = getNextKey(currentKey, item)
                onSuccess(item, currentKey)
                onLoadUpdated(false)
                isEndReached = endReached(currentKey, item)
            }
        }

        isMakingRequest = false
    }

    fun reset() {
        currentKey = initialKey
        isEndReached = false
        isMakingRequest = false
    }
}
