package com.resolum.intiva.features.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resolum.intiva.features.agent.data.AgentRepository
import com.resolum.intiva.features.agent.data.SuggestRequest
import com.resolum.intiva.features.agent.data.Suggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SuggestionState(
    val busy: Boolean = false,
    val result: Suggestion? = null,
    val unavailable: Boolean = false
)

@HiltViewModel
class CategorySuggestionViewModel @Inject constructor(private val repository: AgentRepository): ViewModel() {
    private val _state = MutableStateFlow(SuggestionState())
    val state = _state.asStateFlow()
    private var request = 0L
    fun clear() {
        request++
        _state.value = SuggestionState()
    }
    fun suggest(description: String, amount: BigDecimal) {
        val key = ++request
        _state.value = SuggestionState(busy = true)
        viewModelScope.launch {
            try {
                val result = repository.api.suggest(SuggestRequest(description.trim(), amount))
                if (key == request) _state.value = if (result.status == "SUGGESTED") {
                    SuggestionState(result = result)
                } else SuggestionState(unavailable = true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { if (key == request) _state.value = SuggestionState(unavailable = true) }
        }
    }
}
